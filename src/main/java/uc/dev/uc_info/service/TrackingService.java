package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.dto.TrackingStatDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.repository.NoticeReadLogRepository;
import uc.dev.uc_info.repository.UserRepository;

import java.util.List;

/**
 * 공지 열람 현황(Tracking) 서비스. 새 Repository를 만들 필요 없다 —
 * {@link NoticeService}(공지 목록) + {@link NoticeReadLogRepository}(열람
 * 수) + {@link UserRepository}(대상 학생 수) 세 개를 조합해서 계산만 한다.
 *
 * <p>이 클래스 안에 아래 1개의 public 메서드(+필요하면 private 헬퍼)를
 * 직접 작성하세요.</p>
 *
 * <h3>만들어야 할 메서드</h3>
 * <ol>
 *   <li>{@code List<TrackingStatDTO> findTrackingStats(Admin admin)}
 *       처리 흐름:
 *       <ol>
 *         <li>{@code noticeService.findAllForLink(admin)}으로 권한 범위 +
 *         PUBLISHED 공지 목록을 가져온다(연결용으로 만든 메서드지만
 *         "권한범위+게시중만"이라는 조건이 Tracking에도 정확히 맞아서
 *         재사용한다).</li>
 *         <li>공지마다: {@code notice.getDepartment()}가 있으면
 *         {@code deptId}, 없으면 null. {@code notice.getTargetGrade()}
 *         (String)를 Integer로 변환 — null이거나 숫자로 못 바꾸면
 *         null로 취급(전체 학년). {@code Integer.parseInt}가
 *         {@code NumberFormatException} 던질 수 있으니 try-catch로
 *         감싸서 실패하면 null 처리할 것(공지 데이터 이상 때문에 화면
 *         전체가 죽으면 안 됨).</li>
 *         <li>{@code userRepository.countTargetStudents(deptId, grade)}로
 *         대상 학생 수, {@code noticeReadLogRepository
 *         .countByNotice_NoticeId(notice.getNoticeId())}로 열람 수를
 *         구한다.</li>
 *         <li>{@code TrackingStatDTO}에 담는다: unreadCount는
 *         {@code Math.max(0, targetCount - readCount)}, readRate는
 *         targetCount가 0이면 0.0, 아니면
 *         {@code readCount * 100.0 / targetCount}.</li>
 *       </ol>
 *       </li>
 * </ol>
 *
 * <p>공지마다 대상자 수 쿼리를 한 번씩 날리는 구조라(N+1과 비슷한 모양이지만
 * 여긴 Hibernate 연관관계 지연로딩이 아니라 의도된 집계 쿼리 반복이다),
 * 공지가 아주 많아지면 느려질 수 있다 — 지금 규모(학교 하나, 공지 수십~
 * 수백 건)에서는 문제없는 수준이라 최적화는 나중 과제로 남겨둔다.</p>
 *
 * <h3>이 서비스가 던지는 예외</h3>
 * <p>{@code findAllForLink}(내부적으로 {@code findNoticesFor} 호출)가 던지는
 * {@code IllegalStateException}/{@code AccessDeniedException}이 그대로
 * 전파된다. 이 서비스 자체가 새로 던지는 예외는 없다.</p>
 */
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final NoticeService noticeService;
    private final NoticeReadLogRepository noticeReadLogRepository;
    private final UserRepository userRepository;
}