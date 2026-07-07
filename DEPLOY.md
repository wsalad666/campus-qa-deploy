# Campus QA 部署指南

## 环境要求

服务器需要安装：

- **Docker** >= 20.10
- **Docker Compose** >= 2.0

## 快速开始

### 1. 克隆项目

```bash
git clone <你的仓库地址>
cd campus-qa-deploy
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 文件，**至少修改以下三项**：

| 变量 | 说明 |
|------|------|
| `DB_PASSWORD` | MySQL root 密码 |
| `JWT_SECRET` | 用户端 JWT 密钥（随机字符串） |
| `ADMIN_JWT_SECRET` | 管理员端 JWT 密钥（随机字符串） |

> 生成随机密钥：`openssl rand -base64 32`

### 3. 启动

```bash
docker compose up -d
```

首次启动会自动构建镜像并初始化数据库，等待 1-2 分钟即可。

### 4. 访问

- **前端页面**：`http://你的服务器IP`
- **Swagger 文档**：`http://你的服务器IP/swagger-ui.html`

---

## 服务架构

```
                    ┌─────────────────────────┐
用户 ───────────────►│  Nginx (frontend) :80   │
                    │  /          -> 静态文件   │
                    │  /api/*     -> backend   │
                    │  /uploads/* -> backend   │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │  Spring Boot :8080       │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │  MySQL 8.0  :3306       │
                    └─────────────────────────┘
```

## 常用命令

```bash
# 查看运行状态
docker compose ps

# 查看日志
docker compose logs -f backend

# 重启服务
docker compose restart

# 停止服务
docker compose down

# 停止并清除数据（危险！）
docker compose down -v
```

## 数据备份

```bash
# 备份数据库
docker exec campus-qa-mysql mysqldump -u root -p campus_qa > backup.sql

# 备份上传文件
docker cp campus-qa-backend:/app/uploads ./uploads-backup
```
