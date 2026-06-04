// src/components/Step1Ruc.jsx
export default function Step1Ruc({ email, setEmail, ruc, setRuc, loading, error, verificarRuc }) {
  return (
    <div className="card">
      <div className="card-title">Identificación del Contribuyente</div>

      <div className="field">
        <label>Correo Electrónico</label>
        <input
          type="email"
          placeholder="usuario@ejemplo.com"
          value={email}
          onChange={e => setEmail(e.target.value)}
          disabled={loading}
        />
      </div>

      <div className="field">
        <label>RUC / Cédula (persona natural)</label>
        <input
          type="text"
          placeholder="1712345678001"
          maxLength={13}
          value={ruc}
          onChange={e => setRuc(e.target.value)}
          disabled={loading}
        />
      </div>

      {error && <div className="alert alert-err">⚠ {error}</div>}

      <button
        className="btn btn-primary"
        onClick={verificarRuc}
        disabled={loading}
      >
        {loading
          ? <><span className="spinner" /> Verificando en SRI…</>
          : '→ Verificar en SRI'}
      </button>

      <div className="alert alert-info mt-12">
        ⓘ Llama a <code>MS-SRI</code> →{' '}
        <code>/ConsolidadoContribuyente/existePorNumeroRuc</code>
      </div>
    </div>
  );
}
