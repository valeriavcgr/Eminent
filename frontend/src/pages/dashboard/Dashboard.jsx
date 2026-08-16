import { useState, useEffect } from 'react';
import { obtenerResumenDashboard } from '../../services/dashboardService';
import { obtenerComentariosEvento } from '../../services/encuestaService';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Modal } from '../../components/ui/Modal';
import { Calendar, Users, ListFilter, X, ChevronDown, ChevronUp, RefreshCw, QrCode, UserCheck, Trophy, Star, MessageCircle } from 'lucide-react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function Dashboard() {
  const [datos, setDatos] = useState(null);
  const [filtros, setFiltros] = useState({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });
  const [filtrosVisibles, setFiltrosVisibles] = useState(false);
  const [cargando, setCargando] = useState(false);

  // --- Modal de comentarios ---
  const [modalAbierto, setModalAbierto] = useState(false);
  const [eventoSeleccionado, setEventoSeleccionado] = useState(null);
  const [comentarios, setComentarios] = useState([]);
  const [cargandoComentarios, setCargandoComentarios] = useState(false);

  useEffect(() => {
    cargar();
  }, [filtros]);

  const cargar = async () => {
    setCargando(true);
    try {
      const params = {};
      Object.entries(filtros).forEach(([k, v]) => { if (v) params[k] = v; });
      const resultado = await obtenerResumenDashboard(params);
      setDatos(resultado);
    } catch (error) {
      console.error("Error al cargar datos del dashboard", error);
    } finally {
      setCargando(false);
    }
  };

  const handleFiltro = (campo, valor) => setFiltros({ ...filtros, [campo]: valor });
  const limpiarFiltros = () => setFiltros({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });
  const hayFiltrosActivos = Object.values(filtros).some((v) => v);

  const abrirComentarios = async (evento) => {
    setEventoSeleccionado(evento);
    setModalAbierto(true);
    setCargandoComentarios(true);
    try {
      const datos = await obtenerComentariosEvento(evento.id, { size: 1000 });
      setComentarios(datos.content || []);
    } catch (error) {
      console.error("Error al cargar comentarios", error);
      setComentarios([]);
    } finally {
      setCargandoComentarios(false);
    }
  };

  if (!datos) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3">
        <RefreshCw className="w-8 h-8 text-blue-600 animate-spin" />
        <p className="text-slate-500 font-medium animate-pulse">Cargando dashboard...</p>
      </div>
    );
  }

  const tarjetas = [
    { label: 'Total de Eventos', valor: datos.totalEventos, icon: Calendar, color: 'text-blue-600 bg-blue-50 border-blue-100 hover:border-blue-300' },
    { label: 'Inscritos Activos', valor: datos.inscritosActivosTotal, icon: Users, color: 'text-emerald-600 bg-emerald-50 border-emerald-100 hover:border-emerald-300' },
    { label: 'Asistencia por QR', valor: datos.asistieronQrTotal, icon: QrCode, color: 'text-orange-600 bg-orange-50 border-orange-100 hover:border-orange-300' },
    { label: 'Asistencia Manual', valor: datos.asistieronManualTotal, icon: UserCheck, color: 'text-purple-600 bg-purple-50 border-purple-100 hover:border-purple-300' },
    { label: 'Satisfacción Promedio', valor: datos.promedioSatisfaccionGeneral ? `${datos.promedioSatisfaccionGeneral.toFixed(1)} ★` : 'Sin datos', icon: Star, color: 'text-amber-600 bg-amber-50 border-amber-100 hover:border-amber-300' },
  ];

  const COLORES_DONUT = {
    // Eventos por Tipo
    TALLER: '#2563eb', CAPACITACION: '#8b5cf6', TORNEO: '#f59e0b',
    // Eventos por Modalidad
    PRESENCIAL: '#2563eb', VIRTUAL: '#10b981',
    // Eventos por Estado
    PROGRAMADO: '#3b82f6', EN_CURSO: '#10b981', FINALIZADO: '#94a3b8', CANCELADO: '#ef4444',
  };
  const COLOR_DONUT_DEFECTO = '#cbd5e1';

  const Donut = ({ datos: entradas }) => {
    const items = Object.entries(entradas || {});
    const total = items.reduce((acc, [, v]) => acc + v, 0);

    if (total === 0) {
      return <p className="text-sm text-slate-400 text-center py-8">Sin datos registrados</p>;
    }

    let acumulado = 0;
    const segmentos = items.map(([k, v]) => {
      const inicio = (acumulado / total) * 360;
      acumulado += v;
      const fin = (acumulado / total) * 360;
      return { key: k, valor: v, color: COLORES_DONUT[k] || COLOR_DONUT_DEFECTO, inicio, fin };
    });
    const gradiente = segmentos.map((s) => `${s.color} ${s.inicio}deg ${s.fin}deg`).join(', ');

    return (
      <div className="flex items-center gap-6">
        <div
          className="relative w-28 h-28 flex-shrink-0 rounded-full"
          style={{ background: `conic-gradient(${gradiente})` }}
        >
          <div className="absolute inset-3 bg-white rounded-full flex flex-col items-center justify-center shadow-inner">
            <span className="text-xl font-black text-slate-800 leading-none">{total}</span>
            <span className="text-[10px] text-slate-400 font-medium uppercase tracking-wide mt-0.5">Total</span>
          </div>
        </div>
        <div className="space-y-2 flex-1 min-w-0">
          {segmentos.map((s) => (
            <div key={s.key} className="flex items-center gap-2 text-xs">
              <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: s.color }}></span>
              <span className="text-slate-600 font-medium truncate flex-1">{s.key}</span>
              <span className="text-slate-500 font-semibold">{s.valor}</span>
            </div>
          ))}
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header section with modern layout */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-200 pb-5">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight text-slate-900">Panel de Control</h1>
          <p className="text-slate-500 text-sm mt-1">Monitorea y analiza las estadisticas de tus eventos en tiempo real</p>
        </div>
        <div className="flex items-center gap-2">
          {cargando && <RefreshCw className="w-4 h-4 text-blue-600 animate-spin mr-2" />}
          <button 
            onClick={() => setFiltrosVisibles(!filtrosVisibles)}
            className={`flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg border transition-all ${
              filtrosVisibles 
                ? 'bg-slate-900 text-white border-slate-900' 
                : 'bg-white text-slate-700 border-slate-300 hover:bg-slate-50'
            }`}
          >
            <ListFilter className="w-4 h-4" />
            Filtros
            {filtrosVisibles ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Advanced Filters with Smooth Collapse Animation */}
      <div 
        className={`transition-all duration-300 ease-in-out overflow-hidden ${
          filtrosVisibles ? 'max-h-[400px] opacity-100' : 'max-h-0 opacity-0 pointer-events-none'
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
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
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
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
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
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
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
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Hasta</label>
              <input
                type="date"
                value={filtros.fechaHasta ? filtros.fechaHasta.split('T')[0] : ''}
                onChange={(e) => handleFiltro('fechaHasta', e.target.value ? `${e.target.value}T23:59:59` : '')}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* KPI Metrics Cards Grid with Premium Interactions */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-5">
        {tarjetas.map((t) => (
          <Card
            key={t.label}
            className="group relative border border-slate-200/80 hover:shadow-lg hover:-translate-y-1 transition-all duration-300 bg-white overflow-hidden cursor-default"
          >
            {/* Shimmer element on hover */}
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent -translate-x-full group-hover:animate-[shimmer_1.5s_infinite] pointer-events-none"></div>

            <CardContent className="p-6 flex items-center gap-5">
              <div className={`w-14 h-14 rounded-xl flex items-center justify-center border transition-all duration-300 shadow-sm ${t.color}`}>
                <t.icon className="w-7 h-7" />
              </div>
              <div className="space-y-1">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">{t.label}</p>
                <p className="text-2xl font-black text-slate-800 tracking-tight transition-colors group-hover:text-blue-600">{t.valor}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Interactive Visualizations - Event Breakdown Section */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="border border-slate-200/60 hover:shadow-md transition-shadow duration-300 bg-white">
          <CardHeader className="border-b border-slate-100 bg-slate-50/20 py-4">
            <CardTitle className="text-base font-bold text-slate-800">Eventos por Tipo</CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <Donut datos={datos.eventosPorTipo} />
          </CardContent>
        </Card>

        <Card className="border border-slate-200/60 hover:shadow-md transition-shadow duration-300 bg-white">
          <CardHeader className="border-b border-slate-100 bg-slate-50/20 py-4">
            <CardTitle className="text-base font-bold text-slate-800">Eventos por Modalidad</CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <Donut datos={datos.eventosPorModalidad} />
          </CardContent>
        </Card>

        <Card className="border border-slate-200/60 hover:shadow-md transition-shadow duration-300 bg-white">
          <CardHeader className="border-b border-slate-100 bg-slate-50/20 py-4">
            <CardTitle className="text-base font-bold text-slate-800">Eventos por Estado</CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <Donut datos={datos.eventosPorEstado} />
          </CardContent>
        </Card>
      </div>

      {/* Ranking cruzado: asistencia real por evento individual */}
      <Card className="border border-slate-200/60 hover:shadow-md transition-shadow duration-300 bg-white">
        <CardHeader className="border-b border-slate-100 bg-slate-50/20 py-4">
          <CardTitle className="text-base font-bold text-slate-800 flex items-center gap-2">
            <Trophy className="w-4 h-4 text-amber-500" /> Ranking de Eventos por porcentaje de Asistencia
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-3">Evento</th>
                  <th className="px-6 py-3">Tipo</th>
                  <th className="px-6 py-3">Inscritos</th>
                  <th className="px-6 py-3">Asistieron</th>
                  <th className="px-6 py-3 w-1/3">% Asistencia</th>
                  <th className="px-6 py-3">Satisfacción</th>
                  <th className="px-6 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {(datos.detalleEventos || []).length === 0 ? (
                  <tr><td colSpan="7" className="px-6 py-8 text-center text-slate-400">Sin eventos para este filtro.</td></tr>
                ) : (
                  datos.detalleEventos.map((e) => (
                    <tr key={e.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-3 font-medium text-slate-800">{e.nombre}</td>
                      <td className="px-6 py-3 text-slate-500">{e.tipo}</td>
                      <td className="px-6 py-3 text-slate-500">{e.inscritos}</td>
                      <td className="px-6 py-3 text-slate-500">{e.asistieron}</td>
                      <td className="px-6 py-3">
                        <div className="flex items-center gap-2">
                          <div className="w-full bg-slate-100 rounded-full h-2">
                            <div
                              className="bg-emerald-500 h-2 rounded-full transition-all duration-700 ease-out"
                              style={{ width: `${Math.min(100, e.porcentajeAsistencia)}%` }}
                            ></div>
                          </div>
                          <span className="text-xs text-slate-500 w-10 text-right">{Math.min(100, e.porcentajeAsistencia).toFixed(0)}%</span>
                        </div>
                      </td>
                      <td className="px-6 py-3">
                        {e.promedioSatisfaccion
                          ? <span className="flex items-center gap-1 text-amber-600 font-medium"><Star className="w-3.5 h-3.5 fill-amber-400" /> {e.promedioSatisfaccion.toFixed(1)}</span>
                          : <span className="text-slate-400 text-xs">Sin calificar</span>}
                      </td>
                      <td className="px-6 py-3 text-right">
                        <button
                          onClick={() => abrirComentarios(e)}
                          className="flex items-center gap-1 text-xs font-medium text-blue-600 hover:text-blue-700"
                        >
                          <MessageCircle className="w-3.5 h-3.5" /> Ver comentarios
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* Modal de comentarios */}
      <Modal
        isOpen={modalAbierto}
        onClose={() => setModalAbierto(false)}
        title={eventoSeleccionado ? `Comentarios — ${eventoSeleccionado.nombre}` : 'Comentarios'}
      >
        {cargandoComentarios ? (
          <p className="text-sm text-slate-500 text-center py-6">Cargando comentarios...</p>
        ) : comentarios.length === 0 ? (
          <p className="text-sm text-slate-400 text-center py-6">Nadie ha dejado comentarios para este evento todavía.</p>
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