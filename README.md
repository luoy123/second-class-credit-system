# 基于 Spring Boot 的第二课堂学分统计与分析系统

## 项目简介

本项目用于支撑高校第二课堂活动学分的登记、审批、统计与分析，提供面向学生与管理端的基础数据能力。

## 核心功能（当前版本）

- 学生信息管理（新增、查询）
- 第二课堂活动管理（新增、查询）
- 学分申请与记录管理
- 学生学分汇总统计（总学分 + 分类汇总）

## 本地启动

1. 准备 MySQL 8+ 数据库，并创建库 `second_class_credit`
   - 可直接执行 `sql/create_database.sql`
2. 初始化表结构与基础规则数据
   - 执行 `sql/init_schema.sql`
   - 执行 `sql/init_seed_data.sql`
3. 配置数据库连接环境变量（推荐）
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
4. 执行命令：

```bash
mvn spring-boot:run
```

5. 运行接口联调脚本（可选）

```powershell
powershell -ExecutionPolicy Bypass -File "./scripts/api-smoke-test.ps1"
```

## API 示例

- `POST /api/students`：创建学生
- `GET /api/students/{id}`：查询学生
- `POST /api/activities`：创建活动
- `POST /api/credits/apply`：提交学分申请
- `GET /api/credits/students/{studentId}/summary`：查询学分汇总

## 文档

- 开发流程规划：`docs/DEVELOPMENT_PLAN.md`
- 技术选型说明：`docs/TECH_STACK.md`
- 数据库初始化说明：`docs/DB_SETUP.md`
- API 联调说明：`docs/API_DEBUG.md`
