package io.github.ismaele77.liveminds.controller;

import io.github.ismaele77.liveminds.dto.LoginDto;
import io.github.ismaele77.liveminds.dto.UserDto;
import io.github.ismaele77.liveminds.model.AppUser;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;
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
    public ResponseEntity<EntityModel<UserDto>> performLogin(@RequestBody @Valid LoginDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        log.info("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
        Authentication authenticationResponse =
                this.authenticationManager.authenticate(authenticationRequest);
        // Set authentication in security context
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authenticationResponse);

        // Save security context
        securityContextRepository.saveContext(context, request, response);

        //get user info
        AppUser appUser = (AppUser) authenticationResponse.getPrincipal();
        var user = new UserDto();
        user.mapFromAppUser(appUser);

        // Return a more structured response
        log.info("User login successful for user: {}", loginRequest.getUsernameOrEmail());
        return ResponseEntity.ok(EntityModel.of(user));

    }

    @PostMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"string\" ,\"status\": \"string\"}"))})})
    public ResponseEntity<?> performLogout(HttpServletRequest request, HttpServletResponse response) {

        // Get the current security context
        SecurityContext context = SecurityContextHolder.getContext();

        // Create a logout handler
        SecurityContextLogoutHandler securityContextLogoutHandler =
                new SecurityContextLogoutHandler();

        // Perform logout
        securityContextLogoutHandler.logout(request, response, context.getAuthentication());

        // Invalidate the session if necessary
        request.getSession().invalidate();

        log.info("User logout successful");

        return ResponseEntity.ok(Map.of("message", "User logout successful", "status", "success"));

    }
}
