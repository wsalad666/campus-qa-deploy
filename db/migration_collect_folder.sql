-- ============================================
-- 用户收藏夹自定义分类管理
-- 用法：mysql -u root -p"20060828" campus_qa < migration_collect_folder.sql
-- ============================================

USE campus_qa;

-- 1. 收藏文件夹表
CREATE TABLE IF NOT EXISTS user_collect_folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_name VARCHAR(50) NOT NULL COMMENT '文件夹名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏文件夹表';

-- 2. 收藏关联表
CREATE TABLE IF NOT EXISTS user_collect_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_id BIGINT NOT NULL COMMENT '文件夹ID',
    target_type TINYINT NOT NULL COMMENT '收藏类型(1=提问, 2=资源)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_folder_target (user_id, folder_id, target_type, target_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏关联表';

-- 3. 迁移旧收藏数据到「默认收藏」文件夹
-- 为每个已有收藏的用户创建默认文件夹
INSERT INTO user_collect_folder (user_id, folder_name, create_time)
SELECT DISTINCT user_id, '默认收藏', NOW()
FROM favorite
WHERE user_id NOT IN (SELECT user_id FROM user_collect_folder WHERE folder_name = '默认收藏');

-- 迁移收藏提问数据
INSERT INTO user_collect_relation (user_id, folder_id, target_type, target_id, create_time)
SELECT uf.user_id, ucf.id, 1, uf.target_id, uf.create_time
FROM favorite uf
INNER JOIN user_collect_folder ucf ON uf.user_id = ucf.user_id AND ucf.folder_name = '默认收藏'
WHERE uf.type = 1
AND NOT EXISTS (
    SELECT 1 FROM user_collect_relation ucr 
    WHERE ucr.user_id = uf.user_id AND ucr.folder_id = ucf.id AND ucr.target_type = 1 AND ucr.target_id = uf.target_id
);

-- 迁移收藏资源数据
INSERT INTO user_collect_relation (user_id, folder_id, target_type, target_id, create_time)
SELECT uf.user_id, ucf.id, 2, uf.target_id, uf.create_time
FROM favorite uf
INNER JOIN user_collect_folder ucf ON uf.user_id = ucf.user_id AND ucf.folder_name = '默认收藏'
WHERE uf.type = 2
AND NOT EXISTS (
    SELECT 1 FROM user_collect_relation ucr 
    WHERE ucr.user_id = uf.user_id AND ucr.folder_id = ucf.id AND ucr.target_type = 2 AND ucr.target_id = uf.target_id
);