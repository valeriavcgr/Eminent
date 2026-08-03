import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { listarEventosFinalizados, descargarCertificado } from '../../services/certificacionService';
import { toast } from 'sonner';
import { IdCard, CalendarCheck, Download, ArrowLeft, Award, AlertCircle } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';

export default function RecibirCertificado() {
  const [eventos, setEventos] = useState([]);
  const [errorCarga, setErrorCarga] = useState(false);
  const [documento, setDocumento] = useState('');
  const [eventoId, setEventoId] = useState('');
  const [descargando, setDescargando] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    listarEventosFinalizados()
      .then(setEventos)
      .catch(() => setErrorCarga(true));
  }, []);

  const handleDescargar = async (e) => {
    e.preventDefault();
    setDescargando(true);
    try {
      const pdfBlob = await descargarCertificado(documento, eventoId);
      const url = window.URL.createObjectURL(pdfBlob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `certificado-${documento}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
      toast.success('Certificado descargado correctamente');
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'No hay certificado disponible para este evento');
    } finally {
      setDescargando(false);
    }
  };

  return (
    <AuthLayout
      title="Descarga tu Certificado"
      subtitle="Reconoce tu esfuerzo. Ingresa tus datos para descargar tu certificado oficial de asistencia."
      imageSrc="https://images.unsplash.com/photo-1523580494863-6f3031224c94?auto=format&fit=crop&w=2000&q=80"
    >
      <div className="mx-auto w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center mb-6">
        <Award className="w-7 h-7 text-blue-600" />
      </div>

      {errorCarga ? (
        <div className="flex flex-col items-center text-center py-8 bg-slate-50 rounded-2xl border border-slate-200">
          <div className="bg-amber-100 text-amber-600 w-12 h-12 rounded-full flex items-center justify-center mb-3">
            <AlertCircle className="w-6 h-6" />
          </div>
          <p className="font-bold text-slate-900">No se pudieron cargar los eventos</p>
          <p className="text-sm text-slate-500 mt-1">Intenta nuevamente más tarde.</p>
          <button onClick={() => window.location.reload()} className="mt-4 px-4 py-2 text-sm font-medium text-blue-600 hover:text-blue-700 underline">
            Reintentar
          </button>
        </div>
      ) : (
        <form onSubmit={handleDescargar} className="space-y-5">
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
            <CalendarCheck className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 z-10" />
            <select
              value={eventoId}
              onChange={(e) => setEventoId(e.target.value)}
              className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm appearance-none bg-slate-50 font-medium text-slate-700"
              required
            >
              <option value="">Selecciona el evento finalizado</option>
              {eventos.map((ev) => (
                <option key={ev.id} value={ev.id}>{ev.nombre}</option>
              ))}
            </select>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={descargando}
              className="w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-4 focus:ring-blue-600/20 transition-all disabled:opacity-70"
            >
              {descargando ? 'Descargando...' : (
                <>
                  <Download className="w-4 h-4 mr-2" /> Descargar Certificado
                </>
              )}
            </button>
          </div>
        </form>
      )}
    </AuthLayout>
  );
}