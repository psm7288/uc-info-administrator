-- =====================================================================
-- UC Info Admin — PostgreSQL DDL (최신화)
-- =====================================================================

-- =====================================================================
-- 1. department (학과 마스터)
-- =====================================================================
CREATE TABLE department (
    dept_id     BIGSERIAL    PRIMARY KEY,
    dept_code   INTEGER      NOT NULL UNIQUE,
    dept_name   VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);
COMMENT ON TABLE  department            IS '학과 마스터';
COMMENT ON COLUMN department.dept_id    IS '학과 PK(대리키)';
COMMENT ON COLUMN department.dept_code  IS '학과 코드(비즈니스 키)';
COMMENT ON COLUMN department.dept_name  IS '학과명';

-- =====================================================================
-- 2. admin (관리자: DEPT_ADMIN / SUPER_ADMIN)
-- =====================================================================
CREATE TABLE admin (
    admin_id        BIGSERIAL   PRIMARY KEY,
    dept_id         BIGINT      NULL,                 -- SUPER_ADMIN 은 NULL
    role            VARCHAR(20) NULL,                 -- DEPT_ADMIN / SUPER_ADMIN
    username        VARCHAR(20) NOT NULL UNIQUE,
    password        VARCHAR     NOT NULL,             -- BCrypt 해시
    admin_name      VARCHAR(20) NOT NULL,
    alarm_tracking  BOOLEAN     NULL,
    alarm_send      BOOLEAN     NULL,
    CONSTRAINT fk_admin_dept FOREIGN KEY (dept_id) REFERENCES department (dept_id)
);
COMMENT ON TABLE  admin           IS '관리자(부서/전체 계정)';
COMMENT ON COLUMN admin.dept_id   IS '소속 학과. SUPER_ADMIN은 NULL';
COMMENT ON COLUMN admin.role      IS 'DEPT_ADMIN / SUPER_ADMIN';

-- =====================================================================
-- 3. users (학생)
-- =====================================================================
CREATE TABLE users (
    user_id          BIGSERIAL     PRIMARY KEY,
    dept_id          BIGINT        NOT NULL,
    student_number   VARCHAR(20)   NOT NULL UNIQUE,
    user_name        VARCHAR(20)   NOT NULL,
    grade            INTEGER       NULL,             -- 학년(1~)
    fcm_token        VARCHAR       NULL,             -- 학생 앱 푸시 토큰
    scholarship_info TEXT          NULL,             -- 장학 통합 정보(JSON)
    tel              VARCHAR(20)   NOT NULL,
    email            VARCHAR(100)  NOT NULL,
    academic_status  VARCHAR(20)   NOT NULL,
    gender           CHAR(1)       NOT NULL,
    advisor_name     VARCHAR(30)   NULL,
    score            DECIMAL(3,2)  NOT NULL,
    access           BOOLEAN       NOT NULL,
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES department (dept_id)
);
COMMENT ON TABLE  users          IS '학생(모바일 앱 사용자)';
COMMENT ON COLUMN users.student_number IS '학번(학생 앱 로그인 인증 키, 유니크)';
COMMENT ON COLUMN users.grade    IS '학년(공지/일정 target_grade 매칭용)';

-- =====================================================================
-- 4. notice (공지)
-- =====================================================================
CREATE TABLE notice (
    notice_id       BIGSERIAL    PRIMARY KEY,
    admin_id        BIGINT       NOT NULL,
    dept_id         BIGINT       NULL,              -- NULL = 전체 학과 대상
    title           VARCHAR      NOT NULL,
    content         TEXT         NULL,
    category        VARCHAR(20)  NOT NULL,          -- DEPARTMENT/ACADEMIC/SCHOLARSHIP/EVENT/EMPLOYMENT
    priority        VARCHAR(20)  NOT NULL,          -- NORMAL/IMPORTANT/URGENT
    target_grade    VARCHAR(10)  NULL,              -- NULL = 전체 학년
    status          VARCHAR(20)  NOT NULL,          -- DRAFT/PUBLISHED/CLOSED
    start_date      DATE         NULL,
    end_date        DATE         NULL,
    pinned          BOOLEAN      NOT NULL DEFAULT false,
    push_sent       BOOLEAN      NOT NULL DEFAULT false,
    attachment_url  VARCHAR      NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NULL,
    CONSTRAINT fk_notice_admin FOREIGN KEY (admin_id) REFERENCES admin (admin_id),
    CONSTRAINT fk_notice_dept  FOREIGN KEY (dept_id)  REFERENCES department (dept_id)
);
COMMENT ON TABLE  notice         IS '공지';
COMMENT ON COLUMN notice.dept_id IS '대상 학과. NULL=전체';

