package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 讀取與 FileStorageService 相同的設定值，確保路徑一致
    @Value("${app.file.upload-path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 將 URL 路徑 /images/** 對應到本機資料夾 uploads/
        // 這樣前端訪問 http://localhost:8080/images/xxx.jpg 時，
        // Spring Boot 會去讀取專案根目錄下的 uploads/xxx.jpg
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
