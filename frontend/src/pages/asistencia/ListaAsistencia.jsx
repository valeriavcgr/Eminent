import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { listarParticipantesEvento, registrarAsistenciaManual } from '../../services/asistenciaService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { 
  Users, 
  CheckSquare, 
  Percent, 
  QrCode, 
  CheckCircle2, 
  XCircle,
  ArrowLeft
} from 'lucide-react';
import { toast } from 'sonner';

export default function ListaAsistencia() {
  const { id } = useParams();
  const { rol } = useAuth();
  const [datos, setDatos] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    cargarDatos();
  }, [id]);

  const cargarDatos = async () => {
    try {
      setIsLoading(true);
      const respuesta = await listarParticipantesEvento(id);
      setDatos(respuesta);
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al cargar los participantes');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAsistio = async (inscripcionId) => {
    try {
      await registrarAsistenciaManual(inscripcionId);
      toast.success('Asistencia registrada correctamente');
      cargarDatos();
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al registrar asistencia');
    }
  };

  const isMonitor = rol === 'MONITOR';
  const themeColor = isMonitor ? 'orange' : 'emerald';

  if (isLoading && !datos) {
    return (
      <div className="flex justify-center items-center h-64">
        <p className="text-slate-500">Cargando lista de asistencia...</p>
      </div>
    );
  }

  if (!datos) {
    return (
      <div className="flex justify-center items-center h-64">
        <p className="text-slate-500">No se encontraron datos para este evento.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <Button 
              variant="ghost" 
              size="sm" 
              icon={ArrowLeft}
              className="text-slate-500 hover:text-slate-700 p-0 h-auto"
              onClick={() => navigate(-1)}
            />
            <h1 className="text-2xl font-bold tracking-tight text-slate-900">Control de Asistencia</h1>
          </div>
        </div>
        
        {isMonitor && (
          <Button 
            className={`bg-${themeColor}-600 hover:bg-${themeColor}-700 text-white shadow-md shadow-${themeColor}-200`}
            icon={QrCode}
            onClick={() => navigate(`/eventos/${id}/escaner`)}
          >
            Abrir Escáner QR
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6 flex items-center gap-4">
            <div className={`p-3 bg-${themeColor}-100 text-${themeColor}-600 rounded-lg`}>
              <Users className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Inscritos Totales</p>
              <h3 className="text-2xl font-bold text-slate-900">{datos.resumen.totalInscritos}</h3>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 flex items-center gap-4">
            <div className={`p-3 bg-${themeColor}-100 text-${themeColor}-600 rounded-lg`}>
              <CheckSquare className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Asistieron</p>
              <h3 className="text-2xl font-bold text-slate-900">{datos.resumen.totalAsistieron}</h3>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 flex items-center gap-4">
            <div className={`p-3 bg-${themeColor}-100 text-${themeColor}-600 rounded-lg`}>
              <Percent className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">% Aforo Ocupado</p>
              <h3 className="text-2xl font-bold text-slate-900">{datos.resumen.porcentajeAforoOcupado.toFixed(1)}%</h3>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="bg-slate-50/50">
          <CardTitle className="text-base font-semibold">Lista de Participantes</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                <tr>
                  <th className="px-6 py-4">Participante</th>
                  <th className="px-6 py-4">Documento</th>
                  <th className="p-3 text-left">Correo</th>
                  <th className="p-3 text-left">Teléfono</th>
                  <th className="px-6 py-4">Estado</th>
                  <th className="px-6 py-4">Método</th>
                  {isMonitor && <th className="px-6 py-4 text-right">Acciones</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {datos.participantes.length === 0 ? (
                  <tr>
                    <td colSpan={isMonitor ? "5" : "4"} className="px-6 py-8 text-center text-slate-500">
                      No hay participantes registrados.
                    </td>
                  </tr>
                ) : (
                  datos.participantes.map((p) => (
                    <tr key={p.inscripcionId} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4 font-medium text-slate-900">
                        {p.participanteNombre}
                      </td>
                      <td className="px-6 py-4 text-slate-600">
                        {p.participanteDocumento}
                      </td>
                      <td className="p-3">{p.participanteCorreo || '-'}</td>
                      <td className="p-3">{p.participanteTelefono || '-'}</td>
                      <td className="px-6 py-4">
                        {p.asistio ? (
                          <div className="flex items-center text-green-600 font-medium">
                            <CheckCircle2 className="w-4 h-4 mr-1" />
                            <span>Asistió</span>
                          </div>
                        ) : (
                          <div className="flex items-center text-red-400">
                            <XCircle className="w-4 h-4 mr-1" />
                            <span>No asistió</span>
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 text-slate-600 capitalize">
                        {p.metodo ? p.metodo.toLowerCase() : '-'}
                      </td>
                      {isMonitor && (
                        <td className="px-6 py-4 text-right">
                          {!p.asistio && (
                            <Button
                              size="sm"
                              variant="secondary"
                              className={`text-${themeColor}-700 bg-${themeColor}-50 hover:bg-${themeColor}-100 border-${themeColor}-200`}
                              onClick={() => handleAsistio(p.inscripcionId)}
                            >
                              Dar Asistencia
                            </Button>
                          )}
                        </td>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}