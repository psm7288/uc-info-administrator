package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 공지 재발송(Resend) 서비스. 새 Repository는 필요 없다 — {@link NoticeService}
 * 를 그대로 재사용한다.
 *
 * <p><b>이번 라운드 범위 — 반드시 지킬 것</b>: 실제 FCM 푸시 발송 연동은
 * 하지 않는다. "재발송" 버튼을 누르면 {@code notice.pushSent}를 true로
 * 갱신하고 끝난다. 실제 발송 로직은 TODO 주석으로 자리만 남겨둔다 —
 * 나중에 별도 작업으로 FCM 연동할 자리다.</p>
 *
 * <h3>먼저 NoticeService에 메서드 하나를 추가해야 한다 — Banner 담당자가
 * findAllForLink를 추가했던 것과 같은 방식</h3>
 * <p>기존 {@code createNotice}/{@code updateNotice}는 폼 전체를 갱신하는
 * 용도라 "pushSent만 켜기"에는 안 맞는다. {@code getNoticeForAdmin}은
 * {@code readOnly} 트랜잭션이라 여기서 값을 바꿔도 저장이 안 된다. 그래서
 * {@code NoticeService}에 새 메서드를 하나 추가해야 한다:</p>
 * <pre>{@code
 * @Transactional
 * public Notice markPushSent(Long id, Admin admin) {
 *     Notice notice = getNotice(id);
 *     adminScopeValidator.validateAccess(admin, notice);
 *     if (!"PUBLISHED".equals(notice.getStatus())) {
 *         throw new IllegalStateException("게시중인 공지만 재발송할 수 있습니다.");
 *     }
 *     notice.setPushSent(true);
 *     return noticeRepository.save(notice);
 * }
 * }</pre>
 *
 * <p>이 클래스(ResendService) 안에는 아래 2개의 public 메서드를 직접
 * 작성하세요.</p>
 *
 * <h3>만들어야 할 메서드</h3>
 * <ol>
 *   <li>{@code List<Notice> findResendCandidates(Admin admin)}
 *       — 재발송 후보 목록. {@code noticeService.findAllForLink(admin)}을
 *       그대로 호출한다 — 권한 범위(본인학과+전체/전체)와 PUBLISHED 필터가
 *       이미 정확히 이 용도에 맞게 구현되어 있다(이름이 "연결용"이라
 *       어색해도 로직은 완전히 같으니 새로 안 만들어도 된다).</li>
 *   <li>{@code void resend(Long noticeId, Admin admin)}
 *       — 위에서 추가한 {@code noticeService.markPushSent(noticeId, admin)}
 *       호출 한 줄이면 끝. // TODO(Phase 2): 여기서 실제 FCM 푸시 발송 연동.</li>
 * </ol>
 *
 * <h3>이 서비스가 던지는 예외</h3>
 * <ul>
 *   <li>{@code EntityNotFoundException}/{@code AccessDeniedException} —
 *       {@code markPushSent}에서 그대로 전파됨.</li>
 *   <li>{@code IllegalStateException} — PUBLISHED 상태가 아닌 공지를
 *       재발송하려는 경우.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ResendService {

    private final NoticeService noticeService;
}