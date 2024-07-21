package com.in3rovert_so.securedoc.security;

import com.in3rovert_so.securedoc.handler.ApiAccessDeniedHandler;
import com.in3rovert_so.securedoc.handler.ApiAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static com.in3rovert_so.securedoc.constant.Constants.LOGIN_PATH;
import static com.in3rovert_so.securedoc.constant.Constants.PUBLIC_URLS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainConfiguration {

    private final ApiAccessDeniedHandler apiAccessDeniedHandler;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiHttpConfigurer httpConfigurer;

    //public static final String LOGIN_PATH = "/user/login";

    //Todo: 3. Filter the endpoint (Open up some endPoints)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(null))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(apiAccessDeniedHandler)
                                .authenticationEntryPoint(apiAuthenticationEntryPoint))
                .authorizeHttpRequests(request ->
                        //Define every URL to open
                        request.requestMatchers(PUBLIC_URLS).permitAll() //For every http request that matches a specific pattern permit them.
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/user/delete/**")
                                .hasAnyAuthority("user:delete")
                                .requestMatchers(HttpMethod.DELETE, "/document/delete/**")
                                .hasAnyAuthority("document:delete")
                                .anyRequest().authenticated()) //Any other user that does match "Authenticate them"
                .apply(httpConfigurer)
                return http.build();
    }


}
