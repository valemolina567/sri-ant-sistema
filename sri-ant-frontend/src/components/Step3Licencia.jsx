// src/components/Step3Licencia.jsx
export default function Step3Licencia({ licencia, loading, error, consultarLicencia, reset }) {
  const fromCache = licencia?.fuenteDatos === 'CACHE';

  return (
    <div>
      <div className="card">
        <div className="card-title">
          Puntos de Licencia — ANT
          {licencia && (
            <span className={`tag ${fromCache ? 'tag-cache' : 'tag-ok'}`} style={{ marginLeft: 'auto' }}>
              {fromCache ? '⚡ DESDE CACHÉ REDIS' : '✓ DESDE ANT'}
            </span>
          )}
        </div>

        {loading && (
          <div className="loading-center">
            <div className="spinner spinner-lg" />
            <p>Consultando ANT…</p>
            <small>La ANT tiene baja disponibilidad — puede tardar varios segundos</small>
          </div>
        )}

        {!loading && !licencia && !error && (
          <div className="center">
            <button className="btn btn-primary" onClick={consultarLicencia}>
              → Consultar Puntos ANT
            </button>
          </div>
        )}

        {!loading && licencia && (
          <>
            <PuntosCircle
              puntos={licencia.puntosActuales}
              descontados={licencia.puntosDescontados}
            />

            <div className="info-grid">
              <InfoItem label="Cédula" value={licencia.cedula} accent />
              <InfoItem label="Placa" value={licencia.placa} accent />
              <InfoItem label="Puntos actuales"
                value={`${licencia.puntosActuales} / 30`}
                color={licencia.puntosActuales >= 20 ? 'ok' : 'warn'} />
              <InfoItem label="Puntos descontados"
                value={licencia.puntosDescontados}
                color={licencia.puntosDescontados > 0 ? 'warn' : 'ok'} />
            </div>

            {fromCache && (
              <div className="alert alert-info mt-12">
                ⚡ Respuesta servida desde <strong>Redis Cloud</strong> — ANT no consultada (Cache-Aside)
              </div>
            )}

            {licencia.infracciones?.length > 0 ? (
              <div className="infracciones">
                <div className="infracciones-title">Infracciones registradas</div>
                {licencia.infracciones.map((inf, i) => (
                  <div key={i} className="infraccion-item">
                    <strong>{inf.fecha}</strong> · {inf.descripcion}
                    <span className="tag tag-err">-{inf.puntosDescontados} pts</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="alert alert-ok mt-12">✓ Sin infracciones registradas</div>
            )}
          </>
        )}

        {!loading && error && (
          <>
            <div className="alert alert-err">⚠ {error}</div>
            <button className="btn btn-ghost mt-12" onClick={consultarLicencia}>
              ↻ Reintentar consulta ANT
            </button>
          </>
        )}
      </div>

      <div className="alert alert-info">
        ⓘ <code>MS-ANT</code>: Cache-Aside → busca en Redis → si miss consulta ANT → guarda con TTL 24h
      </div>

      <div className="center mt-24">
        <button className="btn btn-ghost" style={{ width: 'auto' }} onClick={reset}>
          ↩ Nueva consulta
        </button>
      </div>
    </div>
  );
}

function PuntosCircle({ puntos }) {
  const color = puntos >= 20 ? '#00d4aa' : puntos >= 10 ? '#f59e0b' : '#f43f5e';
  return (
    <div className="puntos-circle" style={{ borderColor: color }}>
      <div className="puntos-num" style={{ color }}>{puntos}</div>
      <div className="puntos-lbl">puntos</div>
    </div>
  );
}

function InfoItem({ label, value, accent, color }) {
  return (
    <div className="info-item">
      <div className="lbl">{label}</div>
      <div className={`val ${accent ? 'accent' : color || ''}`}>{value}</div>
    </div>
  );
}
