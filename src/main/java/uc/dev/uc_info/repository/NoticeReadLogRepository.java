package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.NoticeReadLog;

/**
 * 공지 열람 기록(NoticeReadLog) 영속성 접근 인터페이스. 기본 CRUD는
 * JpaRepository가 제공한다. Tracking(열람현황) 화면에서 특정 공지의 열람
 * 인원을 세는 데 쓰인다.
 */
public interface NoticeReadLogRepository extends JpaRepository<NoticeReadLog, Long> {

    /**
     * 특정 공지를 열람한 전체 학생 수를 센다(학과 구분 없음). SUPER_ADMIN이
     * "전체 대상" 공지를 볼 때만 쓴다
     *
     * @param noticeId 열람 기록을 셀 공지 PK
     * @return 해당 공지의 열람 기록 개수
     */
    long countByNotice_NoticeId(Long noticeId);

    /**
     * 특정 공지를, 특정 학과 소속 학생이 열람한 수를 센다. DEPT_ADMIN이
     * "전체 대상" 공지를 볼 때 본인 학과 학생만 집계하기 위해 필요하다
     *
     * @param noticeId 열람 기록을 셀 공지 PK
     * @param deptId   열람자를 좁힐 학과 PK
     * @return 해당 학과 소속 학생 중 해당 공지를 열람한 수
     */
    long countByNotice_NoticeIdAndUser_Department_DeptId(Long noticeId, Long deptId);
}