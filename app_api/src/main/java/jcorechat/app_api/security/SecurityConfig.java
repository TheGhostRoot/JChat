package jcorechat.app_api.security;

import jcorechat.app_api.API;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String API_PREFIX = "/api/v"+ API.API_VERSION;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.requestMatchers(API_PREFIX+"/account")
                                .permitAll()
                                .requestMatchers(API_PREFIX+"/captcha")
                                .permitAll()
                                .requestMatchers(API_PREFIX+"/captcha/solve")
                                .permitAll()

        ).addFilter(new RequestFilter());

        return http.build();

    }
}
