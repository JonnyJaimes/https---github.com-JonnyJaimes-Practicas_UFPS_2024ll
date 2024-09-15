package com.admision.maestrias.api.pam.controller;

import com.admision.maestrias.api.pam.models.responses.AnyResponse;
import com.admision.maestrias.api.pam.service.implementations.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private JWTService jwtService;

    @Secured({ "ROLE_ADMIN", "ROLE_ENCARGADO", "ROLE_USUARIO" })
    @GetMapping("/home")
    public ResponseEntity<AnyResponse> getDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();

        String role = jwtService.getRoleFromToken(token); // Obtener el rol desde el token JWT

        AnyResponse response = new AnyResponse();
        switch (role) {
            case "ADMIN":
                response.setMessage("Redirect to Admin Dashboard");
                break;
            case "ENCARGADO":
                response.setMessage("Redirect to Encargado Dashboard");
                break;
            case "USUARIO":
                response.setMessage("Redirect to Usuario Dashboard");
                break;
            default:
                response.setMessage("Role not found");
        }
        return ResponseEntity.ok(response);
    }
}
