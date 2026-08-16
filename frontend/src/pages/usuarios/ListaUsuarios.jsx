import { useState, useEffect } from 'react';
import { listarUsuarios, cambiarEstadoUsuario } from '../../services/usuarioService';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'sonner';
import { Search, Plus, Edit2, Eye, ShieldOff, ShieldCheck, Filter, AlertTriangle, Mail, Phone, Calendar, IdCard } from 'lucide-react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import Pagination from '../../components/Pagination';

export default function ListaUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [rolFiltro, setRolFiltro] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const { token } = useAuth();
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;
  const [estadoModalOpen, setEstadoModalOpen] = useState(false);
  const [usuarioParaCambiarEstado, setUsuarioParaCambiarEstado] = useState(null);
  const [detalleModalOpen, setDetalleModalOpen] = useState(false);
  const [usuarioDetalle, setUsuarioDetalle] = useState(null);

  let currentUserEmail = '';
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      currentUserEmail = payload.sub;
    } catch (e) {
      // ignore
    }
  }

  useEffect(() => {
    cargarUsuarios();
  }, [rolFiltro]);

  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm, rolFiltro]);

  const cargarUsuarios = async () => {
    try {
      setIsLoading(true);
      const datos = await listarUsuarios(rolFiltro || undefined);
      setUsuarios(datos || []);
    } catch (error) {
      toast.error('Error al cargar la lista de usuarios');
    } finally {
      setIsLoading(false);
    }
  };

  const abrirDetalleModal = (u) => {
    setUsuarioDetalle(u);
    setDetalleModalOpen(true);
  };

  const abrirModalEstado = (u) => {
    setUsuarioParaCambiarEstado(u);
    setEstadoModalOpen(true);
  };

  const confirmarCambioEstado = async () => {
    if (!usuarioParaCambiarEstado) return;
    const u = usuarioParaCambiarEstado;
    const nuevoEstado = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    try {
      await cambiarEstadoUsuario(u.id, nuevoEstado);
      toast.success(`Usuario ${u.nombre} ha sido ${nuevoEstado === 'ACTIVO' ? 'activado' : 'desactivado'}`);
      setEstadoModalOpen(false);
      setUsuarioParaCambiarEstado(null);
      cargarUsuarios();
    } catch (error) {
      toast.error('Error al cambiar el estado del usuario');
    }
  };

  const filteredUsuarios = usuarios.filter(u =>
    `${u.nombre} ${u.apellido}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.correo.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalPaginas = Math.ceil(filteredUsuarios.length / FILAS_POR_PAGINA);
  const usuariosPagina = filteredUsuarios.slice(
    (paginaActual - 1) * FILAS_POR_PAGINA,
    paginaActual * FILAS_POR_PAGINA
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Gestión de Usuarios</h1>
        </div>
        <Button onClick={() => navigate('/usuarios/nuevo')} icon={Plus}>
          Nuevo Usuario
        </Button>
      </div>

      <Card>
        <CardHeader className="flex flex-col sm:flex-row justify-between items-center gap-4 bg-slate-50/50">
          <CardTitle className="text-base font-semibold">Usuarios del Sistema</CardTitle>
          <div className="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
            <div className="relative">
              <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 z-10" />
              <select
                value={rolFiltro}
                onChange={(e) => setRolFiltro(e.target.value)}
                className="pl-10 pr-8 py-2 border border-slate-300 rounded-lg text-sm bg-white appearance-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">Todos los roles</option>
                <option value="ADMIN">Administrador</option>
                <option value="OPERADOR">Operador</option>
                <option value="MONITOR">Monitor</option>
              </select>
            </div>
            <div className="relative w-full sm:w-72">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search className="h-4 w-4 text-slate-400" />
              </div>
              <input
                type="text"
                placeholder="Buscar por nombre o correo..."
                className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-4">Usuario</th>
                  <th className="px-6 py-4">Rol</th>
                  <th className="px-6 py-4">Estado</th>
                  <th className="px-6 py-4 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {isLoading ? (
                  <tr>
                    <td colSpan="4" className="px-6 py-8 text-center text-slate-500">
                      Cargando usuarios...
                    </td>
                  </tr>
                ) : usuariosPagina.length === 0 ? (
                  <tr>
                    <td colSpan="4" className="px-6 py-8 text-center text-slate-500">
                      No se encontraron usuarios
                    </td>
                  </tr>
                ) : (
                  usuariosPagina.map((u) => (
                    <tr key={u.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center">
                          <div className="h-10 w-10 flex-shrink-0 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 font-bold uppercase">
                            {u.nombre.charAt(0)}{u.apellido.charAt(0)}
                          </div>
                          <div className="ml-4">
                            <div className="font-medium text-slate-900">{u.nombre} {u.apellido}</div>
                            <div className="text-slate-500 text-xs">{u.correo}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex gap-1 flex-wrap">
                          {u.roles.map((r) => (
                            <Badge key={r} variant={r.toLowerCase()}>
                              {r}
                            </Badge>
                          ))}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <Badge variant={u.estado === 'ACTIVO' ? 'success' : 'danger'}>
                          {u.estado}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            icon={Eye}
                            onClick={() => abrirDetalleModal(u)}
                            title="Ver detalle del usuario"
                          />
                          <Button
                            variant="ghost"
                            size="sm"
                            icon={Edit2}
                            onClick={() => navigate(`/usuarios/editar/${u.id}`)}
                            title="Editar usuario"
                          />
                          {u.correo !== currentUserEmail && (
                            <Button
                              variant="ghost"
                              size="sm"
                              className={u.estado === 'ACTIVO' ? 'text-red-600 hover:text-red-700 hover:bg-red-50' : 'text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50'}
                              icon={u.estado === 'ACTIVO' ? ShieldOff : ShieldCheck}
                              onClick={() => abrirModalEstado(u)}
                              title={u.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'}
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
        isOpen={detalleModalOpen}
        onClose={() => setDetalleModalOpen(false)}
        title="Detalle del Usuario"
      >
        {usuarioDetalle && (
          <div className="space-y-5 flex flex-col items-center text-center">
            <div className="flex flex-col items-center gap-2">
              <div className="h-14 w-14 flex-shrink-0 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 font-bold text-lg uppercase">
                {usuarioDetalle.nombre.charAt(0)}{usuarioDetalle.apellido.charAt(0)}
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-900">{usuarioDetalle.nombre} {usuarioDetalle.apellido}</h3>
                <Badge variant={usuarioDetalle.estado === 'ACTIVO' ? 'success' : 'danger'}>
                  {usuarioDetalle.estado}
                </Badge>
              </div>
            </div>

            <div className="w-full">
              <h4 className="text-sm font-semibold text-slate-700 flex items-center justify-center gap-1.5 mb-2">
                <IdCard className="w-4 h-4" /> Roles
              </h4>
              <div className="flex gap-1.5 flex-wrap justify-center">
                {usuarioDetalle.roles.map((r) => (
                  <Badge key={r} variant={r.toLowerCase()}>{r}</Badge>
                ))}
              </div>
            </div>

            <div className="space-y-2 w-full">
              <div className="flex items-center justify-center text-sm text-slate-600">
                <Mail className="w-4 h-4 mr-2 text-slate-400 flex-shrink-0" />
                {usuarioDetalle.correo}
              </div>
              <div className="flex items-center justify-center text-sm text-slate-600">
                <Phone className="w-4 h-4 mr-2 text-slate-400 flex-shrink-0" />
                {usuarioDetalle.telefono || 'Sin teléfono registrado'}
              </div>
            </div>

            <div className="text-xs text-slate-400 border-t border-slate-100 pt-3 flex items-center justify-center gap-1.5 w-full">
              <Calendar className="w-3.5 h-3.5" />
              Creado el {usuarioDetalle.fechaCreacion ? format(new Date(usuarioDetalle.fechaCreacion), "d MMM yyyy - HH:mm", { locale: es }) : '—'}
            </div>
          </div>
        )}
      </Modal>

      <Modal
        isOpen={estadoModalOpen}
        onClose={() => setEstadoModalOpen(false)}
        title={usuarioParaCambiarEstado?.estado === 'ACTIVO' ? 'Desactivar Usuario' : 'Activar Usuario'}
      >
        <div className="flex flex-col items-center text-center space-y-4">
          <div className={`p-3 rounded-full ${usuarioParaCambiarEstado?.estado === 'ACTIVO' ? 'bg-red-100 text-red-600' : 'bg-emerald-100 text-emerald-600'}`}>
            <AlertTriangle className="w-8 h-8" />
          </div>
          <p className="text-slate-700">
            ¿Estás seguro de que deseas {usuarioParaCambiarEstado?.estado === 'ACTIVO' ? 'desactivar' : 'activar'} a{' '}
            <strong>{usuarioParaCambiarEstado?.nombre} {usuarioParaCambiarEstado?.apellido}</strong>?
          </p>
          <p className="text-sm text-slate-500">
            {usuarioParaCambiarEstado?.estado === 'ACTIVO'
              ? 'El usuario no podrá iniciar sesión hasta que sea activado nuevamente.'
              : 'El usuario podrá volver a iniciar sesión en el sistema.'}
          </p>
          <div className="flex w-full gap-3 mt-4">
            <Button
              variant="secondary"
              className="flex-1"
              onClick={() => setEstadoModalOpen(false)}
            >
              Cerrar
            </Button>
            <Button
              className={`flex-1 text-white ${usuarioParaCambiarEstado?.estado === 'ACTIVO' ? 'bg-red-600 hover:bg-red-700' : 'bg-emerald-600 hover:bg-emerald-700'}`}
              onClick={confirmarCambioEstado}
            >
              {usuarioParaCambiarEstado?.estado === 'ACTIVO' ? 'Sí, Desactivar' : 'Sí, Activar'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}