import api from './api';

export async function enviarEncuesta(codigoUnico, calificacion, comentario) {
  const response = await api.post('/encuestas', { codigoUnico, calificacion, comentario });
  return response.data;
}

export async function obtenerComentariosEvento(eventoId, { calificacion, page = 0, size = 10 } = {}) {
  const response = await api.get(`/encuestas/evento/${eventoId}`, {
    params: { calificacion, page, size, sort: 'fechaCreacion,desc' },
  });
  return response.data;
}

export async function obtenerPromedioEvento(eventoId) {
  const response = await api.get(`/encuestas/evento/${eventoId}/promedio`);
  return response.data.promedio;
}
