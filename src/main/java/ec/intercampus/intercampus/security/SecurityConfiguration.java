package ec.intercampus.intercampus.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import java.util.ArrayList;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public InMemoryUserDetailsManager staffAccounts(PasswordEncoder encoder,
            @Value("${staff.admin-password:}") String adminPassword,
            @Value("${staff.driver-password:}") String driverPassword) {
        var accounts = new ArrayList<UserDetails>();
        // Sin contraseña configurada no se habilita la cuenta correspondiente.
        if (!adminPassword.isBlank())
            accounts.add(User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build());
        if (!driverPassword.isBlank())
            accounts.add(User.withUsername("conductor").password(encoder.encode(driverPassword)).roles("DRIVER").build());
        return new InMemoryUserDetailsManager(accounts);
    }

    @Bean
    public SecurityFilterChain staffPermissions(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(access -> access
                .requestMatchers("/login", "/access-denied", "/error", "/css/**", "/js/**", "/favicon.svg").permitAll()
                .requestMatchers("/arrivals", "/arrivals/**").hasRole("DRIVER")
                .requestMatchers("/h2-console", "/h2-console/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/fleet", "/fleet/**").hasRole("ADMIN")
                .anyRequest().permitAll())
            .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/staff", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(errors -> errors.accessDeniedPage("/access-denied"))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frames -> frames.sameOrigin()));
        // La protección CSRF permanece activa; Thymeleaf agrega el token a los formularios.
        return http.build();
    }
}
