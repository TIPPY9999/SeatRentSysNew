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
        // 保持原本的 uploads 映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // ✨ 新增這個：把前端請求的 /images/** 指向你的實體存放資料夾
        // 假設你的圖片是存放在專案根目錄下的 uploads 資料夾
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/");
    }
}
