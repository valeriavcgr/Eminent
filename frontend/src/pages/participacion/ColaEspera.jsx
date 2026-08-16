import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { consultarColaEspera, promoverInscripcion } from '../../services/participacionService';
import { obtenerEvento } from '../../services/eventoService';
import { toast } from 'sonner';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { ArrowLeft, Clock3, IdCard, ArrowUpCircle, Users2, AlertTriangle, CheckCircle2, Edit2 } from 'lucide-react';
import Pagination from '../../components/Pagination';

export default function ColaEspera() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [cola, setCola] = useState([]);
  const [evento, setEvento] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [promoviendoId, setPromoviendoId] = useState(null);
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;

  const [promoverModalOpen, setPromoverModalOpen] = useState(false);
  const [inscripcionAPromover, setInscripcionAPromover] = useState(null);

  useEffect(() => {
    cargarCola();
  }, [id]);

  const cargarCola = async () => {
    try {
      setIsLoading(true);
      const [datos, eventoDatos] = await Promise.all([
        consultarColaEspera(id),
        obtenerEvento(id),
      ]);
      setCola(datos || []);
      setEvento(eventoDatos);
    } catch (err) {
      toast.error('Error al cargar la cola de espera');
    } finally {
      setIsLoading(false);
    }
  };

  const cuposDisponibles = evento ? evento.aforo - (evento.inscritos || 0) : null;
  const hayCupo = cuposDisponibles !== null && cuposDisponibles > 0;

  const abrirModalPromover = (inscripcion) => {
    setInscripcionAPromover(inscripcion);
    setPromoverModalOpen(true);
  };

  const confirmarPromover = async () => {
    if (!inscripcionAPromover) return;
    const inscripcionId = inscripcionAPromover.inscripcionId;
    setPromoviendoId(inscripcionId);
    try {
      await promoverInscripcion(inscripcionId);
      toast.success('Inscripción promovida correctamente');
      setPromoverModalOpen(false);
      setInscripcionAPromover(null);
      cargarCola();
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al promover la inscripción');
    } finally {
      setPromoviendoId(null);
    }
  };

  const totalPaginas = Math.ceil(cola.length / FILAS_POR_PAGINA);
  const colaPagina = cola.slice(
    (paginaActual - 1) * FILAS_POR_PAGINA,
    paginaActual * FILAS_POR_PAGINA
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Lista de espera</h1>
          <p className="text-sm text-slate-500 mt-1">
            {evento ? evento.nombre : 'Promueve participantes cuando se libere cupo'}
          </p>
        </div>
        <Button variant="secondary" icon={ArrowLeft} onClick={() => navigate('/eventos')}>
          Volver a Eventos
        </Button>
      </div>

      {evento && (
        <div className={`p-4 rounded-lg border flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-6 ${hayCupo ? 'bg-emerald-50 border-emerald-200' : 'bg-amber-50 border-amber-200'}`}>
          <div className="flex items-center gap-2">
            {hayCupo
              ? <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
              : <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0" />}
            <p className={`text-sm font-medium ${hayCupo ? 'text-emerald-800' : 'text-amber-800'}`}>
              {hayCupo
                ? `Hay ${cuposDisponibles} cupo${cuposDisponibles === 1 ? '' : 's'} disponible${cuposDisponibles === 1 ? '' : 's'} para promover participantes.`
                : 'No hay cupos disponibles. Debes ampliar el aforo del evento antes de poder promover a alguien.'}
            </p>
          </div>
          <div className="flex items-center gap-4 text-xs text-slate-500 sm:ml-auto">
            <span><strong className="text-slate-700">{evento.inscritos ?? 0}</strong> inscritos</span>
            <span><strong className="text-slate-700">{evento.aforo}</strong> aforo total</span>
            {!hayCupo && (
              <Button
                size="sm"
                variant="secondary"
                icon={Edit2}
                className="text-amber-700 bg-white border-amber-200 hover:bg-amber-100"
                onClick={() => navigate(`/eventos/editar/${id}`, { state: { returnTo: `/eventos/${id}/cola-espera` } })}
              >
                Ampliar aforo
              </Button>
            )}
          </div>
        </div>
      )}

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
                ) : colaPagina.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-10 text-center text-slate-400">
                      <Clock3 className="w-8 h-8 mx-auto mb-2 text-slate-300" />
                      No hay participantes en lista de espera.
                    </td>
                  </tr>
                ) : (
                  colaPagina.map((c) => (
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
                          disabled={promoviendoId === c.inscripcionId || !hayCupo}
                          title={!hayCupo ? 'No hay cupo disponible: amplía el aforo del evento primero' : undefined}
                          onClick={() => abrirModalPromover(c)}
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
          <Pagination
            paginaActual={paginaActual}
            totalPaginas={totalPaginas}
            onCambiarPagina={setPaginaActual}
          />
        </CardContent>
      </Card>

      <Modal
        isOpen={promoverModalOpen}
        onClose={() => setPromoverModalOpen(false)}
        title="Promover Participante"
      >
        <div className="flex flex-col items-center text-center space-y-4">
          <div className="p-3 rounded-full bg-emerald-100 text-emerald-600">
            <ArrowUpCircle className="w-8 h-8" />
          </div>
          <p className="text-slate-700">
            ¿Estás seguro de que deseas promover a{' '}
            <strong>{inscripcionAPromover?.participanteNombre}</strong> de la lista de espera?
          </p>
          <p className="text-sm text-slate-500">
            Pasará a estar inscrito activo en el evento y se le generará su código QR de asistencia.
          </p>
          <div className="flex w-full gap-3 mt-4">
            <Button
              variant="secondary"
              className="flex-1"
              onClick={() => setPromoverModalOpen(false)}
              disabled={promoviendoId === inscripcionAPromover?.inscripcionId}
            >
              Cerrar
            </Button>
            <Button
              className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white"
              onClick={confirmarPromover}
              disabled={promoviendoId === inscripcionAPromover?.inscripcionId}
            >
              {promoviendoId === inscripcionAPromover?.inscripcionId ? 'Promoviendo...' : 'Sí, Promover'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}