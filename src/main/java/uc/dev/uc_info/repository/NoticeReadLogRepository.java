package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.NoticeReadLog;

/**
 * 공지 열람 기록(NoticeReadLog) 영속성 접근 인터페이스. 기본 CRUD는
 * JpaRepository가 제공한다. Tracking(열람현황) 화면에서 특정 공지의 열람
 * 인원을 세는 데 쓰인다. 지금 당장은 이 카운트 메서드 하나만 필요하다.
 */
public interface NoticeReadLogRepository extends JpaRepository<NoticeReadLog, Long> {

    /**
     * 특정 공지를 열람한 학생 수를 센다. (notice_id, user_id) 유니크
     * 제약 덕분에 같은 학생이 중복 집계되지 않는다.
     *
     * @param noticeId 열람 기록을 셀 공지 PK
     * @return 해당 공지의 열람 기록 개수
     */
    long countByNotice_NoticeId(Long noticeId);
}