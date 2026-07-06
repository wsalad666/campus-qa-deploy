-- ============================================
-- 初始数据脚本（建表后执行）
-- ============================================

USE campus_qa;

-- 1. 插入管理员账号
--    用户名: admin   密码: admin123
INSERT INTO admin (username, password, nickname, role) VALUES
('admin', '$2a$10$EMkD25NreNJr2EUk9Rrj5OWZBlNyAxV/dxf5jleTuw4ZUlrJxLtxq', '超级管理员', 2);

-- 2. 为管理员创建对应的 user 记录（用于前台个人主页等功能）
INSERT INTO user (username, student_no, nickname, signature, password, create_time, update_time) VALUES
('admin', 'ADMIN001', '超级管理员', '校园管理员', '$2a$10$EMkD25NreNJr2EUk9Rrj5OWZBlNyAxV/dxf5jleTuw4ZUlrJxLtxq', NOW(), NOW());

-- 3. 关联 admin 与 user
UPDATE admin SET user_id = (SELECT id FROM user WHERE username = 'admin') WHERE username = 'admin';

-- 4. 插入课程分类
INSERT INTO course (name, description, sort_order) VALUES
('高等数学', '微积分、线性代数、概率统计等课程', 1),
('大学英语', '英语听说读写、四六级备考', 2),
('程序设计', 'C/C++、Java、Python 等编程语言', 3),
('数据结构与算法', '常见数据结构与算法分析', 4),
('计算机网络', 'TCP/IP、HTTP、网络协议', 5),
('操作系统', '进程管理、内存管理、文件系统', 6),
('数据库原理', 'SQL、关系代数、数据库设计', 7),
('软件工程', '需求分析、设计模式、项目管理', 8),
('人工智能', '机器学习、深度学习、NLP', 9),
('其他课程', '公共课、选修课等', 10);