package jcorechat.app_api.security;

import jcorechat.app_api.API;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String API_PREFIX = "/api/v"+ API.API_VERSION;

    private final String[] paths = {
            API_PREFIX+"/account",
            API_PREFIX+"/captcha"

    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.requestMatchers(paths).permitAll()
                                .anyRequest().denyAll())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.disable())
                .headers(h ->
                        h.referrerPolicy(referrerPolicy -> referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                                .contentSecurityPolicy(contentSecurityPolicy -> contentSecurityPolicy.policyDirectives("script-src 'self'")));
        return http.build();

    }
}
