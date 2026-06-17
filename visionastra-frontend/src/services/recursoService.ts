// src/services/recursoService.ts

import api from "./api";

export type TipoRecurso = "imagen" | "video" | "documento" | "copy";
export type EstadoRecurso = "activo" | "archivado";

export type Recurso = {
  idRecurso: number;
  idCampana: number;
  nombreCampana: string;
  tipo: TipoRecurso;
  titulo: string | null;
  nombreArchivo: string;
  urlArchivo: string | null;
  contenidoTexto: string | null;
  pesoMb: number | null;
  formato: string | null;
  estado: EstadoRecurso;
  fechaSubida: string | null;
};

export type RecursoRequest = {
  idCampana: number;
  tipo: TipoRecurso;
  titulo?: string | null;
  nombreArchivo: string;
  urlArchivo?: string | null;
  contenidoTexto?: string | null;
  pesoMb?: number | null;
  formato?: string | null;
};

export type SubirRecursoArchivoRequest = {
  idCampana: number;
  tipo: Exclude<TipoRecurso, "copy">;
  titulo?: string | null;
  archivo: File;
};

export async function obtenerRecursosPorCampana(
  idCampana: number
): Promise<Recurso[]> {
  const response = await api.get<Recurso[]>(`/recursos/campana/${idCampana}`);
  return response.data;
}

export async function obtenerRecursoPorId(idRecurso: number): Promise<Recurso> {
  const response = await api.get<Recurso>(`/recursos/${idRecurso}`);
  return response.data;
}

export async function crearRecurso(data: RecursoRequest): Promise<Recurso> {
  const response = await api.post<Recurso>("/recursos", data);
  return response.data;
}

export async function subirRecursoArchivo(
  data: SubirRecursoArchivoRequest
): Promise<Recurso> {
  const formData = new FormData();

  formData.append("idCampana", String(data.idCampana));
  formData.append("tipo", data.tipo);

  if (data.titulo && data.titulo.trim().length > 0) {
    formData.append("titulo", data.titulo.trim());
  }

  formData.append("archivo", data.archivo);

  const response = await api.post<Recurso>("/recursos/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return response.data;
}

export async function obtenerArchivoRecurso(idRecurso: number): Promise<Blob> {
  const response = await api.get(`/recursos/archivo/${idRecurso}`, {
    responseType: "blob",
  });

  return response.data;
}

export async function crearUrlArchivoRecurso(
  idRecurso: number
): Promise<string> {
  const archivoBlob = await obtenerArchivoRecurso(idRecurso);
  return URL.createObjectURL(archivoBlob);
}

export async function actualizarRecurso(
  idRecurso: number,
  data: RecursoRequest
): Promise<Recurso> {
  const response = await api.put<Recurso>(`/recursos/${idRecurso}`, data);
  return response.data;
}

// ✅ Nuevo: editar solo el título del recurso.
// Se usa especialmente para videos IA, para diferenciarlos sin tocar el archivo.
export async function actualizarTituloRecurso(
  idRecurso: number,
  titulo: string
): Promise<Recurso> {
  const response = await api.patch<Recurso>(`/recursos/${idRecurso}/titulo`, {
    titulo,
  });

  return response.data;
}

export async function archivarRecurso(idRecurso: number): Promise<Recurso> {
  const response = await api.patch<Recurso>(`/recursos/${idRecurso}/archivar`);
  return response.data;
}

export async function desarchivarRecurso(idRecurso: number): Promise<Recurso> {
  const response = await api.patch<Recurso>(
    `/recursos/${idRecurso}/desarchivar`
  );

  return response.data;
}

export async function eliminarRecurso(
  idRecurso: number
): Promise<{ mensaje: string }> {
  const response = await api.delete<{ mensaje: string }>(
    `/recursos/${idRecurso}`
  );

  return response.data;
}
