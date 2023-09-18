package telran.accounting.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //http.requiresChannel(chanel -> chanel.anyRequest().requiresSecure());
        http.httpBasic(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        http.cors(Customizer.withDefaults());
        http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/user/login","/user/registration", "/user/resetpassword/*", "/user/geteducation")
                .permitAll()
                //User section//
                .requestMatchers(HttpMethod.PUT, "/user/editname/{userId}")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .requestMatchers(HttpMethod.PUT, "/user/editeducation/{userId}/*")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .requestMatchers(HttpMethod.PUT, "/user/editscientificinterests/{userId}")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .requestMatchers(HttpMethod.PUT, "/user/editlocation/{userId}")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .requestMatchers(HttpMethod.PUT, "/user/editavatar/{userId}/*")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .requestMatchers(HttpMethod.PUT, "/user/editpassword/{userId}")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .requestMatchers(HttpMethod.DELETE, "/user/delete/{userId}")
                .access(new WebExpressionAuthorizationManager("#userId == authentication.name"))

                //Administrative section//
                .requestMatchers(HttpMethod.PUT, "/user/editrole/{userId}/*")
                .access(new WebExpressionAuthorizationManager("hasAuthority(T(telran.accounting.model.Roles).ADMINISTRATOR) and #userId == authentication.name"))
                .requestMatchers(HttpMethod.DELETE, "/user/deleteuser/{userId}/*")
                .access(new WebExpressionAuthorizationManager("hasAuthority(T(telran.accounting.model.Roles).ADMINISTRATOR) and #userId == authentication.name"))
                .requestMatchers(HttpMethod.DELETE, "/user/deleteavatar/{userId}/*")
                .access(new WebExpressionAuthorizationManager("hasAuthority(T(telran.accounting.model.Roles).ADMINISTRATOR) and #userId == authentication.name"))
                .anyRequest()
                .authenticated()
        );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5173/", "http://127.0.0.1:5173", "https://j-a-n.netlify.app/", "https://j-a-n.netlify.app"));
        configuration.setAllowedMethods(List.of("POST", "PUT", "GET", "OPTIONS", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
