import { useState, useEffect } from 'react';
import { listarEventos, cancelarEvento, asignarMonitor } from '../../services/eventoService';
import { listarUsuarios } from '../../services/usuarioService';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import {
  Search,
  Plus,
  Edit2,
  Calendar as CalendarIcon,
  Users,
  Ban,
  ClipboardList,
  Upload,
  MapPin,
  Clock,
  AlertTriangle,
  UserPlus
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import Pagination from '../../components/Pagination';

export default function ListarEvento() {
  const [eventos, setEventos] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
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

  const navigate = useNavigate();

  useEffect(() => {
    cargarEventos();
  }, []);

  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm]);

  const cargarEventos = async () => {
    try {
      setIsLoading(true);
      const datos = await listarEventos();
      setEventos(datos || []);
    } catch (error) {
      toast.error('Error al cargar la lista de eventos');
    } finally {
      setIsLoading(false);
    }
  };

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
      const data = await listarUsuarios('MONITOR');
      setMonitores(data || []);
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
        <Button onClick={() => navigate('/eventos/nuevo')} icon={Plus} className="bg-emerald-600 hover:bg-emerald-700 text-white">
          Nuevo Evento
        </Button>
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
                  <th className="px-6 py-4">Cupos</th>
                  <th className="px-6 py-4">Estado</th>
                  <th className="px-6 py-4 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {isLoading ? (
                  <tr>
                    <td colSpan="6" className="px-6 py-8 text-center text-slate-500">
                      Cargando eventos...
                    </td>
                  </tr>
                ) : eventosPagina.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="px-6 py-8 text-center text-slate-500">
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
            Esta acción no se puede deshacer. Los inscritos mantendrán su registro pero el evento aparecerá como cancelado.
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
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        title="Asignar Monitor"
      >
        <div className="space-y-4">
          <p className="text-sm text-slate-600">
            Selecciona el usuario (Monitor) que deseas asignar al evento <strong>{eventoToAssign?.nombre}</strong> para encargarse del control de asistencia y escaneo de códigos QR.
          </p>

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

          <div className="flex w-full gap-3 mt-4">
            <Button
              variant="secondary"
              className="flex-1"
              onClick={() => setAssignModalOpen(false)}
              disabled={isAssigning}
            >
              Cancelar
            </Button>
            <Button
              className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white"
              onClick={handleAssignMonitor}
              disabled={!selectedMonitor || isAssigning}
            >
              {isAssigning ? 'Asignando...' : 'Guardar Asignación'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}