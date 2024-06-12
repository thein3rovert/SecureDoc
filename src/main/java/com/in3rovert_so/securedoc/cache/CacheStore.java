package com.in3rovert_so.securedoc.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CacheStore <K, V>{
    //Defining the cache
    private final Cache<K, V> cache;

    //Define a constructor for this cacheStore
    public CacheStore(int expiryDuration, TimeUnit timeUnit) {
        //Example: Expiry duration = 5, TimeUnit = Min -> 5 min
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration,timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public V get(@NotNull K key) {
        log.info("Retrieving from Cache with Key {} ", key.toString());
        return cache.getIfPresent(key);
    }

    public void put(@NotNull K key, @NotNull V value) {
        log.info("Storing record in the cache relating to the key {} ", key.toString());
        cache.put(key, value);
    }

    public void evict(@NotNull K key) {
        log.info("Removing from cache with key {} ", key.toString());
        cache.invalidate(key);
    }


}
