package com.in3rovert_so.securedoc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainConfiguration {

    //Todo: 3. Filter the endpoint (Open up some endPoints)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/user/login").permitAll() //For every http request that matches a specific pattern permit them.
                                .anyRequest().authenticated()) //Any other user that does match "Authenticate them"
                .build();
    }
    //Overriding the Auth Provider
    //Todo: 2. Override the Auth Provider and the Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(daoAuthenticationProvider);
    }
    //Todo: 1. We need to tell spring these are our users
    //Overriding the User details Services
    @Bean
    public UserDetailsService userDetailsService() {
        //First User
        var daniel = User.withDefaultPasswordEncoder()
                .username("daniel")
                .password("letdanin")
                .roles("USER")
                .build();
        //Second User
        var james = User.withDefaultPasswordEncoder()
                .username("james")
                .password("letjamesin")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(List.of(daniel, james)); //Override the user in memory user details with our custom user.
    }
}
