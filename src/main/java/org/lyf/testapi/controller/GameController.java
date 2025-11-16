package org.lyf.testapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyf.testapi.dto.Result;
import org.lyf.testapi.dto.UserGameDataRequest;
import org.lyf.testapi.dto.UserGameDataResponse;
import org.lyf.testapi.dto.WxLoginRequest;
import org.lyf.testapi.dto.WxLoginResponse;
import org.lyf.testapi.service.UserGameDataService;
import org.lyf.testapi.service.WechatLoginService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final WechatLoginService wechatLoginService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 微信登录接口
     * POST /api/game/wx-login
     *
     * @param wxLoginRequest 微信登录请求
     * @return 登录结果，包含token和过期时间
     */
    @PostMapping("/wx-login")
    public Result<WxLoginResponse> wxLogin(@RequestBody WxLoginRequest wxLoginRequest) {
        String interfaceName = "/api/game/wx-login";
        String method = "POST";
        
        try {
            log.debug("========== 接口调用开始 ==========");
            log.debug("接口名称: {}, 请求方式: {}", interfaceName, method);
            
            if (wxLoginRequest == null || wxLoginRequest.getCode() == null || wxLoginRequest.getCode().trim().isEmpty()) {
                Result<WxLoginResponse> result = Result.badRequest("code参数不能为空");
                log.warn("微信登录失败: code参数不能为空");
                log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
                log.debug("========== 接口调用结束 ==========");
                return result;
            }

            // 调用微信登录服务
            WechatLoginService.TokenResult tokenResult = wechatLoginService.loginByCode(wxLoginRequest.getCode());
            
            if (!tokenResult.getSuccess()) {
                Result<WxLoginResponse> result = Result.error(401, tokenResult.getError());
                log.warn("微信登录失败: {}", tokenResult.getError());
                log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
                log.debug("========== 接口调用结束 ==========");
                return result;
            }

            // 构建响应
            WxLoginResponse wxLoginResponse = new WxLoginResponse();
            wxLoginResponse.setToken(tokenResult.getToken());
            wxLoginResponse.setExpiresIn(tokenResult.getExpiresIn());
            
            Result<WxLoginResponse> result = Result.success(wxLoginResponse);
            
            log.info("微信登录成功, token:{}", tokenResult.getToken());
            log.debug("返回结果: code={}, message={}, data存在={}", 
                result.getCode(), result.getMessage(), result.getData() != null);
            log.debug("========== 接口调用结束 ==========");
            
            return result;
        } catch (Exception e) {
            log.error("微信登录接口异常", e);
            Result<WxLoginResponse> result = Result.error(500, "服务器内部错误");
            log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
            log.debug("========== 接口调用结束 ==========");
            return result;
        }
    }

    /**
     * 获取用户游戏数据接口
     * GET /api/game/user-data
     * 注意：需要通过请求头传递token（X-Token 或 Authorization: Bearer {token}）
     *
     * @param request HTTP请求对象
     * @return 用户游戏数据
     */
    @GetMapping("/user-data")
    public Result<UserGameDataResponse> getUserData(HttpServletRequest request) {
        String interfaceName = "/api/game/user-data";
        String method = "GET";
        
        try {
            log.debug("========== 接口调用开始 ==========");
            log.debug("接口名称: {}, 请求方式: {}", interfaceName, method);
            
            // 从request属性中获取openid（由拦截器设置）
            String openid = (String) request.getAttribute("openid");
            if (openid == null || openid.trim().isEmpty()) {
                Result<UserGameDataResponse> result = Result.error(401, "未获取到用户信息");
                log.warn("获取用户游戏数据失败: 未获取到用户信息");
                log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
                log.debug("========== 接口调用结束 ==========");
                return result;
            }

            log.debug("请求参数: openid={}", openid);
            UserGameDataResponse response = userGameDataService.getUserGameData(openid);
            Result<UserGameDataResponse> result = Result.success(response);
            
            // 打印返回结果（DEBUG级别）
            log.debug("返回结果: code={}, message={}, data存在={}", 
                result.getCode(), result.getMessage(), result.getData() != null);
            log.debug("========== 接口调用结束 ==========");
            
            return result;
        } catch (Exception e) {
            log.error("获取用户游戏数据接口异常", e);
            Result<UserGameDataResponse> result = Result.error(500, "服务器内部错误");
            log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
            log.debug("========== 接口调用结束 ==========");
            return result;
        }
    }

    /**
     * 保存用户游戏数据接口
     * POST /api/game/user-data
     * 注意：需要通过请求头传递token（X-Token 或 Authorization: Bearer {token}）
     * 注意：请求体中不需要包含userId，服务器会根据token自动解析出openid
     *
     * @param request 保存请求（不包含userId）
     * @param httpRequest HTTP请求对象
     * @return 保存结果
     */
    @PostMapping("/user-data")
    public Result<Map<String, Object>> saveUserData(@RequestBody UserGameDataRequest request, HttpServletRequest httpRequest) {
        String interfaceName = "/api/game/user-data";
        String method = "POST";
        
        try {
            log.debug("========== 接口调用开始 ==========");
            log.debug("接口名称: {}, 请求方式: {}", interfaceName, method);
            
            if (request == null) {
                log.warn("保存用户游戏数据失败: 请求参数不能为空");
                Result<Map<String, Object>> result = Result.badRequest("请求参数不能为空");
                log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
                log.debug("========== 接口调用结束 ==========");
                return result;
            }
            
            // 从request属性中获取openid（由拦截器设置）
            String openid = (String) httpRequest.getAttribute("openid");
            if (openid == null || openid.trim().isEmpty()) {
                Result<Map<String, Object>> result = Result.error(401, "未获取到用户信息");
                log.warn("保存用户游戏数据失败: 未获取到用户信息");
                log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
                log.debug("========== 接口调用结束 ==========");
                return result;
            }
            
            // 设置userId为openid
            request.setUserId(openid);
            log.debug("请求参数: openid={}", openid);

            UserGameDataService.SaveResult saveResult = userGameDataService.saveUserGameData(request);
            if (!saveResult.getSuccess()) {
                log.warn("保存用户游戏数据失败, openid:{}, error:{}", openid, saveResult.getError());
                Result<Map<String, Object>> result = Result.badRequest(saveResult.getError());
                log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
                log.debug("========== 接口调用结束 ==========");
                return result;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("lastUpdateTime", saveResult.getLastUpdateTime());
            Result<Map<String, Object>> result = Result.success(data);
            
            // 打印返回结果（DEBUG级别）
            log.debug("返回结果: code={}, message={}, data存在={}", 
                result.getCode(), result.getMessage(), result.getData() != null);
            log.debug("========== 接口调用结束 ==========");
            
            return result;
        } catch (Exception e) {
            log.error("保存用户游戏数据接口异常, openid:{}", 
                httpRequest.getAttribute("openid") != null ? httpRequest.getAttribute("openid") : "null", e);
            Result<Map<String, Object>> result = Result.error(500, "服务器内部错误");
            log.debug("返回结果: code={}, message={}", result.getCode(), result.getMessage());
            log.debug("========== 接口调用结束 ==========");
            return result;
        }
    }
}

