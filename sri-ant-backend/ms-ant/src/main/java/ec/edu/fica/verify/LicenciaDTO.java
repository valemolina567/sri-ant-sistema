package ec.edu.fica.verify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa los puntos de licencia y las infracciones de un conductor.
 * Serializable para almacenamiento en Redis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenciaDTO implements Serializable {

    private String cedula;
    private String placa;
    private int puntosActuales;        // Puntos vigentes (máx 30)
    private int puntosDescontados;     // Puntos perdidos por infracciones
    private List<InfraccionDTO> infracciones;
    private LocalDateTime fechaConsulta;
    private String fuenteDatos;        // "ANT" | "CACHE"

    // ── DTO anidado de infracción ──────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfraccionDTO implements Serializable {
        private String fecha;
        private String descripcion;
        private int puntosDescontados;
        private String numeroResolucion;
    }
}
