// src/App.jsx
import { useVerificacion } from './hooks/useVerificacion';
import Step1Ruc from './components/Step1Ruc';
import Step2Vehiculo from './components/Step2Vehiculo';
import Step3Licencia from './components/Step3Licencia';
import Stepper from './components/Stepper';
import './App.css';

export default function App() {
  const ctx = useVerificacion();

  return (
    <div className="app">
      <header className="header">
        <div className="badge">Sistema de Verificación</div>
        <h1>SRI / ANT <span className="accent">Verificador</span></h1>
        <p className="subtitle">React + Java Microservicios + Redis Cache-Aside</p>
      </header>

      <Stepper step={ctx.step} />

      {ctx.step === 1 && <Step1Ruc {...ctx} />}
      {ctx.step === 2 && <Step2Vehiculo {...ctx} />}
      {ctx.step === 3 && <Step3Licencia {...ctx} />}
    </div>
  );
}
