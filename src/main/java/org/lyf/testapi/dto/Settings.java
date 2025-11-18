package org.lyf.testapi.dto;

import lombok.Data;

/**
 * 用户设置DTO
 * 用于存储用户的各种设置项，采用JSON对象格式，便于后续扩展
 */
@Data
public class Settings {
    /**
     * 音效开关（true=开启，false=关闭）
     * 控制游戏内所有点击音效的播放
     */
    private Boolean soundEnabled;

    /**
     * 背景音乐开关（true=开启，false=关闭）
     * 控制背景音乐的播放
     */
    private Boolean musicEnabled;

    /**
     * 创建默认设置（所有设置项为true）
     *
     * @return 默认设置对象
     */
    public static Settings createDefault() {
        Settings settings = new Settings();
        settings.setSoundEnabled(true);
        settings.setMusicEnabled(true);
        return settings;
    }
}

