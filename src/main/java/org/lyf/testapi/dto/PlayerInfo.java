package org.lyf.testapi.dto;

import lombok.Data;

/**
 * 玩家基础信息DTO
 */
@Data
public class PlayerInfo {
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
    private Integer clickRewardBase;

    /**
     * 点击收益倍率
     */
    private Double clickMultiplier;

    /**
     * 升级所需金币
     */
    private Integer upgradeCost;
}

