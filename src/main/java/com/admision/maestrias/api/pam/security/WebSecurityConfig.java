package com.admision.maestrias.api.pam.security;

import com.admision.maestrias.api.pam.service.implementations.JWTService;
import com.admision.maestrias.api.pam.service.interfaces.UserServiceInterface;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserServiceInterface userService;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public WebSecurityConfig(UserServiceInterface userService, JWTService jwtService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Deshabilitamos CSRF ya que estamos usando JWT
                .cors(cors -> cors.configure(http))  // Configuración de CORS directamente en la cadena de seguridad
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/register","/users", "/public/**", "/status/ping","/tiposDoc").permitAll()
                        .requestMatchers("/aspirante/**", "/dashboard/home","/documentos","/notificacion").hasAnyRole("USUARIO", "ADMIN", "ENCARGADO")
                        .requestMatchers("/cohorte/**", "/doc/**").hasAnyRole("ADMIN", "ENCARGADO")
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Sin estado para JWT
                .addFilter(authenticationFilter(authenticationConfiguration))  // Añadimos los filtros de autenticación y autorización
                .addFilter(new AuthorizationFilter(authenticationManager(authenticationConfiguration), jwtService));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    public AuthenticationFilter authenticationFilter(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager(authenticationConfiguration), jwtService);
        filter.setFilterProcessesUrl("/login");  // Establecemos la URL de procesamiento del login
        return filter;
    }

    // Definimos el CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")  // Permitir solicitudes desde el frontend Angular
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos permitidos
                        .allowedHeaders("*")  // Permitimos todos los encabezados
                        .allowCredentials(true);  // Permitir el uso de cookies y autenticación basada en JWT
            }
        };
    }
}
