import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { inscribir } from '../../services/participacionService';
import { toast } from 'sonner';
import { QRCodeCanvas } from 'qrcode.react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { User, IdCard, Mail, Phone, CheckCircle2, Clock, ArrowLeft, Download, Ticket } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';

export default function FormularioInscripcion() {
  const { eventoId } = useParams();
  const [form, setForm] = useState({ nombre: '', apellido: '', documento: '', correo: '', telefono: '' });
  const [resultado, setResultado] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);
    try {
      const data = await inscribir({ ...form, eventoId: Number(eventoId) });
      setResultado(data);
      toast.success('Inscripción registrada correctamente');
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al inscribirse');
    } finally {
      setEnviando(false);
    }
  };

  const handleDescargarQr = () => {
    const canvas = document.querySelector('#qr-descargable canvas');
    if (!canvas) return;
    const url = canvas.toDataURL('image/png');
    const link = document.createElement('a');
    link.href = url;
    link.download = 'mi-qr-inscripcion.png';
    link.click();
  };

  if (resultado) {
    return (
      <AuthLayout
        title="¡Inscripción Recibida!"
        subtitle="Tu solicitud ha sido procesada. Guarda tu código QR para el día del evento."
        imageSrc="https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=2000&q=80"
      >
        <div className="text-center py-6">
          {resultado.estado === 'ACTIVA' ? (
            <>
              <div className="bg-emerald-100 text-emerald-600 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle2 className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-bold text-slate-900 mb-2">¡Inscripción exitosa!</h2>
              <p className="text-sm text-slate-500 mb-6">Presenta este código QR el día del evento.</p>

              <div className="flex justify-center mb-6">
                <div id="qr-descargable" className="p-4 bg-slate-50 border border-slate-200 rounded-2xl shadow-sm">
                  <QRCodeCanvas value={resultado.codigoQr} size={240} includeMargin={true} />
                </div>
              </div>

              <Button
                variant="secondary"
                icon={Download}
                onClick={handleDescargarQr}
                className="w-full mb-3 bg-blue-600 hover:bg-blue-700 text-gray-700"
              >
                Descargar mi código QR
              </Button>
            </>
          ) : (
            <>
              <div className="bg-orange-100 text-orange-600 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Clock className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-bold text-slate-900 mb-2">Estás en lista de espera</h2>
              <p className="text-slate-600">
                Posición actual: <span className="font-bold text-xl text-slate-900 ml-1">#{resultado.posicion}</span>
              </p>
              <p className="text-sm text-slate-500 mt-4 mb-6">Revisa en la sección de Consultar Inscripción para ver si ya fuiste promovido</p>
            </>
          )}
          <Button variant="ghost" icon={ArrowLeft} onClick={() => navigate('/')} className="w-full mt-2 rounded-xl border border-slate-200">
            Volver al inicio
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      title="Regístrate al Evento"
      subtitle="Completa tus datos cuidadosamente para reservar tu cupo en este evento."
      imageSrc="https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=2000&q=80"
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div className="relative">
            <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              name="nombre" placeholder="Nombre" value={form.nombre} onChange={handleChange}
              className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
              required
            />
          </div>
          <div className="relative">
            <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              name="apellido" placeholder="Apellido" value={form.apellido} onChange={handleChange}
              className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
              required
            />
          </div>
        </div>

        <div className="relative">
          <IdCard className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            name="documento" placeholder="Número de documento (mín. 8 dígitos)" value={form.documento}
            onChange={(e) => setForm({ ...form, documento: e.target.value.replace(/\D/g, '') })}
            pattern="[0-9]{8,15}" minLength={8} inputMode="numeric"
            title="El documento debe ser numérico, mínimo 8 dígitos"
            className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
            required
          />
        </div>

        <div className="relative">
          <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            name="correo" type="email" placeholder="Correo electrónico" value={form.correo} onChange={handleChange}
            className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
            required
          />
        </div>

        <div className="relative">
          <Phone className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            name="telefono" placeholder="Teléfono (opcional)" value={form.telefono} onChange={handleChange}
            className="w-full pl-10 pr-3 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600 text-sm bg-slate-50 font-medium"
          />
        </div>

        <div className="pt-2">
          <Button type="submit" disabled={enviando} className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-xl shadow-sm text-sm font-bold">
            {enviando ? 'Enviando...' : 'Confirmar inscripción'}
          </Button>
        </div>
      </form>
    </AuthLayout>
  );
}