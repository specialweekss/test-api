package org.lyf.testapi.dto;

import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
public class WxLoginRequest {
    /**
     * 微信登录凭证code（通过wx.login()获取）
     */
    private String code;
}

