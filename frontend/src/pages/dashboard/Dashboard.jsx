import { useState, useEffect } from 'react';
import { obtenerResumenDashboard } from '../../services/dashboardService';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Calendar, Users, TrendingUp, CheckCircle2 } from 'lucide-react';

export default function Dashboard() {
  const [datos, setDatos] = useState(null);

  useEffect(() => {
    obtenerResumenDashboard().then(setDatos);
  }, []);

  if (!datos) return <p className="text-slate-500">Cargando dashboard...</p>;

  const tarjetas = [
    { label: 'Total de eventos', valor: datos.totalEventos, icon: Calendar, color: 'text-blue-600 bg-blue-100' },
    { label: 'Aforo total', valor: datos.aforoTotal, icon: Users, color: 'text-emerald-600 bg-emerald-100' },
    { label: '% Aforo ocupado', valor: `${datos.porcentajeAforoOcupado.toFixed(1)}%`, icon: TrendingUp, color: 'text-orange-600 bg-orange-100' },
    { label: '% Asistencia', valor: `${datos.porcentajeAsistenciaSobreInscritos.toFixed(1)}%`, icon: CheckCircle2, color: 'text-purple-600 bg-purple-100' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Dashboard</h1>
      </div>

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
          <CardContent>
            {Object.entries(datos.eventosPorTipo || {}).map(([k, v]) => (
              <div key={k} className="flex justify-between text-sm py-1 border-b border-slate-100 last:border-0">
                <span className="text-slate-600">{k}</span><span className="font-medium">{v}</span>
              </div>
            ))}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-base">Eventos por modalidad</CardTitle></CardHeader>
          <CardContent>
            {Object.entries(datos.eventosPorModalidad || {}).map(([k, v]) => (
              <div key={k} className="flex justify-between text-sm py-1 border-b border-slate-100 last:border-0">
                <span className="text-slate-600">{k}</span><span className="font-medium">{v}</span>
              </div>
            ))}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-base">Eventos por estado</CardTitle></CardHeader>
          <CardContent>
            {Object.entries(datos.eventosPorEstado || {}).map(([k, v]) => (
              <div key={k} className="flex justify-between text-sm py-1 border-b border-slate-100 last:border-0">
                <span className="text-slate-600">{k}</span><span className="font-medium">{v}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}