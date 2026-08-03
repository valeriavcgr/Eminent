import { useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Users, 
  Calendar, 
  History, 
  ShieldAlert, 
  LogOut, 
  Menu, 
  X,
  ChevronRight,
  LayoutDashboard
} from 'lucide-react';

export default function Layout() {
  const { rol, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    ...(rol === 'ADMIN' ? [
      { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { name: 'Usuarios', path: '/usuarios', icon: Users },
      { name: 'Eventos', path: '/eventos', icon: Calendar },
      { name: 'Historial', path: '/participacion/historial', icon: History },
      { name: 'Auditoría', path: '/auditoria', icon: ShieldAlert },
    ] : []),
    ...(rol === 'OPERADOR' ? [
      { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { name: 'Eventos', path: '/eventos', icon: Calendar },
      { name: 'Historial', path: '/participacion/historial', icon: History },
    ] : []),
    ...(rol === 'MONITOR' ? [
      { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { name: 'Mis Eventos', path: '/eventos/mis-eventos', icon: Calendar },
    ] : []),
  ];

  // Determinar color de acento según rol
  const getRoleColor = () => {
    switch (rol) {
      case 'ADMIN': return 'bg-blue-600';
      case 'OPERADOR': return 'bg-emerald-600';
      case 'MONITOR': return 'bg-orange-500';
      default: return 'bg-gray-800';
    }
  };

  const getRoleTextColor = () => {
    switch (rol) {
      case 'ADMIN': return 'text-blue-600';
      case 'OPERADOR': return 'text-emerald-600';
      case 'MONITOR': return 'text-orange-500';
      default: return 'text-gray-800';
    }
  };

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
          <div className="px-3 py-2 bg-slate-800/50 rounded-lg border border-slate-700/50">
            <div className="flex items-center gap-2">
              <div className={`w-2 h-2 rounded-full ${getRoleColor()}`}></div>
              <p className="text-sm font-medium text-white">{rol}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center w-full px-3 py-2.5 text-sm font-medium text-slate-400 rounded-lg hover:bg-slate-800 hover:text-white transition-colors"
          >
            <LogOut className="w-5 h-5 mr-3" />
            Cerrar Sesión
          </button>
        </div>
      </aside>

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

        <div className="flex items-center gap-3">
          <div className={`w-9 h-9 rounded-full flex items-center justify-center text-white font-bold text-sm ${getRoleColor()}`}>
            {rol?.[0]}
          </div>
          <div className="hidden sm:block">
            <p className="text-sm font-semibold text-slate-800 leading-tight">{rol}</p>
          </div>
        </div>
      </header>

        {/* Page Content */}
        <main className="flex-1 overflow-auto p-4 sm:p-6 lg:p-8">
          <Outlet />
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