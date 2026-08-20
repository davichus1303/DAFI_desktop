# DAFI Desktop

**Despacho de Asesoría Funeraria Integral** - Sistema de administración de clientes

## Descripción

Aplicación de escritorio para administrar los clientes de un despacho funerario. Permite gestionar información de contratos, clientes y su estado.

## Características

- Autenticación segura con Argon2id
- Cifrado de datos con AES-256-GCM
- Persistencia local en JSON cifrado
- Interfaz gráfica con JavaFX
- Arquitectura Hexagonal

## Requisitos

- Java 17 o superior
- Maven 3.6+

## Instalación

```bash
# Clonar el repositorio
git clone <url-del-repositorio>

# Navegar al directorio del proyecto
cd dafi-desktop

# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run
```

## Configuración Inicial

Al iniciar la aplicación por primera vez, se solicitará:

1. Nombre de usuario administrador
2. Contraseña del administrador

La contraseña se almacenará de forma segura utilizando Argon2id.

## Estructura del Proyecto

```
dafi-desktop/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dafi/desktop/
│   │   │       ├── domain/          # Entidades y reglas de negocio
│   │   │       ├── application/     # Casos de uso y puertos
│   │   │       ├── adapters/        # Adaptadores inbound/outbound
│   │   │       └── infrastructure/  # Configuración de la aplicación
│   │   └── resources/
│   │       ├── fxml/               # Vistas FXML
│   │       └── css/                # Estilos
│   └── test/                       # Pruebas unitarias
└── pom.xml
```

## Seguridad

- Contraseñas: Argon2id (no almacena texto plano)
- Datos: AES-256-GCM (confidencialidad + integridad)
- Claves: Almacenamiento abstracto (extensible a keyring del SO)

## Datos

Los datos se almacenan en:`~/.dafi/`:

- `config/` - Configuración y credenciales
- `data/` - Datos cifrado de clientes

**IMPORTANTE**: Estos archivos contienen información sensible y no deben compartirse.

## Licencia

Propietario - Uso interno
