package org.lyf.testapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyf.testapi.dto.*;
import org.lyf.testapi.entity.UserGameData;
import org.lyf.testapi.mapper.UserGameDataMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户游戏数据服务类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserGameDataService {

    private final UserGameDataMapper userGameDataMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取用户游戏数据
     *
     * @param userId 用户ID
     * @return 用户游戏数据响应
     */
    public UserGameDataResponse getUserGameData(String userId) {
        try {
            log.debug("获取用户游戏数据, userId:{}", userId);
            UserGameData userGameData = userGameDataMapper.selectByUserId(userId);

            // 如果用户不存在，创建并插入默认初始数据
            if (userGameData == null) {
                log.info("用户首次登录, 创建默认数据并插入数据库, userId:{}", userId);
                return createAndInsertDefaultData(userId);
            }

            // 转换为响应DTO
            UserGameDataResponse response = new UserGameDataResponse();
            response.setUserId(userGameData.getUserId());
            response.setLastUpdateTime(userGameData.getLastUpdateTime());

            // 设置玩家信息
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setPlayerLevel(userGameData.getPlayerLevel());
            playerInfo.setMoney(userGameData.getMoney());
            playerInfo.setClickRewardBase(userGameData.getClickRewardBase());
            playerInfo.setClickMultiplier(userGameData.getClickMultiplier());
            playerInfo.setUpgradeCost(userGameData.getUpgradeCost());
            // 如果字段不存在（旧数据），返回默认值 0
            playerInfo.setTrainingCount(userGameData.getTrainingCount() != null ? userGameData.getTrainingCount() : 0);
            response.setPlayerInfo(playerInfo);

            // 解析助理数据JSON
            try {
                List<Assistant> assistants = objectMapper.readValue(
                        userGameData.getAssistantsData(),
                        new TypeReference<List<Assistant>>() {}
                );
                response.setAssistants(assistants);
            } catch (Exception e) {
                log.error("解析助理数据JSON失败, userId:{}", userId, e);
                response.setAssistants(createDefaultAssistants());
            }

            // 解析挑战数据JSON
            try {
                List<Challenge> challenges = objectMapper.readValue(
                        userGameData.getChallengesData(),
                        new TypeReference<List<Challenge>>() {}
                );
                response.setChallenges(challenges);
            } catch (Exception e) {
                log.error("解析挑战数据JSON失败, userId:{}", userId, e);
                response.setChallenges(createDefaultChallenges());
            }

            // 解析设置数据JSON
            try {
                if (userGameData.getSettingsData() != null && !userGameData.getSettingsData().trim().isEmpty()) {
                    Settings settings = objectMapper.readValue(
                            userGameData.getSettingsData(),
                            Settings.class
                    );
                    // 如果设置项缺失，使用默认值
                    if (settings.getSoundEnabled() == null) {
                        settings.setSoundEnabled(true);
                    }
                    if (settings.getMusicEnabled() == null) {
                        settings.setMusicEnabled(true);
                    }
                    response.setSettings(settings);
                } else {
                    // 如果数据库中没有设置数据，使用默认设置
                    response.setSettings(Settings.createDefault());
                }
            } catch (Exception e) {
                log.error("解析设置数据JSON失败, userId:{}", userId, e);
                response.setSettings(Settings.createDefault());
            }

            return response;
        } catch (Exception e) {
            log.error("获取用户游戏数据异常, userId:{}", userId, e);
            // 异常时尝试创建默认数据（不插入数据库，避免异常循环）
            return createDefaultData(userId);
        }
    }

    /**
     * 保存用户游戏数据
     *
     * @param request 保存请求
     * @return 保存结果响应
     */
    public SaveResult saveUserGameData(UserGameDataRequest request) {
        try {
            log.debug("保存用户游戏数据, userId:{}", request.getUserId());

            // 数据校验
            String validateError = validateRequest(request);
            if (validateError != null) {
                log.warn("数据校验失败, userId:{}, error:{}", request.getUserId(), validateError);
                return new SaveResult(false, null, validateError);
            }

            // 转换为实体对象
            UserGameData userGameData = convertToEntity(request);
            
            // 调试日志：打印 trainingCount 值
            log.info("保存用户游戏数据 - trainingCount: {}, userId:{}", 
                userGameData.getTrainingCount(), request.getUserId());

            // 查询是否存在
            UserGameData existing = userGameDataMapper.selectByUserId(request.getUserId());
            if (existing == null) {
                // 插入新数据
                userGameData.setCreateTime(LocalDateTime.now());
                userGameData.setLastUpdateTime(LocalDateTime.now());
                int result = userGameDataMapper.insert(userGameData);
                log.debug("插入用户游戏数据, userId:{}, result:{}", request.getUserId(), result);
            } else {
                // 更新现有数据
                userGameData.setId(existing.getId());
                userGameData.setLastUpdateTime(LocalDateTime.now());
                int result = userGameDataMapper.updateByUserId(userGameData);
                log.debug("更新用户游戏数据, userId:{}, result:{}", request.getUserId(), result);
            }

            return new SaveResult(true, LocalDateTime.now(), null);
        } catch (Exception e) {
            log.error("保存用户游戏数据异常, userId:{}", request.getUserId(), e);
            return new SaveResult(false, null, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 数据校验
     */
    private String validateRequest(UserGameDataRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return "用户ID不能为空";
        }

        if (request.getPlayerInfo() == null) {
            return "玩家信息不能为空";
        }

        PlayerInfo playerInfo = request.getPlayerInfo();
        if (playerInfo.getPlayerLevel() == null || playerInfo.getPlayerLevel() < 1 || playerInfo.getPlayerLevel() > 999) {
            return "玩家等级必须在1-999之间";
        }
        if (playerInfo.getMoney() == null || playerInfo.getMoney() < 0) {
            return "金钱不能为负数";
        }
        if (playerInfo.getClickRewardBase() == null || playerInfo.getClickRewardBase() < 0) {
            return "点击收益基础值不能为负数";
        }
        if (playerInfo.getClickMultiplier() == null || playerInfo.getClickMultiplier() < 0) {
            return "点击收益倍率不能为负数";
        }
        if (playerInfo.getUpgradeCost() == null || playerInfo.getUpgradeCost() < 0) {
            return "升级所需金币不能为负数";
        }
        // trainingCount 必须为非负整数（>= 0），如果未提供或为负数，使用默认值 0
        if (playerInfo.getTrainingCount() != null && playerInfo.getTrainingCount() < 0) {
            return "助理培训次数不能为负数";
        }

        if (request.getAssistants() == null || request.getAssistants().isEmpty()) {
            return "助理数据不能为空";
        }
        for (Assistant assistant : request.getAssistants()) {
            if (assistant.getId() == null || assistant.getId() < 1) {
                return "助理ID必须大于0";
            }
            if (assistant.getLevel() == null || assistant.getLevel() < 0 || assistant.getLevel() > 50) {
                return "助理等级必须在0-50之间";
            }
        }

        if (request.getChallenges() == null || request.getChallenges().isEmpty()) {
            return "挑战数据不能为空";
        }
        for (Challenge challenge : request.getChallenges()) {
            if (challenge.getId() == null || challenge.getId() < 1) {
                return "挑战ID必须大于0";
            }
        }

        return null;
    }

    /**
     * 转换为实体对象
     */
    private UserGameData convertToEntity(UserGameDataRequest request) throws Exception {
        UserGameData userGameData = new UserGameData();
        userGameData.setUserId(request.getUserId());

        PlayerInfo playerInfo = request.getPlayerInfo();
        userGameData.setPlayerLevel(playerInfo.getPlayerLevel());
        userGameData.setMoney(playerInfo.getMoney());
        userGameData.setClickRewardBase(playerInfo.getClickRewardBase());
        userGameData.setClickMultiplier(playerInfo.getClickMultiplier());
        userGameData.setUpgradeCost(playerInfo.getUpgradeCost());
        // 如果请求中未包含 trainingCount 字段，使用默认值 0
        userGameData.setTrainingCount(playerInfo.getTrainingCount() != null ? playerInfo.getTrainingCount() : 0);

        // 将助理数据转换为JSON
        userGameData.setAssistantsData(objectMapper.writeValueAsString(request.getAssistants()));

        // 将挑战数据转换为JSON
        userGameData.setChallengesData(objectMapper.writeValueAsString(request.getChallenges()));

        // 将设置数据转换为JSON
        Settings settings = request.getSettings();
        if (settings == null) {
            // 如果请求中未包含 settings 字段，使用默认设置
            settings = Settings.createDefault();
        } else {
            // 如果设置项缺失，使用默认值
            if (settings.getSoundEnabled() == null) {
                settings.setSoundEnabled(true);
            }
            if (settings.getMusicEnabled() == null) {
                settings.setMusicEnabled(true);
            }
        }
        userGameData.setSettingsData(objectMapper.writeValueAsString(settings));

        return userGameData;
    }

    /**
     * 创建并插入默认数据到数据库
     *
     * @param userId 用户ID
     * @return 用户游戏数据响应
     */
    private UserGameDataResponse createAndInsertDefaultData(String userId) {
        try {
            // 创建默认响应数据
            UserGameDataResponse response = createDefaultData(userId);

            // 转换为实体对象并插入数据库
            UserGameData userGameData = new UserGameData();
            userGameData.setUserId(userId);
            userGameData.setPlayerLevel(response.getPlayerInfo().getPlayerLevel());
            userGameData.setMoney(response.getPlayerInfo().getMoney());
            userGameData.setClickRewardBase(response.getPlayerInfo().getClickRewardBase());
            userGameData.setClickMultiplier(response.getPlayerInfo().getClickMultiplier());
            userGameData.setUpgradeCost(response.getPlayerInfo().getUpgradeCost());
            userGameData.setTrainingCount(response.getPlayerInfo().getTrainingCount() != null ? response.getPlayerInfo().getTrainingCount() : 0);

            // 将助理和挑战数据转换为JSON
            userGameData.setAssistantsData(objectMapper.writeValueAsString(response.getAssistants()));
            userGameData.setChallengesData(objectMapper.writeValueAsString(response.getChallenges()));
            // 将设置数据转换为JSON
            userGameData.setSettingsData(objectMapper.writeValueAsString(response.getSettings()));

            LocalDateTime now = LocalDateTime.now();
            userGameData.setCreateTime(now);
            userGameData.setLastUpdateTime(now);

            // 插入数据库
            int result = userGameDataMapper.insert(userGameData);
            log.info("插入默认用户游戏数据成功, userId:{}, result:{}", userId, result);

            return response;
        } catch (Exception e) {
            log.error("创建并插入默认数据失败, userId:{}", userId, e);
            // 插入失败时仍然返回默认数据（不插入数据库）
            return createDefaultData(userId);
        }
    }

    /**
     * 创建默认数据（不插入数据库）
     *
     * @param userId 用户ID
     * @return 用户游戏数据响应
     */
    private UserGameDataResponse createDefaultData(String userId) {
        UserGameDataResponse response = new UserGameDataResponse();
        response.setUserId(userId);
        response.setLastUpdateTime(LocalDateTime.now());

        // 默认玩家信息
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerLevel(1);
        playerInfo.setMoney(0L);
        playerInfo.setClickRewardBase(100L);
        playerInfo.setClickMultiplier(1.0);
        playerInfo.setUpgradeCost(10L);
        playerInfo.setTrainingCount(0); // 默认培训次数为 0
        response.setPlayerInfo(playerInfo);

        // 默认助理数据
        response.setAssistants(createDefaultAssistants());

        // 默认挑战数据
        response.setChallenges(createDefaultChallenges());

        // 默认设置数据
        response.setSettings(Settings.createDefault());

        return response;
    }

    /**
     * 创建默认助理数据
     */
    private List<Assistant> createDefaultAssistants() {
        List<Assistant> assistants = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Assistant assistant = new Assistant();
            assistant.setId(i);
            assistant.setUnlocked(false);
            assistant.setLevel(0);
            assistants.add(assistant);
        }
        return assistants;
    }

    /**
     * 创建默认挑战数据
     */
    private List<Challenge> createDefaultChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Challenge challenge = new Challenge();
            challenge.setId(i);
            challenge.setCompleted(false);
            challenges.add(challenge);
        }
        return challenges;
    }

    /**
     * 保存结果内部类
     */
    @lombok.Data
    public static class SaveResult {
        private Boolean success;
        private LocalDateTime lastUpdateTime;
        private String error;

        public SaveResult(Boolean success, LocalDateTime lastUpdateTime, String error) {
            this.success = success;
            this.lastUpdateTime = lastUpdateTime;
            this.error = error;
        }
    }
}

