package br.lanx.api.shared.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/auth/login",
                        "/actuator/health",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
        };

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthoritiesConverter jwtConverter)
                        throws Exception {
                var problems = new SecurityProblemHandler();

                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .cors(Customizer.withDefaults())
                                .headers(h -> h
                                                .contentTypeOptions(Customizer.withDefaults())
                                                .frameOptions(f -> f.deny())
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true).maxAgeInSeconds(31_536_000))
                                                .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER))
                                                .contentSecurityPolicy(csp -> csp.policyDirectives(
                                                                "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                                                                                + "object-src 'none'; frame-ancestors 'none'")))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth -> oauth
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                                                .authenticationEntryPoint(problems)
                                                .accessDeniedHandler(problems))
                                .exceptionHandling(e -> e
                                                .authenticationEntryPoint(problems)
                                                .accessDeniedHandler(problems));
                return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
                var config = new CorsConfiguration();
                config.setAllowedOrigins(props.allowedOrigins());
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(
                                List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT));
                config.setExposedHeaders(List.of(HttpHeaders.LOCATION));
                config.setAllowCredentials(false);
                config.setMaxAge(3600L);

                var source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

}
