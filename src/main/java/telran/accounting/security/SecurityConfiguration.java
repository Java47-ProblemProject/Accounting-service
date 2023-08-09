package telran.accounting.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic(Customizer.withDefaults());
        http.csrf().disable();
        http.authorizeRequests(authorize -> authorize
                .requestMatchers("/user/registration")
                    .permitAll()

                                        //User section//
                .requestMatchers(HttpMethod.PUT, "/user/editname/{userId}")
                    .access("#userId == authentication.name")
                .requestMatchers(HttpMethod.PUT, "/user/editeducation/{userId}/**")
                    .access("#userId == authentication.name")
                .requestMatchers(HttpMethod.PUT, "/user/editscientificinterests/{userId}")
                    .access("#userId == authentication.name")
                .requestMatchers(HttpMethod.PUT, "/user/editlocation/{userId}")
                    .access("#userId == authentication.name")
                .requestMatchers(HttpMethod.PUT, "/user/editavatar/{userId}/**")
                    .access("#userId == authentication.name")
                .requestMatchers(HttpMethod.PUT, "/user/editpassword/{userId}")
                    .access("#userId == authentication.name")
                .requestMatchers(HttpMethod.DELETE, "/user/delete/{userId}")
                    .access("#userId == authentication.name")

                                    //Administrative section//
                .requestMatchers(HttpMethod.PUT, "/user/editrole/{user}/**")
                    .access("hasRole(T(telran.accounting.model.Roles).ADMINISTRATOR) and #userId == authentication.name")
                .requestMatchers(HttpMethod.DELETE, "/user/deleteuser/{user}/**")
                    .access("hasRole(T(telran.accounting.model.Roles).ADMINISTRATOR) and #userId == authentication.name")
                .requestMatchers(HttpMethod.DELETE, "/user/deleteavatar/{user}/**")
                    .access("hasRole(T(telran.accounting.model.Roles).ADMINISTRATOR) and #userId == authentication.name")
                .anyRequest()
                    .authenticated()
        );
        return http.build();
    }
}
