// src/components/Stepper.jsx
export default function Stepper({ step }) {
  const labels = ['RUC & Email', 'Persona & Vehículo', 'Licencia ANT'];
  return (
    <div className="stepper">
      {labels.map((label, i) => {
        const n = i + 1;
        const done = step > n;
        const active = step === n;
        return (
          <div key={n} className="step-group">
            <div className={`step-dot ${done ? 'done' : active ? 'active' : ''}`}>
              {done ? '✓' : n}
            </div>
            <span className={`step-label ${active ? 'active' : ''}`}>{label}</span>
            {i < labels.length - 1 && (
              <div className={`step-line ${step > n ? 'done' : ''}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}
