# 基于 Spring Boot 的第二课堂学分统计与分析系统

## 项目简介

本项目用于支撑高校第二课堂活动学分的登记、审批、统计与分析，提供面向学生与管理端的基础数据能力。

## 核心功能（当前版本）

- 学生信息管理（新增、查询）
- 第二课堂活动管理（新增、查询）
- 学分申请、审核与记录管理
- 管理员审批操作日志（成功/失败全量留痕）
- 学生学分汇总统计（总学分 + 分类汇总）
- 多维统计分析（分类、专业、年级、月份、排名）
- CSV 报表导出（分类统计、排名统计）
- Vue 管理端（登录、审批、统计、日志）前后端分离部署

## 本地启动

1. 准备 MySQL 8+ 数据库，并创建库 `second_class_credit`
   - 可直接执行 `sql/create_database.sql`
2. 初始化表结构与基础规则数据
   - 执行 `sql/init_schema.sql`
   - 执行 `sql/init_seed_data.sql`
   - 已存在库可执行增量脚本：`sql/patch_add_credit_review_log_table.sql`
3. 配置数据库连接环境变量（推荐）
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - （可选）认证相关：
     - `AUTH_ADMIN_USERNAME`
     - `AUTH_ADMIN_PASSWORD`
     - `JWT_SECRET`
     - `JWT_EXPIRE_SECONDS`
4. 启动后端服务：

```bash
mvn spring-boot:run
```

5. 启动前端服务（新终端）：

```bash
cd frontend
npm install
npm run dev
```

6. 访问地址：
   - 前端管理端：`http://127.0.0.1:5173`
   - 后端接口：`http://127.0.0.1:8080/api`

7. 运行接口联调脚本（可选）

```powershell
powershell -ExecutionPolicy Bypass -File "./scripts/api-smoke-test.ps1"
```

## API 示例

- 管理员接口支持以下两种鉴权方式：
  - `Authorization: Bearer <token>`（推荐，先调用登录接口获取）
  - `X-Role: ADMIN`（兼容保留）
- 审批与批量审批接口会自动写入 `sc_credit_review_log`

- `POST /api/auth/login`：管理员登录并获取 JWT
- `POST /api/students`：创建学生
- `GET /api/students/{id}`：查询学生
- `POST /api/activities`：创建活动
- `POST /api/credits/apply`：提交学分申请
- `POST /api/credits/{recordId}/approve`：审核通过学分申请
- `POST /api/credits/{recordId}/reject`：驳回学分申请
- `POST /api/credits/batch/approve`：批量审核通过学分申请
- `POST /api/credits/batch/reject`：批量驳回学分申请
- `GET /api/credits/students/{studentId}/summary`：查询学分汇总
- `GET /api/credits/students/{studentId}/records/page?page=0&size=10&status=PENDING&category=志愿服务&startDate=2026-03-01&endDate=2026-03-31`：分页查询学生学分记录（支持组合筛选）
- `GET /api/credits/pending/page?page=0&size=10`：分页查询待审核记录
- `GET /api/credits/review-logs/page?page=0&size=10&action=APPROVE&success=true&startDate=2026-03-01&endDate=2026-03-31`：分页查询审批日志
- `GET /api/credits/review-logs/stats?action=APPROVE&startDate=2026-03-01&endDate=2026-03-31`：查询审批日志统计
- `GET /api/credits/review-logs/export?action=APPROVE&success=true&limit=1000`：导出审批日志 CSV
- `GET /api/credits/analytics/categories`：查询分类学分统计
- `GET /api/credits/analytics/majors`：查询专业学分统计
- `GET /api/credits/analytics/grades`：查询年级学分统计
- `GET /api/credits/analytics/monthly?year=2026`：查询年度按月学分统计
- `GET /api/credits/analytics/ranking?topN=10`：查询学生学分排名
- `GET /api/credits/analytics/export/categories`：导出分类统计 CSV
- `GET /api/credits/analytics/export/ranking?topN=10`：导出排名统计 CSV

## 文档

- 开发流程规划：`docs/DEVELOPMENT_PLAN.md`
- 技术选型说明：`docs/TECH_STACK.md`
- 数据库初始化说明：`docs/DB_SETUP.md`
- API 联调说明：`docs/API_DEBUG.md`
