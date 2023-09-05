package telran.accounting.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/user/registration", "/user/resetpassword/*", "/user/geteducation")
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
}
