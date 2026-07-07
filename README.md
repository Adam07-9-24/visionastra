# VisionAstra

VisionAstra es una plataforma SaaS de marketing digital impulsada por inteligencia artificial. Permite administrar campañas, almacenar recursos multimedia, crear ideas de contenido, generar guiones y prompts mediante IA, producir videos y preparar publicaciones para plataformas externas.

El proyecto está compuesto por una aplicación web para usuarios, un panel administrativo, una API principal desarrollada con Spring Boot, un backend administrativo con Django y una aplicación móvil Android.

---

## Características principales

- Registro e inicio de sesión de usuarios.
- Autenticación mediante JWT y refresh tokens.
- Administración de sesiones por dispositivo.
- Creación y administración de campañas.
- Gestión de imágenes, ideas y videos.
- Generación de contenido mediante inteligencia artificial.
- Preparación de resumen, guion y prompts.
- Generación de videos con Google Veo.
- Reproducción y descarga de videos.
- Creación y administración de publicaciones.
- Automatización de publicaciones mediante n8n.
- Integración con YouTube.
- Panel administrativo para supervisar usuarios, campañas, generaciones y publicaciones.
- Aplicación móvil Android conectada a la misma API de Spring Boot.

---

# Arquitectura del proyecto

```text
visionastra/
│
├── visionastra-frontend/
│   └── Aplicación web principal para los usuarios.
│
├── visionastra-admin-frontend/
│   └── Interfaz web del panel administrativo.
│
├── visionastra-admin/
│   └── Backend administrativo desarrollado con Django.
│
├── visionastra-mobile/
│   └── Aplicación móvil Android desarrollada con Kotlin y Jetpack Compose.
│
├── visionastra-api/
│   └── Backend principal desarrollado con Spring Boot.
│
└── README.md
```

> La carpeta `visionastra-api` corresponde al backend principal de Spring Boot. Debe agregarse al repositorio raíz cuando se complete su integración dentro del monorepositorio.

---

# Arquitectura general

```text
┌─────────────────────────────┐
│ VisionAstra Web             │
│ React + TypeScript          │
└──────────────┬──────────────┘
               │
               │ HTTPS / REST
               ▼
┌─────────────────────────────┐
│ VisionAstra API             │
│ Spring Boot                 │
│ Puerto local: 8083          │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ MySQL                       │
│ Base de datos: visionastra  │
└─────────────────────────────┘
               │
               ├──────────── OpenAI
               ├──────────── Gemini / Google Veo
               ├──────────── n8n
               └──────────── YouTube

┌─────────────────────────────┐
│ VisionAstra Mobile          │
│ Kotlin + Jetpack Compose    │
└──────────────┬──────────────┘
               │
               └──────────────► Spring Boot API

┌─────────────────────────────┐
│ Admin Frontend              │
│ React + TypeScript          │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ Admin Backend               │
│ Django                      │
│ Puerto local: 8000          │
└─────────────────────────────┘
```

---

# Tecnologías utilizadas

## Frontend de usuarios

- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui
- React Router DOM
- Axios
- Sonner
- STOMP WebSocket
- Recharts

## Backend principal

- Java 17
- Spring Boot
- Spring Security
- JWT
- Refresh Token
- BCrypt
- Gradle
- JPA / Hibernate
- MySQL
- WebSocket STOMP
- OpenAI API
- Gemini API
- Google Veo
- Integración con n8n

## Panel administrativo

### Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- Axios

### Backend

- Python 3.12
- Django
- Django REST Framework
- Gunicorn para producción
- MySQL

## Aplicación móvil

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Hilt
- Retrofit
- OkHttp
- Moshi
- DataStore
- Android Keystore
- Coil
- Media3
- Coroutines
- StateFlow

---

# Requisitos previos

Para ejecutar el proyecto localmente se necesita:

