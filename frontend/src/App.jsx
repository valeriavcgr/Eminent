import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import RutaProtegida from './components/RutaProtegida';
import Layout from './components/Layout';
import Login from './pages/Login';
import LandingPublica from './pages/eventos/LandingPublica';
import ListaUsuarios from './pages/usuarios/ListaUsuarios';
import FormularioUsuario from './pages/usuarios/FormularioUsuario';
import ListarEvento from './pages/eventos/ListarEvento';
import FormularioEvento from './pages/eventos/FormularioEvento';
import FormularioInscripcion from './pages/participacion/FormularioInscripcion';
import ColaEspera from './pages/participacion/ColaEspera';
import ImportarCsv from './pages/participacion/ImportarCsv';
import HistorialPersona from './pages/participacion/HistorialPersona';
import ListaAsistencia from './pages/asistencia/ListaAsistencia';
import MisEventos from './pages/eventos/MisEventos';
import EscanerQr from './pages/asistencia/EscanerQr';
import Auditoria from './pages/auditoria/Auditoria';
import RecibirCertificado from './pages/certificacion/RecibirCertificado';
import VerificarCertificado from './pages/certificacion/VerificarCertificado';
import ConsultarInscripcion from './pages/participacion/ConsultarInscripcion';
import Dashboard from './pages/dashboard/Dashboard';
import VerificarCertificadoInterno from './pages/certificacion/VerificarCertificadoInterno';

import { Toaster } from 'sonner';

function App() {
  return (
    <AuthProvider>
      <Toaster position="top-right" richColors />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPublica />} />
          <Route path="/eventos/registro/:eventoId" element={<FormularioInscripcion />} />
          <Route path="/certificados/recibir" element={<RecibirCertificado />} />
          <Route path="/inscripciones/consultar" element={<ConsultarInscripcion />} />
          <Route path="/certificados/verificar" element={<VerificarCertificado />} />
          <Route path="/login" element={<Login />} />


          <Route element={<RutaProtegida><Layout /></RutaProtegida>}>
            <Route path="/usuarios" element={<ListaUsuarios />} />
            <Route path="/usuarios/nuevo" element={<FormularioUsuario />} />
            <Route path="/usuarios/editar/:id" element={<FormularioUsuario />} />

            <Route path="/eventos" element={<ListarEvento />} />
            <Route path="/eventos/nuevo" element={<FormularioEvento />} />
            <Route path="/eventos/editar/:id" element={<FormularioEvento />} />
            <Route path="/eventos/:id/cola-espera" element={<ColaEspera />} />
            <Route path="/eventos/:id/importar-csv" element={<ImportarCsv />} />
            <Route path="/eventos/:id/asistencia" element={<ListaAsistencia />} />
            <Route path="/eventos/mis-eventos" element={<MisEventos />} />
            <Route path="/eventos/:id/escaner" element={<EscanerQr />} />

            <Route path="/participacion/historial" element={<HistorialPersona />} />

            <Route path="/certificados/verificar-interno" element={<VerificarCertificadoInterno />} />

            <Route path="/dashboard" element={<Dashboard />} />

            <Route path="/auditoria" element={<Auditoria />} />

          </Route>

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;