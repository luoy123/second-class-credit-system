# API 联调说明

## 前置条件

- 数据库已执行：
  - `sql/create_database.sql`
  - `sql/init_schema.sql`
  - `sql/init_seed_data.sql`
- 服务已启动：`mvn spring-boot:run`
- 管理员接口推荐携带请求头：`Authorization: Bearer <token>`
- 兼容方式：`X-Role: ADMIN`
- 已执行增量脚本（若是旧库）：`sql/patch_add_credit_review_log_table.sql`

## 推荐联调顺序

1. 创建学生
2. 创建活动
3. 提交学分申请
4. 审核通过或驳回学分申请
5. 查询学生学分汇总
6. 查询统计分析接口

## Curl 示例

### 0) 管理员登录获取 JWT

```bash
TOKEN=$(curl -s -X POST "http://127.0.0.1:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}" | jq -r '.data.token')
```

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
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"remark\":\"审核通过\"}"
```

### 5) 驳回学分申请

```bash
curl -X POST "http://127.0.0.1:8080/api/credits/2/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"remark\":\"材料不完整\"}"
```

### 6) 批量审核通过

```bash
curl -X POST "http://127.0.0.1:8080/api/credits/batch/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"recordIds\":[1,2,3],\"remark\":\"批量审核通过\"}"
```

### 7) 批量驳回

```bash
curl -X POST "http://127.0.0.1:8080/api/credits/batch/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"recordIds\":[4,5],\"remark\":\"材料不齐\"}"
```

### 8) 查询学分汇总

```bash
curl "http://127.0.0.1:8080/api/credits/students/1/summary"
```

### 9) 分页查询学分记录（支持状态/分类/日期组合筛选）

```bash
curl "http://127.0.0.1:8080/api/credits/students/1/records/page?page=0&size=10&status=PENDING&category=志愿服务&startDate=2026-03-01&endDate=2026-03-31"
```

### 10) 分页查询待审核记录

```bash
curl -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/credits/pending/page?page=0&size=10"
```

### 11) 分页查询审批日志（可按动作/结果/日期筛选）

```bash
curl -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/credits/review-logs/page?page=0&size=10&action=APPROVE&success=true&startDate=2026-03-01&endDate=2026-03-31"
```

### 12) 导出审批日志 CSV

```bash
curl -L -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/credits/review-logs/export?action=APPROVE&success=true&limit=1000" -o "credit_review_logs.csv"
```

### 13) 查询审批日志统计

```bash
curl -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/credits/review-logs/stats?action=APPROVE&startDate=2026-03-01&endDate=2026-03-31"
```

### 14) 查询分类统计

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/categories"
```

### 15) 查询专业统计

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/majors"
```

### 16) 查询年级统计

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/grades"
```

### 17) 查询年度按月统计

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/monthly?year=2026"
```

### 18) 查询学分排名

```bash
curl "http://127.0.0.1:8080/api/credits/analytics/ranking?topN=10"
```

### 19) 导出分类统计 CSV

```bash
curl -L -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/credits/analytics/export/categories" -o "category_statistics.csv"
```

### 20) 导出排名统计 CSV

```bash
curl -L -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/credits/analytics/export/ranking?topN=10" -o "student_ranking.csv"
```

## 审批日志核验（SQL）

审批通过、驳回、批量审核都会写入 `sc_credit_review_log`，可用以下 SQL 查看：

```sql
SELECT id, record_id, action, operator_role, success, remark, detail, created_at
FROM sc_credit_review_log
ORDER BY id DESC
LIMIT 20;
```

## PowerShell 一键联调

- 脚本路径：`scripts/api-smoke-test.ps1`
- 运行命令：

```powershell
powershell -ExecutionPolicy Bypass -File "./scripts/api-smoke-test.ps1"
```
