package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.DTO.LoginDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Log4j2
public class AccountController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextLogoutHandler logoutHandler;

    public AccountController(AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, SecurityContextLogoutHandler logoutHandler) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.logoutHandler = logoutHandler;
    }


    @PostMapping("/login")
    public ResponseEntity<?> performLogin(@RequestBody @Valid LoginDto loginRequest, Errors errors, HttpServletRequest request, HttpServletResponse response) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
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
            return ResponseEntity.ok(Map.of("message", "User login successful", "status", "success"));
        } catch (AuthenticationException e) {
            // Handle authentication failure
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication failed", "status", "error"));
        } catch (ValidationException e) {
            // Handle validation failure
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage(), "status", "error"));
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> performLogout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.logout();
            // Log successful logout
            log.info("User logged out successfully");
            return ResponseEntity.ok(Map.of("message", "User logout successful", "status", "success"));
        } catch (ServletException e) {
            // Log the exception and return an appropriate response
            log.error("Logout failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Logout failed", "status", "error"));
        }
    }



}
