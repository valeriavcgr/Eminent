// se inicializa la base de la url que se usa en  todos los endpoint, 
// cambia dependiendo el compoente  y la petición

import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

//FUNCION interceptor: un interceptor es como un dto? 
// ya que antes de llegar al backend tiene que verificar el token de acceso
// 
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const rolActivo = localStorage.getItem('rolActivo');
  if (rolActivo) {
    config.headers['X-Rol-Activo'] = rolActivo;
  }
  return config;
});

// Si el token expiró o es inválido, el backend responde 401 en cualquier ruta protegida.
// Se limpia la sesión y se redirige a login, salvo que el 401 venga del propio login
// (ahí ya lo maneja Login.jsx como "credenciales incorrectas").
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const esLogin = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !esLogin) {
      localStorage.removeItem('token');
      localStorage.removeItem('roles');
      localStorage.removeItem('rolActivo');
      localStorage.removeItem('ultimaActividad');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

//localstorage: "variable que guarda texto plano, no se borra al regenerar la pagina"
export default api;