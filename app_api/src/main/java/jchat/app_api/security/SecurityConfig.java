package jchat.app_api.security;

import jchat.app_api.API;
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
            API_PREFIX + "/account",
            API_PREFIX + "/captcha",
            API_PREFIX + "/friend",
            API_PREFIX + "/friend/chat",
            API_PREFIX + "/profile",
            API_PREFIX + "/profile/avatar",
            API_PREFIX + "/profile/banner",
            API_PREFIX + "/reaction",
            API_PREFIX + "/posts",
            API_PREFIX + "/posts/comment",
            API_PREFIX + "/update",
            API_PREFIX + "/notifications",
            API_PREFIX + "/shop",
            API_PREFIX + "/group",
            API_PREFIX + "/group/role",
            API_PREFIX + "/group/channel",
            API_PREFIX + "/group/category",
            API_PREFIX + "/group/member",
            API_PREFIX + "/group/chat"
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
