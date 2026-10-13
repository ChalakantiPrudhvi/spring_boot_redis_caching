# Spring Boot Redis Caching

A backend application demonstrating Redis caching with Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs. This project implements CRUD operations, cache-aside using `@Cacheable`, `@CachePut`, and `@CacheEvict`, JSON serialization, TTL-based expiration, and graceful Redis cache error handling.

## 🚀 Features

- Product CRUD operations using REST APIs
- PostgreSQL database integration using Spring Data JPA
- Redis caching to reduce repeated database queries
- Cache management using Spring caching annotations
- JSON serialization and deserialization of cached objects
- Configurable cache expiration using TTL
- Custom Redis cache error handling
- Environment-variable-based configuration
- Database fallback when Redis cache operations fail

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming language |
| Spring Boot | Application framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access and ORM |
| PostgreSQL | Persistent database |
| Supabase | Managed PostgreSQL hosting |
| Spring Data Redis | Redis integration |
| Redis | In-memory caching |
| Maven | Build and dependency management |

## 🏗️ Architecture

```text
Client / Postman
       |
       v
ProductController
       |
       v
ProductService
       |
       +-----------> Redis Cache
       |
       +-----------> ProductRepository
                           |
                           v
                       PostgreSQL
```

### Component Responsibilities

- **ProductController:** Handles HTTP requests and responses.
- **ProductService:** Contains application logic and cache annotations.
- **ProductRepository:** Performs database operations using Spring Data JPA.
- **PostgreSQL:** Stores permanent product records.
- **Redis:** Stores temporary copies of frequently accessed data.
- **RedisCacheConfig:** Configures TTL, key serialization, value serialization, and cache behaviour.
- **RedisCacheErrorHandler:** Handles Redis cache errors to help database-backed requests continue during cache failures.

## 📚 Core Concepts Explained

### 1. What Is Redis Caching?

Caching stores frequently accessed data in a faster storage system so the application does not need to repeatedly retrieve the same information from the database.

In this project, PostgreSQL stores the permanent product data, while Redis stores temporary cached copies.

**Benefits:**
- Reduces repeated database queries.
- Improves response time for repeated reads.
- Reduces database workload.

### 2. Cache-Aside Pattern

The application follows a cache-aside-style approach:

1. Check Redis for the requested product.
2. If the product exists in Redis, return the cached value.
3. If it does not exist, retrieve the product from PostgreSQL.
4. Store the retrieved product in Redis.
5. Return the product to the client.

This makes repeated reads more efficient.

### 3. `@Cacheable`

`@Cacheable` checks the cache before executing the method.

Example:

```java
@Cacheable(value = "products", key = "#id")
public Product getProductById(Long id) {
    System.out.println("Fetching product from PostgreSQL: " + id);

    return productRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Product not found"));
}
```

**Explanation:**
- `value = "products"` specifies the cache name.
- `key = "#id"` uses the product ID as the cache key.
- On a cache hit, Spring returns the cached result without normally executing the method.
- On a cache miss, the method executes and its successful result is cached.

### 4. `@CachePut`

`@CachePut` always executes the method and updates the cache with the returned result.

Example:

```java
@CachePut(value = "products", key = "#id")
```

When a product is updated, the returned updated product is placed in the matching cache entry.

This helps ensure that subsequent reads return the latest cached product, provided the database update and cache operation succeed.

### 5. `@CacheEvict`

`@CacheEvict` removes an entry from the cache.

Example:

```java
@CacheEvict(value = "products", key = "#id")
```

When a product is deleted, its corresponding cache entry is removed so that the old cached product is not returned.

Cache invalidation is important because stale data can otherwise remain available after database changes.

### 6. TTL (Time To Live)

TTL specifies how long a cache entry remains valid before expiring.

Example:

```java
.entryTtl(Duration.ofMinutes(10))
```

This configures a default cache entry lifetime of 10 minutes.

After expiration, a subsequent request normally loads the latest data from PostgreSQL and caches it again.

TTL limits how long stale data may remain in the cache, but it does not replace correct cache updates and evictions.

### 7. Serialization and Deserialization

Redis stores data as bytes rather than live Java objects.

**Serialization** converts a Java object into a storable representation, such as JSON.

**Deserialization** converts that stored representation back into a Java object.

For example, a `Product` object containing `id`, `name`, `description`, and `price` must be serialized before it can be stored as a cache value.

This project uses `GenericJacksonJsonRedisSerializer` to serialize and deserialize cached values.

Type information may be required to reconstruct the correct Java class. The project's type validator restricts allowed types to the application package and `java.math.BigDecimal`.

**Key serializer:** `StringRedisSerializer` stores cache keys as strings.

**Value serializer:** `GenericJacksonJsonRedisSerializer` stores cached objects using JSON serialization.

Restrict deserialization to trusted, expected types rather than enabling unrestricted polymorphic deserialization.

### 8. Redis Cache Configuration

