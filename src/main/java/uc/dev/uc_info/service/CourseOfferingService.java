package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uc.dev.uc_info.common.util.TextNormalizer;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.CourseOffering;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.model.User;
import uc.dev.uc_info.repository.CourseOfferingRepository;
import uc.dev.uc_info.repository.DepartmentRepository;
import uc.dev.uc_info.repository.UserRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 개설과목(CourseOffering) 비즈니스 로직 서비스.
 *
 * <p>DEPT_ADMIN/SUPER_ADMIN이 CSV 파일로 개설과목을 일괄 등록한다. 학과는
 * CSV에 이름("컴퓨터공학과")으로 들어오고, DEPT_ADMIN이 본인 학과가
 * 아닌 다른 학과 이름을 올리려 하면 그 행에서 전체 업로드를 거부한다</p>
 */
@Service
@RequiredArgsConstructor
public class CourseOfferingService {

    private static final Set<String> VALID_DAYS = Set.of("월", "화", "수", "목", "금");

    private final CourseOfferingRepository courseOfferingRepository;
    private final DepartmentRepository departmentRepository;
    private final AdminScopeValidator adminScopeValidator;
    private final UserRepository userRepository;

    /**
     * 관리자 권한 범위의 개설과목 목록을 조회한다.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위 내 개설과목 목록
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> findOfferingsFor(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return courseOfferingRepository.findAllByOrderByCreatedAtDesc();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }
            return courseOfferingRepository.findByDepartment_DeptIdOrderByCreatedAtDesc(
                    admin.getDepartment().getDeptId()
            );
        }

        throw new AccessDeniedException("개설과목 조회 권한이 없는 관리자입니다.");
    }

    /**
     * 관리자 권한 범위 내 전체 개설과목 개수를 조회한다.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위 내 개설과목 개수
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public long countAll(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return courseOfferingRepository.count();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }
            return courseOfferingRepository.countByDepartment_DeptId(admin.getDepartment().getDeptId());
        }

        throw new AccessDeniedException("개설과목 통계를 조회할 권한이 없습니다.");
    }

    /**
     * CSV 파일을 파싱해서 개설과목을 일괄 등록한다. 파일 전체를 먼저
     * 검증하고(학과 권한 포함), 통과해야 저장한다.
     *
     * @param file  업로드된 CSV 파일
     * @param admin 등록 관리자
     * @return 등록된 행 수
     * @throws IllegalArgumentException 파일이 비어있거나, 형식이 잘못된 행이 있는 경우
     * @throws EntityNotFoundException  CSV의 학과명이 실제 존재하지 않는 경우
     * @throws AccessDeniedException    DEPT_ADMIN이 본인 학과가 아닌 과목을 올리려는 경우
     */
    @Transactional
    public int uploadCsv(MultipartFile file, Admin admin) {
        List<CourseOffering> parsed = parseCsv(file, admin);

        for (CourseOffering offering : parsed) {
            offering.setAdmin(admin);
            courseOfferingRepository.save(offering);
        }

        return parsed.size();
    }

    /**
     * 학생이 담을 수 있는(본인 학과) 개설과목 목록을 조회한다.
     *
     * @param studentId 조회 요청 학생의 학번(토큰에서 추출)
     * @return 본인 학과 개설과목 목록(소속 학과가 없으면 빈 목록)
     * @throws EntityNotFoundException studentId에 해당하는 학생이 없는 경우
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> findAvailableForStudent(String studentId) {
        User user = userRepository.findByStudentNumber(studentId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));

        if (user.getDepartment() == null) {
            return List.of();
        }

        return courseOfferingRepository.findByDepartment_DeptIdOrderByCreatedAtDesc(
                user.getDepartment().getDeptId()
        );
    }

    /**
     * CSV 파일을 읽어 검증까지 마친 CourseOffering 목록으로 변환한다.
     *
     * @param file  업로드된 CSV 파일
     * @param admin 업로드 요청 관리자(학과 권한 검증용)
     * @return 검증 통과한 CourseOffering 목록(비어있지 않음, admin 미설정)
     */
    private List<CourseOffering> parseCsv(MultipartFile file, Admin admin) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 CSV 파일을 선택해주세요.");
        }

        List<CourseOffering> offerings = new ArrayList<>();

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
                offerings.add(parseRow(record, rowNum, admin));
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("CSV 파일을 읽는 중 오류가 발생했습니다.");
        }

        if (offerings.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일에 등록할 데이터가 없습니다.");
        }

        return offerings;
    }

    /**
     * CSV 한 행을 검증하고 CourseOffering 객체로 변환한다. 학과 권한도
     * 이 단계에서 확인한다(DEPT_ADMIN이 다른 학과 과목을 올리려 하면 거부).
     *
     * @param record CSV 파서가 읽은 한 행
     * @param rowNum 에러 메시지에 표시할 행 번호(헤더를 1번으로 침)
     * @param admin  업로드 요청 관리자
     * @return 검증 통과한 CourseOffering(admin 미설정)
     */
    private CourseOffering parseRow(CSVRecord record, int rowNum, Admin admin) {
        String deptName = record.get("department");
        String subject = record.get("subject");
        String targetGrade = record.get("targetGrade");
        String day = record.get("day");
        String startHourStr = record.get("startHour");
        String endHourStr = record.get("endHour");
        String room = record.get("room");
        String professor = record.get("professor");

        Department department = departmentRepository.findByDeptName(deptName)
                .orElseThrow(() -> new EntityNotFoundException(
                        rowNum + "번째 행: '" + deptName + "' 학과를 찾을 수 없습니다."
                ));

        adminScopeValidator.validateAssignable(admin, department);

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(rowNum + "번째 행: 과목명이 비어있습니다.");
        }

        if (!VALID_DAYS.contains(day)) {
            throw new IllegalArgumentException(
                    rowNum + "번째 행: day는 월/화/수/목/금 중 하나여야 합니다. 입력값: " + day
            );
        }

        int startHour;
        int endHour;
        try {
            startHour = Integer.parseInt(startHourStr);
            endHour = Integer.parseInt(endHourStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(rowNum + "번째 행: 시작/종료 시각은 숫자여야 합니다.");
        }

        if (endHour <= startHour) {
            throw new IllegalArgumentException(rowNum + "번째 행: 종료 시각은 시작 시각보다 늦어야 합니다.");
        }

        CourseOffering offering = new CourseOffering();
        offering.setDepartment(department);
        offering.setSubject(subject.trim());
        offering.setTargetGrade(TextNormalizer.emptyToNull(targetGrade));
        offering.setDay(day);
        offering.setStartHour(startHour);
        offering.setEndHour(endHour);
        offering.setRoom(room);
        offering.setProfessor(professor);

        return offering;
    }
}