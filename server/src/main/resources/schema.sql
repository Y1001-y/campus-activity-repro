-- 真实建表：H2 内存库，spring.sql.init 在每次进程启动时执行
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  real_name VARCHAR(64) NOT NULL,
  role VARCHAR(32) NOT NULL,
  student_no VARCHAR(32),
  phone VARCHAR(32),
  status INT NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS activity_category (
  id BIGINT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  sort INT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS activity (
  id BIGINT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  category_id BIGINT NOT NULL,
  cover VARCHAR(255),
  place VARCHAR(128),
  start_time VARCHAR(32),
  end_time VARCHAR(32),
  signup_deadline VARCHAR(32),
  capacity INT NOT NULL DEFAULT 0,
  signed_up INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  audit_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
  organizer_id BIGINT,
  description VARCHAR(1000),
  checkin_code VARCHAR(32)
);
CREATE TABLE IF NOT EXISTS activity_signup (
  id BIGINT PRIMARY KEY,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at VARCHAR(32)
);
CREATE TABLE IF NOT EXISTS activity_checkin (
  id BIGINT PRIMARY KEY,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  checked_in_at VARCHAR(32)
);
CREATE TABLE IF NOT EXISTS sys_notice (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200),
  content VARCHAR(500),
  created_at VARCHAR(32)
);
CREATE TABLE IF NOT EXISTS op_log (
  id BIGINT PRIMARY KEY,
  user_id BIGINT,
  username VARCHAR(64),
  action VARCHAR(128),
  detail VARCHAR(500),
  created_at VARCHAR(32)
);
