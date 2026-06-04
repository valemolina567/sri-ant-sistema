package ec.edu.fica.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootApplication
public class MsAntApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsAntApplication.class, args);
    }
}

// ── Configuración Redis ────────────────────────────────────────────────────────
@Configuration
class RedisConfig {

    /**
     * RedisTemplate configurado para serializar claves como String y valores
     * como JSON (Jackson). Esto permite inspeccionar el caché directamente
     * en Redis con redis-cli.
     */
    @Bean
    public RedisTemplate<String, LicenciaDTO> redisTemplate(
            RedisConnectionFactory factory, ObjectMapper objectMapper) {

        RedisTemplate<String, LicenciaDTO> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(factory);
        tpl.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<LicenciaDTO> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, LicenciaDTO.class);

        tpl.setValueSerializer(jsonSerializer);
        tpl.setHashValueSerializer(jsonSerializer);
        return tpl;
    }
}
