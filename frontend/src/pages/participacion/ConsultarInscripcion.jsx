import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { listarEventosPublicos } from '../../services/eventoService';
import { consultarEstadoInscripcion } from '../../services/participacionService';
import { toast } from 'sonner';
import { Button } from '../../components/ui/Button';
import { QRCodeCanvas } from 'qrcode.react';
import { Search, IdCard, CalendarClock, Download, ArrowLeft, CheckCircle2, Clock } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';

export default function ConsultarInscripcion() {
  const [eventos, setEventos] = useState([]);
  const [documento, setDocumento] = useState('');
  const [eventoId, setEventoId] = useState('');
  const [resultado, setResultado] = useState(null);
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    listarEventosPublicos().then((todos) => {
      setEventos(todos.filter((e) => e.estado === 'PROGRAMADO'));
    });
  }, []);

  const handleConsultar = async (e) => {
    e.preventDefault();
    setResultado(null);
    setCargando(true);
    try {
      const datos = await consultarEstadoInscripcion(documento, eventoId);
      setResultado(datos);
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'No se encontró ninguna inscripción con esos datos');
    } finally {
      setCargando(false);
    }
  };

  const handleDescargarQr = () => {
    const canvas = document.querySelector('#qr-consulta canvas');
    if (!canvas) return;
    const url = canvas.toDataURL('image/png');
    const link = document.createElement('a');
    link.href = url;
    link.download = 'mi-qr-inscripcion.png';
    link.click();
  };

  return (
    <AuthLayout
      title="Consultar Inscripción"
      subtitle="Verifica tu estado actual en la lista de espera de un evento."
      imageSrc="https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=2000&q=80"
    >
      <div className="mx-auto w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center mb-6">
        <Search className="w-7 h-7 text-blue-600" />
      </div>

      {!resultado ? (
        <form onSubmit={handleConsultar} className="space-y-5">
          <div className="relative">
            <IdCard className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              value={documento}
              onChange={(e) => setDocumento(e.target.value.replace(/\D/g, ''))}
              placeholder="Número de documento (mín. 8 dígitos)"
              pattern="[0-9]{8,15}" minLength={8} inputMode="numeric"
              title="El documento debe ser numérico, mínimo 8 dígitos"
              className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
              required
            />
          </div>
          <div className="relative">
            <CalendarClock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 z-10" />
            <select
              value={eventoId}
              onChange={(e) => setEventoId(e.target.value)}
              className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm appearance-none bg-slate-50 font-medium text-slate-700"
              required
            >
              <option value="">Selecciona el evento</option>
              {eventos.map((ev) => (
                <option key={ev.id} value={ev.id}>{ev.nombre}</option>
              ))}
            </select>
          </div>
          <div className="pt-2">
            <button
              type="submit"
              disabled={cargando}
              className="w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-4 focus:ring-blue-600/20 transition-all disabled:opacity-70"
            >
              {cargando ? 'Consultando...' : 'Consultar'}
            </button>
          </div>
        </form>
      ) : (
        <div className="text-center py-4">
          {resultado.estado === 'ACTIVA' ? (
            <>
              <div className="bg-emerald-100 text-emerald-600 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle2 className="w-8 h-8" />
              </div>
              <h2 className="text-xl font-bold text-slate-900 mb-2">¡Ya fuiste promovido!</h2>
              <p className="text-sm text-slate-500 mb-6">Presenta este código QR el día del evento.</p>
              <div className="flex justify-center mb-6">
                <div id="qr-consulta" className="p-4 bg-slate-50 border border-slate-200 rounded-2xl shadow-sm">
                  <QRCodeCanvas value={resultado.codigoQr} size={180} />
                </div>
              </div>
              <Button variant="secondary" icon={Download} onClick={handleDescargarQr} className="w-full mb-3text text-gray-700">
                Descargar mi código QR
              </Button>
            </>
          ) : (
            <>
              <div className="bg-orange-100 text-orange-600 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Clock className="w-8 h-8" />
              </div>
              <h2 className="text-xl font-bold text-slate-900 mb-2">Aún en lista de espera</h2>
              <p className="text-slate-600">
                Posición actual: <span className="font-bold text-xl text-slate-900 ml-1">#{resultado.posicion}</span>
              </p>
              <p className="text-sm text-slate-500 mt-4 mb-6">Sigue pendiente en esta sección para ver si ya fuiste promovido</p>
            </>
          )}
        </div>
      )}

      <button onClick={() => navigate('/')} className="w-full mt-6 py-3 border border-slate-200 text-slate-600 font-medium rounded-xl hover:bg-slate-50 flex items-center justify-center gap-2 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Volver al inicio
      </button>
    </AuthLayout>
  );
}