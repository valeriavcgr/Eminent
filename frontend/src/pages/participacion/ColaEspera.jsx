import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { consultarColaEspera, promoverInscripcion } from '../../services/participacionService';
import { toast } from 'sonner';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { ArrowLeft, Clock3, IdCard, ArrowUpCircle, Users2 } from 'lucide-react';

export default function ColaEspera() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [cola, setCola] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [promoviendoId, setPromoviendoId] = useState(null);

  useEffect(() => {
    cargarCola();
  }, [id]);

  const cargarCola = async () => {
    try {
      setIsLoading(true);
      const datos = await consultarColaEspera(id);
      setCola(datos || []);
    } catch (err) {
      toast.error('Error al cargar la cola de espera');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePromover = async (inscripcionId) => {
    setPromoviendoId(inscripcionId);
    try {
      await promoverInscripcion(inscripcionId);
      toast.success('Inscripción promovida correctamente');
      cargarCola();
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al promover la inscripción');
    } finally {
      setPromoviendoId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Lista de espera</h1>
          <p className="text-sm text-slate-500 mt-1">Evento #{id} — promueve participantes cuando se libere cupo.</p>
        </div>
        <Button variant="secondary" icon={ArrowLeft} onClick={() => navigate('/eventos')}>
          Volver a Eventos
        </Button>
      </div>

      <Card>
        <CardHeader className="bg-slate-50/50">
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <Users2 className="w-4 h-4 text-slate-500" />
            Participantes en espera
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-4">Posición</th>
                  <th className="px-6 py-4">Participante</th>
                  <th className="px-6 py-4">Documento</th>
                  <th className="px-6 py-4">Fecha de inscripción</th>
                  <th className="px-6 py-4 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {isLoading ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-8 text-center text-slate-500">Cargando cola de espera...</td>
                  </tr>
                ) : cola.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-10 text-center text-slate-400">
                      <Clock3 className="w-8 h-8 mx-auto mb-2 text-slate-300" />
                      No hay participantes en lista de espera.
                    </td>
                  </tr>
                ) : (
                  cola.map((c) => (
                    <tr key={c.inscripcionId} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4">
                        <Badge variant="warning">#{c.posicion}</Badge>
                      </td>
                      <td className="px-6 py-4 font-medium text-slate-900">{c.participanteNombre}</td>
                      <td className="px-6 py-4">
                        <div className="flex items-center text-slate-600">
                          <IdCard className="w-4 h-4 mr-2 text-slate-400" />
                          {c.participanteDocumento}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-slate-600">
                        {c.fechaInscripcion
                          ? format(new Date(c.fechaInscripcion), "d MMM, yyyy - HH:mm", { locale: es })
                          : '-'}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <Button
                          size="sm"
                          icon={ArrowUpCircle}
                          disabled={promoviendoId === c.inscripcionId}
                          onClick={() => handlePromover(c.inscripcionId)}
                          className="bg-emerald-600 hover:bg-emerald-700 text-white"
                        >
                          {promoviendoId === c.inscripcionId ? 'Promoviendo...' : 'Promover'}
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}