package io.github.ismaele77.liveminds.controller;

import io.github.ismaele77.liveminds.dto.LoginDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextLogoutHandler logoutHandler;

    @PostMapping("/login")
    public ResponseEntity<?> performLogin(@RequestBody @Valid LoginDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());
        try {
            // Perform authentication
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);

            // Set authentication in security context
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);

            // Save security context
            securityContextRepository.saveContext(context, request, response);

            // Return a more structured response
            log.info("User login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(Map.of("message", "User login successful", "status", "success"));
        } catch (AuthenticationException e) {
            // Handle authentication failure
            log.warn("Authentication failed during login for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed", "status", "error"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> performLogout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Attempting logout.");
        try {
            request.logout();
            // Log successful logout
            log.info("User logout successful");
            return ResponseEntity.ok(Map.of("message", "User logout successful", "status", "success"));
        } catch (ServletException e) {
            // Log the exception and return an appropriate response
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed", "status", "error"));
        }
    }
}
