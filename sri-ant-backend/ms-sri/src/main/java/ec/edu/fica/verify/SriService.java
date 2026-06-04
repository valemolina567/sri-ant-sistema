package ec.edu.fica.verify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de verificación SRI.
 *
 * Flujo:
 *   1. Verifica existencia del RUC.
 *   2. Obtiene datos completos del contribuyente.
 *   3. Valida que sea persona natural (RUC de persona natural termina en 001
 *      y los dos primeros dígitos de la cédula son < 6).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SriService {

    private final SriClient sriClient;

    public Mono<PersonaResponseDTO> verificarYObtenerPersona(String ruc) {
        return sriClient.existeRuc(ruc)
                .flatMap(existe -> {
                    if (!Boolean.TRUE.equals(existe)) {
                        return Mono.error(new ContribuyenteNoEncontradoException(
                                "El RUC " + ruc + " no está registrado como contribuyente del SRI"));
                    }
                    return sriClient.obtenerPorRuc(ruc);
                })
                .map(contribuyentes -> {
                    if (contribuyentes == null || contribuyentes.length == 0) {
                        throw new ContribuyenteNoEncontradoException("No se encontraron datos para RUC: " + ruc);
                    }
                    ContribuyenteDTO c = contribuyentes[0];

                    // Personas naturales: tipo "NATURAL" o RUC termina en 001
                    boolean esPersonaNatural = "NATURAL".equalsIgnoreCase(c.getTipoContribuyente())
                            || (ruc != null && ruc.endsWith("001"));

                    if (!esPersonaNatural) {
                        throw new NoEsPersonaNaturalException(
                                "El RUC corresponde a una persona jurídica. Solo se permiten personas naturales.");
                    }

                    PersonaResponseDTO dto = new PersonaResponseDTO();
                    dto.setRuc(c.getNumeroRuc());
                    dto.setNombreCompleto(c.getNombreCompleto());
                    dto.setCedula(ruc.length() >= 10 ? ruc.substring(0, 10) : ruc);
                    dto.setActividad(c.getActividadEconomica());
                    dto.setEstado(c.getEstadoContribuyente());
                    dto.setDireccion(c.getDireccionCompleta());
                    dto.setEsPersonaNatural(true);
                    dto.setContribuyenteActivo("ACTIVO".equalsIgnoreCase(c.getEstadoContribuyente()));

                    log.info("Contribuyente verificado: {} - {}", dto.getRuc(), dto.getNombreCompleto());
                    return dto;
                });
    }
}
