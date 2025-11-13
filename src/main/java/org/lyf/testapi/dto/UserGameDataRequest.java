package org.lyf.testapi.dto;

import lombok.Data;

import java.util.List;

/**
 * 保存用户游戏数据请求DTO
 */
@Data
public class UserGameDataRequest {
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
}

