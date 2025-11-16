package org.lyf.testapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyf.testapi.service.WechatLoginService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Token验证拦截器
 * 用于验证请求头中的token，并将openid存储到request属性中
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final WechatLoginService wechatLoginService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 排除登录接口，不需要token验证
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/api/game/wx-login")) {
            return true;
        }

        // 从请求头获取token
        String token = request.getHeader("X-Token");
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null || token.isEmpty()) {
            log.warn("token未提供, uri:{}", requestURI);
            writeErrorResponse(response, 401, "token未提供");
            return false;
        }

        // 通过token获取openid
        String openid = wechatLoginService.getOpenidByToken(token);
        if (openid == null || openid.isEmpty()) {
            log.warn("token无效或已过期, token:{}, uri:{}", token, requestURI);
            // 返回401错误，提示客户端需要重新登录
            writeErrorResponse(response, 401, "token无效或已过期，请重新登录");
            return false;
        }

        // 将openid存储到request属性中，供后续使用
        request.setAttribute("openid", openid);
        log.debug("token验证成功, openid:{}, uri:{}", openid, requestURI);
        return true;
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

