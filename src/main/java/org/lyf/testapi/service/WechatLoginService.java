package org.lyf.testapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 微信登录服务类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WechatLoginService {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Token存储（内存缓存）
     * Key: token
     * Value: openid
     * 实际生产环境建议使用Redis
     */
    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    /**
     * 定时清理过期token的线程池
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Token信息内部类
     */
    @lombok.Data
    private static class TokenInfo {
        private String openid;
        private String sessionKey;
        private long expireTime;

        public TokenInfo(String openid, String sessionKey, int expiresIn) {
            this.openid = openid;
            this.sessionKey = sessionKey;
            this.expireTime = System.currentTimeMillis() + expiresIn * 1000L;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 通过微信code换取openid和session_key，并生成自定义登录态token
     *
     * @param code 微信登录凭证code
     * @return Token信息，包含token和过期时间
     */
    public TokenResult loginByCode(String code) {
        try {
            log.debug("微信登录开始, code:{}", code);
            
            // 输出当前使用的AppID和AppSecret（debug级别，用于调试）
            log.debug("当前使用的微信配置 - AppID: {}, AppSecret: {}", appid, secret);

            // 调用微信 code2Session 接口
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appid, secret, code
            );
            
            log.debug("请求微信接口URL: {}", url.replace(secret, "***")); // 隐藏secret，只显示部分URL

            String response = restTemplate.getForObject(url, String.class);
            log.debug("微信接口响应: {}", response);

            // 解析响应
            Map<String, Object> wxResponse = objectMapper.readValue(response, Map.class);

            // 检查是否有错误
            if (wxResponse.containsKey("errcode")) {
                Integer errcode = (Integer) wxResponse.get("errcode");
                String errmsg = (String) wxResponse.get("errmsg");
                log.warn("微信登录失败, errcode:{}, errmsg:{}", errcode, errmsg);
                return new TokenResult(false, null, 0, "微信登录失败: " + errmsg);
            }

            // 获取 openid 和 session_key
            String openid = (String) wxResponse.get("openid");
            String sessionKey = (String) wxResponse.get("session_key");
            String unionid = (String) wxResponse.get("unionid");

            if (openid == null || openid.isEmpty()) {
                log.error("未获取到openid");
                return new TokenResult(false, null, 0, "未获取到openid");
            }

            log.info("微信登录成功, openid:{}, unionid:{}", openid, unionid);

            // 生成自定义登录态token
            String token = UUID.randomUUID().toString().replace("-", "");

            // 存储token与openid的映射关系（2小时过期）
            int expiresIn = 7200; // 2小时
            tokenCache.put(token, new TokenInfo(openid, sessionKey, expiresIn));

            // 定时清理过期token（每小时清理一次）
            scheduler.schedule(() -> {
                tokenCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            }, 1, TimeUnit.HOURS);

            log.debug("生成token成功, token:{}, openid:{}", token, openid);

            return new TokenResult(true, token, expiresIn, null);
        } catch (Exception e) {
            log.error("微信登录异常, code:{}", code, e);
            return new TokenResult(false, null, 0, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 通过token获取openid
     *
     * @param token 自定义登录态token
     * @return openid，如果token无效或已过期返回null
     */
    public String getOpenidByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        TokenInfo tokenInfo = tokenCache.get(token);
        if (tokenInfo == null) {
            log.debug("token不存在, token:{}", token);
            return null;
        }

        if (tokenInfo.isExpired()) {
            log.debug("token已过期, token:{}", token);
            tokenCache.remove(token);
            return null;
        }

        return tokenInfo.getOpenid();
    }

    /**
     * Token结果内部类
     */
    @lombok.Data
    public static class TokenResult {
        private Boolean success;
        private String token;
        private Integer expiresIn;
        private String error;

        public TokenResult(Boolean success, String token, Integer expiresIn, String error) {
            this.success = success;
            this.token = token;
            this.expiresIn = expiresIn;
            this.error = error;
        }
    }
}

