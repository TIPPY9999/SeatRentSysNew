package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 設定靜態資源路徑，讓前端可以存取上傳的圖片
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 將 /uploads/** 的請求映射到 uploads/ 資料夾
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
