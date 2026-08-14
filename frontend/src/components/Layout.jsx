import { useEffect, useRef, useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { colorFondoRol, colorTextoRol } from '../utils/rolColores';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';
import {
  Users,
  Calendar,
  History,
  ShieldAlert,
  LogOut,
  Menu,
  X,
  AlertTriangle,
  ChevronRight,
  ChevronDown,
  UserCircle,
  LayoutDashboard
} from 'lucide-react';

export default function Layout() {
  const { roles, rolActivo, cambiarRolActivo, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [logoutModalOpen, setLogoutModalOpen] = useState(false);
  const [perfilMenuAbierto, setPerfilMenuAbierto] = useState(false);
  const perfilMenuRef = useRef(null);

  useEffect(() => {
    function handleClickFuera(e) {
      if (perfilMenuRef.current && !perfilMenuRef.current.contains(e.target)) {
        setPerfilMenuAbierto(false);
      }
    }
    document.addEventListener('mousedown', handleClickFuera);
    return () => document.removeEventListener('mousedown', handleClickFuera);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    ...(rolActivo === 'ADMIN' ? [
      { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { name: 'Usuarios', path: '/usuarios', icon: Users },
      { name: 'Eventos', path: '/eventos', icon: Calendar },
      { name: 'Historial', path: '/participacion/historial', icon: History },
      { name: 'Auditoría', path: '/auditoria', icon: ShieldAlert },
    ] : []),
    ...(rolActivo === 'OPERADOR' ? [
      { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { name: 'Eventos', path: '/eventos', icon: Calendar },
      { name: 'Historial', path: '/participacion/historial', icon: History },
    ] : []),
    ...(rolActivo === 'MONITOR' ? [
      { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { name: 'Mis Eventos', path: '/eventos/mis-eventos', icon: Calendar },
    ] : []),
  ];

  const getRoleColor = () => colorFondoRol(rolActivo);
  const getRoleTextColor = () => colorTextoRol(rolActivo);

  return (
    <div className="min-h-screen bg-[#f8f9ff] flex font-sans text-slate-800">
      
      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 text-slate-300 transition-transform duration-300 ease-in-out ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:relative md:translate-x-0 shadow-xl`}>
        <div className="h-16 flex items-center justify-between px-4 bg-slate-950">
          <div className="flex items-center gap-2">
            <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${getRoleColor()}`}>
              <span className="text-white font-bold">E</span>
            </div>
            <span className="text-white font-bold text-lg tracking-tight">EMINENT</span>
          </div>
          <button onClick={() => setSidebarOpen(false)} className="md:hidden text-slate-400 hover:text-white">
            <X size={20} />
          </button>
        </div>

        <div className="p-4">
          <nav className="space-y-1">
            {menuItems.map((item) => {
              const isActive = location.pathname.startsWith(item.path);
              const Icon = item.icon;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive 
                      ? 'bg-blue-600/10 text-white border border-blue-500/20' 
                      : 'hover:bg-slate-800 hover:text-white'
                  }`}
                >
                  <Icon className={`w-5 h-5 mr-3 ${isActive ? getRoleTextColor() : 'text-slate-400'}`} />
                  {item.name}
                </Link>
              );
            })}
          </nav>
        </div>

        <div className="absolute bottom-0 w-full p-4 border-t border-slate-800 flex flex-col gap-4">
          {roles.length > 1 ? (
            <div className="px-3 py-2 bg-slate-800/50 rounded-lg border border-slate-700/50">
              <label className="block text-xs font-medium text-slate-400 mb-1">Ver como</label>
              <div className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full shrink-0 ${getRoleColor()}`}></div>
                <select
                  value={rolActivo}
                  onChange={(e) => {
                    cambiarRolActivo(e.target.value);
                    navigate('/dashboard');
                  }}
                  className="w-full bg-transparent text-sm font-medium text-white focus:outline-none cursor-pointer"
                >
                  {roles.map((r) => (
                    <option key={r} value={r} className="bg-slate-800 text-white">{r}</option>
                  ))}
                </select>
              </div>
            </div>
          ) : (
            <div className="px-3 py-2 bg-slate-800/50 rounded-lg border border-slate-700/50">
              <div className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${getRoleColor()}`}></div>
                <p className="text-sm font-medium text-white">{rolActivo}</p>
              </div>
            </div>
          )}
          <button
            onClick={() => setLogoutModalOpen(true)}
            className="flex items-center w-full px-3 py-2.5 text-sm font-medium text-slate-400 rounded-lg hover:bg-slate-800 hover:text-white transition-colors"
          >
            <LogOut className="w-5 h-5 mr-3" />
            Cerrar Sesión
          </button>
        </div>
      </aside>

      <Modal
        isOpen={logoutModalOpen}
        onClose={() => setLogoutModalOpen(false)}
        title="Cerrar Sesión"
      >
        <div className="flex flex-col items-center text-center space-y-4">
          <div className="bg-red-100 p-3 rounded-full text-red-600">
            <AlertTriangle className="w-8 h-8" />
          </div>
          <p className="text-slate-700">
            ¿Estás seguro de que deseas cerrar sesión?
          </p>
          <div className="flex w-full gap-3 mt-4">
            <Button variant="secondary" className="flex-1" onClick={() => setLogoutModalOpen(false)}>
              Cancelar
            </Button>
            <Button className="flex-1 bg-red-600 hover:bg-red-700 text-white" onClick={handleLogout}>
              Sí, Cerrar Sesión
            </Button>
          </div>
        </div>
      </Modal>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header */}
      <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-4 sm:px-6 lg:px-8 shrink-0 z-40 shadow-sm">
        <div className="flex items-center">
          <button
            onClick={() => setSidebarOpen(true)}
            className="md:hidden text-gray-500 hover:text-gray-700 mr-4"
          >
            <Menu size={24} />
          </button>
          <div className="flex items-center gap-2">
            <span className={`w-1.5 h-6 rounded-full ${getRoleColor()}`}></span>
            <h1 className="text-lg font-bold text-slate-900 capitalize">
              {menuItems.find((item) => location.pathname.startsWith(item.path))?.name || 'Panel'}
            </h1>
          </div>
        </div>

        {roles.includes('ADMIN') ? (
          <div className="relative" ref={perfilMenuRef}>
            <button
              onClick={() => setPerfilMenuAbierto((abierto) => !abierto)}
              className="flex items-center gap-3 rounded-lg px-1.5 py-1 hover:bg-slate-100 transition-colors"
            >
              <div className={`w-9 h-9 rounded-full flex items-center justify-center text-white font-bold text-sm ${getRoleColor()}`}>
                {rolActivo?.[0]}
              </div>
              <div className="hidden sm:block text-left">
                <p className="text-sm font-semibold text-slate-800 leading-tight">{rolActivo}</p>
              </div>
              <ChevronDown className={`w-4 h-4 text-slate-400 transition-transform ${perfilMenuAbierto ? 'rotate-180' : ''}`} />
            </button>

            {perfilMenuAbierto && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-50">
                <Link
                  to="/perfil"
                  onClick={() => setPerfilMenuAbierto(false)}
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
                >
                  <UserCircle className="w-4 h-4 text-slate-400" />
                  Mi Perfil
                </Link>
              </div>
            )}
          </div>
        ) : (
          <div className="flex items-center gap-3">
            <div className={`w-9 h-9 rounded-full flex items-center justify-center text-white font-bold text-sm ${getRoleColor()}`}>
              {rolActivo?.[0]}
            </div>
            <div className="hidden sm:block">
              <p className="text-sm font-semibold text-slate-800 leading-tight">{rolActivo}</p>
            </div>
          </div>
        )}
      </header>

        {/* Page Content */}
        {/* key=rolActivo fuerza a React a desmontar y volver a montar la página
            actual al cambiar de rol, para que recargue sus datos con el rol nuevo
            en vez de quedarse con lo que ya había cargado con el rol anterior. */}
        <main className="flex-1 overflow-auto p-4 sm:p-6 lg:p-8">
          <Outlet key={rolActivo} />
        </main>
      </div>

      {/* Overlay for mobile sidebar */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-slate-900/50 z-40 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </div>
  );
}