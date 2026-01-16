package com.example.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-path:uploads}")
    private String uploadPath;

    private String location; // file:/D:/.../images/

    // 1) 圖片路徑映射設定
    @PostConstruct
    public void init() throws IOException {
        if (!StringUtils.hasText(uploadPath)) {
            System.err.println("警告: app.file.upload-path 未設定，圖片功能可能無法使用");
            return;
        }

        // 取得絕對路徑，避免相對路徑在不同環境下出錯
        Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            System.out.println("✅ 已成功建立上傳目錄: " + dir);
        }
        // 轉成 URI 格式 (例如 file:///D:/project/uploads/)
        location = dir.toUri().toString();

        // 確保路徑以 / 結尾，這是 Spring ResourceHandler 對目錄路徑的要求
        if (!location.endsWith("/")) {
            location += "/";
        }

        System.out.println("\n--- [圖片映射檢查] ---");
        System.out.println("本地磁碟路徑: " + dir);
        System.out.println("映射資源位置: " + location);
        System.out.println("對外 URL: /images/**");
        System.out.println("---------------------\n");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用計算好的絕對路徑 location，比寫死 file:./uploads/ 更安全
        if (location != null) {
            registry.addResourceHandler("/images/**")
                    .addResourceLocations(location);
        }
    }

    // 2) CORS 跨域設定（穩定版）

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 明確列出前端網址（避免 allowCredentials(true) + "*" 直接炸）
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 3) 健康檢查控制器
    @RestController
    public static class HealthCheckController {
        @GetMapping("/test")
        public String check() {
            return "Backend is running! (後端運作中)";
        }
    }
}
