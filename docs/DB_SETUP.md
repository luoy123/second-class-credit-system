# 数据库初始化说明

## 执行顺序

1. `sql/create_database.sql`
2. `sql/init_schema.sql`
3. `sql/init_seed_data.sql`
4. （增量）`sql/patch_add_pending_index.sql`

## 命令示例

```bash
mysql -h127.0.0.1 -uroot -p second_class_credit < sql/init_schema.sql
mysql -h127.0.0.1 -uroot -p second_class_credit < sql/init_seed_data.sql
```

## 说明

- 若重复执行，`CREATE TABLE IF NOT EXISTS` 不会覆盖现有表
- 若已存在表结构且需补充新索引，请手动执行对应 `ALTER TABLE` 或重建测试库后重跑脚本
- 学分规则脚本使用 `ON DUPLICATE KEY UPDATE`，可用于更新基础规则