- Git
- Node.js 20 o superior
- npm
- Java 17
- Gradle
- Python 3.12
- MySQL 8
- Android Studio
- Android SDK
- Una cuenta o credenciales para los servicios de IA utilizados
- Credenciales de n8n y YouTube para las integraciones externas

Comprobar las instalaciones:

```bash
git --version
node --version
npm --version
java --version
python --version
mysql --version
```

---

# Clonar el repositorio

```bash
git clone https://github.com/Adam07-9-24/visionastra.git
cd visionastra
```

---

# Base de datos

Crear la base de datos principal:

```sql
CREATE DATABASE visionastra
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Crear un usuario exclusivo para la aplicación en producción:

```sql
CREATE USER 'visionastra_user'@'localhost'
IDENTIFIED BY 'CAMBIAR_POR_UNA_CONTRASENA_SEGURA';

GRANT ALL PRIVILEGES
ON visionastra.*
TO 'visionastra_user'@'localhost';

FLUSH PRIVILEGES;
```

No se recomienda utilizar el usuario `root` de MySQL en producción.

---

# Ejecución local

## 1. Backend principal Spring Boot

Entrar en la carpeta:

```bash
cd visionastra-api
```

Configurar las propiedades necesarias mediante variables de entorno o mediante un archivo local que no debe subirse a Git.

Ejemplo de configuración:

```properties
server.port=8083

spring.datasource.url=jdbc:mysql://localhost:3306/visionastra
spring.datasource.username=root
spring.datasource.password=TU_CONTRASENA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

También deben configurarse las credenciales correspondientes a:

- JWT.
- OpenAI.
- Gemini o Google Veo.
- n8n.
- YouTube.

Los nombres exactos de estas propiedades deben coincidir con los definidos actualmente dentro del backend.

En Windows:

```powershell
.\gradlew.bat bootRun
```

En Linux o macOS:

```bash
./gradlew bootRun
```

La API estará disponible en:

```text
http://localhost:8083
```

---

## 2. Frontend principal

Entrar en la carpeta:

```bash
cd visionastra-frontend
```

Instalar dependencias:

```bash
npm install
```

Crear el archivo de variables de entorno correspondiente.

Ejemplo:

```env
VITE_API_URL=http://localhost:8083
```

Ejecutar:

```bash
npm run dev
```

Vite mostrará la dirección local, normalmente:

```text
http://localhost:5173
```

---

## 3. Backend administrativo Django

Entrar en la carpeta:

```bash
cd visionastra-admin
```

### Crear el entorno virtual

```powershell
python -m venv .venv
```

### Activarlo en PowerShell

```powershell
.\.venv\Scripts\Activate.ps1
```

Al activarse, la terminal mostrará algo parecido a:

```text
(.venv) PS C:\Users\User\visionastra\visionastra-admin>
```

### Instalar dependencias

```powershell
pip install -r requirements.txt
```

### Aplicar migraciones

```powershell
python manage.py migrate
```

### Crear un administrador

```powershell
python manage.py createsuperuser
```

### Ejecutar Django

```powershell
python manage.py runserver
```

El backend administrativo estará disponible en:

```text
http://127.0.0.1:8000
```

El panel propio de Django estará disponible en:

```text
http://127.0.0.1:8000/admin/
```

> `python manage.py runserver` debe utilizarse únicamente durante el desarrollo.

---

## 4. Frontend administrativo

Entrar en la carpeta:

```bash
cd visionastra-admin-frontend
```

Instalar dependencias:

```bash
npm install
```

Crear el archivo de variables de entorno:

```env
VITE_ADMIN_API_URL=http://127.0.0.1:8000
```

Ejecutar:

```bash
npm run dev
```

Vite mostrará la dirección asignada, normalmente:

```text
http://localhost:5173
```

o:

```text
http://localhost:5174
```

---

## 5. Aplicación móvil Android

Abrir la carpeta:

```text
visionastra-mobile
```

desde Android Studio.

La dirección del backend para el emulador Android debe utilizar:

```text
http://10.0.2.2:8083/
```

