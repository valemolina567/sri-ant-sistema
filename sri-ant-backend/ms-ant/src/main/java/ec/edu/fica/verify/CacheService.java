package ec.edu.fica.verify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Servicio de caché que abstrae el acceso a Redis.
 *
 * Patrón Cache-Aside:
 *   - get(key)       → busca en Redis, retorna Optional vacío si no existe
 *   - set(key, val)  → guarda en Redis con TTL configurable
 *   - delete(key)    → elimina entrada (invalidación manual)
 *
 * Clave Redis: ant:{cedula}:{placa}
 * Ejemplo:     ant:1712345678:ABC1234
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, LicenciaDTO> redisTemplate;

    @Value("${ant.cache-ttl-hours:24}")
    private long cacheTtlHours;

    private static final String KEY_PREFIX = "ant:";

    /** Construye la clave Redis para una cédula y placa. */
    public String buildKey(String cedula, String placa) {
        return KEY_PREFIX + cedula + ":" + placa.toUpperCase();
    }

    /**
     * Busca datos de licencia en Redis.
     * @return Optional con los datos si existe en caché, vacío si no.
     */
    public Optional<LicenciaDTO> get(String cedula, String placa) {
        String key = buildKey(cedula, placa);
        try {
            LicenciaDTO cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("[CACHE HIT] key={}", key);
                return Optional.of(cached);
            }
            log.info("[CACHE MISS] key={}", key);
        } catch (Exception e) {
            // Si Redis no está disponible, no fallamos — continuamos a la ANT
            log.warn("[CACHE ERROR] No se pudo leer de Redis: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Guarda datos de licencia en Redis con TTL configurable.
     */
    public void set(String cedula, String placa, LicenciaDTO dto) {
        String key = buildKey(cedula, placa);
        try {
            redisTemplate.opsForValue().set(key, dto, Duration.ofHours(cacheTtlHours));
            log.info("[CACHE SET] key={} ttl={}h", key, cacheTtlHours);
        } catch (Exception e) {
            log.warn("[CACHE ERROR] No se pudo guardar en Redis: {}", e.getMessage());
        }
    }

    /** Elimina una entrada del caché (útil para invalidación). */
    public void delete(String cedula, String placa) {
        String key = buildKey(cedula, placa);
        redisTemplate.delete(key);
        log.info("[CACHE DELETE] key={}", key);
    }
}
