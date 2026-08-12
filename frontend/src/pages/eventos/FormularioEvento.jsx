import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { crearEvento, editarEvento, listarEventos } from '../../services/eventoService';
import { toast } from 'sonner';
import {
  Save,
  X,
  Type,
  AlignLeft,
  Calendar as CalendarIcon,
  Clock,
  Users,
  MapPin,
  CalendarDays,
  Tag
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { format, isBefore, isAfter, isEqual, parseISO } from 'date-fns';

export default function FormularioEvento() {
  const { id } = useParams();
  const esEdicion = Boolean(id);
  const [form, setForm] = useState({
    nombre: '',
    tipo: 'TALLER',
    modalidad: 'PRESENCIAL',
    descripcion: '',
    fechaInicio: '',
    fechaFin: '',
    aforo: ''
  });
  const [estadoActual, setEstadoActual] = useState('PROGRAMADO');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (esEdicion) {
      cargarEvento();
    }
  }, [id]);

  const cargarEvento = async () => {
    try {
      const eventos = await listarEventos();
      const evento = eventos.find((e) => e.id === Number(id));
      if (evento) {
        setForm({
          ...evento,
          fechaInicio: evento.fechaInicio?.slice(0, 16) || '',
          fechaFin: evento.fechaFin?.slice(0, 16) || '',
        });
        setEstadoActual(evento.estado);
      }
    } catch (err) {
      toast.error('Error al cargar datos del evento');
    }
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validarFormulario = () => {
    const ahora = new Date();
    const fInicio = new Date(form.fechaInicio);
    const fFin = new Date(form.fechaFin);

    if (!esEdicion && isBefore(fInicio, ahora)) {
      toast.error('La fecha de inicio no puede ser en el pasado');
      return false;
    }

    if (isBefore(fFin, fInicio) || isEqual(fFin, fInicio)) {
      toast.error('La fecha de fin debe ser posterior a la fecha de inicio');
      return false;
    }
    if (format(fInicio, 'yyyy-MM-dd') !== format(fFin, 'yyyy-MM-dd')) {
      toast.error('El evento debe iniciar y finalizar el mismo día (no se admiten eventos de varios días por ahora)');
      return false;
}

    if (Number(form.aforo) < 1) {
      toast.error('El aforo debe ser mayor a 0');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validarFormulario()) return;

    setIsSubmitting(true);
    try {
      const datos = { ...form, aforo: Number(form.aforo) };
      if (esEdicion) {
        await editarEvento(id, datos);
        toast.success('Evento actualizado exitosamente');
      } else {
        await crearEvento(datos);
        toast.success('Evento creado exitosamente');
      }
      navigate('/eventos');
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al guardar el evento');
    } finally {
      setIsSubmitting(false);
    }
  };

  const fechaInicioDeshabilitada = esEdicion && estadoActual !== 'PROGRAMADO';
  const formularioDeshabilitado = esEdicion && estadoActual === 'FINALIZADO';

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white">
      <div className="bg-gradient-to-r from-blue-600 to-blue-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
              <CalendarDays className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white tracking-tight">
                {esEdicion ? 'Editar Evento' : 'Nuevo Evento'}
              </h1>
              <p className="text-blue-100 text-sm mt-1">
                {esEdicion
                  ? 'Modifica los detalles del evento programado'
                  : 'Completa los datos para programar un nuevo evento.'}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 -mt-8 relative z-10 pb-16">
        {formularioDeshabilitado && (
          <div className="mb-6 p-4 rounded-lg bg-amber-50 border border-amber-200 text-amber-800">
            <p className="font-medium flex items-center gap-2">
              <Clock className="w-5 h-5" /> Evento Finalizado
            </p>
            <p className="text-sm mt-1">
              Este evento ya ha concluido, por lo que sus detalles son de solo lectura y no pueden modificarse.
            </p>
          </div>
        )}

        <Card className="shadow-xl shadow-gray-200/60 border border-gray-100">
          <CardHeader className="bg-slate-50/50 border-b border-slate-100">
            <CardTitle className="flex items-center gap-2">
              <Tag className="w-5 h-5 text-blue-600" />
              Información General
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <fieldset disabled={formularioDeshabilitado}>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 mb-1">Nombre del Evento</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Type className="h-4 w-4 text-slate-400" />
                      </div>
                      <input
                        name="nombre"
                        value={form.nombre}
                        onChange={handleChange}
                        className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                        placeholder="Ej. Taller de Liderazgo"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Tipo de Evento</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Tag className="h-4 w-4 text-slate-400" />
                      </div>
                      <select
                        name="tipo"
                        value={form.tipo}
                        onChange={handleChange}
                        className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      >
                        <option value="TALLER">Taller</option>
                        <option value="CAPACITACION">Capacitación</option>
                        <option value="TORNEO">Torneo</option>
                      </select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Modalidad</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <MapPin className="h-4 w-4 text-slate-400" />
                      </div>
                      <select
                        name="modalidad"
                        value={form.modalidad}
                        onChange={handleChange}
                        className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                      >
                        <option value="PRESENCIAL">Presencial</option>
                        <option value="VIRTUAL">Virtual</option>
                      </select>
                    </div>
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 mb-1">Descripción</label>
                    <div className="relative">
                      <div className="absolute top-3 left-3 pointer-events-none">
                        <AlignLeft className="h-4 w-4 text-slate-400" />
                      </div>
                      <textarea
                        name="descripcion"
                        value={form.descripcion}
                        onChange={handleChange}
                        rows="4"
                        className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                        placeholder="Proporciona detalles sobre los temas a tratar, objetivos..."
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Fecha y Hora de Inicio</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <CalendarIcon className="h-4 w-4 text-slate-400" />
                      </div>
                      <input
                        name="fechaInicio"
                        type="datetime-local"
                        value={form.fechaInicio}
                        onChange={handleChange}
                        className={`block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm ${fechaInicioDeshabilitada ? 'bg-slate-100 text-slate-500' : ''}`}
                        required
                        disabled={fechaInicioDeshabilitada}
                      />
                    </div>
                    {fechaInicioDeshabilitada && (
                      <p className="text-xs text-amber-600 mt-1">El evento ya está en curso, la fecha de inicio no se puede modificar.</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Fecha y Hora de Finalización</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Clock className="h-4 w-4 text-slate-400" />
                      </div>
                      <input
                        name="fechaFin"
                        type="datetime-local"
                        value={form.fechaFin}
                        onChange={handleChange}
                        className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                        required
                      />
                    </div>
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 mb-1">Aforo Máximo (Cupos)</label>
                    <div className="relative w-1/2">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Users className="h-4 w-4 text-slate-400" />
                      </div>
                      <input
                        name="aforo"
                        type="number"
                        value={form.aforo}
                        onChange={handleChange}
                        min="1"
                        className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm"
                        placeholder="Ej. 50"
                        required
                      />
                    </div>
                  </div>

                </div>
              </fieldset>

              <div className="pt-6 border-t border-slate-200 flex items-center justify-end gap-3">
                <Button
                  variant="secondary"
                  icon={X}
                  type="button"
                  onClick={() => navigate('/eventos')}
                  disabled={isSubmitting}
                >
                  Cancelar
                </Button>
                {!formularioDeshabilitado && (
                  <Button
                    type="submit"
                    className="bg-blue-600 hover:bg-blue-700 text-white"
                    icon={Save}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? 'Guardando...' : (esEdicion ? 'Guardar Cambios' : 'Crear Evento')}
                  </Button>
                )}
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}