import { useState, useEffect } from 'react';
import { obtenerResumenDashboard } from '../../services/dashboardService';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Calendar, Users, ListFilter, X, ChevronDown, ChevronUp, RefreshCw, QrCode, UserCheck } from 'lucide-react';

export default function Dashboard() {
  const [datos, setDatos] = useState(null);
  const [filtros, setFiltros] = useState({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });
  const [filtrosVisibles, setFiltrosVisibles] = useState(false);
  const [cargando, setCargando] = useState(false);

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
  ];

  const BarraDesglose = ({ datos: entradas, colorClass = 'bg-blue-500' }) => {
    const items = Object.entries(entradas || {});
    const max = Math.max(...items.map(([, v]) => v), 1);
    return (
      <div className="space-y-4">
        {items.length === 0 && <p className="text-sm text-slate-400">Sin datos registrados</p>}
        {items.map(([k, v]) => (
          <div key={k} className="group">
            <div className="flex justify-between text-xs mb-1.5">
              <span className="text-slate-600 font-medium group-hover:text-slate-900 transition-colors">{k}</span>
              <span className="text-slate-500 font-semibold">{v}</span>
            </div>
            <div className="w-full bg-slate-100/80 rounded-full h-2 overflow-hidden border border-slate-200/20">
              <div
                className={`${colorClass} h-2 rounded-full transition-all duration-700 ease-out`}
                style={{ width: `${(v / max) * 100}%` }}
              ></div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header section with modern layout */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-200 pb-5">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight text-slate-900">Panel de Control</h1>
          <p className="text-slate-500 text-sm mt-1">Monitorea y analiza las métricas de tus eventos en tiempo real.</p>
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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
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
            <BarraDesglose datos={datos.eventosPorTipo} colorClass="bg-blue-600" />
          </CardContent>
        </Card>

        <Card className="border border-slate-200/60 hover:shadow-md transition-shadow duration-300 bg-white">
          <CardHeader className="border-b border-slate-100 bg-slate-50/20 py-4">
            <CardTitle className="text-base font-bold text-slate-800">Eventos por Modalidad</CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <BarraDesglose datos={datos.eventosPorModalidad} colorClass="bg-emerald-500" />
          </CardContent>
        </Card>

        <Card className="border border-slate-200/60 hover:shadow-md transition-shadow duration-300 bg-white">
          <CardHeader className="border-b border-slate-100 bg-slate-50/20 py-4">
            <CardTitle className="text-base font-bold text-slate-800">Eventos por Estado</CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <BarraDesglose datos={datos.eventosPorEstado} colorClass="bg-purple-500" />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}