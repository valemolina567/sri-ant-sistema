package ec.edu.fica.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controlador REST del microservicio ANT.
 *
 * GET /ant/puntos?cedula={cedula}&placa={placa}
 *   → Retorna los puntos de licencia aplicando Cache-Aside con Redis.
 *
 * DELETE /ant/cache?cedula={cedula}&placa={placa}
 *   → Invalida el caché para forzar una consulta fresca a la ANT.
 */
@RestController
@RequestMapping("/ant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AntController {

    private final LicenciaService licenciaService;
    private final CacheService cacheService;

    @GetMapping("/puntos")
    public Mono<ResponseEntity<LicenciaDTO>> consultarPuntos(
            @RequestParam String cedula,
            @RequestParam String placa) {

        return licenciaService.consultarPuntos(cedula, placa.toUpperCase())
                .map(ResponseEntity::ok)
                .onErrorResume(AntNoDisponibleException.class,
                        e -> Mono.just(ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .build()));
    }

    /** Invalida el caché de un conductor — útil para testing y administración. */
    @DeleteMapping("/cache")
    public ResponseEntity<Void> invalidarCache(
            @RequestParam String cedula,
            @RequestParam String placa) {
        cacheService.delete(cedula, placa.toUpperCase());
        return ResponseEntity.noContent().build();
    }
}

// ── Excepción ANT ──────────────────────────────────────────────────────────────
class AntNoDisponibleException extends RuntimeException {
    public AntNoDisponibleException(String message) { super(message); }
}
