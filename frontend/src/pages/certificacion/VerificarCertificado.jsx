import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { verificarCertificado } from '../../services/certificacionService';
import { Badge } from '../../components/ui/Badge';
import { ShieldCheck, ShieldX, Search, ArrowLeft, User, Calendar, Clock, Hash } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function VerificarCertificado() {
  const [searchParams] = useSearchParams();
  const [codigo, setCodigo] = useState(searchParams.get('codigo') || '');
  const [resultado, setResultado] = useState(null);
  const [noEncontrado, setNoEncontrado] = useState(false);
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  const verificar = async (codigoAVerificar) => {
    setNoEncontrado(false);
    setResultado(null);
    setCargando(true);
    try {
      const datos = await verificarCertificado(codigoAVerificar);
      setResultado(datos);
    } catch (err) {
      setNoEncontrado(true);
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    const codigoDeUrl = searchParams.get('codigo');
    if (codigoDeUrl) verificar(codigoDeUrl);
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    verificar(codigo);
  };

  return (
    <AuthLayout
      title="Verificar Certificado"
      subtitle="Confirma la autenticidad y validez de un certificado emitido por EMINENT."
      imageSrc="https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=2000&q=80"
    >
      <div className="mx-auto w-14 h-14 bg-emerald-50 rounded-2xl flex items-center justify-center mb-6">
        <ShieldCheck className="w-7 h-7 text-emerald-600" />
      </div>

      <form onSubmit={handleSubmit} className="flex gap-3 mb-6">
        <div className="relative flex-1">
          <Hash className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
            placeholder="Código del certificado"
            className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
            required
          />
        </div>
        <button 
          type="submit" 
          disabled={cargando} 
          className="flex items-center justify-center px-6 bg-slate-900 hover:bg-blue-600 text-white rounded-xl text-sm font-bold shadow-sm transition-all disabled:opacity-70"
        >
          {cargando ? '...' : <Search className="w-5 h-5" />}
        </button>
      </form>

      {noEncontrado && (
        <div className="flex flex-col items-center text-center py-8 bg-slate-50 rounded-2xl border border-slate-200">
          <div className="bg-red-100 text-red-600 w-12 h-12 rounded-full flex items-center justify-center mb-3">
            <ShieldX className="w-6 h-6" />
          </div>
          <p className="font-bold text-slate-900">Certificado inválido</p>
          <p className="text-sm text-slate-500 mt-1">Verifica que el código sea correcto.</p>
        </div>
      )}

  {resultado && (
    <div className="border border-emerald-200 bg-emerald-50 rounded-2xl p-6 shadow-sm">
      <div className="flex items-center gap-3 mb-5 pb-4 border-b border-emerald-200/60">
        <div className="w-10 h-10 bg-emerald-100 rounded-full flex items-center justify-center">
          <ShieldCheck className="w-5 h-5 text-emerald-600" />
        </div>
        <div>
          <p className="font-bold text-emerald-900 text-sm">Certificado Auténtico</p>
          <p className="text-xs text-emerald-600 font-medium">Validado por EMINENT</p>
        </div>
      </div>
      <div className="space-y-3 text-sm text-slate-700">
        <div className="flex justify-between">
          <span className="text-slate-500">Participante:</span>
          <span className="font-medium">{resultado.participanteNombre}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-slate-500">Evento:</span>
          <span className="font-medium">{resultado.eventoNombre}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-slate-500">Duración:</span>
          <span className="font-medium">{resultado.duracionHoras} horas</span>
        </div>
        <div className="flex justify-between">
          <span className="text-slate-500">Código:</span>
          <span className="font-mono text-slate-500">{resultado.codigoUnico}</span>
        </div>
        <p className="text-xs text-slate-400 pt-3 border-t border-emerald-200/50 mt-3">
          Emitido el {resultado.fechaEmision
            ? format(new Date(resultado.fechaEmision), "d 'de' MMMM 'de' yyyy", { locale: es })
            : '-'}
        </p>
      </div>
    </div>
  )}

      <button onClick={() => navigate('/')} className="w-full mt-6 py-3 border border-slate-200 text-slate-600 font-medium rounded-xl hover:bg-slate-50 flex items-center justify-center gap-2 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Volver al inicio
      </button>
    </AuthLayout>
  );
}