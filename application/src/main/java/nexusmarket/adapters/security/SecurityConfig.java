package nexusmarket.adapters.security;

import java.util.List;
import nexusmarket.domain.ports.out.JwtServicePort;
import nexusmarket.domain.services.user.LoadAuthenticatedUserService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Web security configuration (SDD/Adapters/Rest-security-cors.md).
 *
 * <p>Stateless JWT authentication: no HTTP session, CSRF disabled, CORS from
 * {@code app.security.cors.allowed-origins}. Login, public registration and the public catalog are
 * open; logout requires any authenticated user; each role path requires its SystemRole. 401 and 403
 * raised by Spring Security are delegated to the MVC exception resolvers, so
 * {@code GlobalExceptionHandler} writes them with the standard error format.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtServicePort jwtServicePort,
                                                   LoadAuthenticatedUserService loadAuthenticatedUserService,
                                                   @Qualifier("handlerExceptionResolver")
                                                   HandlerExceptionResolver exceptionResolver) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        .requestMatchers("/api/v1/buyer/**").hasRole(SystemRole.BUYER.getCode())
                        .requestMatchers("/api/v1/seller/**").hasRole(SystemRole.SELLER.getCode())
                        .requestMatchers("/api/v1/logistics/**").hasRole(SystemRole.LOGISTICS_OPERATOR.getCode())
                        .requestMatchers("/api/v1/admin/**").hasRole(SystemRole.ADMINISTRATOR.getCode())
                        .requestMatchers("/api/v1/supervisor/**").hasRole(SystemRole.SUPERVISOR.getCode())
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, exception) ->
                                exceptionResolver.resolveException(request, response, null, exception))
                        .accessDeniedHandler((request, response, exception) ->
                                exceptionResolver.resolveException(request, response, null, exception)))
                .addFilterBefore(new JwtAuthenticationFilter(jwtServicePort, loadAuthenticatedUserService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Shared BCrypt encoder; used by BCryptPasswordServiceAdapter (PasswordServicePort). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.security.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
