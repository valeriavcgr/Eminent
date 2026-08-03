# EMINENT
 
Sistema integral de gestión de eventos, participantes y certificación digital.
Plataforma FullStack (Spring Boot + React) que cubre todo el ciclo de vida de
un evento: creación, inscripción de participantes, control de asistencia por
QR y generación automática de certificados verificables.
 
---
 
## Funcionalidades
 
1. **Gestión de eventos** — talleres, capacitaciones y torneos, presenciales
   o virtuales, con control de aforo y cambio de estado automático
   (programado → en curso →  finalizado).
2. **Gestión avanzada de participantes**
   - Registro público (individual o masivo por CSV, con previsualización y
     manejo de lista de espera cuando se agota el cupo).
   - Check-in presencial (manual) o por escaneo de código QR.
   - Historial completo por persona (documento → todas sus inscripciones,
     asistencias y certificados).
3. **Certificados PDF automáticos** — generados solos al finalizar un
   evento, con nombre, curso, fecha(s), duración y un código único
   verificable.
4. **Web pública de verificación de certificados** — por código manual o
   escaneando el QR impreso en el certificado.
5. **Auditoría completa** — registra acción, entidad afectada, usuario (o
   "Sistema" si fue automático), fecha/hora e IP de origen.
6. **Importación CSV masiva** — con validación de formato, detección de
   duplicados y respeto real del cupo/lista de espera.
7. **Dashboard con filtros avanzados** — por tipo, modalidad, estado y rango
   de fechas, más un ranking de eventos por porcentaje de asistencia.
8. **Control de roles** — Administrador, Operador, Monitor e Invitado, cada
   uno con su propia vista y permisos.
---
 
## Stack tecnológico
 
**Backend**
- Java 21 + Spring Boot 3
- Spring Data JPA, Spring Security (JWT)
- PostgreSQL
- ZXing (generación de códigos QR)
- iText (generación de PDF)
- OpenCSV (importación masiva)
- Lombok
**Frontend**
- React 18 + Vite
- React Router
- Tailwind CSS
- Axios
- lucide-react (íconos), sonner (notificaciones), date-fns (fechas)
- qrcode.react (dibujar QR), html5-qrcode (escanear QR con cámara)
---
 
## Requisitos previos
 
- **Java 21** y **Maven**
- **Node.js 20+** y **npm**
- **PostgreSQL** corriendo localmente (o accesible por red)
---
 
## Instalación y ejecución
 
### 1. Base de datos
 
Crea una base de datos vacía llamada `Eminent`:
 
 
Ejecuta el script SQL de creación de tablas (ubicado en `Documentación/`)
contra esa base de datos.
 
Configura las credenciales de conexión en
`backend/src/main/resources/application.properties`
(`spring.datasource.username` / `spring.datasource.password`).
 
### 2. Backend
 
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
 
El backend queda disponible en `http://localhost:8080`.
 
> **Primer arranque:** el proyecto incluye un inicializador de datos
> (`DataInitializer`) que, **solo si la base de datos está vacía**, crea
> automáticamente 9 usuarios de prueba y 12 eventos de ejemplo (6
> finalizados con asistencia y certificados generados, 6 programados) para
> que puedas explorar el sistema sin cargar datos a mano. Si la base ya
> tiene datos, este paso se omite solo.
 
### 3. Frontend
 
```bash
cd frontend
npm install
npm run dev
```
 
El frontend queda disponible en `http://localhost:5173`.
 
> Asegúrate de que el backend esté corriendo antes de usar el frontend —
> todas las peticiones apuntan a `http://localhost:8080/api`.
 
---
 
## Cuentas de prueba (datos semilla)
 
Al arrancar el backend, se crean estas cuentas existentes: 
 
| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@eminent.com` | `Admin1234` |
| Administrador | `admin2@eminent.com` | `Admin1234` |
| Operador | `operador@eminent.com` | `Operador1` |
| Operador | `operador2@eminent.com` | `Operador1` |
| Operador | `operador3@eminent.com` | `Operador1` |
| Monitor | `monitor@eminent.com` | `Monitor12` |
| Monitor | `monitor2@eminent.com` | `Monitor12` |
| Monitor | `monitor3@eminent.com` | `Monitor12` |
| Monitor | `monitor4@eminent.com` | `Monitor12` |
 
El rol **Invitado** no requiere cuenta — se accede directamente desde la
landing pública (`http://localhost:5173`) para inscribirse a eventos,
descargar certificados o verificarlos.
 
---
 
## Estructura del proyecto
 
```
Eminent/
├── backend/                # API REST (Spring Boot)
│   └── src/main/java/com/example/Eminent/
│       ├── usuarios/        # Autenticación, CRUD de usuarios
│       ├── eventos/         # CRUD de eventos, scheduler de estados
│       ├── participacion/   # Inscripción, lista de espera, CSV, historial
│       ├── asistencia/      # Check-in manual y por QR
│       ├── certificacion/   # Generación de PDF, verificación pública
│       ├── auditoria/       #
│       ├── dashboard/       
│       ├── auth/            # JWT, seguridad
│       └── config/          # Semilla de datos inicial
├── frontend/                # SPA (React + Vite)
│   └── src/
│       ├── pages/            # Una carpeta por módulo
│       ├── components/       # Layout, componentes, UI reutilizables
│       ├── services/         # Comunicación con la API (Axios)
│       └── context/          # Estado de sesión (JWT, rol)
└── Documentación/            # HU, casos de uso, BPMN, requerimientos
```
 
---
 
## Roles del sistema
 
| Rol | Puede |
|---|---|
| **Administrador** | Todo: usuarios, eventos, auditoría, dashboard global |
| **Operador** | Crear/gestionar sus propios eventos, importar CSV, asignar monitores, dashboard de sus eventos |
| **Monitor** | Ver sus eventos asignados, registrar asistencia (manual o QR), dashboard de asistencia en vivo |
| **Invitado** | Sin cuenta — inscribirse, consultar su estado en lista de espera, descargar y verificar certificados |
 
---
 
## Documentación adicional
 
En la carpeta `Documentación/` se encuentran, por módulo: Historias de
Usuario, casos de uso, diagramas BPMN y el documento de Requerimientos
Funcionales y No Funcionales completo.
