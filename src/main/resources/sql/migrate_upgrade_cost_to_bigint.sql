-- 数据库迁移脚本：将 upgrade_cost 和 click_reward_base 字段从 INT 改为 BIGINT
-- 执行时间：2025-11-15
-- 原因：支持更大的数值范围，避免超出 int 范围（-2147483648 到 2147483647）的错误

-- 修改 upgrade_cost 字段类型
ALTER TABLE user_game_data 
MODIFY COLUMN upgrade_cost BIGINT NOT NULL DEFAULT 10 COMMENT '升级所需金币';

-- 修改 click_reward_base 字段类型
ALTER TABLE user_game_data 
MODIFY COLUMN click_reward_base BIGINT NOT NULL DEFAULT 100 COMMENT '点击收益基础值';

