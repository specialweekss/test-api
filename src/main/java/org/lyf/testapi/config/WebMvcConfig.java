package org.lyf.testapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于注册拦截器和静态资源配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    /**
     * 静态资源路径配置
     * 支持通过配置文件指定，如果未配置则使用默认相对路径 ./resources/
     * 生产环境建议使用绝对路径，如：/opt/test-api/resources/
     */
    @Value("${app.resources.path:./resources/}")
    private String resourcesPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/game/**")
                .excludePathPatterns("/api/game/wx-login"); // 排除登录接口
    }

    /**
     * 配置静态资源处理器
     * 将 /resources/** 路径映射到配置的资源文件夹
     * 用于提供视频文件等静态资源访问
     * 
     * 路径格式：
     * - 相对路径：./resources/ （相对于 JAR 文件所在目录）
     * - 绝对路径：/opt/test-api/resources/ （推荐用于生产环境）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾
        String location = resourcesPath.endsWith("/") ? resourcesPath : resourcesPath + "/";
        
        // 如果路径不是以 file: 开头，则添加 file: 前缀
        if (!location.startsWith("file:")) {
            location = "file:" + location;
        }
        
        // 配置视频资源路径：/resources/assist/{challengeId}/success.mp4
        registry.addResourceHandler("/resources/**")
                .addResourceLocations(location)
                .setCachePeriod(3600); // 缓存1小时
    }
}

