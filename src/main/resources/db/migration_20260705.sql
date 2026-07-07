-- ============================================================
-- 2026-07-05 更新：数据库迁移脚本
-- 需要执行以下SQL（在 campus_qa 数据库中执行）
-- ============================================================

-- 1. resource 表新增 resource_type 字段（资源分类）
ALTER TABLE resource
    ADD COLUMN resource_type INT DEFAULT NULL COMMENT '资源类型(0=试卷, 1=习题, 2=笔记, 3=课件)';

-- 2. 新建用户常用课程表
CREATE TABLE IF NOT EXISTS user_course_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_course (user_id, course_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户常用课程表';