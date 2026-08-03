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
  return config;
});

//localstorage: "variable que guarda texto plano, no se borra al regenerar la pagina"
export default api;