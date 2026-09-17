package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Notice;

import java.util.List;

/**
 * 공지 재발송 서비스.
 *
 * <p>권한 범위 내 게시 공지를 조회하고,
 * 재발송 요청 시 pushSent 상태를 갱신한다.</p>
 */
@Service
@RequiredArgsConstructor
public class ResendService {

    private final NoticeService noticeService;

    /**
     * 재발송 가능한 공지 목록을 조회한다.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위 내 게시 공지 목록
     */
    @Transactional(readOnly = true)
    public List<Notice> findResendCandidates(Admin admin) {
        return noticeService.findAllForLink(admin);
    }

    /**
     * 선택한 공지를 재발송 처리한다.
     *
     * @param noticeId 공지 PK
     * @param admin 로그인 관리자
     */
    @Transactional
    public void resend(Long noticeId, Admin admin) {
        noticeService.markPushSent(noticeId, admin);
    }
}