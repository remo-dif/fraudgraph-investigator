package com.fraudgraph.investigator.dataset;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
class PreviewSecurity {
    @Bean
    @Order(1)
    @org.springframework.context.annotation.Profile("demo")
    SecurityFilterChain datasetSecurity(HttpSecurity http,
                                        @Value("${fraudgraph.preview.token:}") String token) throws Exception {
        byte[] expected = token.getBytes(StandardCharsets.UTF_8);
        return http.securityMatcher("/api/datasets/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, failure) -> {
                    response.setHeader("WWW-Authenticate", "Bearer");
                    response.setStatus(401);
                }))
                .authorizeHttpRequests(routes -> routes
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/datasets/preview")
                        .hasAuthority("DATASET_PREVIEW")
                        .anyRequest().denyAll())
                .addFilterBefore((request, response, chain) -> {
                    var httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
                    String authorization = httpRequest.getHeader("Authorization");
                    if (expected.length >= 32 && authorization != null && authorization.startsWith("Bearer ")
                            && MessageDigest.isEqual(expected, authorization.substring(7).getBytes(StandardCharsets.UTF_8))) {
                        var context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                                "preview-operator", null, List.of(new SimpleGrantedAuthority("DATASET_PREVIEW"))));
                        SecurityContextHolder.setContext(context);
                    }
                    chain.doFilter(request, response);
                }, AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    @org.springframework.context.annotation.Profile("demo")
    SecurityFilterChain demoSecurity(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(routes -> routes.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    @org.springframework.context.annotation.Profile("import")
    SecurityFilterChain importSecurity(HttpSecurity http) throws Exception {
        return http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(routes -> routes.anyRequest().denyAll())
                .build();
    }
}
