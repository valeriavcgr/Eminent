export function compartirEnLinkedIn(codigoUnico) {
  const urlVerificacion = `${window.location.origin}/certificados/verificar?codigo=${codigoUnico}`;
  const urlLinkedIn = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(urlVerificacion)}`;
  window.open(urlLinkedIn, '_blank', 'width=600,height=600');
}