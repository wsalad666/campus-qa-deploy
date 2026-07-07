-- Fix schema: sync database with Entity classes after teammate code merge
-- Date: 2026-07-06

-- 1. Create missing table: user_course_favorite
CREATE TABLE IF NOT EXISTS user_course_favorite (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id),
  INDEX idx_course_id (course_id),
  UNIQUE INDEX uk_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Create missing table: user_collect_folder
CREATE TABLE IF NOT EXISTS user_collect_folder (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  folder_name VARCHAR(128) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Create missing table: user_collect_relation
CREATE TABLE IF NOT EXISTS user_collect_relation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  folder_id BIGINT NOT NULL,
  target_type TINYINT NOT NULL COMMENT '1=提问, 2=资源',
  target_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id),
  INDEX idx_folder_id (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Add missing column: resource.resource_type
ALTER TABLE resource ADD COLUMN resource_type TINYINT DEFAULT NULL COMMENT '0=试卷,1=习题,2=笔记,3=课件' AFTER download_count;

-- 5. Add missing column: admin.user_id
ALTER TABLE admin ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '关联的user表ID' AFTER avatar;

-- 6. Remove is_offline from favorite
ALTER TABLE favorite DROP COLUMN is_offline;

-- 7. Remove is_offline from follow
ALTER TABLE follow DROP COLUMN is_offline;

-- 8. Remove is_offline from question_like
ALTER TABLE question_like DROP COLUMN is_offline;
