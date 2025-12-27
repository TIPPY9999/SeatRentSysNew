package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 取得絕對路徑並標準化斜線
        File file = new File(uploadPath);
        String absolutePath = file.getAbsolutePath().replace("\\", "/");
        if (!absolutePath.endsWith("/"))
            absolutePath += "/";

        // 2. Windows 必須使用 file:/// (三個斜線) 開頭
        String location = "file:///" + absolutePath;

        // 3. 這一行非常重要！如果啟動時沒看到這行，代表這個檔案沒被載入
        System.out.println("\n--- [圖片映射檢查] ---");
        System.out.println("本地磁碟路徑: " + absolutePath);
        System.out.println("映射資源位置: " + location);
        System.out.println("---------------------\n");

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
}