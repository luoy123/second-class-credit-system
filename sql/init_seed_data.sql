USE second_class_credit;

INSERT INTO sc_credit_rule (category, base_credit, description, enabled)
VALUES
    ('志愿服务', 1.00, '参与志愿服务活动的基础学分', 1),
    ('学术竞赛', 2.00, '参与校级及以上学术竞赛的基础学分', 1),
    ('文体活动', 1.50, '参与文体类活动的基础学分', 1),
    ('创新创业', 2.50, '参与创新创业项目与训练的基础学分', 1)
ON DUPLICATE KEY UPDATE
    base_credit = VALUES(base_credit),
    description = VALUES(description),
    enabled = VALUES(enabled);
