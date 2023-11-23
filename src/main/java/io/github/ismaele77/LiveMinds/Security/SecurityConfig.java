package io.github.ismaele77.LiveMinds.Security;

import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;


import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepo) {
        return username -> {
            return userRepo.findByUserName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username " + username + " not found"));
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityContextLogoutHandler securityContextLogoutHandler(){
        return new SecurityContextLogoutHandler();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http , SecurityContextRepository repo) throws Exception {
        http
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/api/v1/login").permitAll()
                .requestMatchers("/api/v1/logout").permitAll()
                .anyRequest().authenticated()
            )
            .securityContext((context) -> context
                    .securityContextRepository(repo)
            )
            .csrf(httpSecurityCsrfConfigurer -> {
                httpSecurityCsrfConfigurer.disable();
            })

            .cors(Customizer.withDefaults());

//        http.sessionManagement( session -> session
//                .maximumSessions(2)
//        );

        return http.build();
    }
}
