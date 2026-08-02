import { useState, useEffect } from 'react';
import { obtenerResumenDashboard } from '../../services/dashboardService';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Calendar, Users, TrendingUp, CheckCircle2, ListFilter, X } from 'lucide-react';

export default function Dashboard() {
  const [datos, setDatos] = useState(null);
  const [filtros, setFiltros] = useState({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });

  useEffect(() => {
    cargar();
  }, [filtros]);

  const cargar = async () => {
    const params = {};
    Object.entries(filtros).forEach(([k, v]) => { if (v) params[k] = v; });
    const resultado = await obtenerResumenDashboard(params);
    setDatos(resultado);
  };

  const handleFiltro = (campo, valor) => setFiltros({ ...filtros, [campo]: valor });
  const limpiarFiltros = () => setFiltros({ tipo: '', modalidad: '', estado: '', fechaDesde: '', fechaHasta: '' });
  const hayFiltrosActivos = Object.values(filtros).some((v) => v);

  if (!datos) return <p className="text-slate-500">Cargando dashboard...</p>;

  const tarjetas = [
    { label: 'Total de eventos', valor: datos.totalEventos, icon: Calendar, color: 'text-blue-600 bg-blue-100' },
    { label: 'Aforo total', valor: datos.aforoTotal, icon: Users, color: 'text-emerald-600 bg-emerald-100' },
    { label: '% Aforo ocupado', valor: `${datos.porcentajeAforoOcupado.toFixed(1)}%`, icon: TrendingUp, color: 'text-orange-600 bg-orange-100' },
    { label: '% Asistencia', valor: `${datos.porcentajeAsistenciaSobreInscritos.toFixed(1)}%`, icon: CheckCircle2, color: 'text-purple-600 bg-purple-100' },
  ];

  const BarraDesglose = ({ datos: entradas }) => {
    const items = Object.entries(entradas || {});
    const max = Math.max(...items.map(([, v]) => v), 1);
    return (
      <div className="space-y-3">
        {items.length === 0 && <p className="text-sm text-slate-400">Sin datos</p>}
        {items.map(([k, v]) => (
          <div key={k}>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-600 font-medium">{k}</span>
              <span className="text-slate-500">{v}</span>
            </div>
            <div className="w-full bg-slate-100 rounded-full h-2">
              <div
                className="bg-blue-500 h-2 rounded-full transition-all"
                style={{ width: `${(v / max) * 100}%` }}
              ></div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Dashboard</h1>
        </div>
      </div>

      <Card>
        <CardHeader className="bg-slate-50/50 flex flex-row items-center justify-between">
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <ListFilter className="w-4 h-4 text-slate-500" /> Filtros avanzados
          </CardTitle>
          {hayFiltrosActivos && (
            <button onClick={limpiarFiltros} className="flex items-center gap-1 text-xs text-slate-500 hover:text-red-600">
              <X className="w-3.5 h-3.5" /> Limpiar filtros
            </button>
          )}
        </CardHeader>
        <CardContent className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
          <select
            value={filtros.tipo}
            onChange={(e) => handleFiltro('tipo', e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
          >
            <option value="">Todos los tipos</option>
            <option value="TALLER">Taller</option>
            <option value="CAPACITACION">Capacitación</option>
            <option value="TORNEO">Torneo</option>
          </select>
          <select
            value={filtros.modalidad}
            onChange={(e) => handleFiltro('modalidad', e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
          >
            <option value="">Todas las modalidades</option>
            <option value="PRESENCIAL">Presencial</option>
            <option value="VIRTUAL">Virtual</option>
          </select>
          <select
            value={filtros.estado}
            onChange={(e) => handleFiltro('estado', e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
          >
            <option value="">Todos los estados</option>
            <option value="PROGRAMADO">Programado</option>
            <option value="EN_CURSO">En curso</option>
            <option value="FINALIZADO">Finalizado</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
          <input
            type="date"
            value={filtros.fechaDesde}
            onChange={(e) => handleFiltro('fechaDesde', e.target.value ? `${e.target.value}T00:00:00` : '')}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
          />
          <input
            type="date"
            value={filtros.fechaHasta}
            onChange={(e) => handleFiltro('fechaHasta', e.target.value ? `${e.target.value}T23:59:59` : '')}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white"
          />
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {tarjetas.map((t) => (
          <Card key={t.label}>
            <CardContent className="p-5 flex items-center gap-4">
              <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${t.color}`}>
                <t.icon className="w-6 h-6" />
              </div>
              <div>
                <p className="text-xs text-slate-500">{t.label}</p>
                <p className="text-xl font-bold text-slate-900">{t.valor}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader><CardTitle className="text-base">Eventos por tipo</CardTitle></CardHeader>
          <CardContent><BarraDesglose datos={datos.eventosPorTipo} /></CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-base">Eventos por modalidad</CardTitle></CardHeader>
          <CardContent><BarraDesglose datos={datos.eventosPorModalidad} /></CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-base">Eventos por estado</CardTitle></CardHeader>
          <CardContent><BarraDesglose datos={datos.eventosPorEstado} /></CardContent>
        </Card>
      </div>
    </div>
  );
}