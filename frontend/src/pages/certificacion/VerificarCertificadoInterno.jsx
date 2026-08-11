import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { verificarCertificado } from '../../services/certificacionService';
import { toast } from 'sonner';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import {
  ShieldCheck, ShieldX, Search, User, Calendar, CalendarDays, Clock, Hash, QrCode, X, Share2
} from 'lucide-react';

function compartirEnLinkedIn(codigoUnico) {
  const urlVerificacion = `${window.location.origin}/certificados/verificar?codigo=${codigoUnico}`;
  const urlLinkedIn = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(urlVerificacion)}`;
  window.open(urlLinkedIn, '_blank', 'width=600,height=600');
}

export default function VerificarCertificadoInterno() {
  const [searchParams] = useSearchParams();
  const [codigo, setCodigo] = useState(searchParams.get('codigo') || '');
  const [resultado, setResultado] = useState(null);
  const [noEncontrado, setNoEncontrado] = useState(false);
  const [cargando, setCargando] = useState(false);
  const [escaneando, setEscaneando] = useState(false);

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

  useEffect(() => {
    if (!escaneando) return;
    const scanner = new Html5QrcodeScanner('lector-qr-interno', { fps: 10, qrbox: 250 }, false);
    scanner.render(
      (texto) => {
        scanner.clear().catch(() => {});
        setEscaneando(false);
        let codigoExtraido = texto;
        try {
          const url = new URL(texto);
          const param = url.searchParams.get('codigo');
          if (param) codigoExtraido = param;
        } catch {
          // no era una URL, se usa el texto tal cual escaneado
        }
        setCodigo(codigoExtraido);
        verificar(codigoExtraido);
      },
      () => {}
    );
    return () => {
      scanner.clear().catch(() => {});
    };
  }, [escaneando]);

  const handleSubmit = (e) => {
    e.preventDefault();
    verificar(codigo);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Verificar certificado</h1>
        <p className="text-sm text-slate-500 mt-1">Confirma la autenticidad de un certificado desde el panel interno.</p>
      </div>

      <Card>
        <CardHeader className="bg-slate-50/50">
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-blue-600" /> Buscar certificado
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="flex gap-2 mb-4">
            <div className="relative flex-1">
              <Hash className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                value={codigo}
                onChange={(e) => setCodigo(e.target.value)}
                placeholder="Código del certificado"
                className="w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 text-sm"
                required
              />
            </div>
            <Button type="submit" icon={Search} disabled={cargando} className="bg-blue-600 hover:bg-blue-700 text-white">
              {cargando ? '...' : 'Verificar'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              icon={escaneando ? X : QrCode}
              onClick={() => setEscaneando(!escaneando)}
            >
              {escaneando ? 'Cerrar' : 'Escanear QR'}
            </Button>
          </form>

          {escaneando && (
            <div id="lector-qr-interno" className="mb-4 rounded-lg overflow-hidden border border-slate-200"></div>
          )}

          {noEncontrado && (
            <div className="flex flex-col items-center text-center py-6">
              <div className="bg-red-100 text-red-600 w-14 h-14 rounded-full flex items-center justify-center mb-3">
                <ShieldX className="w-7 h-7" />
              </div>
              <p className="font-semibold text-slate-800">Certificado no encontrado o inválido</p>
            </div>
          )}

          {resultado && (
            <div className="border border-emerald-200 bg-emerald-50 rounded-xl p-5">
              <div className="flex items-center gap-2 mb-4">
                <ShieldCheck className="w-5 h-5 text-emerald-600" />
                <Badge variant="success">Certificado válido</Badge>
              </div>
              <div className="space-y-2 text-sm text-slate-700">
                <div className="flex items-center gap-2"><User className="w-4 h-4 text-slate-400" /> {resultado.participanteNombre}</div>
                <div className="flex items-center gap-2"><Calendar className="w-4 h-4 text-slate-400" /> {resultado.eventoNombre}</div>
                <div className="flex items-center gap-2"><CalendarDays className="w-4 h-4 text-slate-400" /> {resultado.fechasEvento}</div>
                <div className="flex items-center gap-2"><Clock className="w-4 h-4 text-slate-400" /> {resultado.duracionHoras} horas</div>
                <div className="flex items-center gap-2"><Hash className="w-4 h-4 text-slate-400" /> {resultado.codigoUnico}</div>
              </div>

              <button
                onClick={() => compartirEnLinkedIn(resultado.codigoUnico)}
                className="w-full mt-4 py-2 bg-[#0A66C2] hover:bg-[#004182] text-white rounded-lg text-sm font-semibold flex items-center justify-center gap-2 transition-colors"
              >
                <Share2 className="w-4 h-4" /> Compartir en LinkedIn
              </button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}