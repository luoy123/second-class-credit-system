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
2. 修改 `src/main/resources/application.yml` 中数据库连接
3. 执行命令：

```bash
mvn spring-boot:run
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
