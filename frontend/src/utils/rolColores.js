const COLORES_FONDO = {
  ADMIN: 'bg-blue-600',
  OPERADOR: 'bg-emerald-600',
  MONITOR: 'bg-orange-500',
};

const COLORES_TEXTO = {
  ADMIN: 'text-blue-600',
  OPERADOR: 'text-emerald-600',
  MONITOR: 'text-orange-500',
};

export function colorFondoRol(rol) {
  return COLORES_FONDO[rol] || 'bg-gray-800';
}

export function colorTextoRol(rol) {
  return COLORES_TEXTO[rol] || 'text-gray-800';
}
