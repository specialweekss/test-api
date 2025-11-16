package org.lyf.testapi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户游戏数据实体类
 * 对应数据库表 user_game_data
 */
@Data
public class UserGameData {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 玩家等级
     */
    private Integer playerLevel;

    /**
     * 当前金钱
     */
    private Long money;

    /**
     * 点击收益基础值
     */
    private Long clickRewardBase;

    /**
     * 点击收益倍率
     */
    private Double clickMultiplier;

    /**
     * 升级所需金币
     */
    private Long upgradeCost;

    /**
     * 助理培训次数（基础收益倍率 = 2^trainingCount）
     */
    private Integer trainingCount;

    /**
     * 助理数据JSON
     */
    private String assistantsData;

    /**
     * 挑战数据JSON
     */
    private String challengesData;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

