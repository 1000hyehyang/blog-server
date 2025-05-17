package com.thousandhyehyang.blog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@OpenAPIDefinition(
        info = @Info(
                title = "천혜향의 블로그 API",
                description = "천혜향의 기술 블로그 백엔드 API 문서입니다.",
                version = "v1.0",
                contact = @Contact(name = "천혜향", email = "ducogus12@gmail.com")
        ),
        servers = @Server(url = "/api", description = "기본 API 서버")
)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v3/api-docs/**",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; " +
                                        "style-src 'self'; img-src 'self' data:; " +
                                        "frame-ancestors 'none'; frame-src 'none'; " +
                                        "connect-src 'self'; font-src 'self'"))
                );

        return http.build();
    }
}
