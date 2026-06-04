package ec.edu.fica.verify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Cliente reactivo para los endpoints REST del SRI.
 *
 * Endpoints usados:
 *   - existePorNumeroRuc?numeroRuc={ruc}
 *   - obtenerPorNumerosRuc?&ruc={ruc}
 */
@Slf4j
@Component
public class SriClient {

    private final WebClient webClient;
    private final long timeoutSeconds;

    public SriClient(
            WebClient.Builder builder,
            @Value("${sri.base-url}") String baseUrl,
            @Value("${sri.timeout-seconds:10}") long timeoutSeconds) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    /** Verifica si un RUC existe en el padrón del SRI. */
    public Mono<Boolean> existeRuc(String ruc) {
        log.debug("Verificando existencia RUC: {}", ruc);
        return webClient.get()
                .uri("/existePorNumeroRuc?numeroRuc={ruc}", ruc)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(r -> log.info("RUC {} existe: {}", ruc, r))
                .doOnError(e -> log.error("Error consultando existencia RUC {}: {}", ruc, e.getMessage()));
    }

    /** Obtiene los datos del contribuyente por RUC. */
    public Mono<ContribuyenteDTO[]> obtenerPorRuc(String ruc) {
        log.debug("Obteniendo datos contribuyente RUC: {}", ruc);
        return webClient.get()
                .uri("/obtenerPorNumerosRuc?&ruc={ruc}", ruc)
                .retrieve()
                .bodyToMono(ContribuyenteDTO[].class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnError(e -> log.error("Error obteniendo datos RUC {}: {}", ruc, e.getMessage()));
    }
}
