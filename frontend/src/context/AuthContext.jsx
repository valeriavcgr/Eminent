import { createContext, useContext, useState } from 'react';

const AuthContext = createContext();

const JERARQUIA = ['ADMIN', 'OPERADOR', 'MONITOR'];

function rolDeMayorRango(roles) {
  return JERARQUIA.find((r) => roles.includes(r));
}

function leerRolesGuardados() {
  try {
    const guardado = JSON.parse(localStorage.getItem('roles'));
    return Array.isArray(guardado) ? guardado : [];
  } catch {
    return [];
  }
}

function leerRolActivoGuardado(roles) {
  const guardado = localStorage.getItem('rolActivo');
  if (guardado && roles.includes(guardado)) return guardado;
  return rolDeMayorRango(roles) || null;
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [roles, setRoles] = useState(leerRolesGuardados());
  const [rolActivo, setRolActivoState] = useState(() => leerRolActivoGuardado(leerRolesGuardados()));

  const login = (nuevoToken, nuevosRoles, rolActivoInicial) => {
    localStorage.setItem('token', nuevoToken);
    localStorage.setItem('roles', JSON.stringify(nuevosRoles));
    localStorage.setItem('rolActivo', rolActivoInicial);
    setToken(nuevoToken);
    setRoles(nuevosRoles);
    setRolActivoState(rolActivoInicial);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('rolActivo');
    setToken(null);
    setRoles([]);
    setRolActivoState(null);
  };

  const cambiarRolActivo = (nuevoRol) => {
    if (!roles.includes(nuevoRol)) return;
    localStorage.setItem('rolActivo', nuevoRol);
    setRolActivoState(nuevoRol);
  };

  return (
    <AuthContext.Provider value={{ token, roles, rolActivo, login, logout, cambiarRolActivo }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
