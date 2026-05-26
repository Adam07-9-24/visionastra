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

export async function actualizarRecurso(
  idRecurso: number,
  data: RecursoRequest
): Promise<Recurso> {
  const response = await api.put<Recurso>(`/recursos/${idRecurso}`, data);
  return response.data;
}

export async function archivarRecurso(idRecurso: number): Promise<Recurso> {
  const response = await api.patch<Recurso>(`/recursos/${idRecurso}/archivar`);

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
