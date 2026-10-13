
package com.backend.spring_boot_redis_caching.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log =
            LoggerFactory.getLogger(RedisCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(
            RuntimeException exception, Cache cache, Object key) {

        log.warn(
            "Redis GET failed for key {} in cache {}. Falling back to the database.",
            key, cache.getName(), exception
        );
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception, Cache cache,
            Object key, Object value) {

        log.warn(
            "Redis PUT failed for key {} in cache {}. Returning the database result.",
            key, cache.getName(), exception
        );
    }

    @Override
    public void handleCacheEvictError(
            RuntimeException exception, Cache cache, Object key) {

        log.warn(
            "Redis EVICT failed for key {} in cache {}.",
            key, cache.getName(), exception
        );
    }

    @Override
    public void handleCacheClearError(
            RuntimeException exception, Cache cache) {

        log.warn(
            "Redis CLEAR failed for cache {}.",
            cache.getName(), exception
        );
    }
}
