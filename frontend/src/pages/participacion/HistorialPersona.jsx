import { useState } from 'react';
import { Link } from 'react-router-dom';
import { buscarHistorial } from '../../services/participacionService';
import { toast } from 'sonner';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Search, IdCard, Mail, Phone, User, Calendar,
  CheckCircle2, XCircle, Award, FileSearch
} from 'lucide-react';

export default function HistorialPersona() {
  const [documento, setDocumento] = useState('');
  const [resultados, setResultados] = useState(null);
  const [buscando, setBuscando] = useState(false);
  const [busquedaHecha, setBusquedaHecha] = useState(false);

  const handleBuscar = async (e) => {
    e.preventDefault();
    setBuscando(true);
    setBusquedaHecha(true);
    try {
      const datos = await buscarHistorial({ documento });
      setResultados(datos);
      if (!datos || datos.length === 0) {
        toast.error('No se encontró ningún participante con ese documento');
      }
    } catch (err) {
      toast.error('Error al buscar el historial');
      setResultados(null);
    } finally {
      setBuscando(false);
    }
  };

  const estadoInscripcionColor = (estado) => (estado === 'ACTIVA' ? 'success' : 'warning');

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Historial por persona</h1>
        <p className="text-sm text-slate-500 mt-1">Consulta el recorrido completo de un participante en la plataforma.</p>
      </div>

      <Card>
        <CardContent className="p-4">
          <form onSubmit={handleBuscar} className="flex gap-2">
            <div className="relative flex-1">
              <IdCard className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                value={documento}
                onChange={(e) => setDocumento(e.target.value)}
                placeholder="Número de documento"
                className="w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 text-sm"
                required
              />
            </div>
            <Button type="submit" icon={Search} disabled={buscando} className="bg-blue-600 hover:bg-blue-700 text-white">
              {buscando ? 'Buscando...' : 'Buscar'}
            </Button>
          </form>
        </CardContent>
      </Card>

      {busquedaHecha && !buscando && (!resultados || resultados.length === 0) && (
        <Card>
          <CardContent className="py-12 text-center text-slate-400">
            <FileSearch className="w-10 h-10 mx-auto mb-3 text-slate-300" />
            No se encontró ningún participante con ese documento.
          </CardContent>
        </Card>
      )}

      {resultados && resultados.map((ficha) => (
        <Card key={ficha.id}>
          <CardHeader className="bg-slate-50/50">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-lg">
                {ficha.nombre?.[0]}{ficha.apellido?.[0]}
              </div>
              <div>
                <CardTitle>{ficha.nombre} {ficha.apellido}</CardTitle>
                <div className="flex flex-wrap gap-x-4 gap-y-1 mt-1 text-xs text-slate-500">
                  <span className="flex items-center gap-1"><IdCard className="w-3.5 h-3.5" /> {ficha.documento}</span>
                  <span className="flex items-center gap-1"><Mail className="w-3.5 h-3.5" /> {ficha.correo || 'Sin correo'}</span>
                  <span className="flex items-center gap-1"><Phone className="w-3.5 h-3.5" /> {ficha.telefono || 'Sin teléfono'}</span>
                </div>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            <div className="divide-y divide-slate-200">
              {ficha.inscripciones.length === 0 ? (
                <p className="p-6 text-center text-slate-400 text-sm">Sin inscripciones registradas.</p>
              ) : (
                ficha.inscripciones.map((insc, idx) => (
                  <div key={idx} className="p-5 flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-6">
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-slate-900">{insc.eventoNombre}</p>
                      <div className="flex flex-wrap items-center gap-2 mt-1">
                        <Badge variant="neutral">{insc.eventoTipo}</Badge>
                        <Badge variant={estadoInscripcionColor(insc.inscripcionEstado)}>{insc.inscripcionEstado}</Badge>
                        {insc.fechaInscripcion && (
                          <span className="flex items-center gap-1 text-xs text-slate-400">
                            <Calendar className="w-3.5 h-3.5" />
                            {format(new Date(insc.fechaInscripcion), "d MMM yyyy", { locale: es })}
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-2 text-sm">
                      {insc.asistio ? (
                        <span className="flex items-center gap-1 text-emerald-600 font-medium">
                          <CheckCircle2 className="w-4 h-4" /> Asistió
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-slate-400">
                          <XCircle className="w-4 h-4" /> No asistió
                        </span>
                      )}
                    </div>

                    <div className="sm:w-48">
                      {insc.codigoCertificado ? (
                        <Link
                          to={`/certificados/verificar?codigo=${insc.codigoCertificado}`}
                          className="flex items-center gap-1 text-blue-600 hover:text-blue-700 text-sm font-medium"
                        >
                          <Award className="w-4 h-4" /> Ver certificado
                        </Link>
                      ) : (
                        <span className="text-xs text-slate-400">Sin certificado</span>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}