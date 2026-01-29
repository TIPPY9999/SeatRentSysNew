package com.example.backend.config;

import com.example.backend.repository.member.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Autowired
        private ClientRegistrationRepository clientRegistrationRepository;

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // 1. CSRF 排除名單 (放行所有 API，避免組員 POST 被擋)
                                .csrf(csrf -> csrf.ignoringRequestMatchers(
                                                "/api/**",
                                                "/login/**",
                                                "/oauth2/**"))

                                // 2. 載入自定義的 CORS 設定
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // 3. 處理 iframe 同源問題
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                                // 4. 請求權限控管
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/favicon.ico", "/error", "/css/**", "/js/**",
                                                                "/images/**")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/**", "/login/**", "/oauth2/**", "/api/auth/**")
                                                .permitAll()
                                                .anyRequest().permitAll() // 開發期間放行所有請求
                                )

                                // 5. OAuth2 登入配置
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .authorizationRequestResolver(
                                                                                authorizationRequestResolver(
                                                                                                clientRegistrationRepository)))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .defaultSuccessUrl("http://localhost:5173/", true))

                                // 6. 登出配置
                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(200);
                                                })
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID"));

                return http.build();
        }

        /**
         * 強制 Google 顯示帳號選擇視窗
         */
        private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
                        ClientRegistrationRepository clientRegistrationRepository) {
                DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                                clientRegistrationRepository, "/oauth2/authorization");
                resolver.setAuthorizationRequestCustomizer(customizer -> customizer
                                .additionalParameters(params -> params.put("prompt", "select_account")));
                return resolver;
        }

        /**
         * 整合後的 CORS 設定
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                // A. 前端 Vue 使用 (需 credentials, 用 Pattern)
                CorsConfiguration frontendConfig = new CorsConfiguration();
                frontendConfig.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:5173",
                                "https://*.ngrok-free.dev",
                                "https://*.trycloudflare.com",
                                "https://*.loca.lt"));
                frontendConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                frontendConfig.setAllowedHeaders(Arrays.asList("*"));
                frontendConfig.setAllowCredentials(true);

                // B. 綠界回傳使用 (不允許 credentials, 用 *)
                CorsConfiguration ecpayConfig = new CorsConfiguration();
                ecpayConfig.setAllowedOrigins(Arrays.asList("*"));
                ecpayConfig.setAllowedMethods(Arrays.asList("POST", "GET"));
                ecpayConfig.setAllowedHeaders(Arrays.asList("*"));
                ecpayConfig.setAllowCredentials(false);

                // 註冊路徑：具體路徑優先權高
                source.registerCorsConfiguration("/api/payment/payment-success", ecpayConfig);
                source.registerCorsConfiguration("/**", frontendConfig);

                return source;
        }
}