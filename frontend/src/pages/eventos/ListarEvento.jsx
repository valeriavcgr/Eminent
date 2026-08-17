import { useState, useEffect } from 'react';
import { listarEventos, cancelarEvento, asignarMonitor, listarMonitoresEvento } from '../../services/eventoService';
import { listarUsuarios } from '../../services/usuarioService';
import { listarParticipantesEvento } from '../../services/asistenciaService';
import { obtenerComentariosEvento, obtenerPromedioEvento } from '../../services/encuestaService';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import {
  Search,
  Plus,
  Edit2,
  Eye,
  Calendar as CalendarIcon,
  CalendarDays,
  Users,
  Ban,
  ClipboardList,
  Upload,
  MapPin,
  Clock,
  AlertTriangle,
  UserPlus,
  Star,
  MessageCircle,
  ListFilter,
  ChevronDown,
  ChevronUp,
  X,
  FileText,
  Shield
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { format, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';
import Pagination from '../../components/Pagination';

export default function ListarEvento() {
  const [eventos, setEventos] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filtros, setFiltros] = useState({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });
  const [filtrosVisibles, setFiltrosVisibles] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;

  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [eventoToCancel, setEventoToCancel] = useState(null);

  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [eventoToAssign, setEventoToAssign] = useState(null);
  const [monitores, setMonitores] = useState([]);
  const [selectedMonitor, setSelectedMonitor] = useState('');
  const [isAssigning, setIsAssigning] = useState(false);

  const [detalleModalOpen, setDetalleModalOpen] = useState(false);
  const [eventoDetalle, setEventoDetalle] = useState(null);
  const [monitoresDetalle, setMonitoresDetalle] = useState([]);
  const [cargandoDetalle, setCargandoDetalle] = useState(false);

  const [encuestaModalOpen, setEncuestaModalOpen] = useState(false);
  const [eventoEncuesta, setEventoEncuesta] = useState(null);
  const [resumenEncuesta, setResumenEncuesta] = useState(null);
  const [comentariosEncuesta, setComentariosEncuesta] = useState([]);
  const [cargandoEncuesta, setCargandoEncuesta] = useState(false);
  const [filtroCalificacion, setFiltroCalificacion] = useState(null);
  const [paginaComentarios, setPaginaComentarios] = useState(1);
  const [totalPaginasComentarios, setTotalPaginasComentarios] = useState(1);
  const [totalComentarios, setTotalComentarios] = useState(0);
  const [promedioSatisfaccion, setPromedioSatisfaccion] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    cargarEventos();
  }, [filtros]);

  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm, filtros]);

  const cargarEventos = async () => {
    try {
      setIsLoading(true);
      const params = {};
      Object.entries(filtros).forEach(([k, v]) => { if (v) params[k] = v; });
      const datos = await listarEventos(params);
      setEventos(datos || []);
    } catch (error) {
      toast.error('Error al cargar la lista de eventos');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFiltro = (campo, valor) => setFiltros({ ...filtros, [campo]: valor });
  const limpiarFiltros = () => setFiltros({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });
  const hayFiltrosActivos = Object.values(filtros).some((v) => v);

  const openCancelModal = (evento) => {
    setEventoToCancel(evento);
    setCancelModalOpen(true);
  };

  const confirmarCancelacion = async () => {
    if (!eventoToCancel) return;
    try {
      await cancelarEvento(eventoToCancel.id);
      toast.success('Evento cancelado exitosamente');
      setCancelModalOpen(false);
      setEventoToCancel(null);
      cargarEventos();
    } catch (error) {
      toast.error(error.response?.data?.mensaje || 'Error al cancelar el evento');
    }
  };

  const openAssignModal = async (evento) => {
    setEventoToAssign(evento);
    setAssignModalOpen(true);
    try {
      const [data, asignados] = await Promise.all([
        listarUsuarios('MONITOR'),
        listarMonitoresEvento(evento.id),
      ]);
      const idsAsignados = new Set((asignados || []).map((m) => m.monitorId));
      const disponibles = (data || []).filter(
        (m) => m.estado === 'ACTIVO' && !idsAsignados.has(m.id)
      );
      setMonitores(disponibles);
    } catch (error) {
      toast.error('Error al cargar la lista de monitores');
    }
  };

  const handleAssignMonitor = async () => {
    if (!selectedMonitor || !eventoToAssign) return;
    setIsAssigning(true);
    try {
      await asignarMonitor(eventoToAssign.id, selectedMonitor);
      toast.success('Monitor asignado exitosamente');
      setAssignModalOpen(false);
      setEventoToAssign(null);
      setSelectedMonitor('');
    } catch (error) {
      toast.error(error.response?.data?.mensaje || 'Error al asignar monitor');
    } finally {
      setIsAssigning(false);
    }
  };

  const abrirDetalleModal = async (evento) => {
    setEventoDetalle(evento);
    setDetalleModalOpen(true);
    setCargandoDetalle(true);
    try {
      const data = await listarMonitoresEvento(evento.id);
      setMonitoresDetalle(data || []);
    } catch (error) {
      setMonitoresDetalle([]);
    } finally {
      setCargandoDetalle(false);
    }
  };

  const cargarComentariosEncuesta = async (eventoId, calificacion, pagina) => {
    try {
      const datos = await obtenerComentariosEvento(eventoId, { calificacion: calificacion || undefined, page: pagina - 1 });
      setComentariosEncuesta(datos.content || []);
      setTotalPaginasComentarios(datos.totalPages || 1);
      setTotalComentarios(datos.totalElements || 0);
    } catch (error) {
      toast.error('Error al cargar los comentarios de la encuesta');
      setComentariosEncuesta([]);
      setTotalPaginasComentarios(1);
      setTotalComentarios(0);
    }
  };

  const abrirResultadosEncuesta = async (evento) => {
    setEventoEncuesta(evento);
    setEncuestaModalOpen(true);
    setCargandoEncuesta(true);
    setFiltroCalificacion(null);
    setPaginaComentarios(1);
    try {
      const [asistencia, promedio] = await Promise.all([
        listarParticipantesEvento(evento.id),
        obtenerPromedioEvento(evento.id),
      ]);
      setResumenEncuesta(asistencia.resumen);
      setPromedioSatisfaccion(promedio);
      await cargarComentariosEncuesta(evento.id, null, 1);
    } catch (error) {
      toast.error('Error al cargar los resultados de la encuesta');
      setResumenEncuesta(null);
      setPromedioSatisfaccion(null);
      setComentariosEncuesta([]);
    } finally {
      setCargandoEncuesta(false);
    }
  };

  const cambiarFiltroCalificacion = (calificacion) => {
    setFiltroCalificacion(calificacion);
    setPaginaComentarios(1);
    cargarComentariosEncuesta(eventoEncuesta.id, calificacion, 1);
  };

  const cambiarPaginaComentarios = (pagina) => {
    setPaginaComentarios(pagina);
    cargarComentariosEncuesta(eventoEncuesta.id, filtroCalificacion, pagina);
  };

  const filteredEventos = eventos.filter(e =>
    e.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalPaginas = Math.ceil(filteredEventos.length / FILAS_POR_PAGINA);
  const eventosPagina = filteredEventos.slice(
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
      case 'TALLER': return 'bg-blue-100 text-blue-700';
      case 'CAPACITACION': return 'bg-green-100 text-green-700';
      case 'TORNEO': return 'bg-purple-100 text-purple-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Gestión de Eventos</h1>
          <p className="text-sm text-slate-500 mt-1">Crea y administra talleres, capacitaciones y torneos</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setFiltrosVisibles(!filtrosVisibles)}
            className={`flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg border transition-all ${filtrosVisibles
                ? 'bg-slate-900 text-white border-slate-900'
                : 'bg-white text-slate-700 border-slate-300 hover:bg-slate-50'
              }`}
          >
            <ListFilter className="w-4 h-4" />
            Filtros
            {filtrosVisibles ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
          <Button onClick={() => navigate('/eventos/nuevo')} icon={Plus} className="bg-emerald-600 hover:bg-emerald-700 text-white">
            Nuevo Evento
          </Button>
        </div>
      </div>

      <div
        className={`transition-all duration-300 ease-in-out overflow-hidden ${filtrosVisibles ? 'max-h-[400px] opacity-100' : 'max-h-0 opacity-0 pointer-events-none'
          }`}
      >
        <Card className="border border-slate-200/80 shadow-md">
          <CardHeader className="bg-slate-50/50 flex flex-row items-center justify-between py-3">
            <CardTitle className="text-sm font-bold flex items-center gap-2 text-slate-700">
              Filtros Avanzados
            </CardTitle>
            {hayFiltrosActivos && (
              <button
                onClick={limpiarFiltros}
                className="flex items-center gap-1.5 text-xs font-semibold text-rose-500 hover:text-rose-700 transition-colors"
              >
                <X className="w-3.5 h-3.5" /> Limpiar filtros
              </button>
            )}
          </CardHeader>
          <CardContent className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 py-4 bg-white">
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Tipo de Evento</label>
              <select
                value={filtros.tipo}
                onChange={(e) => handleFiltro('tipo', e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-shadow"
              >
                <option value="">Todos los tipos</option>
                <option value="TALLER">Taller</option>
                <option value="CAPACITACION">Capacitación</option>
                <option value="TORNEO">Torneo</option>
              </select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Modalidad</label>
              <select
                value={filtros.modalidad}
                onChange={(e) => handleFiltro('modalidad', e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-shadow"
              >
                <option value="">Todas las modalidades</option>
                <option value="PRESENCIAL">Presencial</option>
                <option value="VIRTUAL">Virtual</option>
              </select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Estado del Evento</label>
              <select
                value={filtros.estado}
                onChange={(e) => handleFiltro('estado', e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-shadow"
              >
                <option value="">Todos los estados</option>
                <option value="PROGRAMADO">Programado</option>
                <option value="EN_CURSO">En curso</option>
                <option value="FINALIZADO">Finalizado</option>
                <option value="CANCELADO">Cancelado</option>
              </select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Desde</label>
              <input
                type="date"
                value={filtros.fechaDesde ? filtros.fechaDesde.split('T')[0] : ''}
                onChange={(e) => handleFiltro('fechaDesde', e.target.value ? `${e.target.value}T00:00:00` : '')}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-shadow"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Hasta</label>
              <input
                type="date"
                value={filtros.fechaHasta ? filtros.fechaHasta.split('T')[0] : ''}
                onChange={(e) => handleFiltro('fechaHasta', e.target.value ? `${e.target.value}T23:59:59` : '')}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-shadow"
              />
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-col sm:flex-row justify-between items-center gap-4 bg-slate-50/50">
          <CardTitle className="text-base font-semibold">Catálogo de Eventos</CardTitle>
          <div className="relative w-full sm:w-72">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-slate-400" />
            </div>
            <input
              type="text"
              placeholder="Buscar evento por nombre..."
              className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-4">Información del Evento</th>
                  <th className="px-6 py-4">Modalidad</th>
                  <th className="px-6 py-4">Fecha de Inicio</th>
                  <th className="px-6 py-4">Fecha de Fin</th>
                  <th className="px-6 py-4">Cupos</th>
                  <th className="px-6 py-4">Estado</th>
                  <th className="px-6 py-4 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {isLoading ? (
                  <tr>
                    <td colSpan="7" className="px-6 py-8 text-center text-slate-500">
                      Cargando eventos...
                    </td>
                  </tr>
                ) : eventosPagina.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="px-6 py-8 text-center text-slate-500">
                      No se encontraron eventos
                    </td>
                  </tr>
                ) : (
                  eventosPagina.map((e) => (
                    <tr key={e.id} className="hover:bg-slate-50 transition-colors">
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
                        <div className="flex items-center text-slate-600">
                          <CalendarDays className="w-4 h-4 mr-2" />
                          <span>{e.fechaFin ? format(new Date(e.fechaFin), "d MMM, yyyy - HH:mm", { locale: es }) : 'Por definir'}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center text-slate-600">
                          <Users className="w-4 h-4 mr-2" />
                          <span>{e.aforo}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <Badge variant={getEstadoColor(e.estado)}>
                          {e.estado}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            icon={Eye}
                            onClick={() => abrirDetalleModal(e)}
                            title="Ver detalle del evento"
                          />

                          {e.estado !== 'CANCELADO' && e.estado !== 'FINALIZADO' && (
                            <>
                              <Button
                                variant="ghost"
                                size="sm"
                                icon={Edit2}
                                onClick={() => navigate(`/eventos/editar/${e.id}`)}
                                title="Editar evento"
                              />
                              <Button
                                variant="ghost"
                                size="sm"
                                icon={ClipboardList}
                                onClick={() => navigate(`/eventos/${e.id}/cola-espera`)}
                                title="Cola de Espera"
                              />
                              <Button
                                variant="ghost"
                                size="sm"
                                icon={Upload}
                                onClick={() => navigate(`/eventos/${e.id}/importar-csv`)}
                                title="Importar CSV"
                              />
                              <Button
                                variant="ghost"
                                size="sm"
                                icon={UserPlus}
                                onClick={() => openAssignModal(e)}
                                title="Asignar Monitor"
                              />
                            </>
                          )}

                          {e.estado !== 'CANCELADO' && (
                            <Button
                              variant="ghost"
                              size="sm"
                              icon={Users}
                              onClick={() => navigate(`/eventos/${e.id}/asistencia`)}
                              title="Control de Asistencia"
                            />
                          )}

                          {e.estado === 'PROGRAMADO' && (
                            <Button
                              variant="ghost"
                              size="sm"
                              className="text-red-600 hover:text-red-700 hover:bg-red-50"
                              icon={Ban}
                              onClick={() => openCancelModal(e)}
                              title="Cancelar Evento"
                            />
                          )}

                          {e.estado === 'FINALIZADO' && (
                            <Button
                              variant="ghost"
                              size="sm"
                              className="text-amber-600 hover:text-amber-700 hover:bg-amber-50"
                              icon={Star}
                              onClick={() => abrirResultadosEncuesta(e)}
                              title="Resultados de Encuesta"
                            />
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

      <Modal
        isOpen={cancelModalOpen}
        onClose={() => setCancelModalOpen(false)}
        title="Cancelar Evento"
      >
        <div className="flex flex-col items-center text-center space-y-4">
          <div className="bg-red-100 p-3 rounded-full text-red-600">
            <AlertTriangle className="w-8 h-8" />
          </div>
          <p className="text-slate-700">
            ¿Estás seguro de que deseas cancelar el evento <strong>{eventoToCancel?.nombre}</strong>?
          </p>
          <p className="text-sm text-slate-500">
            Esta acción no se puede deshacer. Solo puedes cancelar un evento sin inscripciones registradas
          </p>
          <div className="flex w-full gap-3 mt-4">
            <Button
              variant="secondary"
              className="flex-1"
              onClick={() => setCancelModalOpen(false)}
            >
              Cerrar
            </Button>
            <Button
              className="flex-1 bg-red-600 hover:bg-red-700 text-white"
              onClick={confirmarCancelacion}
            >
              Sí, Cancelar
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={detalleModalOpen}
        onClose={() => setDetalleModalOpen(false)}
        title="Detalle del Evento"
      >
        {eventoDetalle && (
          <div className="space-y-5 max-h-[70vh] overflow-y-auto pr-1">
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <h3 className="text-lg font-bold text-slate-900">{eventoDetalle.nombre}</h3>
                <Badge variant={getEstadoColor(eventoDetalle.estado)}>{eventoDetalle.estado}</Badge>
              </div>
              <div className="flex items-center gap-2 mt-1.5 flex-wrap">
                <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${getTipoColor(eventoDetalle.tipo)}`}>
                  {eventoDetalle.tipo}
                </span>
                <span className="flex items-center gap-1 text-xs text-slate-500">
                  {eventoDetalle.modalidad === 'VIRTUAL' ? <Clock className="w-3.5 h-3.5" /> : <MapPin className="w-3.5 h-3.5" />}
                  <span className="capitalize">{eventoDetalle.modalidad?.toLowerCase()}</span>
                </span>
              </div>
              {eventoDetalle.descripcion && (
                <p className="text-sm text-slate-600 mt-3 flex gap-2">
                  <FileText className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
                  {eventoDetalle.descripcion}
                </p>
              )}
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div className="bg-slate-50 rounded-lg p-3 text-center border border-slate-100">
                <p className="text-xs font-medium text-slate-500">Aforo</p>
                <p className="text-lg font-bold text-slate-800">{eventoDetalle.aforo}</p>
              </div>
              <div className="bg-slate-50 rounded-lg p-3 text-center border border-slate-100">
                <p className="text-xs font-medium text-slate-500">Inscritos</p>
                <p className="text-lg font-bold text-slate-800">{eventoDetalle.inscritos ?? 0}</p>
              </div>
              <div className="bg-slate-50 rounded-lg p-3 text-center border border-slate-100">
                <p className="text-xs font-medium text-slate-500">Jornadas</p>
                <p className="text-lg font-bold text-slate-800">{eventoDetalle.jornadas?.length ?? 0}</p>
              </div>
            </div>

            <div>
              <h4 className="text-sm font-semibold text-slate-700 flex items-center gap-1.5 mb-2">
                <CalendarDays className="w-4 h-4" /> Jornadas del Evento
              </h4>
              {eventoDetalle.jornadas && eventoDetalle.jornadas.length > 0 ? (
                <div className="border border-slate-200 rounded-lg overflow-hidden divide-y divide-slate-100">
                  {eventoDetalle.jornadas
                    .slice()
                    .sort((a, b) => a.numeroDia - b.numeroDia)
                    .map((j) => (
                      <div key={j.id} className="flex items-center justify-between px-3 py-2 text-sm">
                        <span className="font-medium text-slate-700">Día {j.numeroDia}</span>
                        <span className="text-slate-600">
                          {j.fecha ? format(parseISO(j.fecha), "d MMM yyyy", { locale: es }) : '—'}
                        </span>
                        <span className="flex items-center gap-1 text-slate-500">
                          <Clock className="w-3.5 h-3.5" />
                          {j.horaInicio?.slice(0, 5)} – {j.horaFin?.slice(0, 5)}
                        </span>
                      </div>
                    ))}
                </div>
              ) : (
                <p className="text-sm text-slate-400">Sin jornadas registradas.</p>
              )}
            </div>

            <div>
              <h4 className="text-sm font-semibold text-slate-700 flex items-center gap-1.5 mb-2">
               Monitores Asignados
              </h4>
              {cargandoDetalle ? (
                <p className="text-sm text-slate-400">Cargando...</p>
              ) : monitoresDetalle.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {monitoresDetalle.map((m) => (
                    <span key={m.monitorId} className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-orange-50 text-orange-700 border border-orange-200 text-xs font-medium">
                      <Users className="w-3.5 h-3.5" /> {m.monitorNombre}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-slate-400">Ningún monitor asignado todavía.</p>
              )}
            </div>

            <div className="text-xs text-slate-400 border-t border-slate-100 pt-3">
              Creado el {eventoDetalle.fechaCreacion ? format(new Date(eventoDetalle.fechaCreacion), "d MMM yyyy - HH:mm", { locale: es }) : '—'}
            </div>
          </div>
        )}
      </Modal>

      <Modal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        title="Asignar Monitor"
      >
        <div className="space-y-4">
          <p className="text-sm text-slate-600">
            Selecciona el Monitor que deseas asignar al evento <strong>{eventoToAssign?.nombre}</strong> para encargarse del control de asistencia y escaneo de códigos QR.
          </p>

          {monitores.length === 0 ? (
            <div className="flex flex-col items-center text-center gap-1.5 py-4 px-3 bg-slate-50 border border-slate-200 rounded-lg">
              <Users className="w-5 h-5 text-slate-400" />
              <p className="text-sm font-medium text-slate-600">No hay monitores disponibles</p>
            </div>
          ) : (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Monitor disponible</label>
              <select
                className="block w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm"
                value={selectedMonitor}
                onChange={(e) => setSelectedMonitor(e.target.value)}
              >
                <option value="">Seleccione un monitor...</option>
                {monitores.map(m => (
                  <option key={m.id} value={m.id}>{m.nombre} ({m.correo})</option>
                ))}
              </select>
            </div>
          )}

          <div className="flex w-full gap-3 mt-4">
            <Button
              variant="secondary"
              className="flex-1"
              onClick={() => setAssignModalOpen(false)}
              disabled={isAssigning}
            >
              Cancelar
            </Button>
            {monitores.length > 0 && (
              <Button
                className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white"
                onClick={handleAssignMonitor}
                disabled={!selectedMonitor || isAssigning}
              >
                {isAssigning ? 'Asignando...' : 'Guardar Asignación'}
              </Button>
            )}
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={encuestaModalOpen}
        onClose={() => setEncuestaModalOpen(false)}
        title={eventoEncuesta ? `Resultados de Encuesta — ${eventoEncuesta.nombre}` : 'Resultados de Encuesta'}
      >
        {cargandoEncuesta ? (
          <p className="text-sm text-slate-500 text-center py-6">Cargando resultados...</p>
        ) : (
          <div className="space-y-5">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="bg-slate-50 rounded-lg p-3 text-center border border-slate-100">
                <p className="text-xs font-medium text-slate-500">Inscritos</p>
                <p className="text-lg font-bold text-slate-800">{resumenEncuesta?.totalInscritos ?? '—'}</p>
              </div>
              <div className="bg-slate-50 rounded-lg p-3 text-center border border-slate-100">
                <p className="text-xs font-medium text-slate-500">Asistieron</p>
                <p className="text-lg font-bold text-slate-800">{resumenEncuesta?.totalAsistieron ?? '—'}</p>
              </div>
              <div className="bg-slate-50 rounded-lg p-3 text-center border border-slate-100">
                <p className="text-xs font-medium text-slate-500">% Asistencia</p>
                <p className="text-lg font-bold text-slate-800">
                  {resumenEncuesta ? `${resumenEncuesta.porcentajeAsistencia.toFixed(0)}%` : '—'}
                </p>
              </div>
              <div className="bg-amber-50 rounded-lg p-3 text-center border border-amber-100">
                <p className="text-xs font-medium text-amber-700">Satisfacción</p>
                <p className="text-lg font-bold text-amber-700 flex items-center justify-center gap-1">
                  {promedioSatisfaccion ? <>{promedioSatisfaccion.toFixed(1)} <Star className="w-4 h-4 fill-amber-400 text-amber-400" /></> : 'Sin datos'}
                </p>
              </div>
            </div>

            <div className="border-t border-slate-100 pt-4">
              <h3 className="text-sm font-semibold text-slate-700 flex items-center gap-1.5 mb-3">
                <MessageCircle className="w-4 h-4" /> Comentarios ({totalComentarios})
              </h3>
              <div className="flex flex-wrap items-center gap-1.5 mb-3">
                <button
                  onClick={() => cambiarFiltroCalificacion(null)}
                  className={`px-2.5 py-1 text-xs font-medium rounded-full border ${filtroCalificacion === null ? 'bg-emerald-600 text-white border-emerald-600' : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'}`}
                >
                  Todas
                </button>
                {[5, 4, 3, 2, 1].map((n) => (
                  <button
                    key={n}
                    onClick={() => cambiarFiltroCalificacion(n)}
                    className={`flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-full border ${filtroCalificacion === n ? 'bg-emerald-600 text-white border-emerald-600' : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'}`}
                  >
                    {n} <Star className={`w-3 h-3 ${filtroCalificacion === n ? 'fill-white text-white' : 'fill-amber-400 text-amber-400'}`} />
                  </button>
                ))}
              </div>
              {comentariosEncuesta.length === 0 ? (
                <p className="text-sm text-slate-400 text-center py-6">Nadie ha dejado comentarios para este evento todavía.</p>
              ) : (
                <>
                  <div className="space-y-4 max-h-72 overflow-y-auto">
                    {comentariosEncuesta.map((c, idx) => (
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
                  <Pagination
                    paginaActual={paginaComentarios}
                    totalPaginas={totalPaginasComentarios}
                    onCambiarPagina={cambiarPaginaComentarios}
                  />
                </>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}