-- =====================================================================
-- 5. notice_read_log (공지 열람 기록)
-- =====================================================================
CREATE TABLE notice_read_log (
    log_id      BIGSERIAL  PRIMARY KEY,
    notice_id   BIGINT     NOT NULL,
    user_id     BIGINT     NOT NULL,
    read_at     TIMESTAMP  NULL DEFAULT now(),
    CONSTRAINT fk_readlog_notice FOREIGN KEY (notice_id) REFERENCES notice (notice_id),
    CONSTRAINT fk_readlog_user   FOREIGN KEY (user_id)   REFERENCES users (user_id),
    CONSTRAINT uq_readlog UNIQUE (notice_id, user_id)   -- 중복 열람 방지
);
COMMENT ON TABLE notice_read_log IS '공지 열람 기록(열람률/미확인자 산출)';

-- =====================================================================
-- 6. banner (배너)
-- =====================================================================
CREATE TABLE banner (
    banner_id   BIGSERIAL    PRIMARY KEY,
    admin_id    BIGINT       NOT NULL,
    notice_id   BIGINT       NULL,                  -- NULL = 공지 미연결
    title       VARCHAR      NOT NULL,
    subtitle    VARCHAR      NULL,
    status      VARCHAR(20)  NOT NULL,              -- ACTIVE/SCHEDULED/INACTIVE
    start_date  DATE         NULL,
    end_date    DATE         NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_banner_admin  FOREIGN KEY (admin_id)  REFERENCES admin (admin_id),
    CONSTRAINT fk_banner_notice FOREIGN KEY (notice_id) REFERENCES notice (notice_id)
);
COMMENT ON TABLE banner IS '앱 메인 배너';

-- =====================================================================
-- 7. schedule (학사 일정)
-- =====================================================================
CREATE TABLE schedule (
    schedule_id   BIGSERIAL    PRIMARY KEY,
    admin_id      BIGINT       NOT NULL,
    dept_id       BIGINT       NULL,                -- NULL = 전체 대상
    title         VARCHAR      NOT NULL,
    category      VARCHAR(20)  NOT NULL,            -- ACADEMIC/EXAM/REGISTRATION/VACATION/EVENT/ETC
    target_grade  VARCHAR(10)  NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NULL,
    visible       BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_schedule_admin FOREIGN KEY (admin_id) REFERENCES admin (admin_id),
    CONSTRAINT fk_schedule_dept  FOREIGN KEY (dept_id)  REFERENCES department (dept_id)
);
COMMENT ON TABLE schedule IS '학사 일정';

-- =====================================================================
-- 8. shuttle (셔틀버스) — SUPER_ADMIN 전용 관리
-- =====================================================================
CREATE TABLE shuttle (
    shuttle_id       BIGSERIAL    PRIMARY KEY,
    admin_id         BIGINT       NOT NULL,
    route_name       VARCHAR      NOT NULL,
    departure        VARCHAR      NOT NULL,
    destination      VARCHAR      NOT NULL,
    waypoints        VARCHAR      NULL,
    first_departure  VARCHAR(10)  NULL,
    last_departure   VARCHAR(10)  NULL,
    status           VARCHAR(20)  NOT NULL,         -- ACTIVE/SUSPENDED/DELAYED
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NULL,
    CONSTRAINT fk_shuttle_admin FOREIGN KEY (admin_id) REFERENCES admin (admin_id)
);
COMMENT ON TABLE shuttle IS '셔틀버스 노선(SUPER_ADMIN 전용)';

-- =====================================================================
-- 9. scholarship (장학금)
-- =====================================================================
CREATE TABLE scholarship (
    scholarship_id       BIGSERIAL    PRIMARY KEY,
    admin_id             BIGINT       NOT NULL,
    dept_id              BIGINT       NULL,         -- NULL = 전체 대상
    notice_id            BIGINT       NULL,         -- NULL = 공지 미연결
    name                 VARCHAR      NOT NULL,
    type                 VARCHAR(20)  NOT NULL,     -- REGIONAL/GRADE/INTERNAL/EXTERNAL
    target_grade         VARCHAR(10)  NULL,
    residence_condition  VARCHAR      NULL,
    deadline             DATE         NULL,         -- D-Day 계산용
    visible              BOOLEAN      NOT NULL DEFAULT true,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_scholarship_admin  FOREIGN KEY (admin_id)  REFERENCES admin (admin_id),
    CONSTRAINT fk_scholarship_dept   FOREIGN KEY (dept_id)   REFERENCES department (dept_id),
    CONSTRAINT fk_scholarship_notice FOREIGN KEY (notice_id) REFERENCES notice (notice_id)
);
COMMENT ON TABLE scholarship IS '장학금';

