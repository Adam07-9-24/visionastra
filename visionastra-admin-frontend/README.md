# VisionAstra Admin Frontend

Panel administrativo web de VisionAstra, desarrollado para supervisar usuarios, campañas, generaciones mediante inteligencia artificial y publicaciones realizadas desde la plataforma principal.

Este frontend forma parte del monorepo de VisionAstra y se comunica exclusivamente con el backend administrativo desarrollado en Django.

---

## Descripción

VisionAstra Admin permite que los administradores internos consulten información general de la plataforma y supervisen sus principales módulos.

La aplicación utiliza autenticación administrativa mediante sesiones de Django y protección CSRF.

Su propósito es únicamente la supervisión y administración interna.

---

## Tecnologías utilizadas

- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui
- Axios
- React Router DOM
- Lucide React
- Sonner

---

## Módulos disponibles

### Dashboard

Muestra un resumen general de VisionAstra:

- total de usuarios;
- usuarios activos;
- campañas creadas;
- publicaciones;
- generaciones realizadas mediante IA.

### Usuarios

Permite:

- listar usuarios;
- buscar usuarios;
- filtrar por estado;
- consultar información del usuario;
- bloquear cuentas;
- activar cuentas.

Las operaciones de bloqueo y activación son procesadas mediante Django Admin y el backend principal de Spring Boot.

### Campañas

Permite:

- listar campañas;
- buscar por campaña, propietario o correo;
- filtrar por estado;
- filtrar por propietario;
- consultar el detalle de una campaña;
- revisar la cantidad de recursos;
- revisar la cantidad de publicaciones.

Este módulo es únicamente de consulta.

### Generaciones IA

Permite:

- listar generaciones realizadas mediante IA;
- buscar por campaña, usuario o correo;
- filtrar por estado;
- filtrar por usuario;
- paginar resultados.

La tabla muestra:

- identificador de generación;
- campaña;
- usuario;
- estado;
- fecha.

Este módulo no permite editar, eliminar, cancelar ni volver a generar contenido.

### Publicaciones

Permite:

- listar publicaciones;
- buscar por título, campaña, usuario o correo;
- filtrar por estado;
- filtrar por usuario;
- paginar resultados;
- visualizar mensajes de error comprensibles.

La tabla muestra:

- título;
- campaña;
- usuario;
- estado;
- fecha.

El estado interno `enviada` se presenta visualmente como **Publicada**, de acuerdo con el flujo actual de VisionAstra.

Este módulo no permite editar, eliminar, reenviar ni cancelar publicaciones.

---

## Requisitos

Antes de iniciar el proyecto se necesita:

- Node.js
- npm
- backend Django Admin ejecutándose
- base de datos MySQL de VisionAstra disponible

---

## Instalación

Desde la carpeta del proyecto:

```bash
npm install
```
