import api from './api';

export async function crearEvento(datos) {
  const response = await api.post('/eventos', datos);
  return response.data;
}

export async function listarEventos(filtros = {}) {
  const response = await api.get('/eventos', { params: { size: 1000, ...filtros } });
  return response.data.content;
}
export async function editarEvento(id, datos) {
  const response = await api.put(`/eventos/${id}`, datos);
  return response.data;
}

export async function listarEventosPublicos() {
  const response = await api.get('/eventos/publicos', { params: { size: 1000 } });
  return response.data.content;
}

export async function cancelarEvento(id) {
  const response = await api.patch(`/eventos/${id}/cancelar`);
  return response.data;
}

export async function asignarMonitor(eventoId, monitorId) {
  const response = await api.post(`/eventos/${eventoId}/monitores?monitorId=${monitorId}`);
  return response.data;
}

export async function listarEventosMonitor() {
  const response = await api.get('/eventos/mis-eventos', { params: { size: 1000 } });
  return response.data.content;
}