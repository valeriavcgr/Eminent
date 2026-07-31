import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { listarEventosMonitor } from '../../services/eventoService';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { 
  Calendar as CalendarIcon, 
  MapPin, 
  Clock, 
  Users, 
  QrCode, 
  CheckCircle2
} from 'lucide-react';
import { toast } from 'sonner';

export default function MisEventos() {
  const [eventos, setEventos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    cargarEventos();
  }, []);

  const cargarEventos = async () => {
    try {
      setIsLoading(true);
      const datos = await listarEventosMonitor();
      setEventos(datos || []);
    } catch (error) {
      toast.error('Error al cargar los eventos asignados');
    } finally {
      setIsLoading(false);
    }
  };

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'PROGRAMADO': return 'info';
      case 'EN CURSO':
      case 'EN_CURSO': return 'success';
      case 'FINALIZADO': return 'default';
      case 'CANCELADO': return 'danger';
      default: return 'default';
    }
  };

  const getTipoColor = (tipo) => {
    switch (tipo) {
      case 'TALLER': return 'bg-orange-100 text-orange-700';
      case 'CAPACITACION': return 'bg-amber-100 text-amber-700';
      case 'TORNEO': return 'bg-yellow-100 text-yellow-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between items-start gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Mis Eventos Asignados</h1>
          <p className="text-sm text-slate-500 mt-1">Supervisa y controla la asistencia de los eventos a tu cargo.</p>
        </div>
      </div>

      <Card>
        <CardHeader className="bg-orange-50/50">
          <CardTitle className="text-base font-semibold text-orange-900 flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-orange-600" />
            Tus asignaciones activas
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-4">Información del Evento</th>
                  <th className="px-6 py-4">Modalidad</th>
                  <th className="px-6 py-4">Fecha de Inicio</th>
                  <th className="px-6 py-4">Estado</th>
                  <th className="px-6 py-4 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {isLoading ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-8 text-center text-slate-500">
                      Cargando tus eventos...
                    </td>
                  </tr>
                ) : eventos.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-8 text-center text-slate-500">
                      No tienes eventos asignados en este momento.
                    </td>
                  </tr>
                ) : (
                  eventos.map((e) => (
                    <tr key={e.id} className="hover:bg-orange-50/30 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex flex-col">
                          <span className="font-medium text-slate-900">{e.nombre}</span>
                          <div className="flex items-center mt-1">
                            <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${getTipoColor(e.tipo)}`}>
                              {e.tipo}
                            </span>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center text-slate-600">
                          {e.modalidad === 'VIRTUAL' ? <Clock className="w-4 h-4 mr-2" /> : <MapPin className="w-4 h-4 mr-2" />}
                          <span className="capitalize">{e.modalidad ? e.modalidad.toLowerCase() : ''}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center text-slate-600">
                          <CalendarIcon className="w-4 h-4 mr-2" />
                          <span>{e.fechaInicio ? format(new Date(e.fechaInicio), "d MMM, yyyy - HH:mm", { locale: es }) : 'Por definir'}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <Badge variant={getEstadoColor(e.estado)}>
                          {e.estado}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex justify-end gap-2">
                          {/* Ver Lista: Visible salvo que esté CANCELADO */}
                          {e.estado !== 'CANCELADO' && (
                            <Button 
                              variant="secondary" 
                              size="sm" 
                              className="text-slate-700 bg-white border-slate-200 hover:bg-slate-50"
                              icon={Users}
                              onClick={() => navigate(`/eventos/${e.id}/asistencia`)}
                            >
                              Ver Lista
                            </Button>
                          )}

                          {/* Escanear QR: Visible en PROGRAMADO, EN CURSO y EN_CURSO */}
                          {(e.estado === 'PROGRAMADO' || e.estado === 'EN CURSO' || e.estado === 'EN_CURSO') && (
                            <Button 
                              size="sm" 
                              className="bg-orange-600 hover:bg-orange-700 text-white"
                              icon={QrCode}
                              onClick={() => navigate(`/eventos/${e.id}/escaner`)}
                            >
                              Escanear QR
                            </Button>
                          )}
                        </div>
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