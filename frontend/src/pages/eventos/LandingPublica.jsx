import { useState, useEffect } from 'react';
import { listarEventosPublicos } from '../../services/eventoService';
import { Link } from 'react-router-dom';
import { Calendar, Users, ChevronRight, BookOpen, MonitorPlay, Trophy } from 'lucide-react';
import { format, isToday, isWeekend, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';
import { Button } from '../../components/ui/Button';
import PublicNavbar from '../../components/PublicNavbar';

export default function LandingPublica() {
  const [eventos, setEventos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filtroFecha, setFiltroFecha] = useState('Todo'); // 'Todo', 'Hoy', 'Este fin de semana'
  const [limiteActivos, setLimiteActivos] = useState(6);

  useEffect(() => {
    cargarEventos();
  }, []);

  const cargarEventos = async () => {
    try {
      const data = await listarEventosPublicos();
      setEventos(data || []);
    } catch (error) {
      console.error('Error cargando eventos públicos:', error);
    } finally {
      setLoading(false);
    }
  };

  const getTipoBadge = (tipo) => {
    switch (tipo) {
      case 'TALLER': return 'bg-blue-100 text-blue-800';
      case 'CAPACITACION': return 'bg-emerald-100 text-emerald-800';
      case 'TORNEO': return 'bg-orange-100 text-orange-800';
      default: return 'bg-slate-100 text-slate-800';
    }
  };

  const getGradient = (tipo) => {
    switch (tipo) {
      case 'TALLER': return 'from-blue-600 to-blue-800';
      case 'CAPACITACION': return 'from-emerald-600 to-emerald-800';
      case 'TORNEO': return 'from-orange-500 to-orange-700';
      default: return 'from-slate-600 to-slate-800';
    }
  };

  const getTipoIcon = (tipo) => {
    switch (tipo) {
      case 'TALLER': return BookOpen;
      case 'CAPACITACION': return MonitorPlay;
      case 'TORNEO': return Trophy;
      default: return Calendar;
    }
  };

  // Filtrado
  const eventosFiltrados = eventos.filter(evento => {
    // 1. Filtro por búsqueda
    if (searchQuery && !evento.nombre.toLowerCase().includes(searchQuery.toLowerCase())) {
      return false;
    }
    
    // 2. Filtro por fecha (Solo aplica a eventos activos)
    if (evento.estado === 'PROGRAMADO' || evento.estado === 'EN CURSO') {
      if (filtroFecha === 'Hoy' && evento.fechaInicio) {
        return isToday(parseISO(evento.fechaInicio));
      }
      if (filtroFecha === 'Este fin de semana' && evento.fechaInicio) {
        return isWeekend(parseISO(evento.fechaInicio));
      }
    }
    return true;
  });

  const eventosActivos = eventosFiltrados.filter(e => e.estado === 'PROGRAMADO' || e.estado === 'EN CURSO');
  const eventosFinalizados = eventosFiltrados.filter(e => e.estado === 'FINALIZADO' || e.estado === 'CANCELADO').slice(0, 6);

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-900">
      {/* 1. Navbar */}
      <PublicNavbar searchQuery={searchQuery} onSearchChange={setSearchQuery} />

      {/* 2. Hero Section - Publicidad Eminent */}
      <div className="relative overflow-hidden bg-slate-900">
        <div className="absolute inset-0">
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-blue-900/40 via-slate-900 to-slate-900"></div>
          <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-blue-600/10 rounded-full blur-3xl"></div>
        </div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-32 text-center lg:pt-32 lg:pb-40">
          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight text-white mb-6">
            La plataforma líder en <br className="hidden md:block"/>
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-300">
              gestión de eventos corporativos
            </span>
          </h1>
          <p className="mt-4 max-w-2xl text-xl text-slate-300 mx-auto font-medium">
            EMINENT centraliza talleres, capacitaciones y torneos en una experiencia premium. Conecta, aprende y certifícate con los mejores.
          </p>
        </div>
      </div>

      {/* 3. Categorías de Eventos */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-16 relative z-10 mb-20">
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
          <div className="bg-white rounded-2xl p-6 shadow-xl shadow-slate-200/50 border border-slate-100 flex items-center gap-4 hover:-translate-y-1 transition-transform">
            <div className="w-14 h-14 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
              <BookOpen className="w-7 h-7" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900">Talleres</h3>
              <p className="text-sm text-slate-500">Prácticos y directos</p>
            </div>
          </div>
          <div className="bg-white rounded-2xl p-6 shadow-xl shadow-slate-200/50 border border-slate-100 flex items-center gap-4 hover:-translate-y-1 transition-transform">
            <div className="w-14 h-14 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
              <MonitorPlay className="w-7 h-7" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900">Capacitaciones</h3>
              <p className="text-sm text-slate-500">Formación continua</p>
            </div>
          </div>
          <div className="bg-white rounded-2xl p-6 shadow-xl shadow-slate-200/50 border border-slate-100 flex items-center gap-4 hover:-translate-y-1 transition-transform">
            <div className="w-14 h-14 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center shrink-0">
              <Trophy className="w-7 h-7" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900">Torneos</h3>
              <p className="text-sm text-slate-500">Compite y destaca</p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-24">
        
        {/* 4. Filtros Rápidos */}
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-10 gap-4">
          <h2 className="text-3xl font-bold tracking-tight text-slate-900">Próximos Eventos</h2>
          
          <div className="flex items-center gap-2 p-1 bg-slate-200/50 rounded-full w-max">
            {['Todo', 'Hoy', 'Este fin de semana'].map(filtro => (
              <button
                key={filtro}
                onClick={() => setFiltroFecha(filtro)}
                className={`px-5 py-2 rounded-full text-sm font-medium transition-all ${
                  filtroFecha === filtro 
                    ? 'bg-white text-blue-600 shadow-sm' 
                    : 'text-slate-500 hover:text-slate-900'
                }`}
              >
                {filtro}
              </button>
            ))}
          </div>
        </div>

        {/* 5. Grid de Eventos Activos */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : eventosActivos.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-2xl border border-dashed border-slate-300">
            <Calendar className="mx-auto h-12 w-12 text-slate-300 mb-4" />
            <h3 className="text-lg font-medium text-slate-900">No hay eventos para esta fecha</h3>
            <p className="mt-1 text-slate-500">Intenta quitar los filtros o buscar otra cosa.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {eventosActivos.slice(0, limiteActivos).map((evento) => (
                <div key={evento.id} className="bg-white rounded-3xl border border-slate-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 overflow-hidden group flex flex-col">
                  {/* Header con gradiente */}
                  {(() => { const Icon = getTipoIcon(evento.tipo); return (
                  <div className={`h-40 relative overflow-hidden bg-gradient-to-br ${getGradient(evento.tipo)}`}>
                    <div className="absolute -right-6 -bottom-6 opacity-10">
                      <Icon className="w-40 h-40 text-white" strokeWidth={1} />
                    </div>
                    <div className="absolute top-4 left-4">
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-white/20 text-white backdrop-blur-sm">
                        <Icon className="w-3.5 h-3.5" />
                        {evento.tipo}
                      </span>
                    </div>
                    {evento.modalidad && (
                      <div className="absolute bottom-4 left-4">
                        <span className="text-white/80 text-xs font-medium">{evento.modalidad}</span>
                      </div>
                    )}
                  </div>
                  ); })()}
                  
                  {/* Contenido */}
                  <div className="p-6 flex flex-col flex-1">
                    <h3 className="text-xl font-bold text-slate-900 mb-3 line-clamp-2">
                      {evento.nombre}
                    </h3>
                    
                    <div className="space-y-2 mb-8 flex-1">
                      <div className="flex items-center text-sm text-slate-700 font-medium">
                        <Calendar className="w-4 h-4 mr-2 text-slate-500 shrink-0" />
                        <span>
                          {evento.fechaInicio && format(new Date(evento.fechaInicio), "EEEE, d 'de' MMMM", { locale: es })}
                        </span>
                      </div>
                      <div className="flex items-center text-sm text-slate-700 font-medium">
                        <Users className="w-4 h-4 mr-2 text-slate-500 shrink-0" />
                        <span>
                          {Math.max(0, evento.aforo - (evento.inscritos || 0))} cupos disponibles
                        </span>
                      </div>
                    </div>
                    
                    <Link
                      to={`/eventos/registro/${evento.id}`}
                      className="w-full inline-flex justify-center items-center px-4 py-3 bg-slate-900 text-white rounded-xl text-sm font-semibold hover:bg-blue-600 transition-colors group-hover:shadow-md"
                    >
                      Reservar Cupo <ChevronRight className="ml-2 w-4 h-4" />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
            
            {/* Botón Ver Más */}
            {eventosActivos.length > limiteActivos && (
              <div className="mt-12 flex justify-center">
                <Button 
                  variant="outline" 
                  className="rounded-full px-8 text-slate-600 border-slate-300 hover:bg-slate-50"
                  onClick={() => setLimiteActivos(prev => prev + 6)}
                >
                  Ver más eventos
                </Button>
              </div>
            )}
          </>
        )}

        {/* 6. Eventos Finalizados */}
        {eventosFinalizados.length > 0 && (
          <div className="mt-32">
            <h2 className="text-2xl font-bold tracking-tight text-slate-900 mb-8 border-t border-slate-200 pt-10">
              Nuestros últimos eventos
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {eventosFinalizados.map((evento) => (
                <div key={evento.id} className="bg-white rounded-2xl border border-slate-100 p-5 flex flex-col opacity-70 hover:opacity-100 transition-opacity grayscale hover:grayscale-0">
                  <span className="text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">{evento.tipo}</span>
                  <h3 className="text-base font-bold text-slate-900 line-clamp-2 mb-2">{evento.nombre}</h3>
                  <div className="mt-auto text-sm text-slate-500 flex items-center">
                    <Calendar className="w-4 h-4 mr-1.5" />
                    {evento.fechaInicio && format(new Date(evento.fechaInicio), "MMM yyyy", { locale: es })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 7. Footer */}
      <footer className="bg-slate-900 border-t border-slate-800 pt-16 pb-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
            <div>
              <div className="flex items-center gap-2 mb-4">
                <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                  <span className="text-white font-bold text-lg">E</span>
                </div>
                <span className="font-bold text-xl tracking-tight text-white">EMINENT</span>
              </div>
              <p className="text-slate-400 text-sm max-w-xs leading-relaxed">
                Plataforma integral para la gestión y participación en eventos corporativos y educativos de alto nivel.
              </p>
            </div>
            <div>
              <h4 className="font-bold text-white mb-4">Enlaces Rápidos</h4>
              <ul className="space-y-3 text-sm text-slate-400">
                <li><Link to="/certificados/verificar" className="hover:text-blue-400 transition-colors">Verificar Certificado</Link></li>
                <li><Link to="/certificados/recibir" className="hover:text-blue-400 transition-colors">Descargar Certificado</Link></li>
                <li><Link to="/inscripciones/consultar" className="hover:text-blue-400 transition-colors">Consultar Inscripción</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold text-white mb-4">Soporte</h4>
              <ul className="space-y-3 text-sm text-slate-400">
                <li className="hover:text-slate-200 cursor-pointer transition-colors">Contacto</li>
                <li className="hover:text-slate-200 cursor-pointer transition-colors">Preguntas Frecuentes</li>
                <li className="hover:text-slate-200 cursor-pointer transition-colors">Términos de Servicio</li>
              </ul>
            </div>
          </div>
          <div className="border-t border-slate-800 pt-8 flex flex-col md:flex-row justify-between items-center">
            <p className="text-sm text-slate-500 font-medium">
              &copy; {new Date().getFullYear()} EMINENT. Todos los derechos reservados.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
