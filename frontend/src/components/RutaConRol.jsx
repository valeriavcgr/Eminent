import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RutaConRol({ roles, children }) {
  const { rolActivo } = useAuth();
  return roles.includes(rolActivo) ? children : <Navigate to="/dashboard" />;
}