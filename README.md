# EMINENT

Sistema integral de gestión de eventos, participantes y certificación digital.
Plataforma FullStack (Spring Boot + React) que cubre todo el ciclo de vida de
un evento: creación, inscripción de participantes, control de asistencia por
QR, calificación de eventos y generación automática de certificados
verificables.

---

## Funcionalidades

1. **Gestión de eventos** — talleres, capacitaciones y torneos, presenciales
   o virtuales, con control de aforo y cambio de estado automático
   (programado → en curso → finalizado).
2. **Gestión avanzada de participantes**
   - Registro público (individual o masivo por CSV, con previsualización y
     manejo de lista de espera cuando se agota el cupo).
   - Check-in presencial (manual) o por escaneo de código QR.
   - Historial completo por persona (filtrable por documento o correo) →
     todas sus inscripciones, asistencias y certificados.
3. **Certificados PDF automáticos** — generados solos al finalizar un
   evento, con nombre, curso, fecha(s), duración y un código único
   verificable.
4. **Web pública de verificación de certificados** — por código manual o
   escaneando el QR impreso en el certificado.
5. **Calificación de eventos** — los participantes califican (estrellas) y
   comentan un evento finalizado; el equipo organizador consulta el
   promedio y los comentarios por evento, filtrables por rango de
   calificación.
6. **Auditoría completa** — registra acción, entidad afectada, usuario (o
   "Sistema" si fue automático), fecha/hora e IP de origen.
7. **Importación CSV masiva** — con validación de formato, detección de
   duplicados y respeto real del cupo/lista de espera.
8. **Dashboard con filtros avanzados** — por tipo, modalidad, estado y rango
   de fechas, más un ranking de eventos por porcentaje de asistencia.
9. **Control de roles** — Administrador, Operador, Monitor e Invitado. Un
   mismo usuario puede tener varios roles a la vez y cambiar de "rol
   activo" sin cerrar sesión.
10. **Gestión de usuarios** — solo se permiten correos corporativos
    (`@eminent.com`); el correo no es editable una vez creada la cuenta.

---

## Stack tecnológico

**Backend**
- Java 21 + Spring Boot 3
- Spring Data JPA, Spring Security (JWT)
- PostgreSQL
- ZXing (generación de códigos QR)
- iText (generación de PDF)
- OpenCSV (importación masiva)
- Bean Validation (`@Valid`) en DTOs
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
- **Node.js 20+** y **pnpm**
- **PostgreSQL** corriendo localmente

---

## Instalación y ejecución

### 1. Base de datos

Crea una base de datos vacía llamada `Eminent` en tu PostgreSQL

Configura la contraseña de tu base de datos en
`backend/src/main/resources/application.properties`
en `spring.datasource.password=`


### 2. Backend

El backend firma los JWT con una clave que se lee de la variable de entorno
`JWT_SECRET`. Defínela antes de arrancar (usa una cadena larga y aleatoria,
de al menos 32 caracteres):

```bash
# Linux / macOS
export JWT_SECRET="escribe_una_clave_de_32_caracteres_como_minimo"
```

Luego compila y arranca el backend:

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

El backend queda disponible en `http://localhost:8080`.

> **Primer arranque:** el proyecto incluye un inicializador de datos
> (`DataInitializer`) que, **solo si la base de datos está vacía**, crea
> automáticamente usuarios de prueba y eventos de ejemplo (finalizados con
> asistencia, encuestas y certificados generados, y otros programados) para
> que puedas explorar el sistema sin cargar datos a mano. Si la base ya
> tiene datos, este paso se omite solo

### 3. Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

El frontend queda disponible en `http://localhost:5173`.

> Asegúrate de que el backend esté corriendo antes de usar el frontend —
> todas las peticiones apuntan a `http://localhost:8080/api`.

---

## Cuentas de prueba (datos semilla)

Al arrancar el backend con la base de datos vacía, se crean estas cuentas:

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@eminent.com` | `Admin1234` |
| Operador | `operador@eminent.com` | `Operador1` |
| Monitor | `monitor@eminent.com` | `Monitor12` |
| Administrador + Operador + Monitor | `supervisor@eminent.com` | `Supervisor1` |

El rol **Invitado** no requiere cuenta — se accede directamente desde la
landing pública (`http://localhost:5173`) para inscribirse a eventos,
descargar certificados, verificarlos o responder la encuesta de un evento

---

## Estructura del proyecto

```
Eminent/
├── backend/                 # API REST (Spring Boot)
│   └── src/main/java/com/example/Eminent/
│       ├── usuarios/         # Autenticación, roles, CRUD de usuarios
│       ├── eventos/          # CRUD de eventos, scheduler de cambio de estado
│       ├── participacion/    # Inscripción, lista de espera, CSV, historial
│       ├── asistencia/       # Check-in manual y por QR
│       ├── certificacion/    # Generación de PDF, verificación pública
│       ├── encuesta/         # Encuestas de satisfacción por evento
│       ├── dashboard/        # Indicadores y filtros agregados
│       ├── auditoria/        # Registro de acciones del sistema
│       ├── auth/             # JWT, seguridad, resolución de rol activo
│       └── config/           # Configuración general y semilla de datos
├── frontend/                 # SPA (React + Vite)
│   └── src/
│       ├── pages/             # Una carpeta por módulo (eventos, usuarios,
│       │                        participación, asistencia, certificación,
│       │                        auditoría, dashboard)
│       ├── components/        # Layout, componentes, UI reutilizable
│       ├── services/          # Comunicación con la API (Axios)
│       ├── context/           # Estado de sesión (JWT, rol activo)
│       └── hooks/             # Hooks compartidos
└── Documentación/             # HU, casos de uso, BPMN, requerimientos
```

---

## Roles del sistema

| Rol | Puede |
|---|---|
| **Administrador** | Todo: usuarios, eventos, auditoría, dashboard global |
| **Operador** | Crear/gestionar sus propios eventos, importar CSV, asignar monitores, dashboard de sus eventos, ver encuestas de sus eventos |
| **Monitor** | Ver sus eventos asignados, registrar asistencia (manual o QR), dashboard de asistencia en vivo |
| **Invitado** | Sin cuenta — inscribirse, consultar su estado en lista de espera, descargar y verificar certificados, responder encuestas |

Un usuario puede tener varios roles asignados a la vez (por ejemplo,
Operador + Monitor) y elegir el "rol activo" desde el frontend sin volver a
iniciar sesión.

---

## Documentación adicional

En la carpeta `Documentación/` se encuentran, por módulo: Historias de
Usuario, casos de uso, diagramas BPMN y el documento de Requerimientos
Funcionales y No Funcionales completo.
