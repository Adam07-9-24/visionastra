// src/services/campanaService.ts

import api from "./api";

export type EstadoCampana = "borrador" | "activa" | "pausada" | "finalizada";

export type Campana = {
  idCampana: number;
  idUsuario: number;
  nombre: string;
  objetivo: string | null;
  descripcion: string | null;
  presupuesto: number | null;
  estado: EstadoCampana;
  fechaInicio: string | null;
  fechaFin: string | null;
  fechaCreacion: string;
  fechaActualizacion: string;
};

export type CampanaRequest = {
  nombre: string;
  objetivo?: string;
  descripcion?: string;
  presupuesto?: number | null;
  estado?: EstadoCampana;
  fechaInicio?: string | null;
  fechaFin?: string | null;
};

export async function obtenerCampanas(
  estado?: EstadoCampana
): Promise<Campana[]> {
  const response = await api.get<Campana[]>("/campanas", {
    params: estado ? { estado } : {},
  });

  return response.data;
}

export async function obtenerCampanaPorId(idCampana: number): Promise<Campana> {
  const response = await api.get<Campana>(`/campanas/${idCampana}`);
  return response.data;
}

export async function crearCampana(data: CampanaRequest): Promise<Campana> {
  const response = await api.post<Campana>("/campanas", data);
  return response.data;
}

export async function actualizarCampana(
  idCampana: number,
  data: CampanaRequest
): Promise<Campana> {
  const response = await api.put<Campana>(`/campanas/${idCampana}`, data);
  return response.data;
}

export async function cambiarEstadoCampana(
  idCampana: number,
  estado: EstadoCampana
): Promise<Campana> {
  const response = await api.patch<Campana>(`/campanas/${idCampana}/estado`, {
    estado,
  });

  return response.data;
}

export async function eliminarCampana(
  idCampana: number
): Promise<{ mensaje: string }> {
  const response = await api.delete<{ mensaje: string }>(
    `/campanas/${idCampana}`
  );
  return response.data;
}
