package ec.edu.fica.verify;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente HTTP para el portal web de la ANT.
 *
 * La ANT expone un portal web con baja disponibilidad. Este cliente:
 *   1. Realiza una petición HTTP GET con timeout corto (3s por defecto).
 *   2. Parsea el HTML de respuesta con Jsoup para extraer los datos.
 *   3. Está protegido con @CircuitBreaker y @Retry de Resilience4j.
 *
 * URL: https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp
 *       ?ps_tipo_identificacion=CED&ps_identificacion={cedula}&ps_placa={placa}
 */
@Slf4j
@Component
public class AntClient {

    private final WebClient webClient;
    private final long timeoutSeconds;

    public AntClient(WebClient.Builder builder,
                     @Value("${ant.base-url}") String baseUrl,
                     @Value("${ant.timeout-seconds:3}") long timeoutSeconds) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Consulta el portal de la ANT y retorna los datos de licencia.
     * Protegido con Circuit Breaker "ant-cb" y Retry "ant-retry".
     *
     * @throws AntNoDisponibleException si la ANT no responde o hay error.
     */
    @CircuitBreaker(name = "ant-cb", fallbackMethod = "fallbackAnt")
    @Retry(name = "ant-retry")
    public Mono<LicenciaDTO> consultarPuntos(String cedula, String placa) {
        String url = String.format(
                "?ps_tipo_identificacion=CED&ps_identificacion=%s&ps_placa=%s", cedula, placa);

        log.info("[ANT] Consultando: cedula={} placa={}", cedula, placa);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .publishOn(Schedulers.boundedElastic())   // Jsoup es bloqueante
                .map(html -> parseHtml(html, cedula, placa))
                .doOnSuccess(d -> log.info("[ANT] OK – puntos={}", d.getPuntosActuales()))
                .doOnError(e -> log.error("[ANT] Error: {}", e.getMessage()));
    }

    /** Fallback invocado cuando el Circuit Breaker está abierto o se agotaron los reintentos. */
    public Mono<LicenciaDTO> fallbackAnt(String cedula, String placa, Throwable t) {
        log.warn("[ANT] Fallback activado (circuit breaker / timeout): {}", t.getMessage());
        return Mono.error(new AntNoDisponibleException(
                "ANT no disponible. Intente más tarde o revise el caché."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PARSING HTML con Jsoup
    // La ANT retorna una tabla HTML con las citaciones/infracciones.
    // Estructura esperada: <table id="tabla_citaciones"> o similar.
    // ─────────────────────────────────────────────────────────────────────────
    private LicenciaDTO parseHtml(String html, String cedula, String placa) {
        Document doc = Jsoup.parse(html);
        List<LicenciaDTO.InfraccionDTO> infracciones = new ArrayList<>();
        int puntosDescontados = 0;

        // Busca la tabla de citaciones — ajustar el selector si la ANT cambia su HTML
        Elements filas = doc.select("table tr:not(:first-child)");
        for (Element fila : filas) {
            Elements celdas = fila.select("td");
            if (celdas.size() >= 4) {
                try {
                    String fecha = celdas.get(0).text().trim();
                    String descripcion = celdas.get(1).text().trim();
                    String puntosStr = celdas.get(2).text().trim().replaceAll("[^0-9]", "");
                    String resolucion = celdas.size() > 3 ? celdas.get(3).text().trim() : "";

                    int puntos = puntosStr.isEmpty() ? 0 : Integer.parseInt(puntosStr);
                    puntosDescontados += puntos;

                    infracciones.add(LicenciaDTO.InfraccionDTO.builder()
                            .fecha(fecha)
                            .descripcion(descripcion)
                            .puntosDescontados(puntos)
                            .numeroResolucion(resolucion)
                            .build());
                } catch (NumberFormatException e) {
                    log.debug("Fila ignorada al parsear HTML ANT: {}", fila.text());
                }
            }
        }

        int puntosActuales = Math.max(0, 30 - puntosDescontados);

        return LicenciaDTO.builder()
                .cedula(cedula)
                .placa(placa.toUpperCase())
                .puntosActuales(puntosActuales)
                .puntosDescontados(puntosDescontados)
                .infracciones(infracciones)
                .fechaConsulta(LocalDateTime.now())
                .fuenteDatos("ANT")
                .build();
    }
}
