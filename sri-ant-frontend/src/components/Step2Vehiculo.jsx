// src/components/Step2Vehiculo.jsx
export default function Step2Vehiculo({
  persona, vehiculo, placa, setPlaca,
  loading, error, consultarPlaca, irALicencia,
}) {
  return (
    <div>
      {/* Datos del contribuyente */}
      {persona && (
        <div className="card">
          <div className="card-title">Datos del Contribuyente — SRI</div>
          <div className="info-grid">
            <InfoItem label="Nombre" value={persona.nombreCompleto} />
            <InfoItem label="RUC" value={persona.ruc} accent />
            <InfoItem label="Cédula" value={persona.cedula} />
            <InfoItem label="Estado" value={persona.estado} ok />
            <InfoItem label="Actividad" value={persona.actividad} />
            <InfoItem label="Dirección" value={persona.direccion} />
          </div>
        </div>
      )}

      {/* Formulario vehículo */}
      <div className="card">
        <div className="card-title">Consulta de Vehículo — SRI</div>

        <div className="field">
          <label>Número de Placa</label>
          <input
            type="text"
            placeholder="ABC-1234"
            maxLength={8}
            value={placa}
            onChange={e => setPlaca(e.target.value)}
            disabled={loading}
          />
        </div>

        {error && <div className="alert alert-err">⚠ {error}</div>}

        {vehiculo && (
          <div className="vehiculo-info">
            <div className="vehiculo-icon">🚗</div>
            <div className="info-grid">
              <InfoItem label="Placa" value={vehiculo.placa} accent />
              <InfoItem label="Marca / Modelo" value={`${vehiculo.marca} ${vehiculo.modelo}`} />
              <InfoItem label="Año" value={vehiculo.anio} />
              <InfoItem label="Color" value={vehiculo.color} />
              <InfoItem label="Cilindraje" value={vehiculo.cilindraje} />
              <InfoItem label="Estado" value={vehiculo.estadoMatricula} ok />
            </div>
          </div>
        )}

        <div className="btn-group">
          {!vehiculo && (
            <button className="btn btn-primary" onClick={consultarPlaca} disabled={loading}>
              {loading
                ? <><span className="spinner" /> Consultando SRI…</>
                : '→ Consultar Vehículo'}
            </button>
          )}
          {vehiculo && (
            <button className="btn btn-primary" onClick={irALicencia}>
              → Consultar Puntos de Licencia ANT
            </button>
          )}
        </div>
      </div>

      <div className="alert alert-info">
        ⓘ Llama a <code>MS-Vehículos</code> →{' '}
        <code>/BaseVehiculo/obtenerPorNumeroPlacaO...</code>
      </div>
    </div>
  );
}

function InfoItem({ label, value, accent, ok }) {
  return (
    <div className="info-item">
      <div className="lbl">{label}</div>
      <div className={`val ${accent ? 'accent' : ok ? 'ok' : ''}`}>{value}</div>
    </div>
  );
}
