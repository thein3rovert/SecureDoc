package com.in3rovert_so.securedoc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainConfiguration {
    //We need to tell spring these are our users
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
