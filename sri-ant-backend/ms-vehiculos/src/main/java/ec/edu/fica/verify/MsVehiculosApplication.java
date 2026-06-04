package ec.edu.fica.verify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

// ─────────────────────────────────────────────────────────────────────────────
// MAIN
// ─────────────────────────────────────────────────────────────────────────────
@SpringBootApplication
class MsVehiculosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVehiculosApplication.class, args);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DTOs
// ─────────────────────────────────────────────────────────────────────────────
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class VehiculoSriDTO {
    private String numeroPlaca;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private String anioModelo;
    private String colorVehiculo;
    private String cilindraje;
    private String tipoVehiculo;
    private String estadoMatricula;
    private String numeroCampv;
}

@Data
class VehiculoResponseDTO {
    private String placa;
    private String marca;
    private String modelo;
    private String anio;
    private String color;
    private String cilindraje;
    private String tipo;
    private String estadoMatricula;
}

// ─────────────────────────────────────────────────────────────────────────────
// CLIENT
// ─────────────────────────────────────────────────────────────────────────────
@Slf4j
@Component
class VehiculoClient {

    private final WebClient webClient;
    private final long timeoutSeconds;

    VehiculoClient(WebClient.Builder builder,
                   @Value("${sri.vehiculos-url}") String baseUrl,
                   @Value("${sri.timeout-seconds:10}") long timeoutSeconds) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Consulta vehículo por número de placa.
     * Endpoint SRI: /obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn={placa}
     */
    public Mono<VehiculoSriDTO[]> obtenerPorPlaca(String placa) {
        log.debug("Consultando vehículo placa: {}", placa);
        return webClient.get()
                .uri("/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn={placa}", placa)
                .retrieve()
                .bodyToMono(VehiculoSriDTO[].class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnError(e -> log.error("Error consultando vehículo {}: {}", placa, e.getMessage()));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SERVICE
// ─────────────────────────────────────────────────────────────────────────────
@Slf4j
@Service
@RequiredArgsConstructor
class VehiculoService {

    private final VehiculoClient vehiculoClient;

    public Mono<VehiculoResponseDTO> obtenerVehiculo(String placa) {
        return vehiculoClient.obtenerPorPlaca(placa)
                .map(arr -> {
                    if (arr == null || arr.length == 0) {
                        throw new VehiculoNoEncontradoException("No se encontró vehículo con placa: " + placa);
                    }
                    VehiculoSriDTO v = arr[0];
                    VehiculoResponseDTO dto = new VehiculoResponseDTO();
                    dto.setPlaca(v.getNumeroPlaca());
                    dto.setMarca(v.getMarcaVehiculo());
                    dto.setModelo(v.getModeloVehiculo());
                    dto.setAnio(v.getAnioModelo());
                    dto.setColor(v.getColorVehiculo());
                    dto.setCilindraje(v.getCilindraje());
                    dto.setTipo(v.getTipoVehiculo());
                    dto.setEstadoMatricula(v.getEstadoMatricula());
                    log.info("Vehículo encontrado: {} {} {}", dto.getMarca(), dto.getModelo(), dto.getPlaca());
                    return dto;
                });
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
class VehiculoController {

    private final VehiculoService vehiculoService;

    /**
     * GET /vehiculos/consultar?placa={placa}
     * Retorna información del vehículo registrado en el SRI.
     */
    @GetMapping("/consultar")
    public Mono<ResponseEntity<VehiculoResponseDTO>> consultar(@RequestParam String placa) {
        return vehiculoService.obtenerVehiculo(placa.toUpperCase())
                .map(ResponseEntity::ok)
                .onErrorResume(VehiculoNoEncontradoException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXCEPTION
// ─────────────────────────────────────────────────────────────────────────────
class VehiculoNoEncontradoException extends RuntimeException {
    VehiculoNoEncontradoException(String msg) { super(msg); }
}
