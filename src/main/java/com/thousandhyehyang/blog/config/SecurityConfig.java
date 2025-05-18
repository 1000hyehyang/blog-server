package com.thousandhyehyang.blog.config;

import com.thousandhyehyang.blog.security.CustomOAuth2UserService;
import com.thousandhyehyang.blog.security.JwtAuthenticationFilter;
import com.thousandhyehyang.blog.security.OAuth2AuthenticationSuccessHandler;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "천혜향의 블로그 API",
                description = "천혜향의 기술 블로그 백엔드 API 문서입니다.",
                version = "v1.0",
                contact = @Contact(name = "천혜향", email = "ducogus12@gmail.com")
        ),
        servers = @Server(url = "/api", description = "기본 API 서버")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger 문서 공개
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // OAuth2 인증 흐름 관련 엔드포인트 공개
                        .requestMatchers("/oauth2/**", "/auth/refresh-token").permitAll()

                        // 헬스 체크 엔드포인트 공개
                        .requestMatchers("/health/**").permitAll()

                        // 게시글 조회는 전체 공개
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()

                        // 댓글 작성은 익명 사용자도 가능
                        .requestMatchers(HttpMethod.POST, "/posts/*/comments").permitAll()

                        // 게시글 작성/수정/삭제는 ADMIN만 가능
                        .requestMatchers(HttpMethod.POST, "/posts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/posts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")

                        // 계정 정보 등 기타 요청은 인증 필요
                        .requestMatchers("/api/account/**").authenticated()

                        // 그 외 모든 요청도 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .authorizationEndpoint(authorization -> authorization.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection.baseUri("/oauth2/callback/*"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self'; object-src 'none'; style-src 'self'; img-src 'self' data:; " +
                                        "frame-ancestors 'none'; frame-src 'none'; connect-src 'self'; font-src 'self'"
                        ))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://1000hyehyang.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
