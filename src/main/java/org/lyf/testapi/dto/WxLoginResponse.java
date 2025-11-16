package org.lyf.testapi.dto;

import lombok.Data;

/**
 * 微信登录响应DTO
 */
@Data
public class WxLoginResponse {
    /**
     * 自定义登录态token
     */
    private String token;
    
    /**
     * token过期时间（秒）
     */
    private Integer expiresIn;
}

