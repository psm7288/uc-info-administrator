package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Meal;
import uc.dev.uc_info.repository.MealRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 식단(Meal) 비즈니스 로직 서비스. 관리자가 올린 CSV 파일을 파싱해서 DB에
 * 반영하고(업로드), 학생 앱 REST({@code MealRestController})에 오늘 식단을
 * 제공한다.
 *
 * <p>CSV 형식(헤더 포함, UTF-8): {@code mealDate,mealType,timeRange,items}
 * — mealDate는 yyyy-MM-dd, mealType은 BREAKFAST/LUNCH/DINNER, items는
 * 메뉴를 콤마로 구분한 값이라 그 칸은 큰따옴표로 감싸야 한다(표준 CSV
 * 인용 규칙, 예: {@code "쌀밥,김치찌개,계란후라이"}).</p>
 */
@Service
@RequiredArgsConstructor
public class MealService {

    private static final Set<String> VALID_MEAL_TYPES = Set.of("BREAKFAST", "LUNCH", "DINNER");

    private final MealRepository mealRepository;

    /**
     * 특정 날짜의 식단(최대 3건)을 조회한다.
     *
     * @param date 조회할 날짜
     * @return 해당 날짜의 식단 목록(0~3건)
     */
    @Transactional(readOnly = true)
    public List<Meal> getMealsByDate(LocalDate date) {
        return mealRepository.findByMealDate(date);
    }

    /**
     * 관리자 화면의 "최근 업로드 목록"용 전체 식단 조회.
     *
     * @return 날짜 내림차순 전체 식단 목록
     */
    @Transactional(readOnly = true)
    public List<Meal> findRecent() {
        return mealRepository.findAllByOrderByMealDateDesc();
    }

    /**
     * CSV 파일을 파싱해서 식단을 일괄 등록/갱신한다. 먼저 파일 전체를
     * 검증하고(하나라도 문제 있으면 예외), 통과하면 한 번에 저장한다.
     *
     * @param file  업로드된 CSV 파일
     * @param admin 업로드한 관리자
     * @return 반영된 행 수
     * @throws IllegalArgumentException 파일이 비어있거나, 형식이 잘못된 행이 있거나, 읽기 자체에 실패한 경우
     */
    @Transactional
    public int uploadCsv(MultipartFile file, Admin admin) {
        List<Meal> parsed = parseCsv(file);
        for (Meal meal : parsed) {
            Optional<Meal> existing = mealRepository.findByMealDateAndMealType(
                    meal.getMealDate(), meal.getMealType()
            );
            if (existing.isPresent()) {
                Meal target = existing.get();
                target.setAdmin(admin);
                target.setTimeRange(meal.getTimeRange());
                target.setItems(meal.getItems());
                mealRepository.save(target);
            } else {
                meal.setAdmin(admin);
                mealRepository.save(meal);
            }
        }

        return parsed.size();
    }

    /**
     * CSV 파일을 읽어 검증까지 마친 {@link Meal} 목록으로 변환한다. 아직
     * DB에 저장하지 않은, 검증만 통과한 객체들이다.
     *
     * @param file 업로드된 CSV 파일
     * @return 검증 통과한 Meal 목록(비어있지 않음)
     * @throws IllegalArgumentException 파일이 비어있거나, 형식이 잘못된 행이 있거나, 읽기 자체에 실패한 경우
     */
    private List<Meal> parseCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 CSV 파일을 선택해주세요.");
        }
        List<Meal> meals = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;
                meals.add(parseRow(record, rowNum));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("CSV 파일을 읽는 중 오류가 발생했습니다.");
        }
        if (meals.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일에 등록할 데이터가 없습니다.");
        }

        return meals;
    }

    /**
     * CSV 한 행을 검증하고 {@link Meal} 객체로 변환한다.
     *
     * @param record CSV 파서가 읽은 한 행
     * @param rowNum 에러 메시지에 표시할 행 번호(헤더를 1번으로 침)
     * @return 검증 통과한 Meal(아직 admin 미설정)
     * @throws IllegalArgumentException 이 행의 값 중 하나라도 형식이 잘못된 경우
     */
    private Meal parseRow(CSVRecord record, int rowNum) {
        String mealDateStr = record.get("mealDate");
        String mealType = record.get("mealType");
        String timeRange = record.get("timeRange");
        String items = record.get("items");
        LocalDate mealDate;
        try {
            mealDate = LocalDate.parse(mealDateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    rowNum + "번째 행: 날짜 형식이 올바르지 않습니다(yyyy-MM-dd). 입력값: " + mealDateStr
            );
        }
        if (!VALID_MEAL_TYPES.contains(mealType)) {
            throw new IllegalArgumentException(
                    rowNum + "번째 행: mealType은 BREAKFAST/LUNCH/DINNER 중 하나여야 합니다. 입력값: " + mealType
            );
        }
        if (timeRange == null || timeRange.isBlank()) {
            throw new IllegalArgumentException(rowNum + "번째 행: 배식 시간이 비어있습니다.");
        }
        if (items == null || items.isBlank()) {
            throw new IllegalArgumentException(rowNum + "번째 행: 메뉴 항목이 비어있습니다.");
        }
        Meal meal = new Meal();
        meal.setMealDate(mealDate);
        meal.setMealType(mealType);
        meal.setTimeRange(timeRange);
        meal.setItems(items);

        return meal;
    }
}