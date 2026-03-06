# API 联调说明

## 前置条件

- 数据库已执行：
  - `sql/create_database.sql`
  - `sql/init_schema.sql`
  - `sql/init_seed_data.sql`
- 服务已启动：`mvn spring-boot:run`

## 推荐联调顺序

1. 创建学生
2. 创建活动
3. 提交学分申请
4. 审核通过或驳回学分申请
5. 查询学生学分汇总
6. 查询统计分析接口

## Curl 示例

### 1) 创建学生

```bash
curl -X POST "http://127.0.0.1:8080/api/students" \
  -H "Content-Type: application/json" \
  -d "{\"studentNo\":\"20260001\",\"name\":\"张三\",\"major\":\"软件工程\",\"grade\":\"2026\"}"
```

### 2) 创建活动

```bash
curl -X POST "http://127.0.0.1:8080/api/activities" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"校级志愿服务活动\",\"category\":\"志愿服务\",\"organizer\":\"校团委\",\"activityDate\":\"2026-03-06\",\"maxCredit\":1.50}"
```

### 3) 提交学分申请

```bash
curl -X POST "http://127.0.0.1:8080/api/credits/apply" \
  -H "Content-Type: application/json" \
  -d "{\"studentId\":1,\"activityId\":1,\"category\":\"志愿服务\",\"remark\":\"接口联调\"}"
```

### 4) 审核通过学分申请

```bash
curl -X POST "http://127.0.0.1:8080/api/credits/1/approve" \
  -H "Content-Type: application/json" \
  -d "{\"remark\":\"审核通过\"}"
```

### 5) 驳回学分申请

```bash
curl -X POST "http://127.0.0.1:8080/api/credits/2/reject" \
  -H "Content-Type: application/json" \
  -d "{\"remark\":\"材料不完整\"}"
```

### 6) 查询学分汇总

```bash
curl "http://127.0.0.1:8080/api/credits/students/1/summary"
```

### 7) 查询分类统计

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/categories"
```

### 8) 查询年度按月统计

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/monthly?year=2026"
```

### 9) 查询学分排名

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/ranking?topN=10"
```

## PowerShell 一键联调

- 脚本路径：`scripts/api-smoke-test.ps1`
- 运行命令：

```powershell
powershell -ExecutionPolicy Bypass -File "./scripts/api-smoke-test.ps1"
```
