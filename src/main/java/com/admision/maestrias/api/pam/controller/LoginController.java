package com.admision.maestrias.api.pam.controller;

import com.admision.maestrias.api.pam.models.requests.LoginRequest;
import com.admision.maestrias.api.pam.models.responses.LoginResponse;
import com.admision.maestrias.api.pam.service.implementations.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:4200")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    /**
     * Endpoint para el login de usuarios. Este método autentica las credenciales
     * y genera un token JWT con los roles del usuario.
     *
     * @param loginRequest Contiene el email y la contraseña del usuario.
     * @return Retorna un token JWT en caso de éxito.
     */
    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autenticamos las credenciales del usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // Generamos el token JWT si las credenciales son correctas
            String token = jwtService.create(authentication);

            // Retornamos el token al cliente
            LoginResponse response = new LoginResponse(token);
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // Si la autenticación falla, retornamos un 401 Unauthorized
            return ResponseEntity.status(401).body(new LoginResponse("Credenciales inválidas"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
