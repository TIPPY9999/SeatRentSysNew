package com.example.backend.config;

import com.example.backend.repository.member.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // 1. 修正：將所有需要外部 POST 或跨域 POST 的路徑都排除在 CSRF 檢查外
                                .csrf(csrf -> csrf.ignoringRequestMatchers(
                                                "/api/payment/**",
                                                "/login/member", // 💡 解決 403 報錯的關鍵
                                                "/api/auth/**"))

                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/favicon.ico", "/error", "/css/**", "/js/**",
                                                                "/images/**")
                                                .permitAll()

                                                // 確保包含 payment-success 和 login 相關路徑
                                                .requestMatchers(
                                                                "/api/payment/**",
                                                                "/login/**",
                                                                "/oauth2/**",
                                                                "/api/auth/**")
                                                .permitAll()

                                                .anyRequest().permitAll()) // 開發環境先放行所有，確認功能

                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .authorizationRequestResolver(
                                                                                authorizationRequestResolver(
                                                                                                clientRegistrationRepository)))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .defaultSuccessUrl("http://localhost:5173/", true))

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
         * 自定義 OAuth2 請求解析器
         */
        private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
                        ClientRegistrationRepository clientRegistrationRepository) {
                DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                                clientRegistrationRepository, "/oauth2/authorization");
                resolver.setAuthorizationRequestCustomizer(
                                customizer -> customizer.additionalParameters(
                                                params -> params.put("prompt", "select_account")));
                return resolver;
        }

        /**
         * CORS 跨域設定
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // 💡 允許的名單：包含本地與 Localtunnel，並增加綠界官方網域(可選，增加穩定性)
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173",
                                "https://*.ngrok-free.dev", // 💡 增加 ngrok 萬用字元
                                "https://*.loca.lt"));

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}