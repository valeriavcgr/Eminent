//FUNCIONES QUE GUARDAN UN ENDPOINT ESPECIFICO

import api from './api';

export async function login(correo, contrasena) {
  const response = await api.post('/auth/login', { correo, contrasena });
  return response.data.token; //token que js convierte en objeto js
}

//peticion asincrona que espera la respuesta del servidor
//  await significa 
// "espera aquí hasta que la respuesta llegue, antes de seguir". Es la forma en que JavaScript maneja 