`RedisCacheConfig` centralizes cache settings, including:

- Cache entry TTL of 10 minutes.
- Disabling caching of null values.
- String serialization for cache keys.
- JSON serialization for cached values.
- An allowlist for deserialization types.
- Registration of a custom cache error handler.

Centralized configuration makes cache behaviour consistent across service methods.

### 9. Graceful Redis Failure Handling

Redis is used as a performance optimization, not as the permanent data store.

The custom `RedisCacheErrorHandler` implements Spring's `CacheErrorHandler` and handles cache get, put, evict, and clear errors.

In this project, cache errors are logged without being rethrown. This helps the application continue serving database-backed requests when Redis is unavailable.

**Important limitation:** if a cache update or eviction fails, an old entry may remain in Redis. TTL limits its lifetime, but production systems may also need retries, monitoring, and additional cache invalidation strategies.

### 10. PostgreSQL and Spring Data JPA

PostgreSQL is the persistent relational database.

Spring Data JPA simplifies database access through repository interfaces and entity mapping.

The `Product` entity represents a product record, while `ProductRepository` provides persistence operations such as saving, finding, and deleting products.

PostgreSQL remains the source of truth; Redis contains temporary cached copies.

## 📡 REST API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/products` | Create a product |
| GET | `/api/products` | Retrieve all products |
| GET | `/api/products/{id}` | Retrieve a product by ID |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

### Example JSON Request

```json
{
  "name": "Mechanical Keyboard",
  "description": "Compact mechanical keyboard",
  "price": 2499.00
}
```

### Example Requests Using cURL

Create a product:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Mechanical Keyboard","description":"Compact keyboard","price":2499.00}'
```

Retrieve all products:

```bash
curl http://localhost:8080/api/products
```

Retrieve one product:

```bash
curl http://localhost:8080/api/products/1
```

Update a product:

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Mechanical Keyboard Pro","description":"Updated keyboard","price":2999.00}'
```

Delete a product:

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

Replace the example product ID with an ID that exists in your database.

## 📂 Project Structure

```text
spring-boot-redis-caching/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/backend/spring_boot_redis_caching/
│   │   │       ├── config/
│   │   │       │   └── RedisCacheConfig.java
│   │   │       ├── entity/
│   │   │       │   └── Product.java
│   │   │       ├── repository/
│   │   │       │   └── ProductRepository.java
│   │   │       ├── service/
│   │   │       │   └── ProductService.java
│   │   │       ├── controller/
│   │   │       │   └── ProductController.java
│   │   │       └── SpringBootRedisCachingApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

The actual package paths may differ slightly depending on the project structure.

## ⚙️ Prerequisites

- Java Development Kit compatible with the project's `pom.xml`
- Maven or the included Maven Wrapper
- PostgreSQL database or a Supabase PostgreSQL instance
- Accessible Redis instance
- Postman or cURL for API testing

Docker is not required when using managed cloud database and Redis services.

## 🔐 Configuration

Keep database and Redis credentials in environment variables. Never commit real credentials to GitHub.

Example configuration using placeholders:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.username=${REDIS_USERNAME:}
spring.data.redis.password=${REDIS_PASSWORD}

spring.data.redis.connect-timeout=2s
spring.data.redis.timeout=2s
```

Adapt these properties to your actual Redis provider and existing configuration. Some providers require TLS or a Redis connection URI.

Example environment variables on macOS or Linux:

```bash
export DB_URL='YOUR_POSTGRES_JDBC_URL'
export DB_USERNAME='YOUR_DATABASE_USERNAME'
export DB_PASSWORD='YOUR_DATABASE_PASSWORD'

export REDIS_HOST='YOUR_REDIS_HOST'
export REDIS_PORT='YOUR_REDIS_PORT'
export REDIS_USERNAME='YOUR_REDIS_USERNAME'
export REDIS_PASSWORD='YOUR_REDIS_PASSWORD'
```

Use your provider's real connection details locally. Do not put actual credentials in this README or commit them to version control.

