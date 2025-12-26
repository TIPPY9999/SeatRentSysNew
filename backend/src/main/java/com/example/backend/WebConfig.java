package com.example.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允許所有路徑的 API 被呼叫
        registry.addMapping("/**")
                // 允許來自任何來源的請求 (開發階段方便測試，正式上線可改為指定網域)
                .allowedOriginPatterns("*")
                // 允許的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允許攜帶認證資訊 (如 Cookies)
                .allowCredentials(true)
                // 允許的標頭
                .allowedHeaders("*");
    }

    // 內建一個簡單的測試控制器，用來檢查後端是否活著
    @RestController
    public static class HealthCheckController {
        @GetMapping("/test")
        public String check() {
            return "Backend is running! (後端運作中)";
        }
    }
}