package com.example.backend.config;

import com.example.backend.repository.member.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                // 1. 基礎安全設定
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 權限控管
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/**", "/oauth2/**", "/api/auth/**").permitAll()
                        .anyRequest().permitAll())

                // 3. OAuth2 登入設定
                .oauth2Login(oauth2 -> oauth2
                        // 強制 Google 顯示帳號選擇視窗
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(clientRegistrationRepository)))
                        // 自定義註冊與存檔邏輯
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        // 登入成功後跳轉回 Vue
                        .defaultSuccessUrl("http://localhost:5173/", true))

                // 4. 登出設定
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200); // 回傳 OK 給前端 axios
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    /**
     * 自定義 OAuth2 請求解析器
     * 作用：在重新導向到 Google 之前，強制補上 prompt=select_account 參數
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(
                customizer -> customizer.additionalParameters(params -> params.put("prompt", "select_account")));

        return resolver;
    }

    /**
     * CORS 跨域設定
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允許前端地址
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 必須允許 Credentials 才能帶 Session Cookie (重要！)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}