import api from './api';

export async function obtenerResumenDashboard(filtros = {}) {
  const response = await api.get('/dashboard', { params: filtros });
  return response.data;
}