import api from './api';

export async function inscribir(datos) {
  const response = await api.post('/inscripciones', datos);
  return response.data;
}

export async function listarInscripcionesPorEvento(eventoId) {
  const response = await api.get('/inscripciones', { params: { eventoId } });
  return response.data;
}

export async function consultarColaEspera(eventoId) {
  const response = await api.get(`/eventos/${eventoId}/lista-espera`);
  return response.data;
}

export async function promoverInscripcion(id) {
  const response = await api.post(`/inscripciones/${id}/promover`);
  return response.data;
}

export async function previsualizarCsv(eventoId, archivo) {
  const formData = new FormData();
  formData.append('archivo', archivo);
  const response = await api.post(`/eventos/${eventoId}/importar-csv/previsualizar`, formData);
  return response.data;
}

export async function confirmarCsv(eventoId, archivo) {
  const formData = new FormData();
  formData.append('archivo', archivo);
  const response = await api.post(`/eventos/${eventoId}/importar-csv/confirmar`, formData);
  return response.data;
}

export async function buscarHistorial(query) {
  const response = await api.get('/participantes/historial', { params: query });
  return response.data;
}

export async function consultarEstadoInscripcion(documento, eventoId) {
  const response = await api.get('/inscripciones/consultar', { params: { documento, eventoId } });
  return response.data;
}