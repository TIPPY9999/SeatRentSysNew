package com.example.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ 修正：必須與 application.yml 中的 app.file.upload-path 一致
    @Value("${app.file.upload-path}")
    private String uploadPath;

    private String location; // file:/D:/.../images/

    @PostConstruct
    public void init() throws IOException {
        if (!StringUtils.hasText(uploadPath)) {
            throw new IllegalStateException("app.file.upload-path is empty");
        }

        Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(dir);      // 沒資料夾就自動建

        location = dir.toUri().toString(); // 自動轉成正確的 file URI

        System.out.println("\n--- [圖片映射檢查] ---");
        System.out.println("本地磁碟路徑: " + dir);
        System.out.println("映射資源位置: " + location);
        System.out.println("對外 URL: /images/**");
        System.out.println("---------------------\n");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 將 URL 的 /images/** 請求映射到硬碟實體路徑
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
}