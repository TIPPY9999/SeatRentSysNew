package com.example.backend.config;

import com.example.backend.repository.member.CustomOAuth2UserService;// 自訂的 OAuth2UserService
import org.springframework.beans.factory.annotation.Autowired;// 自動注入註解
import org.springframework.context.annotation.Bean;// 定義 Bean 的註解
import org.springframework.context.annotation.Configuration;// 配置類註解
import org.springframework.security.config.annotation.web.builders.HttpSecurity;// HttpSecurity 用於配置安全性
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;// 啟用 Spring Security 的註解
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;// BCrypt 密碼編碼器
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;// OAuth2 客戶端註冊庫
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;// 預設的 OAuth2 授權請求解析器
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;// OAuth2 授權請求解析器介面
import org.springframework.security.web.SecurityFilterChain;// 安全過濾鏈
import org.springframework.web.cors.CorsConfiguration;// CORS 配置類
import org.springframework.web.cors.CorsConfigurationSource;// CORS 配置來源介面
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;// 基於 URL 的 CORS 配置來源

import java.util.Arrays;

@Configuration 
@EnableWebSecurity
public class SecurityConfig {

    /**
     * ✅ 必留：member2/HEAD 的 PasswordEncoder
     * 這會影響一般帳密登入、改密碼、忘記密碼等流程
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                /**
                 * - 綠界回呼 /api/payment/** 多為外部 POST，若不排除常見 403
                 * - /login/member：註解說是解 403 的關鍵，保留
                 * - /api/auth/**：前端跨域呼叫常見，保留
                 */
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                /*這邊我要說明一下(翌帆) 原本寫法是這樣
                         "/api/payment/**", // 綠界回呼的 POST 請求
                         "/login/member",   // 放行前端登入請求
                         "/api/auth/**"    // 放行所有認證相關請求
                但是會發生其他功能模組的 POST 請求也被擋下來的問題，還有後台管理員登入無法進入的問題。
                所以我先改成放行所有後端 API 的請求，跟管理員登入請求，未來再視情況調整。
                */
                         "/api/**",      // 放行所有後端 API (工單、金流、座位、分析...)
                        "/login/**",    // 放行所有登入請求
                        "/oauth2/**"    // 放行 OAuth2 相關 (通常不需要，但加著保險)
                ))

                /**
                 * - 允許前端 localhost 呼叫
                 * - 允許 ngrok/loca.lt 呼叫
                 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                /**
                 * 
                 * 一般不會破壞功能（常見用於 h2-console 或 iframe 同源）
                 */
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                /**
                 * 整合兩邊的 permitAll 名單
                 * - 靜態資源（merchantAndCoupon2）
                 * - payment（merchantAndCoupon2）
                 * - analyze / forgot-password（HEAD）
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/favicon.ico", "/error",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/payment/**",
                                "/login/**",
                                "/oauth2/**",
                                "/api/auth/**",
                                "/api/analyze/**",
                                "/api/forgot-password/**",
                                "/api/admin/forgot-password/**"
                        ).permitAll()
                        /**
                         * ⚠️ 兩邊都寫 anyRequest permitAll → 代表目前是開發期策略
                         * 這裡不改，以免破壞既有功能與組員流程
                         */
                        .anyRequest().permitAll()
                )

                /**
                 * 保留：OAuth2 + prompt=select_account + 成功導回前端
                 */
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(clientRegistrationRepository)
                                ))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .defaultSuccessUrl("http://localhost:5173/", true)
                )

                /**
                 * 保留：logout 行為（兩邊一致）
                 */
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    /**
     * 保留：強制 Google 顯示帳號選擇視窗 prompt=select_account
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params ->
                        params.put("prompt", "select_account")));

<<<<<<< HEAD
                // 💡 允許的名單：包含本地與 Localtunnel，並增加綠界官方網域(可選，增加穩定性)
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173",
                                "https://*.ngrok-free.dev", // 💡 增加 ngrok 萬用字元
                                "https://*.trycloudflare.com",
                                "https://*.loca.lt"));
=======
        return resolver;
    }
>>>>>>> 9e71593542689727d695d101922bab93898a3979

    /**
     * ✅ 整合：CORS 允許 localhost + ngrok/loca.lt（merchantAndCoupon2）
     * ⚠️ 注意：allowedOrigins 不支援萬用字元時會無效（取決於 Spring 版本/設定）
     * 若之後 CORS 仍擋，再改 setAllowedOriginPatterns。 
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

<<<<<<< HEAD
                // 2. 專門給綠界回傳用的設定 (關鍵：不可有 Credentials)
                CorsConfiguration ecpayConfig = new CorsConfiguration();
                ecpayConfig.setAllowedOrigins(Arrays.asList("*")); // 這裡可以直接用 *
                ecpayConfig.setAllowedMethods(Arrays.asList("POST", "GET"));
                ecpayConfig.setAllowedHeaders(Arrays.asList("*"));
                ecpayConfig.setAllowCredentials(false); // 💡 這行是解決 400 的核心

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/payment/payment-success", ecpayConfig);
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
=======
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://*.ngrok-free.dev",
                "https://*.loca.lt"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
>>>>>>> 9e71593542689727d695d101922bab93898a3979
