package io.github.ismaele77.LiveMinds.Security;

//import io.github.ismaele77.LiveMinds.Enum.UserRole;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;

//@Configuration
public class SecurityConfig {
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService(UserRepository userRepo) {
//        return username -> {
//            AppUser user = userRepo.findByUsername(username);
//            if (user != null) return user;
//
//            throw new UsernameNotFoundException("User '" + username + "' not found");
//        };
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(requests -> requests
//                .requestMatchers("/api/v1/login").permitAll()
//                .anyRequest().authenticated()
//        );
//
////        http.sessionManagement( session -> session
////                .maximumSessions(2)
////        );
//
//        return http.build();
//    }
}
