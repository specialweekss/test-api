package org.lyf.testapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 微信配置类
 * 在应用启动时打印微信配置信息（用于调试）
 */
@Component
@Slf4j
public class WechatConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    /**
     * 应用启动完成后打印微信配置信息
     * 
     * @param event 应用就绪事件
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        // 隐藏secret的中间部分，只显示前4位和后4位
        String maskedSecret = maskSecret(secret);
        log.debug("微信配置信息 - AppID: {}, AppSecret: {}", appid, maskedSecret);
    }

    /**
     * 隐藏secret的中间部分，只显示前4位和后4位
     * 
     * @param secret 原始secret
     * @return 隐藏后的secret
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            // 如果secret长度小于等于8，全部隐藏
            return "****";
        }
        // 显示前4位和后4位，中间用*代替
        int length = secret.length();
        String prefix = secret.substring(0, 4);
        String suffix = secret.substring(length - 4);
        return prefix + "****" + suffix;
    }
}

