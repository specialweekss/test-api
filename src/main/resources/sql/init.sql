-- 用户游戏数据表
CREATE TABLE IF NOT EXISTS user_game_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL UNIQUE COMMENT '用户ID',
    player_level INT NOT NULL DEFAULT 1 COMMENT '玩家等级',
    money BIGINT NOT NULL DEFAULT 0 COMMENT '当前金钱',
    click_reward_base BIGINT NOT NULL DEFAULT 100 COMMENT '点击收益基础值',
    click_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0 COMMENT '点击收益倍率',
    upgrade_cost BIGINT NOT NULL DEFAULT 10 COMMENT '升级所需金币',
    assistants_data JSON COMMENT '助理数据JSON',
    challenges_data JSON COMMENT '挑战数据JSON',
    last_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_update_time (last_update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户游戏数据表';

