package ec.edu.fica.verify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// ── DTO: respuesta de existePorNumeroRuc ────────────────────────────
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class ContribuyenteExistenciaDTO {
    private Boolean contribuyenteExistente;
}

// ── DTO: datos de persona natural ──────────────────────────────────
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class ContribuyenteDTO {
    private String numeroRuc;
    private String nombreCompleto;
    private String estadoContribuyente;
    private String tipoContribuyente;     // "NATURAL" | "JURIDICO"
    private String actividadEconomica;
    private String direccionCompleta;
}

// ── DTO de respuesta hacia el Gateway ──────────────────────────────
@Data
class PersonaResponseDTO {
    private String ruc;
    private String nombreCompleto;
    private String cedula;
    private String actividad;
    private String estado;
    private String direccion;
    private boolean esPersonaNatural;
    private boolean contribuyenteActivo;
}
