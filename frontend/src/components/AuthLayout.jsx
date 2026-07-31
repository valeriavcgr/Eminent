import { Link } from 'react-router-dom';
import PublicNavbar from './PublicNavbar';

export default function AuthLayout({ children, title, subtitle, hideNavbar = false }) {

  // Gradientes decorativos por tipo de formulario en lugar de imágenes externas
  const getGradient = () => {
    if (title?.includes('Certificado') || title?.includes('Verificar')) return 'from-emerald-700 to-emerald-900';
    if (title?.includes('Inscripci')) return 'from-blue-700 to-blue-900';
    return 'from-slate-800 to-slate-900';
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Navbar persistente */}
      {!hideNavbar && <PublicNavbar />}
      
      <div className="flex-1 flex flex-col md:flex-row">
        {/* Columna Izquierda: Banner decorativo */}
        <div className={`relative h-48 md:h-auto md:w-1/2 lg:w-7/12 overflow-hidden flex flex-col bg-gradient-to-br ${getGradient()}`}>
          <div className="absolute inset-0">
            <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,_var(--tw-gradient-stops))] from-blue-600/20 via-transparent to-transparent"></div>
            <div className="absolute -bottom-20 -right-20 w-96 h-96 bg-white/5 rounded-full blur-2xl"></div>
          </div>
          
          {/* Contenido sobre el gradiente */}
          <div className="relative z-10 flex-1 flex flex-col justify-between p-8 md:p-12">
            <div>
              <Link to="/" className="inline-flex items-center gap-3">
                <div className="w-10 h-10 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center">
                  <span className="text-white font-bold text-xl tracking-tighter">E</span>
                </div>
                <span className="font-extrabold text-2xl tracking-tight text-white drop-shadow-md">EMINENT</span>
              </Link>
            </div>
            
            <div className="hidden md:block mb-10">
              <h2 className="text-4xl font-bold text-white mb-4 drop-shadow-md">{title}</h2>
              <p className="text-white/70 text-lg max-w-md">
                {subtitle}
              </p>
            </div>
          </div>
        </div>

        {/* Columna Derecha: Formulario */}
        <div className="flex-1 flex flex-col justify-center bg-white relative z-20 md:w-1/2 lg:w-5/12 shadow-[-20px_0_40px_-10px_rgba(0,0,0,0.1)] rounded-t-3xl md:rounded-none -mt-6 md:mt-0">
          <div className="px-6 py-12 md:px-12 lg:px-16 w-full max-w-lg mx-auto">
            {/* Header Móvil */}
            <div className="md:hidden text-center mb-8">
              <h2 className="text-2xl font-bold text-slate-900">{title}</h2>
              <p className="text-slate-500 text-sm mt-2">{subtitle}</p>
            </div>
            
            {children}
          </div>
          
          <div className="absolute bottom-6 left-0 right-0 text-center">
            <p className="text-xs text-slate-400">
              &copy; {new Date().getFullYear()} EMINENT. Todos los derechos reservados.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
