package com.in3rovert_so.securedoc.cache;

/*
We suing this class to do any kind of configuration that we want to do, or any typpe of key value
pair.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    @Bean(name = "userLoginCache")
    public CacheStore<String, Integer> userCache() {
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }

//    @Bean(name = "registrationLoginCache")
//    public CacheStore<Long, String> anotherCache() {
//        return new CacheStore<>(900, TimeUnit.SECONDS);
//    }
}
