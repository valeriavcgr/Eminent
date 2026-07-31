import { useState, useEffect } from 'react';
import { listarUsuarios, cambiarEstadoUsuario } from '../../services/usuarioService';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'sonner';
import { Search, Plus, Edit2, ShieldOff, ShieldCheck } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';

export default function ListaUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const { token } = useAuth();
  
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
  }, []);

  const cargarUsuarios = async () => {
    try {
      setIsLoading(true);
      const datos = await listarUsuarios();
      setUsuarios(datos || []);
    } catch (error) {
      toast.error('Error al cargar la lista de usuarios');
    } finally {
      setIsLoading(false);
    }
  };

  const toggleEstado = async (u) => {
    const nuevoEstado = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    try {
      await cambiarEstadoUsuario(u.id, nuevoEstado);
      toast.success(`Usuario ${u.nombre} ha sido ${nuevoEstado === 'ACTIVO' ? 'activado' : 'desactivado'}`);
      cargarUsuarios();
    } catch (error) {
      toast.error('Error al cambiar el estado del usuario');
    }
  };

  const filteredUsuarios = usuarios.filter(u => 
    `${u.nombre} ${u.apellido}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.correo.toLowerCase().includes(searchTerm.toLowerCase())
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
                ) : filteredUsuarios.length === 0 ? (
                  <tr>
                    <td colSpan="4" className="px-6 py-8 text-center text-slate-500">
                      No se encontraron usuarios
                    </td>
                  </tr>
                ) : (
                  filteredUsuarios.map((u) => (
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
                        <Badge variant={u.rol.toLowerCase()}>
                          {u.rol}
                        </Badge>
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
                              onClick={() => toggleEstado(u)}
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
        </CardContent>
      </Card>
    </div>
  );
}