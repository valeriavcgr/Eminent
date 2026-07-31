import api from './api';

export async function consultarAuditoria(filtros = {}) {
  const response = await api.get('/auditoria', { params: { size: 1000, ...filtros } });
  return response.data.content;
}