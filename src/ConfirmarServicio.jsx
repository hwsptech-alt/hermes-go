import { useState } from 'react';
import axios from 'axios';
import './App.css';

function ConfirmarServicio({ carga, transportador, usuario, onConfirmar, onVolver }) {
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');

  const handleAceptar = async () => {
    setCargando(true);
    try {
      // Llamamos al endpoint del backend para aceptar la carga (R06)
      const response = await axios.put(
        `http://localhost:8080/api/cargas/${carga.id}/aceptar/${transportador.id}`
      );

      if (response.data) {
        setMensaje('¡Servicio confirmado exitosamente!');
        setTimeout(() => onConfirmar(response.data), 1500);
      }
    } catch (error) {
      setMensaje(error.response?.data || 'Error al confirmar el servicio');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="container">
      <div className="form-card">
        <h2>Resumen del Servicio</h2>
        
        <div className="form-section">
          <h3>Información del Transportador</h3>
          <div className="info-item"><span className="info-label">Conductor</span><span className="info-valor">{transportador.nombreCompleto}</span></div>
          <div className="info-item"><span className="info-label">Vehículo</span><span className="info-valor">{transportador.tipoVehiculo || 'No registrado'}</span></div>
          <div className="info-item"><span className="info-label">Placa</span><span className="info-valor">{transportador.placa || 'No registrada'}</span></div>
          <div className="info-item"><span className="info-label">Calificación</span><span className="info-valor">⭐⭐⭐⭐⭐ ({transportador.calificacion || 5.0})</span></div>
        </div>

        <div className="form-section">
          <h3>Información del Servicio</h3>
          <div className="info-item"><span className="info-label">Origen</span><span className="info-valor">{carga.origen}</span></div>
          <div className="info-item"><span className="info-label">Destino</span><span className="info-valor">{carga.destino}</span></div>
          <div className="info-item"><span className="info-label">Fecha y Hora</span><span className="info-valor">{new Date(carga.fechaHora).toLocaleString()}</span></div>
          <div className="info-item"><span className="info-label">Tipo de Carga</span><span className="info-valor">{carga.tipoCarga}</span></div>
          <div className="info-item"><span className="info-label">Peso</span><span className="info-valor">{carga.peso} kg</span></div>
        </div>

        <div className="form-section">
          <h3>Precio Total</h3>
          <div className="precio-total">
            ${carga.precioFinal?.toLocaleString() || carga.precioBase?.toLocaleString() || '0'}
          </div>
        </div>

        {mensaje && <p className="mensaje" style={{ color: '#2e7d32', marginBottom: '10px' }}>{mensaje}</p>}

        <div className="button-group">
          <button className="btn-login" onClick={handleAceptar} disabled={cargando}>
            {cargando ? 'Confirmando...' : 'Aceptar Servicio'}
          </button>
          <button className="btn-volver" onClick={onVolver} disabled={cargando}>
            ← Rechazar y Volver
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmarServicio;