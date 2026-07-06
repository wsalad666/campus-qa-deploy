-- ============================================
-- 管理员前台个人主页 - 数据库迁移脚本
-- 用法：mysql -u root -p"密码" campus_qa < migration_admin_user.sql
-- ============================================

USE campus_qa;

-- 1. 为admin表添加user_id字段
ALTER TABLE admin ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT NULL COMMENT '关联的user表ID，用于前台个人主页';

-- 2. 为已有管理员创建user记录（如果不存在）
INSERT INTO user (username, student_no, nickname, signature, password, create_time, update_time)
SELECT 'admin', 'ADMIN001', '超级管理员', '校园管理员', '$2a$10$EMkD25NreNJr2EUk9Rrj5OWZBlNyAxV/dxf5jleTuw4ZUlrJxLtxq', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 3. 更新admin的user_id
UPDATE admin SET user_id = (SELECT id FROM user WHERE username = 'admin') WHERE username = 'admin' AND user_id IS NULL;