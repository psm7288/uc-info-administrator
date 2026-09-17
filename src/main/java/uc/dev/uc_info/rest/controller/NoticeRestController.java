package uc.dev.uc_info.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.rest.dto.NoticeResponse;
import uc.dev.uc_info.rest.dto.NoticeViewRequest;
import uc.dev.uc_info.service.NoticeService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 공지사항 학생 앱 REST 컨트롤러. 관리자 웹의 {@link NoticeService}를 그대로
 * 재사용하고, 응답만 {@link NoticeResponse}(엔티티 직접 반환 금지)로
 * 변환해서 내보낸다.
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeRestController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM.dd");
    private final NoticeService noticeService;

    /**
     * 카테고리별 공지 목록을 조회한다. category가 없으면 전체를 반환한다.
     * 인증된 학생의 소속 학과 기준으로 본인 학과+전체 대상 공지만 나간다.
     *
     * @param category 좁힐 카테고리(선택)
     * @return 200 + 공지 목록
     */
    @GetMapping
    public ResponseEntity<List<NoticeResponse>> list(
            @RequestParam(required = false) String category
    ) {
        String studentId = currentStudentId();

        List<NoticeResponse> notices = noticeService.findPublishedForStudent(studentId, category)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(notices);
    }

    /**
     * 공지 상세를 조회한다. 게시중이 아니거나 다른 학과 전용 공지면
     * 404로 응답한다(RestExceptionAdvice가 EntityNotFoundException을 404로 변환).
     *
     * @param id 조회할 공지 PK
     * @return 200 + 공지 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> detail(@PathVariable Long id) {
        String studentId = currentStudentId();
        Notice notice = noticeService.getPublishedNoticeForStudent(id, studentId);

        return ResponseEntity.ok(toResponse(notice));
    }

    /**
     * 공지 상세 진입 시 열람을 기록한다(트래킹/미확인자 재발송용). 이미
     * 열람했으면 조용히 무시한다(멱등).
     *
     * @param id      열람한 공지 PK
     * @param request 요청 바디(studentId)
     * @return 204 No Content
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> view(
            @PathVariable Long id,
            @Valid @RequestBody NoticeViewRequest request
    ) {
        noticeService.recordView(id, request.getStudentId());
        return ResponseEntity.noContent().build();
    }

    /**
     * SecurityContext에서 현재 인증된 학생의 studentId를 꺼낸다.
     * {@code JwtAuthenticationFilter}가 principal 자체를 studentId 문자열로
     * 세팅해뒀다.
     *
     * @return 토큰에서 추출한 studentId
     */
    private String currentStudentId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Notice 엔티티를 응답 DTO로 변환한다.
     *
     * @param notice 변환할 공지
     * @return 변환된 응답 DTO
     */
    private NoticeResponse toResponse(Notice notice) {
        NoticeResponse response = new NoticeResponse();

        response.setId(notice.getNoticeId());
        response.setTitle(notice.getTitle());
        response.setDate(
                notice.getCreatedAt() != null
                        ? notice.getCreatedAt().format(DATE_FORMAT)
                        : null
        );
        response.setCategory(notice.getCategory());
        response.setContent(notice.getContent());

        return response;
    }
}