No debe utilizarse `localhost`, porque dentro del emulador `localhost` representa al propio emulador.

Ejemplo:

```kotlin
const val BASE_URL = "http://10.0.2.2:8083/"
```

Compilar desde PowerShell:

```powershell
cd visionastra-mobile
.\gradlew.bat assembleDebug
```

El APK de desarrollo será generado dentro de:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# Orden recomendado para ejecutar el sistema localmente

1. Iniciar MySQL.
2. Iniciar Spring Boot.
3. Iniciar Django.
4. Iniciar el frontend principal.
5. Iniciar el frontend administrativo.
6. Abrir la aplicación móvil en Android Studio.

Servicios locales principales:

| Servicio | Dirección |
|---|---|
| Spring Boot API | `http://localhost:8083` |
| Django Admin API | `http://127.0.0.1:8000` |
| Frontend principal | Dirección mostrada por Vite |
| Frontend administrativo | Dirección mostrada por Vite |
| MySQL | `localhost:3306` |
| Android Emulator hacia Spring Boot | `http://10.0.2.2:8083` |

---

# Despliegue en un servidor Ubuntu

Esta sección explica una configuración de producción utilizando:

- Ubuntu Server.
- MySQL.
- Nginx.
- systemd.
- Gunicorn.
- HTTPS mediante Certbot.

Ejemplo de dominios:

```text
app.visionastra.com
admin.visionastra.com
api.visionastra.com
admin-api.visionastra.com
```

Los dominios deben reemplazarse por los dominios reales del proyecto.

---

## 1. Preparar el servidor

Actualizar paquetes:

```bash
sudo apt update
sudo apt upgrade -y
```

Instalar herramientas necesarias:

```bash
sudo apt install -y \
  git \
  nginx \
  mysql-server \
  openjdk-17-jdk \
  python3 \
  python3-venv \
  python3-pip \
  certbot \
  python3-certbot-nginx
```

Instalar Node.js según la versión requerida por el proyecto.

Comprobar:

```bash
java --version
python3 --version
node --version
npm --version
nginx -v
```

---

## 2. Crear usuario para la aplicación

Por seguridad, se recomienda no ejecutar los servicios con el usuario `root`.

```bash
sudo adduser visionastra
sudo usermod -aG sudo visionastra
```

Crear la carpeta principal:

```bash
sudo mkdir -p /opt/visionastra
sudo chown -R visionastra:visionastra /opt/visionastra
```

Cambiar al usuario:

```bash
sudo su - visionastra
```

---

## 3. Descargar el proyecto

```bash
cd /opt/visionastra
git clone https://github.com/Adam07-9-24/visionastra.git .
```

La estructura esperada será:

```text
/opt/visionastra/
├── visionastra-api/
├── visionastra-frontend/
├── visionastra-admin/
├── visionastra-admin-frontend/
└── visionastra-mobile/
```

---

## 4. Configurar MySQL en producción

Ingresar a MySQL:

```bash
sudo mysql
```

Crear base de datos y usuario:

```sql
CREATE DATABASE visionastra
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER 'visionastra_user'@'localhost'
IDENTIFIED BY 'CAMBIAR_POR_UNA_CONTRASENA_SEGURA';

GRANT ALL PRIVILEGES
ON visionastra.*
TO 'visionastra_user'@'localhost';

FLUSH PRIVILEGES;
EXIT;
```

---

# Despliegue de Spring Boot

## 1. Configurar variables de entorno

Crear un archivo protegido:

```bash
sudo nano /etc/visionastra-api.env
```

Ejemplo:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/visionastra
SPRING_DATASOURCE_USERNAME=visionastra_user
SPRING_DATASOURCE_PASSWORD=CAMBIAR_CONTRASENA

