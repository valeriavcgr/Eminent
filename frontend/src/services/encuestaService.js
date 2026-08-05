import api from './api';

export async function enviarEncuesta(codigoUnico, calificacion, comentario) {
  const response = await api.post('/encuestas', { codigoUnico, calificacion, comentario });
  return response.data;
}

export async function obtenerComentariosEvento(eventoId) {
  const response = await api.get(`/encuestas/evento/${eventoId}`);
  return response.data;
}