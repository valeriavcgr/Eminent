import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Html5Qrcode } from 'html5-qrcode';
import { registrarAsistenciaPorQr } from '../../services/asistenciaService';
import { Button } from '../../components/ui/Button';
import { ArrowLeft, CheckCircle2, XCircle, Camera, StopCircle, Image as ImageIcon } from 'lucide-react';
import { toast } from 'sonner';
import { useAuth } from '../../context/AuthContext';

export default function EscanerQr() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { rol } = useAuth();
  
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [mostrarOverlay, setMostrarOverlay] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  
  const scannerRef = useRef(null);
  const procesandoRef = useRef(false);
  const fileInputRef = useRef(null);
  const startingRef = useRef(null);

  useEffect(() => {
    // Iniciar cámara automáticamente al entrar
    iniciarCamara();

    // Limpieza al desmontar el componente
    return () => {
      detenerCamara(true);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const iniciarCamara = async () => {
    if (isScanning || startingRef.current) return;

    try {
      if (!scannerRef.current) {
        scannerRef.current = new Html5Qrcode('lector-qr');
      }

      startingRef.current = scannerRef.current.start(
        { facingMode: "environment" }, // Usa cámara trasera por defecto
        { fps: 10, qrbox: { width: 250, height: 250 } },
        onScanSuccess,
        (errorMessage) => {
          // ignorar errores de frame vacío
        }
      );
      await startingRef.current;
      setIsScanning(true);
    } catch (err) {
      console.error(err);
      toast.error('No se pudo iniciar la cámara. Verifica los permisos.');
    } finally {
      startingRef.current = null;
    }
  };

  const detenerCamara = async (isUnmounting = false) => {
    // Si hay un start() en curso, hay que esperarlo: la librería no admite
    // stop() mientras su máquina de estados sigue en transición (lanza
    // "Cannot transition to a new state, already under transition").
    if (startingRef.current) {
      try {
        await startingRef.current;
      } catch {
        // start() falló (p. ej. sin permisos de cámara): nada que detener.
      }
    }

    if (scannerRef.current && scannerRef.current.isScanning) {
      try {
        await scannerRef.current.stop();
        scannerRef.current.clear();
        if (!isUnmounting) setIsScanning(false);
      } catch (err) {
        console.error("Error al detener la cámara", err);
      }
    }
  };

  const onScanSuccess = async (contenidoQr) => {
    if (procesandoRef.current) return;
    procesandoRef.current = true;

    // Pausar temporalmente la cámara
    if (scannerRef.current && scannerRef.current.isScanning) {
      await scannerRef.current.pause();
    }

    let exito = false;
    try {
      await registrarAsistenciaPorQr(contenidoQr, id);
      setTipoMensaje('exito');
      setMensaje('¡Acceso Permitido!');
      exito = true;
    } catch (err) {
      setTipoMensaje('error');
      setMensaje(err.response?.data?.mensaje || 'QR Inválido o Ya Registrado');
    }

    setMostrarOverlay(true);

    // Cerrar overlay y redirigir o reanudar
    setTimeout(() => {
      setMostrarOverlay(false);
      setTimeout(async () => {
        procesandoRef.current = false;
        setMensaje('');
        
// Detener cámara antes de cambiar de vista
        await detenerCamara();
        // Redirigir SIEMPRE a la asistencia del evento actual usando el 'id' de la URL
        navigate(`/eventos/${id}/asistencia`);
      }, 300); // Tiempo para la animación de salida
    }, 1500); // Reducido a 1.5 segundos para que la redirección sea más rápida
  };

  const handleFileUpload = async (e) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      try {
        if (isScanning) await detenerCamara();
        
        if (!scannerRef.current) {
          scannerRef.current = new Html5Qrcode('lector-qr');
        }
        
        const decodedText = await scannerRef.current.scanFile(file, true);
        onScanSuccess(decodedText);
      } catch (err) {
        toast.error('No se detectó ningún código QR en la imagen.');
      }
      
      // Limpiar input
      e.target.value = '';
    }
  };

  return (
    <div className="flex flex-col h-full min-h-[80vh] relative">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-2">
          <Button 
            variant="ghost" 
            size="sm" 
            icon={ArrowLeft}
            className="text-slate-500 hover:text-slate-700 p-0 h-auto"
            onClick={() => navigate(-1)}
          />
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Escáner QR</h1>
        </div>
      </div>

      <div className="flex-1 flex flex-col items-center max-w-lg mx-auto w-full space-y-6">
        <p className="text-slate-500 text-center">
          Apunta la cámara al código QR del participante para registrar su ingreso.
        </p>

        {/* Controles Personalizados (En Español) */}
        <div className="flex flex-wrap justify-center gap-3 w-full">
          {!isScanning ? (
            <Button 
              className="bg-orange-600 hover:bg-orange-700 text-white flex-1"
              icon={Camera}
              onClick={iniciarCamara}
            >
              Iniciar Cámara
            </Button>
          ) : (
            <Button 
              variant="outline"
              className="text-red-600 border-red-200 hover:bg-red-50 flex-1"
              icon={StopCircle}
              onClick={() => detenerCamara(false)}
            >
              Detener Cámara
            </Button>
          )}

          <Button 
            variant="secondary"
            className="bg-slate-100 hover:bg-slate-200 text-slate-700 flex-1 relative overflow-hidden"
            icon={ImageIcon}
            onClick={() => fileInputRef.current?.click()}
          >
            Subir Imagen
            <input 
              type="file" 
              ref={fileInputRef}
              accept="image/*" 
              onChange={handleFileUpload}
              className="hidden" 
            />
          </Button>
        </div>

        {/* Contenedor del Lector */}
        <div className="bg-white p-2 rounded-2xl shadow-lg shadow-orange-100 w-full overflow-hidden border-2 border-orange-200 min-h-[300px] flex items-center justify-center bg-slate-50 relative">
          <div id="lector-qr" className="w-full"></div>
          {!isScanning && (
            <div className="absolute inset-0 flex flex-col items-center justify-center bg-slate-50 text-slate-400 p-8 z-10">
              <Camera className="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>Cámara apagada</p>
            </div>
          )}
        </div>
      </div>

      {/* Overlay de Verificación a Pantalla Completa */}
      {mostrarOverlay && (
        <div 
          className={`fixed inset-0 z-50 flex flex-col items-center justify-center p-6 animate-in fade-in zoom-in duration-300
            ${tipoMensaje === 'exito' ? 'bg-green-600' : 'bg-red-600'}
          `}
        >
          <div className="bg-white rounded-full p-6 shadow-2xl mb-8 transform scale-150 animate-bounce">
            {tipoMensaje === 'exito' ? (
              <CheckCircle2 className="w-16 h-16 text-green-600" />
            ) : (
              <XCircle className="w-16 h-16 text-red-600" />
            )}
          </div>
          
          <h2 className="text-4xl font-extrabold text-white text-center tracking-tight drop-shadow-lg">
            {mensaje}
          </h2>
          
          {tipoMensaje === 'exito' && (
            <p className="text-green-100 text-xl mt-4 text-center font-medium">
              Asistencia registrada correctamente
            </p>
          )}
        </div>
      )}
    </div>
  );
}