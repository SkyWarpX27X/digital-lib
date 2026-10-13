package ua.fictionallibrary.digital_lib.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ua.fictionallibrary.digital_lib.security.jwt.JwtAuthFilter;
import ua.fictionallibrary.digital_lib.security.jwt.JwtLoginFilter;
import ua.fictionallibrary.digital_lib.user.UserRole;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {
    private static final String READER = UserRole.READER.name();
    private static final String DIGITAL_ARCHIVE = UserRole.DIGITAL_ARCHIVE_DEPARTMENT.name();
    private static final String BOOK_DELIVERY = UserRole.BOOK_DELIVERY_DEPARTMENT.name();
    private static final String ADMIN = UserRole.ADMIN.name();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthFilter jwtAuthFilter,
                                                   JwtLoginFilter jwtLoginFilter,
                                                   AuthenticationEntryPoint authenticationEntryPoint) {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/users").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/{id}").authenticated()

                        .requestMatchers("/api/v1/users/{userId}/library-card").hasAnyRole(READER, ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/v1/bookmark").hasRole(READER)
                        .requestMatchers("/api/v1/bookmark/{userId}/{bookId}").hasAnyRole(READER, ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/v1/book-orders").hasRole(READER)
                        .requestMatchers(HttpMethod.GET,  "/api/v1/book-orders").hasAnyRole(BOOK_DELIVERY, ADMIN)
                        .requestMatchers(HttpMethod.GET,  "/api/v1/book-orders/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/book-orders/{id}").hasAnyRole(BOOK_DELIVERY, ADMIN)

                        .requestMatchers(HttpMethod.GET,    "/api/v1/physical-books/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/physical-books").hasAnyRole(BOOK_DELIVERY, ADMIN)
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/physical-books/{id}").hasAnyRole(BOOK_DELIVERY, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/physical-books/{id}").hasRole(ADMIN)

                        .requestMatchers(HttpMethod.GET,    "/api/v1/digitized-books/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/digitized-books").hasAnyRole(DIGITAL_ARCHIVE, BOOK_DELIVERY, ADMIN)
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/digitized-books/{id}").hasAnyRole(DIGITAL_ARCHIVE, BOOK_DELIVERY, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/digitized-books/{id}").hasRole(ADMIN)
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
