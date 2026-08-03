import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ShieldCheck, Search, Menu, X } from 'lucide-react';
import { Button } from './ui/Button';

export default function PublicNavbar({ searchQuery, onSearchChange }) {
  const location = useLocation();
  const [menuAbierto, setMenuAbierto] = useState(false);

  const links = [
    { to: '/inscripciones/consultar', label: 'Consultar Inscripción' },
    { to: '/certificados/recibir', label: 'Descargar Certificado' },
    { to: '/certificados/verificar', label: 'Verificar Certificado', icon: ShieldCheck },
  ];

  const isLanding = location.pathname === '/';

  return (
    <nav className="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="flex-shrink-0 flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-600/20">
              <span className="text-white font-bold text-2xl tracking-tighter">E</span>
            </div>
            <span className="font-extrabold text-2xl tracking-tight text-slate-900">EMINENT</span>
          </Link>

          {isLanding && onSearchChange && (
            <div className="hidden lg:flex flex-1 max-w-sm mx-4">
              <div className="relative w-full">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="text"
                  placeholder="Buscar eventos..."
                  className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-full leading-5 bg-slate-50 placeholder-slate-400 focus:outline-none focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all sm:text-sm"
                  value={searchQuery || ''}
                  onChange={(e) => onSearchChange(e.target.value)}
                />
              </div>
            </div>
          )}

          <div className="hidden lg:flex items-center gap-6">
            {links.map(({ to, label, icon: Icon }) => (
              <Link
                key={to}
                to={to}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                  location.pathname === to
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-slate-600 hover:text-blue-600 hover:bg-slate-50'
                }`}
              >
                {Icon && <Icon className="w-4 h-4" />}
                {label}
              </Link>
            ))}
          </div>

          <div className="flex items-center gap-3">
            <Link to="/login" className="hidden sm:block">
              <Button className="rounded-full px-5 py-2 text-sm">
                Iniciar Sesión
              </Button>
            </Link>
            <button
              onClick={() => setMenuAbierto(!menuAbierto)}
              className="lg:hidden inline-flex items-center justify-center w-10 h-10 rounded-lg text-slate-600 hover:bg-slate-100 transition-colors"
              aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
            >
              {menuAbierto ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {menuAbierto && (
        <div className="lg:hidden border-t border-slate-200 bg-white/95 backdrop-blur-md px-4 py-4 space-y-1 shadow-lg">
          <Link
            to="/login"
            className="block px-4 py-3 rounded-lg text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 transition-colors text-center mb-2"
            onClick={() => setMenuAbierto(false)}
          >
            Iniciar Sesión
          </Link>
          {links.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              onClick={() => setMenuAbierto(false)}
              className={`flex items-center gap-2 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                location.pathname === to
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-slate-600 hover:text-blue-600 hover:bg-slate-50'
              }`}
            >
              {Icon && <Icon className="w-4 h-4" />}
              {label}
            </Link>
          ))}
        </div>
      )}
    </nav>
  );
}
