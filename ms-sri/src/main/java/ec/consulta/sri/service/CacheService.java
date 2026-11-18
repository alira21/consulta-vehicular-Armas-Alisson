package ec.consulta.sri.service;

import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class CacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public CacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public <T> void put(String key, T valor, Duration ttl) {
        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(valor), ttl);
        } catch (Exception e) {
            System.out.println("No se pudo escribir en cache: " + e.getMessage());
        }
    }

    public <T> Optional<T> get(String key, Class<T> tipo) {
        try {
            String json = redis.opsForValue().get(key);
            return json == null ? Optional.empty() : Optional.of(mapper.readValue(json, tipo));
        } catch (Exception e) {
            System.out.println("No se pudo leer de cache: " + e.getMessage());
            return Optional.empty();
        }
    }
}