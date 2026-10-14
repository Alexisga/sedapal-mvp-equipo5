package com.utp.sedapal.config;

import com.utp.sedapal.models.RolUsuario;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Value("${app.require-https:false}")
    private boolean requireHttps;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/registro", "/consulta", "/css/**", "/js/**", "/webjars/**", "/error").permitAll()
                        .requestMatchers("/h2-console/**").denyAll()
                        .requestMatchers("/usuario/**").hasRole(RolUsuario.USUARIO.name())
                        .requestMatchers("/operador/**").hasRole(RolUsuario.OPERADOR.name())
                        .requestMatchers("/evaluador/**").hasRole(RolUsuario.EVALUADOR.name())
                        .requestMatchers("/supervisor/**").hasRole(RolUsuario.SUPERVISOR.name())
                        .requestMatchers("/tecnico/**").hasRole(RolUsuario.TECNICO.name())
                        .requestMatchers("/calidad/**").hasRole(RolUsuario.CALIDAD.name())
                        .requestMatchers("/admin/**").hasRole(RolUsuario.ADMINISTRADOR.name())
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("documento")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) ->
                                redirigirPorRol(response, authentication.getAuthorities().iterator().next().getAuthority()))
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, error) -> response.sendError(403)))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        if (requireHttps) http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        return http.build();
    }

    private void redirigirPorRol(jakarta.servlet.http.HttpServletResponse response, String authority)
            throws IOException {
        String ruta = switch (authority) {
            case "ROLE_USUARIO" -> "/usuario";
            case "ROLE_OPERADOR" -> "/operador";
            case "ROLE_EVALUADOR" -> "/evaluador";
            case "ROLE_SUPERVISOR" -> "/supervisor";
            case "ROLE_TECNICO" -> "/tecnico";
            case "ROLE_CALIDAD" -> "/calidad/reportes";
            case "ROLE_ADMINISTRADOR" -> "/admin";
            default -> "/login";
        };
        response.sendRedirect(ruta);
    }
}
