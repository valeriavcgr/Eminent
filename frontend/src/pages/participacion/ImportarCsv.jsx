import { useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { previsualizarCsv, confirmarCsv } from '../../services/participacionService';
import { toast } from 'sonner';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import {
  UploadCloud, FileSpreadsheet, X, CheckCircle2,
  XCircle, ArrowLeft, ClipboardCheck
} from 'lucide-react';
import Pagination from '../../components/Pagination';

export default function ImportarCsv() {
  const { id } = useParams();
  const navigate = useNavigate();
  const inputRef = useRef(null);

  const [archivo, setArchivo] = useState(null);
  const [arrastrando, setArrastrando] = useState(false);
  const [filas, setFilas] = useState(null);
  const [resumen, setResumen] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [paginaActual, setPaginaActual] = useState(1);
  const FILAS_POR_PAGINA = 10;

  const seleccionarArchivo = async (file) => {
    if (!file) return;
    if (!file.name.toLowerCase().endsWith('.csv')) {
      toast.error('El archivo debe tener extensión .csv');
      return;
    }
    setArchivo(file);
    setResumen(null);
    setFilas(null);
    setPaginaActual(1);
    setCargando(true);
    try {
      const datos = await previsualizarCsv(id, file);
      setFilas(datos);
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al previsualizar el archivo');
    } finally {
      setCargando(false);
    }
  };

  const handleInputChange = (e) => seleccionarArchivo(e.target.files[0]);

  const handleDrop = (e) => {
    e.preventDefault();
    setArrastrando(false);
    seleccionarArchivo(e.dataTransfer.files[0]);
  };

  const handleConfirmar = async () => {
    setCargando(true);
    try {
      const datos = await confirmarCsv(id, archivo);
      setResumen(datos);
      setFilas(null);
      toast.success('Importación completada');
    } catch (err) {
      toast.error(err.response?.data?.mensaje || 'Error al confirmar la importación');
    } finally {
      setCargando(false);
    }
  };

  const quitarArchivo = () => {
    setArchivo(null);
    setFilas(null);
    setResumen(null);
    if (inputRef.current) inputRef.current.value = '';
  };

  const filasValidas = filas?.filter((f) => f.valida).length || 0;
  const filasInvalidas = (filas?.length || 0) - filasValidas;

  const totalPaginas = Math.ceil((filas?.length || 0) / FILAS_POR_PAGINA);
  const filasPagina = (filas || []).slice(
    (paginaActual - 1) * FILAS_POR_PAGINA,
    paginaActual * FILAS_POR_PAGINA
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Importar participantes</h1>
        </div>
        <Button variant="secondary" icon={ArrowLeft} onClick={() => navigate('/eventos')}>
          Volver a Eventos
        </Button>
      </div>

      <Card>
        <CardContent className="p-6">
          {!archivo ? (
            <div
              onDragOver={(e) => { e.preventDefault(); setArrastrando(true); }}
              onDragLeave={() => setArrastrando(false)}
              onDrop={handleDrop}
              onClick={() => inputRef.current?.click()}
              className={`border-2 border-dashed rounded-xl p-10 text-center cursor-pointer transition-colors ${
                arrastrando ? 'border-blue-500 bg-blue-50' : 'border-slate-300 hover:border-slate-400 bg-slate-50'
              }`}
            >
              <UploadCloud className="w-10 h-10 mx-auto mb-3 text-slate-400" />
              <p className="text-slate-600 font-medium">Arrastra tu archivo CSV aquí</p>
              <p className="text-sm text-slate-400 mt-1">o haz clic para seleccionarlo</p>
              <input ref={inputRef} type="file" accept=".csv" onChange={handleInputChange} className="hidden" />
            </div>
          ) : (
            <div className="flex items-center justify-between border border-slate-200 rounded-lg p-4 bg-slate-50">
              <div className="flex items-center gap-3">
                <FileSpreadsheet className="w-8 h-8 text-emerald-600" />
                <div>
                  <p className="font-medium text-slate-800">{archivo.name}</p>
                  <p className="text-xs text-slate-500">{(archivo.size / 1024).toFixed(1)} KB</p>
                </div>
              </div>
              <button onClick={quitarArchivo} className="text-slate-400 hover:text-red-500 transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>
          )}

          <div className="flex gap-3 mt-4">
            {filas && (
              <Button
                icon={UploadCloud}
                disabled={cargando}
                onClick={handleConfirmar}
                className="bg-emerald-600 hover:bg-emerald-700 text-white"
              >
                Confirmar importación
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {resumen && (
        <Card>
          <CardContent className="p-6 flex items-center gap-6">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-600" />
              <span className="font-semibold text-slate-800">{resumen.exitosas} activas</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock3 className="w-5 h-5 text-orange-500" />
              <span className="font-semibold text-slate-800">{resumen.enEspera} en espera</span>
            </div>
            <div className="flex items-center gap-2">
              <XCircle className="w-5 h-5 text-red-500" />
              <span className="font-semibold text-slate-800">{resumen.fallidas} fallidas</span>
            </div>
          </CardContent>
        </Card>
      )}

      {filas && (
        <Card>
          <CardHeader className="bg-slate-50/50 flex flex-row items-center justify-between">
            <CardTitle className="text-base font-semibold">Previsualización</CardTitle>
            <div className="flex gap-2">
              <Badge variant="success">{filasValidas} válidas</Badge>
              <Badge variant="danger">{filasInvalidas} con error</Badge>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-3">Fila</th>
                    <th className="px-6 py-3">Nombre</th>
                    <th className="px-6 py-3">Apellido</th>
                    <th className="px-6 py-3">Documento</th>
                    <th className="px-6 py-3">Correo</th>
                    <th className="px-6 py-3">Teléfono</th>
                    <th className="px-6 py-3">Estado</th>
                    <th className="px-6 py-3">Motivo</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {filasPagina.map((f) => (
                    <tr key={f.numeroFila} className={!f.valida ? 'bg-red-50/50' : ''}>
                      <td className="px-6 py-3 text-slate-500">{f.numeroFila}</td>
                      <td className="px-6 py-3 text-slate-800">{f.nombre}</td>
                      <td className="px-6 py-3 text-slate-800">{f.apellido}</td>
                      <td className="px-6 py-3 text-slate-600">{f.documento}</td>
                      <td className="px-6 py-3 text-slate-600">{f.correo || '-'}</td>
                      <td className="px-6 py-3 text-slate-600">{f.telefono || '-'}</td>
                      <td className="px-6 py-3">
                        {!f.valida ? (
                          <span className="flex items-center gap-1 text-red-500"><XCircle className="w-4 h-4" /> Error</span>
                        ) : f.iraListaEspera ? (
                          <span className="flex items-center gap-1 text-orange-500"><Clock3 className="w-4 h-4" /> Lista de espera</span>
                        ) : (
                          <span className="flex items-center gap-1 text-emerald-600"><CheckCircle2 className="w-4 h-4" /> Válida</span>
                        )}
                      </td>
                      <td className="px-6 py-3 text-slate-500">{f.motivoError || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination
              paginaActual={paginaActual}
              totalPaginas={totalPaginas}
              onCambiarPagina={setPaginaActual}
            />
          </CardContent>
        </Card>
      )}
    </div>
  );
}