package uz.pdp.smartinventory.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                 // 1. REST API uchun CSRF himoyasini o'chiramiz (chunki seans ishlatmaymiz)
                .csrf(csrf -> csrf.disable())

                // 2. Serverda Session saqlamaslikni buyuramiz (STATELESS rejim)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. URL manzillar xavfsizligini sozlaymiz
                .authorizeHttpRequests(auth -> auth

                        // 🔓 Ochiq yo'llar (Login, Register, Statik rasmlar/fayllar va Swagger)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll() // Rasmlar frontendda ko'rinishi uchun
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 🔐 Rollar va huquqlarga asoslangan himoya (Controllerda @PreAuthorize ishlatsangiz ham bo'ladi)
                        .requestMatchers("/api/v1/dashboard/**").hasRole("ADMIN")

                        // 🔒 Qolgan barcha API so'rovlar uchun Token majburiy
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

}