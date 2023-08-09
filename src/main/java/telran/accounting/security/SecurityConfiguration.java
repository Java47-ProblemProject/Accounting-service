package telran.accounting.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic(Customizer.withDefaults());
        http.csrf().disable();
//        http.exceptionHandling(exceptionHandling ->
//                exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
//                })
//        );
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/user/registration")
                    .permitAll()
                .requestMatchers(HttpMethod.PUT,"/user/editname/{userId}")
                    .access( new WebExpressionAuthorizationManager("#userId == authentication.name"))
                .anyRequest().authenticated()
        );

        return http.build();
    }

}
