import api from './api';

export async function listarParticipantesEvento(eventoId) {
  const response = await api.get(`/eventos/${eventoId}/participantes`);
  return response.data;
}

export async function registrarAsistenciaManual(inscripcionId) {
  const response = await api.post('/asistencias/manual', { inscripcionId });
  return response.data;
}

export async function registrarAsistenciaPorQr(contenidoQr, eventoId) {
  const response = await api.post('/asistencias/qr', { contenidoQr, eventoId });
  return response.data;
}