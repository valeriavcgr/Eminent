import { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
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
  Tag,
  Plus,
  Trash2
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { format } from 'date-fns';

const JORNADA_VACIA = () => ({ id: null, fecha: '', horaInicio: '', horaFin: '' });

export default function FormularioEvento() {
  const { id } = useParams();
  const esEdicion = Boolean(id);
  const [form, setForm] = useState({
    nombre: '',
    tipo: 'TALLER',
    modalidad: 'PRESENCIAL',
    descripcion: '',
    aforo: ''
  });
  const [jornadas, setJornadas] = useState([JORNADA_VACIA()]);
  const [estadoActual, setEstadoActual] = useState('PROGRAMADO');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const returnTo = location.state?.returnTo || '/eventos';

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
          nombre: evento.nombre,
          tipo: evento.tipo,
          modalidad: evento.modalidad,
          descripcion: evento.descripcion || '',
          aforo: evento.aforo
        });
        if (evento.jornadas && evento.jornadas.length > 0) {
          setJornadas(evento.jornadas.map((j) => ({
            id: j.id,
            fecha: j.fecha,
            horaInicio: j.horaInicio?.slice(0, 5) || '',
            horaFin: j.horaFin?.slice(0, 5) || ''
          })));
        }
        setEstadoActual(evento.estado);
      }
    } catch (err) {
      toast.error('Error al cargar datos del evento');
    }
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleJornadaChange = (index, campo, valor) => {
    setJornadas((prev) => prev.map((j, i) => (i === index ? { ...j, [campo]: valor } : j)));
  };

  const agregarJornada = () => {
    setJornadas((prev) => [...prev, JORNADA_VACIA()]);
  };

  const quitarJornada = (index) => {
    setJornadas((prev) => (prev.length > 1 ? prev.filter((_, i) => i !== index) : prev));
  };

  const validarFormulario = () => {
    const hoy = format(new Date(), 'yyyy-MM-dd');

    if (jornadas.length === 0) {
      toast.error('El evento debe tener al menos una jornada');
      return false;
    }

    const fechasVistas = new Set();
    for (const j of jornadas) {
      if (!j.fecha || !j.horaInicio || !j.horaFin) {
        toast.error('Cada jornada requiere fecha, hora de inicio y hora de fin');
        return false;
      }
      if (!esEdicion && j.fecha < hoy) {
        toast.error('Ninguna jornada puede tener fecha en el pasado');
        return false;
      }
      if (j.horaFin <= j.horaInicio) {
        toast.error('La hora de fin de cada jornada debe ser posterior a la hora de inicio');
        return false;
      }
      if (fechasVistas.has(j.fecha)) {
        toast.error('No puede haber dos jornadas con la misma fecha');
        return false;
      }
      fechasVistas.add(j.fecha);
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
      const datos = {
        ...form,
        aforo: Number(form.aforo),
        jornadas: jornadas.map((j) => ({ id: j.id, fecha: j.fecha, horaInicio: j.horaInicio, horaFin: j.horaFin }))
      };
      if (esEdicion) {
        await editarEvento(id, datos);
        toast.success('Evento actualizado exitosamente');
      } else {
        await crearEvento(datos);
        toast.success('Evento creado exitosamente');
      }
      navigate(returnTo);
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al guardar el evento');
    } finally {
      setIsSubmitting(false);
    }
  };

  const jornadasBloqueadas = esEdicion && estadoActual !== 'PROGRAMADO';
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

                  <div className="md:col-span-2">
                    <div className="flex items-center justify-between mb-2">
                      <label className="block text-sm font-medium text-slate-700">Jornadas del Evento</label>
                      <Button
                        type="button"
                        size="sm"
                        variant="secondary"
                        icon={Plus}
                        onClick={agregarJornada}
                      >
                        Agregar día
                      </Button>
                    </div>
                    {jornadasBloqueadas && (
                      <p className="text-xs text-amber-600 mb-2">
                        El evento ya está en curso: las jornadas existentes son de solo lectura, pero puedes agregar días nuevos.
                      </p>
                    )}
                    <div className="space-y-3">
                      {jornadas.map((j, index) => {
                        const filaBloqueada = jornadasBloqueadas && Boolean(j.id);
                        return (
                          <div key={index} className="flex flex-wrap items-end gap-3 p-3 border border-slate-200 rounded-lg bg-slate-50/50">
                            <span className="text-xs font-semibold text-slate-400 w-14 pb-2">Día {index + 1}</span>
                            <div className="flex-1 min-w-[140px]">
                              <label className="block text-xs text-slate-500 mb-1">Fecha</label>
                              <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                  <CalendarIcon className="h-4 w-4 text-slate-400" />
                                </div>
                                <input
                                  type="date"
                                  value={j.fecha}
                                  onChange={(e) => handleJornadaChange(index, 'fecha', e.target.value)}
                                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm disabled:bg-slate-100 disabled:text-slate-500"
                                  required
                                  disabled={filaBloqueada}
                                />
                              </div>
                            </div>
                            <div className="flex-1 min-w-[110px]">
                              <label className="block text-xs text-slate-500 mb-1">Hora inicio</label>
                              <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                  <Clock className="h-4 w-4 text-slate-400" />
                                </div>
                                <input
                                  type="time"
                                  value={j.horaInicio}
                                  onChange={(e) => handleJornadaChange(index, 'horaInicio', e.target.value)}
                                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm disabled:bg-slate-100 disabled:text-slate-500"
                                  required
                                  disabled={filaBloqueada}
                                />
                              </div>
                            </div>
                            <div className="flex-1 min-w-[110px]">
                              <label className="block text-xs text-slate-500 mb-1">Hora fin</label>
                              <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                  <Clock className="h-4 w-4 text-slate-400" />
                                </div>
                                <input
                                  type="time"
                                  value={j.horaFin}
                                  onChange={(e) => handleJornadaChange(index, 'horaFin', e.target.value)}
                                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white shadow-sm disabled:bg-slate-100 disabled:text-slate-500"
                                  required
                                  disabled={filaBloqueada}
                                />
                              </div>
                            </div>
                            <Button
                              type="button"
                              size="sm"
                              variant="ghost"
                              icon={Trash2}
                              className="text-red-500 hover:text-red-700 hover:bg-red-50"
                              onClick={() => quitarJornada(index)}
                              disabled={jornadas.length === 1 || filaBloqueada}
                            />
                          </div>
                        );
                      })}
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
                  onClick={() => navigate(returnTo)}
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