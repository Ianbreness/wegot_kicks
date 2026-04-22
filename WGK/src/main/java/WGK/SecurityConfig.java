package WGK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request

                // ── Rutas de OAuth2 y recursos estáticos — siempre públicas ──
                .requestMatchers(
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/webjars/**",
                    "/img/**",
                    "/uploads/**",
                    "/css/**",
                    "/js/**"
                ).permitAll()

                // ── Panel admin: solo ADMIN o VENDEDOR ──
                .requestMatchers(
                    "/sneaker/listado",
                    "/sneaker/modificar/**",
                    "/sneaker/guardar",
                    "/sneaker/eliminar",
                    "/marca/listado",
                    "/marca/modificar/**",
                    "/marca/guardar",
                    "/marca/eliminar"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // ── Login y páginas públicas ──
                .requestMatchers(
                    "/",
                    "/index",
                    "/login",
                    "/acceso_denegado",
                    "/buscar",
                    "/marca/**",       // filtro por marca en catálogo público
                    "/sneaker/**",     // detalle de sneaker público
                    "/carrito/**",     // carrito público (el pago verifica login internamente)
                    "/fav/**",
                    "/registro/**"
                ).permitAll()

                // ── Cualquier otra URL requiere autenticación ──
                .anyRequest().authenticated()

        ).formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()

        ).oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )

        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()

        ).exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/acceso_denegado")
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123"))
                .roles("ADMIN")
                .build();
        UserDetails vendedor = User.builder()
                .username("vendedor")
                .password(passwordEncoder.encode("456"))
                .roles("VENDEDOR")
                .build();
        UserDetails cliente = User.builder()
                .username("cliente")
                .password(passwordEncoder.encode("789"))
                .roles("USUARIO")
                .build();
        return new InMemoryUserDetailsManager(admin, vendedor, cliente);
    }
}