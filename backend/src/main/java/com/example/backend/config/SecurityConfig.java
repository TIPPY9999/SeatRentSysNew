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
                 * - /login/member：你們註解說是解 403 的關鍵，保留
                 * - /api/auth/**：前端跨域呼叫常見，保留
                 */
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/payment/**",
                        "/login/member",
                        "/api/auth/**"
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

        return resolver;
    }

    /**
     * ✅ 整合：CORS 允許 localhost + ngrok/loca.lt（merchantAndCoupon2）
     * ⚠️ 注意：allowedOrigins 不支援萬用字元時會無效（取決於 Spring 版本/設定）
     * 若之後 CORS 仍擋，再改 setAllowedOriginPatterns。 
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

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
