package ec.edu.fica.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controlador REST del microservicio SRI.
 *
 * Endpoints expuestos al API Gateway:
 *   GET /sri/verificar?ruc={ruc}   → verifica y retorna datos de persona natural
 */
@RestController
@RequestMapping("/sri")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SriController {

    private final SriService sriService;

    /**
     * Verifica que el RUC corresponda a un contribuyente activo y persona natural.
     * Retorna los datos completos del contribuyente.
     */
    @GetMapping("/verificar")
    public Mono<ResponseEntity<PersonaResponseDTO>> verificar(@RequestParam String ruc) {
        return sriService.verificarYObtenerPersona(ruc)
                .map(ResponseEntity::ok)
                .onErrorResume(ContribuyenteNoEncontradoException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(NoEsPersonaNaturalException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
