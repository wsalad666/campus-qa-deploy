-- ============================================
-- V2: 为admin表添加user_id字段，关联user表
-- 用于管理员前台个人主页功能
-- ============================================

-- 1. 添加user_id字段
ALTER TABLE admin ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '关联的user表ID，用于前台个人主页';

-- 2. 为已有管理员创建user记录（如果不存在）
-- 插入user记录（给admin管理员）
INSERT INTO user (username, nickname, signature, password, create_time)
SELECT 'admin', '超级管理员', '校园管理员', '$2a$10$EMkD25NreNJr2EUk9Rrj5OWZBlNyAxV/dxf5jleTuw4ZUlrJxLtxq', NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 3. 更新admin的user_id
UPDATE admin SET user_id = (SELECT id FROM user WHERE username = 'admin') WHERE username = 'admin';