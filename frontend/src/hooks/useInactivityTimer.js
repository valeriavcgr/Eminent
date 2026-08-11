import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext';

const UMBRAL_INACTIVIDAD_MS = 15 * 60 * 1000; // 15 minutos
const DURACION_AVISO_MS = 30 * 1000; // 30 segundos de cuenta regresiva
const THROTTLE_ACTIVIDAD_MS = 5000; // no escribir en localStorage más de 1 vez cada 5s
const EVENTOS_ACTIVIDAD = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];

/**
 * Detecta inactividad del usuario y expone el estado del aviso de cierre de sesión.
 * La "última actividad" se guarda en localStorage (clave 'ultimaActividad') para que
 * la actividad en cualquier pestaña reinicie el temporizador de todas: cada pestaña
 * reprograma su propio setTimeout tanto por actividad local como al detectar, vía el
 * evento 'storage', que otra pestaña actualizó esa clave.
 */
export function useInactivityTimer() {
  const { token, logout } = useAuth();
  const [avisoVisible, setAvisoVisibleState] = useState(false);
  const [segundosRestantes, setSegundosRestantes] = useState(DURACION_AVISO_MS / 1000);

  const avisoVisibleRef = useRef(false);
  const timeoutRef = useRef(null);
  const countdownIntervalRef = useRef(null);
  const avisoInicioRef = useRef(null);
  const ultimaEscrituraRef = useRef(0);
  const seguirConectadoRef = useRef(() => {});
  const cerrarAhoraRef = useRef(() => {});

  function setAvisoVisible(valor) {
    avisoVisibleRef.current = valor;
    setAvisoVisibleState(valor);
  }

  useEffect(() => {
    if (!token) {
      seguirConectadoRef.current = () => {};
      cerrarAhoraRef.current = () => {};
      return;
    }

    function leerUltimaActividad() {
      return parseInt(localStorage.getItem('ultimaActividad'), 10) || Date.now();
    }

    function limpiarTimers() {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
    }

    function programarUmbral() {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      const restante = UMBRAL_INACTIVIDAD_MS - (Date.now() - leerUltimaActividad());
      timeoutRef.current = setTimeout(alCumplirseUmbral, Math.max(restante, 0));
    }

    function alCumplirseUmbral() {
      // Los navegadores pausan/atrasan timers en pestañas en background: se vuelve a
      // calcular el tiempo real transcurrido antes de actuar, por si el disparo llegó tarde.
      const transcurrido = Date.now() - leerUltimaActividad();
      if (transcurrido >= UMBRAL_INACTIVIDAD_MS) {
        mostrarAviso();
      } else {
        programarUmbral();
      }
    }

    function mostrarAviso() {
      avisoInicioRef.current = Date.now();
      setAvisoVisible(true);
      setSegundosRestantes(DURACION_AVISO_MS / 1000);

      countdownIntervalRef.current = setInterval(() => {
        const ultima = leerUltimaActividad();
        if (Date.now() - ultima < UMBRAL_INACTIVIDAD_MS) {
          // Hubo actividad (en esta pestaña vía el botón, o en otra) mientras el aviso
          // estaba visible: se cancela automáticamente.
          seguirConectado();
          return;
        }
        const restanteMs = avisoInicioRef.current + DURACION_AVISO_MS - Date.now();
        if (restanteMs <= 0) {
          cerrarPorInactividad();
        } else {
          setSegundosRestantes(Math.ceil(restanteMs / 1000));
        }
      }, 1000);
    }

    function seguirConectado() {
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
      setAvisoVisible(false);
      localStorage.setItem('ultimaActividad', Date.now().toString());
      programarUmbral();
    }

    function cerrarPorInactividad() {
      limpiarTimers();
      setAvisoVisible(false);
      logout();
    }

    function registrarActividad() {
      // Mientras el aviso está visible en esta pestaña, solo cuenta la acción explícita
      // del usuario (los botones del modal), no el simple movimiento del mouse al leerlo.
      if (avisoVisibleRef.current) return;
      const ahora = Date.now();
      if (ahora - ultimaEscrituraRef.current < THROTTLE_ACTIVIDAD_MS) return;
      ultimaEscrituraRef.current = ahora;
      localStorage.setItem('ultimaActividad', ahora.toString());
      programarUmbral();
    }

    function handleStorageActividad(event) {
      if (event.key === 'ultimaActividad' && event.newValue !== null) {
        // Actividad detectada en otra pestaña: reprogramar sin volver a escribir localStorage.
        programarUmbral();
      }
    }

    seguirConectadoRef.current = seguirConectado;
    cerrarAhoraRef.current = cerrarPorInactividad;

    // Si el token ya era válido pero pasó mucho tiempo desde la última actividad
    // registrada (se reabrió el navegador horas/días después), no tiene sentido mostrar
    // una cuenta regresiva de 30s: se cierra sesión directo.
    if (Date.now() - leerUltimaActividad() >= UMBRAL_INACTIVIDAD_MS) {
      logout();
      return;
    }

    EVENTOS_ACTIVIDAD.forEach((ev) => window.addEventListener(ev, registrarActividad));
    window.addEventListener('storage', handleStorageActividad);
    programarUmbral();

    return () => {
      EVENTOS_ACTIVIDAD.forEach((ev) => window.removeEventListener(ev, registrarActividad));
      window.removeEventListener('storage', handleStorageActividad);
      limpiarTimers();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  return {
    avisoVisible,
    segundosRestantes,
    seguirConectado: () => seguirConectadoRef.current(),
    cerrarAhora: () => cerrarAhoraRef.current(),
  };
}
