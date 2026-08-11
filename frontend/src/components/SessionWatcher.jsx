import { Clock } from 'lucide-react';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';
import { useInactivityTimer } from '../hooks/useInactivityTimer';

export default function SessionWatcher() {
  const { avisoVisible, segundosRestantes, seguirConectado, cerrarAhora } = useInactivityTimer();

  return (
    <Modal isOpen={avisoVisible} onClose={seguirConectado} title="¿Sigues ahí?">
      <div className="flex flex-col items-center text-center space-y-4">
        <div className="bg-amber-100 p-3 rounded-full text-amber-600">
          <Clock className="w-8 h-8" />
        </div>
        <p className="text-slate-700">
          Tu sesión se cerrará por inactividad en <strong>{segundosRestantes}</strong> segundos.
        </p>
        <p className="text-sm text-slate-500">
          Haz clic en "Seguir conectado" para continuar usando el sistema.
        </p>
        <div className="flex w-full gap-3 mt-4">
          <Button variant="secondary" className="flex-1" onClick={cerrarAhora}>
            Cerrar sesión ahora
          </Button>
          <Button className="flex-1 bg-blue-600 hover:bg-blue-700 text-white" onClick={seguirConectado}>
            Seguir conectado
          </Button>
        </div>
      </div>
    </Modal>
  );
}
