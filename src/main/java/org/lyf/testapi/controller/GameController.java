package org.lyf.testapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyf.testapi.dto.Result;
import org.lyf.testapi.dto.UserGameDataRequest;
import org.lyf.testapi.dto.UserGameDataResponse;
import org.lyf.testapi.service.UserGameDataService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏数据控制器
 */
@RestController
@RequestMapping("/api/game")
@Slf4j
@RequiredArgsConstructor
public class GameController {

    private final UserGameDataService userGameDataService;

    /**
     * 获取用户游戏数据接口
     * GET /api/game/user-data?userId=123456
     *
     * @param userId 用户ID
     * @return 用户游戏数据
     */
    @GetMapping("/user-data")
    public Result<UserGameDataResponse> getUserData(@RequestParam("userId") String userId) {
        try {
            log.info("获取用户游戏数据接口调用, userId:{}", userId);
            if (userId == null || userId.trim().isEmpty()) {
                return Result.badRequest("用户ID不能为空");
            }

            UserGameDataResponse response = userGameDataService.getUserGameData(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取用户游戏数据接口异常, userId:{}", userId, e);
            return Result.error(500, "服务器内部错误");
        }
    }

    /**
     * 保存用户游戏数据接口
     * POST /api/game/user-data
     *
     * @param request 保存请求
     * @return 保存结果
     */
    @PostMapping("/user-data")
    public Result<Map<String, Object>> saveUserData(@RequestBody UserGameDataRequest request) {
        try {
            log.info("保存用户游戏数据接口调用, userId:{}", request != null ? request.getUserId() : "null");
            if (request == null) {
                return Result.badRequest("请求参数不能为空");
            }

            UserGameDataService.SaveResult saveResult = userGameDataService.saveUserGameData(request);
            if (!saveResult.getSuccess()) {
                return Result.badRequest(saveResult.getError());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("lastUpdateTime", saveResult.getLastUpdateTime());
            return Result.success(data);
        } catch (Exception e) {
            log.error("保存用户游戏数据接口异常, userId:{}", request != null ? request.getUserId() : "null", e);
            return Result.error(500, "服务器内部错误");
        }
    }
}

