-- 数据库迁移脚本：添加 settings_data 字段
-- 执行时间：2025-01-XX
-- 原因：支持用户设置字段（JSON格式，包含音效、背景音乐等设置项）

-- 添加 settings_data 字段
ALTER TABLE `user_game_data` 
ADD COLUMN `settings_data` JSON COMMENT '用户设置（JSON格式，包含音效、背景音乐等设置项）' 
AFTER `challenges_data`;

