-- 数据库迁移脚本：添加 training_count 字段
-- 执行时间：2025-01-XX
-- 原因：支持助理培训次数字段，用于计算基础收益倍率（基础收益倍率 = 2^trainingCount）

-- 添加 training_count 字段
ALTER TABLE `user_game_data` 
ADD COLUMN `training_count` INT DEFAULT 0 COMMENT '助理培训次数（基础收益倍率 = 2的trainingCount次方）' 
AFTER `upgrade_cost`;