JWT_SECRET=CAMBIAR_POR_UN_SECRETO_LARGO_Y_SEGURO
OPENAI_API_KEY=CAMBIAR_API_KEY
GEMINI_API_KEY=CAMBIAR_API_KEY
N8N_WEBHOOK_URL=CAMBIAR_URL
```

Los nombres de las variables deben adaptarse a las propiedades reales utilizadas por el backend.

Proteger el archivo:

```bash
sudo chmod 600 /etc/visionastra-api.env
```

Nunca subir las credenciales al repositorio.

---

## 2. Compilar el backend

```bash
cd /opt/visionastra/visionastra-api
chmod +x gradlew
./gradlew clean bootJar
```

El archivo JAR se generará normalmente dentro de:

```text
build/libs/
```

Copiarlo a una ruta estable:

```bash
mkdir -p /opt/visionastra/runtime
cp build/libs/*.jar /opt/visionastra/runtime/visionastra-api.jar
```

---

## 3. Preparar almacenamiento de recursos

VisionAstra guarda archivos dentro de:

```text
uploads/recursos/
```

Crear la carpeta:

```bash
mkdir -p /opt/visionastra/visionastra-api/uploads/recursos
```

Dar permisos:

```bash
sudo chown -R visionastra:visionastra \
  /opt/visionastra/visionastra-api/uploads
```

Esta carpeta debe mantenerse incluso cuando se actualice la aplicación.

Los archivos protegidos deben continuar entregándose mediante el endpoint de Spring Boot:

```text
/api/recursos/archivo/{idRecurso}
```

No se recomienda exponer directamente la carpeta `uploads` mediante Nginx.

---

## 4. Crear servicio systemd de Spring Boot

Crear:

```bash
sudo nano /etc/systemd/system/visionastra-api.service
```

Contenido:

```ini
[Unit]
Description=VisionAstra Spring Boot API
After=network.target mysql.service

[Service]
Type=simple
User=visionastra
Group=visionastra

WorkingDirectory=/opt/visionastra/visionastra-api

EnvironmentFile=/etc/visionastra-api.env

ExecStart=/usr/bin/java -jar /opt/visionastra/runtime/visionastra-api.jar

Restart=always
RestartSec=10

SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

Recargar systemd:

```bash
sudo systemctl daemon-reload
```

Iniciar el servicio:

```bash
sudo systemctl enable visionastra-api
sudo systemctl start visionastra-api
```

Comprobar:

```bash
sudo systemctl status visionastra-api
```

Ver logs:

```bash
sudo journalctl -u visionastra-api -f
```

La API debe escuchar internamente en:

```text
127.0.0.1:8083
```

---

# Despliegue de Django

## 1. Crear entorno virtual

```bash
cd /opt/visionastra/visionastra-admin
python3 -m venv .venv
source .venv/bin/activate
```

Instalar dependencias:

```bash
pip install --upgrade pip
pip install -r requirements.txt
pip install gunicorn
```

---

## 2. Configurar producción

Crear:

```bash
sudo nano /etc/visionastra-admin.env
```

Ejemplo:

```env
DJANGO_SECRET_KEY=CAMBIAR_POR_UN_SECRETO_SEGURO
DJANGO_DEBUG=False
DJANGO_ALLOWED_HOSTS=admin-api.visionastra.com,127.0.0.1
DATABASE_NAME=visionastra
DATABASE_USER=visionastra_user
DATABASE_PASSWORD=CAMBIAR_CONTRASENA
DATABASE_HOST=127.0.0.1
DATABASE_PORT=3306
```

Los nombres deben ajustarse a la lectura de variables configurada en:

```text
visionastra-admin/config/settings.py
```

Proteger:

```bash
sudo chmod 600 /etc/visionastra-admin.env
```

---

## 3. Migraciones y archivos estáticos

```bash
cd /opt/visionastra/visionastra-admin
source .venv/bin/activate

python manage.py migrate
python manage.py collectstatic --noinput
```

Crear administrador, cuando todavía no exista:

```bash
python manage.py createsuperuser
```

---

## 4. Crear servicio systemd de Django

Crear:

```bash
sudo nano /etc/systemd/system/visionastra-admin.service
```

Contenido:

```ini
[Unit]
Description=VisionAstra Django Admin API
After=network.target mysql.service

[Service]
Type=simple
User=visionastra
Group=visionastra

WorkingDirectory=/opt/visionastra/visionastra-admin

EnvironmentFile=/etc/visionastra-admin.env

ExecStart=/opt/visionastra/visionastra-admin/.venv/bin/gunicorn \
  --workers 3 \
  --bind 127.0.0.1:8000 \
  config.wsgi:application

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Activar:

```bash
sudo systemctl daemon-reload
sudo systemctl enable visionastra-admin
sudo systemctl start visionastra-admin
```

Comprobar:

```bash
sudo systemctl status visionastra-admin
```

Ver logs:

```bash
sudo journalctl -u visionastra-admin -f
```

En producción no se debe utilizar:

```bash
python manage.py runserver
```

---

# Despliegue del frontend principal

Entrar en la carpeta:

```bash
cd /opt/visionastra/visionastra-frontend
```

Crear el archivo de producción:

```bash
nano .env.production
```

Ejemplo:

```env
VITE_API_URL=https://api.visionastra.com
```

Instalar y compilar:

```bash
npm ci
npm run build
```

Crear la carpeta pública:

```bash
sudo mkdir -p /var/www/visionastra-app
sudo cp -R dist/* /var/www/visionastra-app/
sudo chown -R www-data:www-data /var/www/visionastra-app
```

---

# Despliegue del frontend administrativo

Entrar en:

```bash
cd /opt/visionastra/visionastra-admin-frontend
```

Crear:

```bash
nano .env.production
```

Ejemplo:

```env
VITE_ADMIN_API_URL=https://admin-api.visionastra.com
```

Compilar:

```bash
npm ci
npm run build
```

Copiar:

```bash
sudo mkdir -p /var/www/visionastra-admin
sudo cp -R dist/* /var/www/visionastra-admin/
sudo chown -R www-data:www-data /var/www/visionastra-admin
```

---

# Configuración de Nginx

## Frontend principal

Crear:

```bash
sudo nano /etc/nginx/sites-available/visionastra-app
```

Contenido:

```nginx
server {
    listen 80;
    server_name app.visionastra.com;

    root /var/www/visionastra-app;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## Frontend administrativo

Crear:

```bash
sudo nano /etc/nginx/sites-available/visionastra-admin-frontend
```

Contenido:

```nginx
server {
    listen 80;
    server_name admin.visionastra.com;

    root /var/www/visionastra-admin;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## API Spring Boot

Crear:

```bash
sudo nano /etc/nginx/sites-available/visionastra-api
```

Contenido:

```nginx
server {
    listen 80;
    server_name api.visionastra.com;

    client_max_body_size 100M;

    location / {
        proxy_pass http://127.0.0.1:8083;

        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_read_timeout 600s;
        proxy_connect_timeout 60s;
        proxy_send_timeout 600s;
    }
}
```

Los tiempos elevados son necesarios para operaciones que pueden tardar, como la generación de videos.

---

## API administrativa Django

Crear:

```bash
sudo nano /etc/nginx/sites-available/visionastra-admin-api
```

Contenido:

```nginx
server {
    listen 80;
    server_name admin-api.visionastra.com;

    client_max_body_size 25M;

    location / {
        proxy_pass http://127.0.0.1:8000;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /static/ {
        alias /opt/visionastra/visionastra-admin/staticfiles/;
    }
}
```

---

## Activar sitios

```bash
sudo ln -s \
  /etc/nginx/sites-available/visionastra-app \
  /etc/nginx/sites-enabled/

sudo ln -s \
  /etc/nginx/sites-available/visionastra-admin-frontend \
  /etc/nginx/sites-enabled/

sudo ln -s \
  /etc/nginx/sites-available/visionastra-api \
  /etc/nginx/sites-enabled/

sudo ln -s \
  /etc/nginx/sites-available/visionastra-admin-api \
  /etc/nginx/sites-enabled/
```

Validar:

```bash
sudo nginx -t
```

Reiniciar:

```bash
sudo systemctl restart nginx
```

---

# Configurar HTTPS

Los dominios deben apuntar previamente a la IP pública del servidor.

Ejecutar:

```bash
sudo certbot --nginx \
  -d app.visionastra.com \
  -d admin.visionastra.com \
  -d api.visionastra.com \
  -d admin-api.visionastra.com
```

Comprobar la renovación automática:

```bash
sudo systemctl status certbot.timer
```

Probar:

```bash
sudo certbot renew --dry-run
```

Todas las aplicaciones deben utilizar HTTPS en producción.

---

# CORS y CSRF

## Spring Boot

El backend debe permitir únicamente los dominios autorizados, por ejemplo:

```text
https://app.visionastra.com
https://admin.visionastra.com
```

No se recomienda permitir todos los orígenes con `*` en producción.

## Django

Configurar los hosts y orígenes permitidos:

```python
ALLOWED_HOSTS = [
    "admin-api.visionastra.com",
    "127.0.0.1",
]

CSRF_TRUSTED_ORIGINS = [
    "https://admin.visionastra.com",
    "https://admin-api.visionastra.com",
]
```

También debe permitirse el dominio del frontend administrativo dentro de la configuración CORS utilizada por el proyecto.

---

# Despliegue de la aplicación móvil

La aplicación Android no se publica directamente dentro de Nginx como una página web.

Antes de generar una versión de producción debe cambiarse la URL del backend:

```text
https://api.visionastra.com/
```

No utilizar en producción:

```text
http://10.0.2.2:8083/
```

## Generar APK

En Windows:

```powershell
cd visionastra-mobile
.\gradlew.bat assembleRelease
```

## Generar Android App Bundle

```powershell
.\gradlew.bat bundleRelease
```

El archivo AAB será generado dentro de:

```text
app/build/outputs/bundle/release/
```

Para publicar en Google Play se debe:

1. Crear una clave de firma.
2. Configurar el firmado de producción.
3. Generar el archivo `.aab`.
4. Subirlo a Google Play Console.
5. Configurar nombre, descripción, imágenes y política de privacidad.
6. Realizar pruebas internas antes de producción.

Las claves de firma no deben subirse al repositorio.

---

# Actualización de la aplicación en producción

## 1. Descargar cambios

```bash
cd /opt/visionastra
git pull origin main
```

## 2. Actualizar Spring Boot

```bash
cd /opt/visionastra/visionastra-api
./gradlew clean bootJar

cp build/libs/*.jar \
  /opt/visionastra/runtime/visionastra-api.jar

sudo systemctl restart visionastra-api
```

## 3. Actualizar Django

```bash
cd /opt/visionastra/visionastra-admin
source .venv/bin/activate

pip install -r requirements.txt
python manage.py migrate
python manage.py collectstatic --noinput

sudo systemctl restart visionastra-admin
```

## 4. Actualizar frontend principal

```bash
cd /opt/visionastra/visionastra-frontend

npm ci
npm run build

sudo rm -rf /var/www/visionastra-app/*
sudo cp -R dist/* /var/www/visionastra-app/
```

## 5. Actualizar frontend administrativo

```bash
cd /opt/visionastra/visionastra-admin-frontend

npm ci
npm run build

sudo rm -rf /var/www/visionastra-admin/*
sudo cp -R dist/* /var/www/visionastra-admin/
```

## 6. Reiniciar Nginx

```bash
sudo nginx -t
sudo systemctl reload nginx
```

---

# Copias de seguridad

## Base de datos

Crear una copia:

```bash
mysqldump \
  -u visionastra_user \
  -p \
  visionastra \
  > visionastra_backup.sql
```

Restaurar:

```bash
mysql \
  -u visionastra_user \
  -p \
  visionastra \
  < visionastra_backup.sql
```

## Recursos multimedia

Crear copia:

```bash
tar -czf visionastra_uploads_backup.tar.gz \
  /opt/visionastra/visionastra-api/uploads
```

Se recomienda programar copias automáticas de:

- Base de datos MySQL.
- Imágenes subidas.
- Videos generados.
- Archivos de configuración protegidos.
- Registros importantes.

---

# Seguridad

No deben subirse al repositorio:

- Contraseñas de MySQL.
- Claves JWT.
- Claves de OpenAI.
- Claves de Gemini o Google Veo.
- Credenciales OAuth de YouTube.
- Webhooks privados de n8n.
- Archivos `.env`.
- `application.properties` con secretos.
- `local.properties`.
- Entornos virtuales `.venv`.
- Claves de firma Android.
- Archivos de credenciales de Google.

Ejemplo de archivos que deben estar en `.gitignore`:

```gitignore
.env
.env.*
!.env.example

application-local.properties
application-prod.properties

local.properties

.venv/
venv/

uploads/
logs/

*.jks
*.keystore

google-services-private.json
```

---

# Verificación del despliegue

## Spring Boot

```bash
sudo systemctl status visionastra-api
sudo journalctl -u visionastra-api -n 100
```

## Django

```bash
sudo systemctl status visionastra-admin
sudo journalctl -u visionastra-admin -n 100
```

## Nginx

```bash
sudo nginx -t
sudo systemctl status nginx
```

## MySQL

```bash
sudo systemctl status mysql
```

## Puertos internos

```bash
sudo ss -lntp
```

Deben aparecer internamente:

```text
127.0.0.1:8083
127.0.0.1:8000
```

Los puertos 8083 y 8000 no necesitan estar expuestos públicamente cuando se utiliza Nginx como proxy inverso.

---

# Solución de problemas

## El frontend no puede conectarse al backend

Comprobar:

- Variable `VITE_API_URL`.
- Configuración CORS.
- Estado del servicio Spring Boot.
- Certificado HTTPS.
- Dirección del dominio.
- Reglas del firewall.

## Error 401

Comprobar:

- Access token.
- Refresh token.
- Usuario activo.
- Credenciales.
- Configuración JWT.
- Hora del servidor.

## Error al subir archivos

Comprobar:

- `client_max_body_size` de Nginx.
- Permisos de la carpeta `uploads`.
- Espacio disponible.
- Propietario de los archivos.

## La aplicación móvil no conecta localmente

En el emulador usar:

```text
http://10.0.2.2:8083/
```

En un teléfono físico se debe utilizar la IP local de la computadora o una API pública HTTPS.

## Django devuelve 403 CSRF

Comprobar:

- `CSRF_TRUSTED_ORIGINS`.
- Cookies.
- Dominio del frontend administrativo.
- Uso de HTTPS.
- Configuración de credenciales en Axios.

## El frontend muestra 404 al actualizar una ruta

Comprobar que Nginx tenga:

```nginx
try_files $uri $uri/ /index.html;
```

---

# Estado actual del proyecto

Actualmente VisionAstra cuenta con:

- Autenticación de usuarios.
- Administración de sesiones.
- Dashboard.
- Campañas.
- Recursos.
- Creación de ideas.
- Subida de imágenes.
- Generación de contenido mediante IA.
- Generación de videos.
- Reproducción y descarga de videos.
- Publicaciones.
- Integración con n8n.
- Integración con YouTube.
- Panel administrativo.
- Aplicación móvil Android.

---

# Equipo

Proyecto desarrollado como parte de la formación en Diseño y Desarrollo de Software.

**Desarrollador:** Yefry Calderón González  
**Proyecto:** VisionAstra  
**Institución:** TECSUP

---

# Licencia

Este proyecto tiene fines académicos y de demostración.

No se autoriza el uso de claves, credenciales, recursos privados o integraciones externas sin la autorización correspondiente.
