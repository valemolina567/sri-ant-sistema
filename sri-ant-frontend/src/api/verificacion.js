// src/api/verificacion.js
// Cliente HTTP que conecta el frontend React con los microservicios Java.
// En desarrollo apunta a localhost; en producción usa las URLs del API Gateway.

const GATEWAY_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

async function get(path) {
  const res = await fetch(`${GATEWAY_URL}${path}`);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
}

/**
 * Verifica si un RUC corresponde a un contribuyente activo y persona natural.
 * Invoca: MS-SRI → GET /sri/verificar?ruc={ruc}
 * SRI endpoint: /ConsolidadoContribuyente/existePorNumeroRuc + /obtenerPorNumerosRuc
 */
export async function verificarContribuyente(ruc) {
  return get(`/sri/verificar?ruc=${encodeURIComponent(ruc)}`);
}

/**
 * Obtiene información de un vehículo por placa.
 * Invoca: MS-Vehículos → GET /vehiculos/consultar?placa={placa}
 * SRI endpoint: /BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn
 */
export async function consultarVehiculo(placa) {
  return get(`/vehiculos/consultar?placa=${encodeURIComponent(placa)}`);
}

/**
 * Obtiene puntos de licencia con Cache-Aside en Redis.
 * Invoca: MS-ANT → GET /ant/puntos?cedula={cedula}&placa={placa}
 * ANT endpoint: /PortalWEB/paginas/clientes/clp_grid_citaciones.jsp
 *
 * La respuesta incluye fuenteDatos: "ANT" | "CACHE"
 */
export async function consultarPuntosLicencia(cedula, placa) {
  return get(`/ant/puntos?cedula=${encodeURIComponent(cedula)}&placa=${encodeURIComponent(placa)}`);
}

/**
 * Invalida el caché Redis para un conductor (admin/testing).
 * Invoca: MS-ANT → DELETE /ant/cache?cedula={cedula}&placa={placa}
 */
export async function invalidarCacheAnt(cedula, placa) {
  const res = await fetch(
    `${GATEWAY_URL}/ant/cache?cedula=${encodeURIComponent(cedula)}&placa=${encodeURIComponent(placa)}`,
    { method: 'DELETE' }
  );
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
}
