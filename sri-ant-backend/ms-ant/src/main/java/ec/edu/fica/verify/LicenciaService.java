package ec.edu.fica.verify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio principal del microservicio ANT.
 *
 * Implementa el patrón CACHE-ASIDE:
 *
 *   ┌─────────────────────────────────────────────────────────┐
 *   │  consultarPuntos(cedula, placa)                         │
 *   │                                                         │
 *   │  1. CacheService.get(cedula, placa)                    │
 *   │     ├── HIT  → retorna datos con fuenteDatos="CACHE"   │
 *   │     └── MISS →                                          │
 *   │         2. AntClient.consultarPuntos(cedula, placa)    │
 *   │            ├── OK  → CacheService.set(datos)           │
 *   │            │          retorna datos con fuente="ANT"    │
 *   │            └── ERR → AntNoDisponibleException          │
 *   └─────────────────────────────────────────────────────────┘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenciaService {

    private final CacheService cacheService;
    private final AntClient antClient;

    public Mono<LicenciaDTO> consultarPuntos(String cedula, String placa) {

        // PASO 1 — Buscar en Redis (operación bloqueante → publishOn boundedElastic)
        return Mono.fromCallable(() -> cacheService.get(cedula, placa))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap((Optional<LicenciaDTO> cached) -> {

                    if (cached.isPresent()) {
                        // ── CACHE HIT ──────────────────────────────────────
                        LicenciaDTO dto = cached.get();
                        dto.setFuenteDatos("CACHE");
                        dto.setFechaConsulta(LocalDateTime.now());
                        log.info("[LICENCIA] Servido desde caché Redis – cedula={} placa={}", cedula, placa);
                        return Mono.just(dto);
                    }

                    // ── CACHE MISS → consultar ANT ─────────────────────────
                    log.info("[LICENCIA] Cache miss – consultando ANT cedula={} placa={}", cedula, placa);
                    return antClient.consultarPuntos(cedula, placa)
                            .flatMap(dto -> {
                                // PASO 3 — Guardar en Redis
                                return Mono.fromRunnable(() -> cacheService.set(cedula, placa, dto))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .thenReturn(dto);
                            })
                            .doOnSuccess(dto ->
                                log.info("[LICENCIA] ANT OK, guardado en Redis – puntos={}", dto.getPuntosActuales())
                            )
                            .onErrorResume(AntNoDisponibleException.class, e -> {
                                log.warn("[LICENCIA] ANT no disponible y sin caché – {}", e.getMessage());
                                return Mono.error(e);
                            });
                });
    }
}
