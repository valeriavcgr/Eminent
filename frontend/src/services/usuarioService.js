import api from './api';

export async function crearUsuario(datos) {
  const response = await api.post('/usuarios', datos);
  return response.data;
}

export async function listarUsuarios(rol) {
  const params = rol ? { rol, size: 1000 } : { size: 1000 };
  const response = await api.get('/usuarios', { params });
  return response.data.content;
}

export async function obtenerUsuario(id) {
  const response = await api.get(`/usuarios/${id}`);
  return response.data;
}

export async function editarUsuario(id, datos) {
  const response = await api.put(`/usuarios/${id}`, datos);
  return response.data;
}

export async function cambiarEstadoUsuario(id, estado) {
  const response = await api.patch(`/usuarios/${id}/estado`, { estado });
  return response.data;
}