package com.admision.maestrias.api.pam.security;

import com.admision.maestrias.api.pam.models.requests.UserDetailsRequest;
import com.admision.maestrias.api.pam.service.implementations.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Autenticación de usuarios mediante email y contraseña.
 * Genera un token JWT en caso de autenticación exitosa.
 *
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    public AuthenticationFilter(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // Lee las credenciales del usuario (email y contraseña) del cuerpo de la petición
            UserDetailsRequest userModel = new ObjectMapper().readValue(request.getInputStream(), UserDetailsRequest.class);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userModel.getEmail(), userModel.getPassword());

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("Error en la autenticación: " + e.getMessage());
        }
    }

    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                         Authentication authentication) throws IOException, ServletException {

        String username = ((User) authentication.getPrincipal()).getUsername();
        String token = jwtService.create(authentication);

        Map<String, Object> body = new HashMap<>();
        body.put("mensaje", String.format("Hola %s, has iniciado sesión con éxito!", username));
        body.put("token", SecurityConstants.TOKEN_PREFIX + token);

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.addHeader("Access-Control-Expose-Headers", "Authorization");
        response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);
        response.setContentType("application/json");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        Map<String, Object> body = new HashMap<>();
        if (failed instanceof BadCredentialsException)
            body.put("message", "Correo o contraseña incorrectos");
        else
            body.put("message", failed.getMessage());

        body.put("error", "Error de autenticación");

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
    }
}
