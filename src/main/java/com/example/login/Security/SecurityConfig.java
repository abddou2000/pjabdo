package com.example.login.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegating;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setAllowCredentials(true);
        cfg.setExposedHeaders(Arrays.asList("Authorization"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                                .requestMatchers("/api/attestations/**").permitAll()

                                ///  ///////////////type de contrat
                                .requestMatchers("/api/types-contrats/**").permitAll()

//////////////////////////////Unite orga
                                .requestMatchers("/api/unites/**").permitAll()
/// ////////////////////////////////////// statut salarial
                                .requestMatchers("/api/statuts-salariaux/**").permitAll()

                                // 🔓 Lecture libre des catégories (GET)
                        /// // Solution temporaire uniquempent pour tester////
                        .requestMatchers("/api/categories-salariales/**").permitAll()

/// /////////.requestMatchers(HttpMethod.POST, "/api/categories-salariales/**").hasRole("CONFIGURATEUR")


                        // 🔐 Modification des catégories réservée aux CONFIGURATEUR
                        .requestMatchers(HttpMethod.POST, "/api/categories-salariales/**").hasRole("CONFIGURATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/categories-salariales/**").hasRole("CONFIGURATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories-salariales/**").hasRole("CONFIGURATEUR")

                        // 🔓 Accès libre aux sociétés
                        .requestMatchers("/api/societes/**").permitAll()

                        // 🔓 Routes publiques
                        .requestMatchers("/api/login", "/api/register", "/api/configurateurs/authenticate").permitAll()
                        .requestMatchers("/setup/**").permitAll()

                        // 🔐 Routes protégées
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/configurateurs/**").hasRole("ADMIN")

                        // 🔐 Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }
}
