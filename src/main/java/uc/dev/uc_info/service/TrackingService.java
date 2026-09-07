package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.dto.TrackingStatDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.repository.NoticeReadLogRepository;
import uc.dev.uc_info.repository.UserRepository;

import java.util.List;

/**
 * 공지 열람 현황(Tracking) 서비스. 별도 Repository 없이
 * {@link NoticeService}(공지 목록) + {@link NoticeReadLogRepository}(열람 수)
 * + {@link UserRepository}(대상 학생 수) 세 개를 조합해서 계산만 한다.
 *
 * <p><b>학과 스코프 원칙</b>: SUPER_ADMIN은 공지 자체의 대상 범위(전체/특정
 * 학과) 그대로 집계하지만, DEPT_ADMIN은 공지가 전체 대상이든 특정 학과
 * 대상이든 상관없이 <b>항상 본인 학과 학생 기준으로만</b> 집계한다</p>
 */
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final NoticeService noticeService;
    private final NoticeReadLogRepository noticeReadLogRepository;
    private final UserRepository userRepository;
    private final AdminScopeValidator adminScopeValidator;

    /**
     * 권한 범위 내 게시중 공지별로 열람 통계를 계산한다.
     * SUPER_ADMIN인지 DEPT_ADMIN 인지에 따라 집계 기준 학과를 다르게 잡는다.
     * 대상 학년(String)은 Integer로 변환하며, 파싱에 실패하면(데이터 이상)
     * 전체 학년으로 취급해 화면 전체가 죽지 않게 방어한다.
     *
     * @param admin 로그인 관리자(권한 범위 판단용)
     * @return 권한 범위 내 게시중 공지의 열람 통계 목록
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws org.springframework.security.access.AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public List<TrackingStatDTO> findTrackingStats(Admin admin) {
        List<Notice> notices = noticeService.findAllForLink(admin);
        boolean isSuper = adminScopeValidator.isSuperAdmin(admin);

        return notices.stream()
                .map(notice -> buildStat(notice, admin, isSuper))
                .toList();
    }

    /**
     * 공지 하나에 대한 통계를 계산한다. SUPER_ADMIN이면 공지 자체의 대상
     * 학과(없으면 전체)로, DEPT_ADMIN이면 무조건 본인 학과로 대상자 수와
     * 열람 수를 같은 기준으로 좁혀서 계산한다.
     *
     * @param notice  통계를 계산할 공지
     * @param admin   로그인 관리자
     * @param isSuper 관리자가 SUPER_ADMIN인지 여부(중복 판정 방지용으로
     *                미리 계산해서 전달받음)
     * @return 계산된 통계 DTO
     */
    private TrackingStatDTO buildStat(Notice notice, Admin admin, boolean isSuper) {
        Long deptId;
        String scopeLabel;
        Integer grade = null;

        if (isSuper) {
            if (notice.getDepartment() != null) {
                deptId = notice.getDepartment().getDeptId();
                scopeLabel = notice.getDepartment().getDeptName();
            } else {
                deptId = null;
                scopeLabel = "전체 학생";
            }
        } else {
            deptId = admin.getDepartment().getDeptId();
            scopeLabel = admin.getDepartment().getDeptName();
        }

        if (notice.getTargetGrade() != null) {
            try {
                grade = Integer.parseInt(notice.getTargetGrade());
            } catch (NumberFormatException e) {
                grade = null;
            }
        }

        long targetCount = userRepository.countTargetStudents(deptId, grade);
        long readCount = (deptId == null)
                ? noticeReadLogRepository.countByNotice_NoticeId(notice.getNoticeId())
                : noticeReadLogRepository.countByNotice_NoticeIdAndUser_Department_DeptId(
                notice.getNoticeId(), deptId);
        long unreadCount = Math.max(0, targetCount - readCount);
        double readRate = targetCount == 0
                ? 0.0
                : readCount * 100.0 / targetCount;

        TrackingStatDTO dto = new TrackingStatDTO();

        dto.setNoticeId(notice.getNoticeId());
        dto.setTitle(notice.getTitle());
        dto.setScopeLabel(scopeLabel);
        dto.setTargetCount(targetCount);
        dto.setReadCount(readCount);
        dto.setUnreadCount(unreadCount);
        dto.setReadRate(readRate);

        return dto;
    }
}