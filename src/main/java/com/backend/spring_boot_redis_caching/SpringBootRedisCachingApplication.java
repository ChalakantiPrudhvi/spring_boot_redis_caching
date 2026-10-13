
package com.backend.spring_boot_redis_caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCaching
public class SpringBootRedisCachingApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                SpringBootRedisCachingApplication.class, args);
    }
}
