import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { crearUsuario, editarUsuario, obtenerUsuario } from '../../services/usuarioService';
import { toast } from 'sonner';
import { Save, X, User, Mail, Lock, Phone, Shield, UserPlus } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';

export default function FormularioUsuario() {
  const { id } = useParams();
  const isEditing = Boolean(id);

  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    correo: '',
    contrasena: '',
    roles: ['OPERADOR'],
    telefono: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (isEditing) {
      cargarUsuario();
    }
  }, [id]);

  const cargarUsuario = async () => {
    try {
      const data = await obtenerUsuario(id);
      setForm({
        nombre: data.nombre || '',
        apellido: data.apellido || '',
        correo: data.correo || '',
        contrasena: '',
        roles: data.roles && data.roles.length > 0 ? data.roles : ['OPERADOR'],
        telefono: data.telefono || ''
      });
    } catch (err) {
      toast.error('Error al cargar datos del usuario');
    }
  };
  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const toggleRol = (rol) => {
    setForm((prev) => ({
      ...prev,
      roles: prev.roles.includes(rol)
        ? prev.roles.filter((r) => r !== rol)
        : [...prev.roles, rol]
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.roles.length === 0) {
      toast.error('El usuario debe tener al menos un rol asignado');
      return;
    }

    setIsSubmitting(true);

    try {
      if (isEditing) {
        const datos = { ...form };
        if (!datos.contrasena) delete datos.contrasena;
        await editarUsuario(id, datos);
        toast.success('Usuario actualizado correctamente');
      } else {
        await crearUsuario(form);
        toast.success('Usuario creado exitosamente');
      }
      navigate('/usuarios');
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al procesar la solicitud');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white">
      <div className="bg-gradient-to-r from-blue-600 to-blue-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
              <UserPlus className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white tracking-tight">
                {isEditing ? 'Editar Usuario' : 'Nuevo Usuario'}
              </h1>
              <p className="text-blue-100 text-sm mt-1">
                {isEditing ? 'Actualiza los datos del usuario' : 'Registra un nuevo usuario en el sistema.'}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 -mt-8 relative z-10 pb-16">
        <Card className="shadow-xl shadow-gray-200/60 border border-gray-100">
          <CardHeader className="bg-slate-50/50 border-b border-slate-100">
            <CardTitle className="flex items-center gap-2">
              <Shield className="w-5 h-5 text-blue-600" />
              Datos Personales y Acceso
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Nombre</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <User className="h-4 w-4 text-slate-400" />
                    </div>
                    <input
                      name="nombre"
                      value={form.nombre}
                      onChange={handleChange}
                      className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      placeholder="Ej. Juan"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Apellido</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <User className="h-4 w-4 text-slate-400" />
                    </div>
                    <input
                      name="apellido"
                      value={form.apellido}
                      onChange={handleChange}
                      className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      placeholder="Ej. Pérez"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Correo Electrónico</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Mail className="h-4 w-4 text-slate-400" />
                    </div>
                    <input
                      name="correo"
                      type="email"
                      value={form.correo}
                      onChange={handleChange}
                      className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      placeholder="juan.perez@eminent.com"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Teléfono (Opcional)</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Phone className="h-4 w-4 text-slate-400" />
                    </div>
                    <input
                      name="telefono"
                      value={form.telefono}
                      onChange={handleChange}
                      className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      placeholder="+1 234 567 890"
                    />
                  </div>
                </div>

                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-slate-700 mb-2">
                    <Shield className="h-4 w-4 text-slate-400 inline mr-1" /> Roles en el Sistema
                  </label>
                  <div className="flex flex-wrap gap-4">
                    {[
                      { valor: 'OPERADOR', etiqueta: 'Operador (Gestión de Eventos)' },
                      { valor: 'MONITOR', etiqueta: 'Monitor (Control de Asistencia)' },
                      { valor: 'ADMIN', etiqueta: 'Administrador (Acceso Total)' },
                    ].map(({ valor, etiqueta }) => (
                      <label key={valor} className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={form.roles.includes(valor)}
                          onChange={() => toggleRol(valor)}
                          className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                        />
                        {etiqueta}
                      </label>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">
                    Contraseña {isEditing && <span className="text-slate-400 text-xs font-normal">(Dejar en blanco para no cambiar)</span>}
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Lock className="h-4 w-4 text-slate-400" />
                    </div>
                    <input
                      name="contrasena"
                      type="password"
                      value={form.contrasena}
                      onChange={handleChange}
                      className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      placeholder="••••••••"
                      required={!isEditing}
                    />
                  </div>
                </div>

              </div>

              <div className="pt-6 border-t border-slate-200 flex items-center justify-end gap-3">
                <Button
                  variant="secondary"
                  icon={X}
                  onClick={() => navigate('/usuarios')}
                  disabled={isSubmitting}
                >
                  Cancelar
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  icon={Save}
                  disabled={isSubmitting}
                  className="bg-blue-600 hover:bg-blue-700 text-white"
                >
                  {isSubmitting ? 'Guardando...' : (isEditing ? 'Guardar Cambios' : 'Crear Usuario')}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}