-- =====================================================================
-- 10. course_offering (개설과목)
-- =====================================================================
CREATE TABLE course_offering (
    course_offering_id  BIGSERIAL    PRIMARY KEY,
    admin_id             BIGINT       NOT NULL,
    dept_id              BIGINT       NOT NULL,
    subject              VARCHAR      NOT NULL,
    target_grade         VARCHAR(10)  NULL,          -- NULL = 전체 학년
    day                  VARCHAR(10)  NOT NULL,      -- 월/화/수/목/금
    start_hour           INTEGER      NOT NULL,
    end_hour             INTEGER      NOT NULL,
    room                 VARCHAR      NULL,
    professor            VARCHAR      NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_course_offering_admin FOREIGN KEY (admin_id) REFERENCES admin (admin_id),
    CONSTRAINT fk_course_offering_dept  FOREIGN KEY (dept_id)  REFERENCES department (dept_id)
);
COMMENT ON TABLE  course_offering             IS '개설과목(학과 단위 강의 목록)';
COMMENT ON COLUMN course_offering.dept_id     IS '개설 학과';
COMMENT ON COLUMN course_offering.target_grade IS '대상 학년. NULL=전체';

-- =====================================================================
-- 11. enrollment (수강선택) — 학생 시간표는 이 테이블을 모아 구성
-- =====================================================================
CREATE TABLE enrollment (
    enrollment_id       BIGSERIAL  PRIMARY KEY,
    user_id              BIGINT     NOT NULL,
    course_offering_id   BIGINT     NOT NULL,
    color                INTEGER    NOT NULL,        -- 학생 앱 시간표 표시 색상 인덱스
    created_at           TIMESTAMP  NOT NULL DEFAULT now(),
    CONSTRAINT fk_enrollment_user            FOREIGN KEY (user_id)            REFERENCES users (user_id),
    CONSTRAINT fk_enrollment_course_offering FOREIGN KEY (course_offering_id) REFERENCES course_offering (course_offering_id),
    CONSTRAINT uq_enrollment UNIQUE (user_id, course_offering_id)   -- 같은 과목 중복 신청 방지
);
COMMENT ON TABLE enrollment IS '수강선택(학생 시간표 구성 항목)';

-- =====================================================================
-- 12. meal (식단) — SUPER_ADMIN 전용 CSV 업로드
-- =====================================================================
CREATE TABLE meal (
    meal_id      BIGSERIAL    PRIMARY KEY,
    admin_id     BIGINT       NOT NULL,
    meal_date    DATE         NOT NULL,
    meal_type    VARCHAR(20)  NOT NULL,          -- 조식/중식/석식
    time_range   VARCHAR(20)  NOT NULL,
    items        TEXT         NOT NULL,          -- 메뉴, 콤마 구분 문자열
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_meal_admin FOREIGN KEY (admin_id) REFERENCES admin (admin_id),
    CONSTRAINT uq_meal UNIQUE (meal_date, meal_type)   -- 같은 날 같은 끼니 중복 방지
);
COMMENT ON TABLE meal IS '식단(SUPER_ADMIN CSV 업로드)';

-- =====================================================================
-- 인덱스 (조회 성능: 자주 쓰는 필터 컬럼)
-- =====================================================================
CREATE INDEX idx_notice_status          ON notice (status);
CREATE INDEX idx_notice_dept            ON notice (dept_id);
CREATE INDEX idx_notice_created         ON notice (created_at DESC);
CREATE INDEX idx_schedule_dept          ON schedule (dept_id);
CREATE INDEX idx_schedule_visible       ON schedule (visible);
CREATE INDEX idx_banner_status          ON banner (status);
CREATE INDEX idx_shuttle_status         ON shuttle (status);
CREATE INDEX idx_user_dept              ON users (dept_id);
CREATE INDEX idx_admin_dept             ON admin (dept_id);
CREATE INDEX idx_course_offering_dept   ON course_offering (dept_id);
CREATE INDEX idx_enrollment_user        ON enrollment (user_id);
CREATE INDEX idx_meal_date              ON meal (meal_date);

-- =====================================================================
-- 시드 데이터
-- =====================================================================

-- 학과
INSERT INTO department (dept_code, dept_name) VALUES
    (1, '컴퓨터공학과'),
    (2, '전기전자공학과'),
    (3, '기계공학부');

-- 관리자 계정
--  - 컴퓨터공학과 학과 관리자(DEPT_ADMIN): dept_id = 컴공
--  - 전체 관리자(SUPER_ADMIN): dept_id = NULL
INSERT INTO admin (dept_id, role, username, password, admin_name, alarm_tracking, alarm_send)
VALUES
    ( (SELECT dept_id FROM department WHERE dept_code = 1),
      'DEPT_ADMIN', 'admin',
      '$2a$10$4.uklpPfAX1.z5aSicUgIuq1/Mu6pHqtjjBTZPZRWAQJH7ZBag4y6',
      '컴퓨터공학과 관리자', true, true ),
    ( NULL,
      'SUPER_ADMIN', 'super',
      '$2a$10$4.uklpPfAX1.z5aSicUgIuq1/Mu6pHqtjjBTZPZRWAQJH7ZBag4y6',
      '전체 관리자', true, true );