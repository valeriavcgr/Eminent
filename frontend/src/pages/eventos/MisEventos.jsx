import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { listarEventosMonitor } from '../../services/eventoService';
import { obtenerComentariosEvento } from '../../services/encuestaService';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import {
  Calendar as CalendarIcon,
  MapPin,
  Clock,
  Users,
  QrCode,
  CheckCircle2,
  Star
} from 'lucide-react';
import { toast } from 'sonner';
import Pagination from '../../components/Pagination';

export default function MisEventos() {
  const [eventos, setEventos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;
  const navigate = useNavigate();

  // --- Modal de encuesta ---
  const [modalAbierto, setModalAbierto] = useState(false);
  const [eventoSeleccionado, setEventoSeleccionado] = useState(null);
  const [comentarios, setComentarios] = useState([]);
  const [cargandoComentarios, setCargandoComentarios] = useState(false);

  useEffect(() => {
    cargarEventos();
  }, []);

  const abrirComentarios = async (evento) => {
    setEventoSeleccionado(evento);
    setModalAbierto(true);
    setCargandoComentarios(true);
    try {
      const datos = await obtenerComentariosEvento(evento.id);
      setComentarios(datos);
    } catch (error) {
      toast.error('Error al cargar los resultados de la encuesta');
      setComentarios([]);
    } finally {
      setCargandoComentarios(false);
    }
  };

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

  const totalPaginas = Math.ceil(eventos.length / FILAS_POR_PAGINA);
  const eventosPagina = eventos.slice(
    (paginaActual - 1) * FILAS_POR_PAGINA,
    paginaActual * FILAS_POR_PAGINA
  );

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'PROGRAMADO': return 'info';
      case 'EN CURSO': return 'success';
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
                ) : eventosPagina.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-8 text-center text-slate-500">
                      No tienes eventos asignados en este momento.
                    </td>
                  </tr>
                ) : (
                  eventosPagina.map((e) => (
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
                          <span className="capitalize">{e.modalidad.toLowerCase()}</span>
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
                          {e.estado !== 'CANCELADO' && e.estado !== 'FINALIZADO' && (
                            <>
                              <Button
                                variant="secondary"
                                size="sm"
                                className="text-slate-700 bg-white border-slate-200 hover:bg-slate-50"
                                icon={Users}
                                onClick={() => navigate(`/eventos/${e.id}/asistencia`)}
                              >
                                Ver Lista
                              </Button>
                              <Button
                                size="sm"
                                className="bg-orange-600 hover:bg-orange-700 text-white"
                                icon={QrCode}
                                onClick={() => navigate(`/eventos/${e.id}/escaner`)}
                              >
                                Escanear QR
                              </Button>
                            </>
                          )}
                          {e.estado === 'FINALIZADO' && (
                            <>
                              <Button
                                variant="secondary"
                                size="sm"
                                className="text-slate-700 bg-white border-slate-200 hover:bg-slate-50"
                                icon={Users}
                                onClick={() => navigate(`/eventos/${e.id}/asistencia`)}
                              >
                                Ver Asistencia
                              </Button>
                              <Button
                                size="sm"
                                variant="secondary"
                                className="text-amber-700 bg-amber-50 border-amber-200 hover:bg-amber-100"
                                icon={Star}
                                onClick={() => abrirComentarios(e)}
                              >
                                Ver Encuesta
                              </Button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <Pagination
            paginaActual={paginaActual}
            totalPaginas={totalPaginas}
            onCambiarPagina={setPaginaActual}
          />
        </CardContent>
      </Card>

      {/* Modal de resultados de encuesta */}
      <Modal
        isOpen={modalAbierto}
        onClose={() => setModalAbierto(false)}
        title={eventoSeleccionado ? `Encuesta — ${eventoSeleccionado.nombre}` : 'Encuesta'}
      >
        {cargandoComentarios ? (
          <p className="text-sm text-slate-500 text-center py-6">Cargando resultados...</p>
        ) : comentarios.length === 0 ? (
          <p className="text-sm text-slate-400 text-center py-6">Nadie ha respondido la encuesta de este evento todavía.</p>
        ) : (
          <div className="space-y-4 max-h-96 overflow-y-auto">
            {comentarios.map((c, idx) => (
              <div key={idx} className="border-b border-slate-100 pb-3 last:border-0">
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium text-sm text-slate-800">{c.participanteNombre}</span>
                  <div className="flex items-center gap-0.5">
                    {[1, 2, 3, 4, 5].map((n) => (
                      <Star key={n} className={`w-3.5 h-3.5 ${n <= c.calificacion ? 'fill-amber-400 text-amber-400' : 'fill-slate-100 text-slate-200'}`} />
                    ))}
                  </div>
                </div>
                {c.comentario && <p className="text-sm text-slate-600">{c.comentario}</p>}
                <p className="text-xs text-slate-400 mt-1">
                  {c.fechaCreacion ? format(new Date(c.fechaCreacion), "d MMM yyyy - HH:mm", { locale: es }) : ''}
                </p>
              </div>
            ))}
          </div>
        )}
      </Modal>
    </div>
  );
}