# 🎓 Ciudadano Digital

## 📄 Descripción

Ciudadano Digital es una plataforma educativa compuesta por un backend (API) y una aplicación móvil en Android. Su objetivo es promover la formación ciudadana mediante un asistente conversacional basado en IA, permitiendo a los usuarios aprender valores, civismo y pensamiento crítico a través de diálogos guiados. Incluye autenticación, gestión de documentos, trazabilidad de sesiones, evaluación del desempeño del sistema y búsqueda semántica mediante vectores.

## 💻 Tecnologías Utilizadas

- Node.js + Express – Backend principal
- Python – Procesamiento de lenguaje natural, Pinecone/OpenAI
- PostgreSQL – Base de datos relacional
- Pinecone – Base de datos vectorial
- OpenAI API – Embeddings y modelo LLM
- AWS S3 – Almacenamiento de documentos
- Kotlin + Jetpack Compose – Aplicación Android
- MVVM + Hilt + Retrofit + Room – Arquitectura de la app
- Gradle – Gestión y compilación del cliente Android

## ✅ Requisitos Previos

- Node.js v18+
- Python 3.10+
- PostgreSQL 14+
- Cuenta en AWS (S3)
- API Keys de OpenAI y Pinecone
- Android Studio Iguana o superior
- Java 11+
- Dispositivo o emulador con Android 7+

## ⬇️ Instalación

### 1. Clonar el repositorio:

```bash
git clone https://github.com/csuvg/PG-2025-21781.git
```

### 2. Instalar dependencias del servidor:

```bash
npm install
```

### 3. Crear y activar entorno virtual de Python

**Linux/macOS:**

```bash
python3 -m venv ciudadano_digital
source ciudadano_digital/bin/activate
```

**Windows (PowerShell):**

```bash
python -m venv ciudadano_digital
.\ciudadano_digital\Scripts\activate
```

### 4. Instalar dependencias de Python

```bash
pip install -r requirements.txt
```

### 5. Configurar variables de entorno

Crear un archivo `.env` basado en `.env.example` y completar:

- Ruta absoluta del proyecto
- URL de la base de datos PostgreSQL
- Clave JWT
- Credenciales y configuración de Pinecone
- API Key y modelos de OpenAI
- Credenciales de correo para recuperación de contraseña
- Credenciales y configuración de AWS S3

### 6. Inicializar la base de datos
```bash
psql -U <usuario> -d <base_de_datos> -f db/tables.sql
```

### 7. Ejecutar el servidor  
**Modo desarrollo**
```bash
npm run dev
```

**Modo producción**
```bash
npm start
```

## 📱 Cliente Android (App)

### 1. Configurar local.properties
Crear un archivo `/Android/local.properties` basado en `/Android/local.properties.example` y completar:

- Entorno (DEV por defecto)
- URL del API

### 2. Compilar aplicación
Desde Android Studio: abrir → sincronizar → Run
O desde terminal:
```bash
cd Android
./gradlew assembleDebug
```


## 📼 Demo
El video demostrativo se encuentra en [Demo del proyecto](./demo/demo.mp4)


## 📜 Documentación
El informe final del proyecto está disponible en [Informe Final](./docs/informe_final.pdf)

## 👨‍💻 Autor
Erick Guerra - 21781

## 🪪 Licencia
MIT License