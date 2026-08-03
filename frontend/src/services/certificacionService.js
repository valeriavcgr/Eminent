import api from './api';

export async function listarEventosFinalizados() {
  const response = await api.get('/certificados/eventos-finalizados');
  return response.data;
}

export async function descargarCertificado(documento, eventoId) {
  const response = await api.get('/certificados/descargar', {
    params: { documento, eventoId },
    responseType: 'blob',
  });
  return response.data;
}

export async function verificarCertificado(codigo) {
  const response = await api.get(`/certificados/verificar/${codigo}`);
  return response.data;
}