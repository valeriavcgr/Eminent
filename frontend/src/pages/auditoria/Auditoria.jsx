import { useState, useEffect } from 'react';
import { consultarAuditoria } from '../../services/auditoriaService';
import { listarUsuarios } from '../../services/usuarioService';
import { toast } from 'sonner';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  PlusCircle, Pencil, XCircle, Ban, Activity,
  Cpu, User, Globe, ListFilter, LogIn, ShieldAlert,
  X, ChevronDown, ChevronUp
} from 'lucide-react';
import Pagination from '../../components/Pagination';

const ICONO_POR_ACCION = {
  CREAR: { icon: PlusCircle, color: 'text-emerald-600' },
  EDITAR: { icon: Pencil, color: 'text-blue-600' },
  DESACTIVAR: { icon: XCircle, color: 'text-red-600' },
  CANCELAR: { icon: Ban, color: 'text-orange-600' },
  LOGIN_EXITOSO: { icon: LogIn, color: 'text-emerald-600' },
  LOGIN_FALLIDO: { icon: ShieldAlert, color: 'text-red-600' },
};

const ETIQUETA_ACCION = {
  CREAR: 'Creación',
  EDITAR: 'Edición',
  DESACTIVAR: 'Desactivación',
  CANCELAR: 'Cancelación',
  VER: 'Consulta',
  LOGIN_EXITOSO: 'Inicio de sesión exitoso',
  LOGIN_FALLIDO: 'Inicio de sesión fallido',
};

const TIPOS_AFECTADOS = ['USUARIO', 'EVENTO', 'PARTICIPANTE', 'INSCRIPCION', 'ASISTENCIA', 'CERTIFICADO', 'ENCUESTA'];

const FILTROS_INICIALES = { tipoAfectado: '', usuarioId: '', fechaInicio: '', fechaFin: '' };

export default function Auditoria() {
  const [registros, setRegistros] = useState([]);
  const [filtros, setFiltros] = useState(FILTROS_INICIALES);
  const [filtrosVisibles, setFiltrosVisibles] = useState(false);
  const [usuarios, setUsuarios] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;

  useEffect(() => {
    listarUsuarios().then(setUsuarios).catch(() => setUsuarios([]));
  }, []);

  useEffect(() => {
    cargar();
    setPaginaActual(1);
  }, [filtros]);

  const cargar = async () => {
    try {
      setIsLoading(true);
      const params = {};
      Object.entries(filtros).forEach(([k, v]) => { if (v) params[k] = v; });
      const datos = await consultarAuditoria(params);
      setRegistros(datos);
    } catch (err) {
      toast.error('Error al cargar la auditoría');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFiltro = (campo, valor) => setFiltros({ ...filtros, [campo]: valor });
  const limpiarFiltros = () => setFiltros(FILTROS_INICIALES);
  const hayFiltrosActivos = Object.values(filtros).some((v) => v);

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

      <div
        className={`transition-all duration-300 ease-in-out overflow-hidden ${
          filtrosVisibles ? 'max-h-[400px] opacity-100' : 'max-h-0 opacity-0 pointer-events-none'
        }`}
      >
        <Card className="border border-slate-200/80 shadow-md">
          <CardHeader className="bg-slate-50/50 flex flex-row items-center justify-between py-3">
            <CardTitle className="text-sm font-bold flex items-center gap-2 text-slate-700">
              Filtros
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
          <CardContent className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 py-4 bg-white">
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Tipo Afectado</label>
              <select
                value={filtros.tipoAfectado}
                onChange={(e) => handleFiltro('tipoAfectado', e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              >
                <option value="">Todos los tipos</option>
                {TIPOS_AFECTADOS.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Persona</label>
              <select
                value={filtros.usuarioId}
                onChange={(e) => handleFiltro('usuarioId', e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              >
                <option value="">Todas las personas</option>
                {usuarios.map((u) => (
                  <option key={u.id} value={u.id}>{u.nombre} {u.apellido} ({u.correo})</option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Desde</label>
              <input
                type="date"
                value={filtros.fechaInicio ? filtros.fechaInicio.split('T')[0] : ''}
                onChange={(e) => handleFiltro('fechaInicio', e.target.value ? `${e.target.value}T00:00:00` : '')}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-500">Hasta</label>
              <input
                type="date"
                value={filtros.fechaFin ? filtros.fechaFin.split('T')[0] : ''}
                onChange={(e) => handleFiltro('fechaFin', e.target.value ? `${e.target.value}T23:59:59` : '')}
                className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              />
            </div>
          </CardContent>
        </Card>
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
                            <Icono className="w-4 h-4" /> {ETIQUETA_ACCION[r.accion] || r.accion}
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