## ▶️ Run the Application

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/spring-boot-redis-caching.git
cd spring-boot-redis-caching
```

Build the project:

```bash
./mvnw clean package
```

Start the application:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

The API should be available at:

```text
http://localhost:8080
```

Ensure that PostgreSQL and Redis configuration is correct before starting the application.

## 🧪 Testing

### Test CRUD Operations

1. Create a product.
2. Retrieve all products.
3. Retrieve a product by ID.
4. Update the product.
5. Retrieve it again to verify the updated values.
6. Delete the product.
7. Attempt to retrieve it again and verify that it is no longer available.

### Test Cache Hits

1. Call `GET /api/products/{id}` for an existing product.
2. Check the application logs for the database-fetch message.
3. Call the same endpoint again before the cache expires.
4. Verify that the database-fetch message does not appear on the second request.

This indicates that the cached value was used, assuming Redis is healthy and the cache entry was not evicted.

### Test Cache Invalidation

1. Retrieve a product to populate the cache.
2. Update the product.
3. Retrieve it again and verify that the updated value is returned.
4. Populate the cache for another product.
5. Delete that product.
6. Retrieve it again and verify that the old cached product is not returned.

### Test Redis Failure Handling

1. Make Redis unavailable in a controlled development environment.
2. Request a product that exists in PostgreSQL.
3. Verify that cache errors are logged and the database-backed request can continue.
4. Restore Redis and check that updates and deletions have not left stale entries.

## 📈 Expected Cache Behaviour

| Scenario | Expected result |
|---|---|
| First product read | Cache miss; product is loaded from PostgreSQL |
| Repeated product read | Cache hit; database lookup is normally skipped |
| Product update | Database is updated and matching cache entry is refreshed |
| Product deletion | Database record is deleted and matching cache entry is evicted |
| TTL expires | The next read normally reloads the product from PostgreSQL |
| Redis becomes unavailable | Cache errors are logged; database fallback can continue |
| Cache eviction fails | Stale data may remain until expiration or another invalidation |

## 🛠️ Troubleshooting

### Redis Connection Failure

- Verify the Redis host and port.
- Check username and password.
- Confirm whether TLS is required.
- Verify network access and provider allowlists.
- Ensure environment variables are available to the Spring Boot process.

### Cached Values Fail to Deserialize

- Check the configured key and value serializers.
- Confirm the expected Java type is allowed by the type validator.
- Check compatibility between the Jackson and Spring Data Redis versions.
- Remove incompatible development cache entries and test with fresh values.

Do not broadly enable unrestricted deserialization to bypass type errors.

### Every Request Queries PostgreSQL

- Confirm caching is enabled using `@EnableCaching`.
- Check that `@Cacheable` is applied to the correct service method.
- Verify the cache name and key.
- Confirm Redis is connected and cache operations are succeeding.
- Remember that proxy-based caching may not apply when an annotated method is called from another method in the same class.

### Product Endpoint Returns 404

- Verify the endpoint path.
- Check whether the product ID exists.
- Review controller mappings and application logs.
- Confirm the request reaches the correct application.

## 🔒 Security and Production Considerations

- Never commit passwords, `.env` files, tokens, or other secrets.
- Restrict access to Redis and use TLS where appropriate.
- Keep deserialization type restrictions narrow.
- Use structured logging, monitoring, and alerts.
- Consider retries or other recovery strategies for failed cache writes and evictions.
- Ensure database updates succeed before considering the cache updated.
- Plan cache invalidation when data changes outside the application.
- Test cache consistency and failure scenarios before production deployment.

## 🎯 Interview Questions

**1. What is Redis?**  
Redis is an in-memory data store commonly used for caching, among other use cases.

**2. Why use Redis with PostgreSQL?**  
PostgreSQL provides durable relational storage, while Redis can speed up repeated reads and reduce database load.

**3. What is a cache miss?**  
A cache miss occurs when the requested value is not present in the cache.

**4. What does `@Cacheable` do?**  
It checks the cache before executing the annotated method and stores the successful result after a cache miss.

**5. What is the difference between `@Cacheable` and `@CachePut`?**  
`@Cacheable` can skip the method on a cache hit. `@CachePut` always executes the method and updates the cache with its result.

**6. What is `@CacheEvict`?**  
It removes an entry from a cache, helping prevent old values from being returned.

**7. What is TTL?**  
Time to live defines how long a cached entry remains valid before it expires.

**8. What is serialization?**  
Serialization converts an object into a representation suitable for storage or transmission.

**9. Why is cache invalidation important?**  
Without proper invalidation, the cache may return outdated data after database updates or deletions.

**10. What happens when Redis is unavailable?**  
The result depends on the error-handling strategy. This project logs cache failures without rethrowing them so database-backed operations can continue where possible.

**11. Does TTL eliminate every stale-data problem?**  
No. TTL limits an entry's lifetime but does not guarantee immediate consistency after an update or delete.

**12. What is the source of truth in this project?**  
PostgreSQL is the source of truth. Redis stores temporary cached values.

## 🚀 Future Improvements

- Add pagination, sorting, and filtering.
- Add DTOs and request validation.
- Add centralized exception handling.
- Write unit and integration tests using JUnit and Mockito.
- Add cache metrics and monitoring.
- Improve cache consistency after transaction commits.
- Add CI/CD workflows.
- Add deployment and performance-testing documentation.

## 📌 Learning Outcomes

Through this project, I learned how to integrate Redis caching into a Spring Boot application, use Spring cache annotations, configure TTL and serializers, persist data using Spring Data JPA and PostgreSQL, and handle cache failures without making Redis the application's source of truth.

The project demonstrates that effective caching requires more than improving speed: it also requires careful handling of serialization, expiration, invalidation, consistency, failure scenarios, and security.

---

**Author:** Prudhvi
