export function formatearDuracion(valor) {
  const dias = Math.round(Number(valor));
  if (dias === 7) return '1 semana';
  if (dias === 1) return '1 día';
  return `${dias} días`;
}