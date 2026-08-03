import { useState, useEffect } from 'react';
import { consultarAuditoria } from '../../services/auditoriaService';
import { toast } from 'sonner';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  PlusCircle, Pencil, XCircle, Ban, Activity,
  Cpu, User, Globe, ListFilter
} from 'lucide-react';
import Pagination from '../../components/Pagination';

const ICONO_POR_ACCION = {
  CREAR: { icon: PlusCircle, color: 'text-emerald-600' },
  EDITAR: { icon: Pencil, color: 'text-blue-600' },
  DESACTIVAR: { icon: XCircle, color: 'text-red-600' },
  CANCELAR: { icon: Ban, color: 'text-orange-600' },
};

const TIPOS_AFECTADOS = ['USUARIO', 'EVENTO', 'INSCRIPCION', 'ASISTENCIA', 'CERTIFICADO'];

export default function Auditoria() {
  const [registros, setRegistros] = useState([]);
  const [tipoFiltro, setTipoFiltro] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;

  useEffect(() => {
    cargar();
  }, [tipoFiltro]);

  useEffect(() => {
    setPaginaActual(1);
  }, [tipoFiltro]);

  const cargar = async () => {
    try {
      setIsLoading(true);
      const filtros = tipoFiltro ? { tipoAfectado: tipoFiltro } : {};
      const datos = await consultarAuditoria(filtros);
      setRegistros(datos);
    } catch (err) {
      toast.error('Error al cargar la auditoría');
    } finally {
      setIsLoading(false);
    }
  };

  const totalPaginas = Math.ceil(registros.length / FILAS_POR_PAGINA);
  const registrosPagina = registros.slice(
    (paginaActual - 1) * FILAS_POR_PAGINA,
    paginaActual * FILAS_POR_PAGINA
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Auditoría del sistema</h1>
        </div>
        <div className="relative">
          <ListFilter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 z-10" />
          <select
            value={tipoFiltro}
            onChange={(e) => setTipoFiltro(e.target.value)}
            className="pl-10 pr-8 py-2 border border-slate-300 rounded-lg text-sm bg-white appearance-none"
          >
            <option value="">Todos los tipos</option>
            {TIPOS_AFECTADOS.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </div>
      </div>

      <Card>
        <CardHeader className="bg-slate-50/50">
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <Activity className="w-4 h-4 text-slate-500" />
            Historial de acciones
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-4">Acción</th>
                  <th className="px-6 py-4">Tipo afectado</th>
                  <th className="px-6 py-4">Usuario</th>
                  <th className="px-6 py-4">Descripción</th>
                  <th className="px-6 py-4">Fecha</th>
                  <th className="px-6 py-4">IP</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {isLoading ? (
                  <tr>
                    <td colSpan="6" className="px-6 py-8 text-center text-slate-500">Cargando auditoría...</td>
                  </tr>
                ) : registrosPagina.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="px-6 py-10 text-center text-slate-400">No hay registros para este filtro.</td>
                  </tr>
                ) : (
                  registrosPagina.map((r) => {
                    const config = ICONO_POR_ACCION[r.accion] || { icon: Activity, color: 'text-slate-500' };
                    const Icono = config.icon;
                    const esSistema = !r.usuarioId;

                    return (
                      <tr key={r.id} className="hover:bg-slate-50 transition-colors">
                        <td className="px-6 py-4">
                          <span className={`flex items-center gap-1.5 font-medium ${config.color}`}>
                            <Icono className="w-4 h-4" /> {r.accion}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <Badge variant="neutral">{r.tipoAfectado}</Badge>
                        </td>
                        <td className="px-6 py-4">
                          {esSistema ? (
                            <span className="flex items-center gap-1 text-slate-500 text-xs">
                              <Cpu className="w-3.5 h-3.5" /> Sistema
                            </span>
                          ) : (
                            <span className="flex items-center gap-1 text-slate-700">
                              <User className="w-3.5 h-3.5 text-slate-400" /> {r.nombreUsuario}
                            </span>
                          )}
                        </td>
                        <td className="px-6 py-4 text-slate-600 max-w-xs truncate" title={r.descripcion}>
                          {r.descripcion}
                        </td>
                        <td className="px-6 py-4 text-slate-500 whitespace-nowrap">
                          {r.fechaHora
                            ? format(new Date(r.fechaHora), "d MMM yyyy - HH:mm:ss", { locale: es })
                            : '-'}
                        </td>
                        <td className="px-6 py-4">
                          <span className="flex items-center gap-1 font-mono text-xs text-slate-500">
                            <Globe className="w-3.5 h-3.5" /> {r.ip || 'No registrada'}
                          </span>
                        </td>
                      </tr>
                    );
                  })
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
    </div>
  );
}