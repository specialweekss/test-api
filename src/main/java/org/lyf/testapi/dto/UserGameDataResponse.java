package org.lyf.testapi.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户游戏数据响应DTO
 */
@Data
public class UserGameDataResponse {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 玩家基础信息
     */
    private PlayerInfo playerInfo;

    /**
     * 助理数据数组
     */
    private List<Assistant> assistants;

    /**
     * 挑战数据数组
     */
    private List<Challenge> challenges;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
}

