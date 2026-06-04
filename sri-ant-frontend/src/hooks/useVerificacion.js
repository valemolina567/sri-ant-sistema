// src/hooks/useVerificacion.js
import { useState } from 'react';
import {
  verificarContribuyente,
  consultarVehiculo,
  consultarPuntosLicencia,
} from '../api/verificacion';

/**
 * Hook que encapsula el flujo completo de verificación:
 *   Paso 1: ingreso de email + RUC → verifica en SRI
 *   Paso 2: muestra persona → ingreso de placa → consulta vehículo SRI
 *   Paso 3: consulta puntos de licencia ANT (Cache-Aside)
 */
export function useVerificacion() {
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [email, setEmail] = useState('');
  const [ruc, setRuc] = useState('');
  const [placa, setPlaca] = useState('');

  const [persona, setPersona] = useState(null);
  const [vehiculo, setVehiculo] = useState(null);
  const [licencia, setLicencia] = useState(null);

  // ── PASO 1: verificar contribuyente SRI ───────────────────────────────────
  async function verificarRuc() {
    if (!ruc || !email) { setError('Completa todos los campos'); return; }
    setError(null);
    setLoading(true);
    try {
      const data = await verificarContribuyente(ruc);
      if (!data.esPersonaNatural) {
        throw new Error('Solo se permite verificar personas naturales');
      }
      setPersona(data);
      setStep(2);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  // ── PASO 2: consultar vehículo SRI ────────────────────────────────────────
  async function consultarPlaca() {
    if (!placa) { setError('Ingresa la placa del vehículo'); return; }
    setError(null);
    setLoading(true);
    try {
      const data = await consultarVehiculo(placa);
      setVehiculo(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  function irALicencia() {
    setStep(3);
    setError(null);
    setLicencia(null);
  }

  // ── PASO 3: consultar puntos ANT (Cache-Aside) ────────────────────────────
  async function consultarLicencia() {
    const cedula = persona?.cedula || ruc.substring(0, 10);
    const p = vehiculo?.placa || placa;
    setError(null);
    setLoading(true);
    try {
      const data = await consultarPuntosLicencia(cedula, p);
      setLicencia(data);
    } catch (e) {
      setError(e.message === 'HTTP 503'
        ? 'ANT no disponible — sin datos en caché. Intente más tarde.'
        : e.message);
    } finally {
      setLoading(false);
    }
  }

  function reset() {
    setStep(1); setLoading(false); setError(null);
    setEmail(''); setRuc(''); setPlaca('');
    setPersona(null); setVehiculo(null); setLicencia(null);
  }

  return {
    step, loading, error,
    email, setEmail, ruc, setRuc, placa, setPlaca,
    persona, vehiculo, licencia,
    verificarRuc, consultarPlaca, irALicencia, consultarLicencia, reset,
  };
}
