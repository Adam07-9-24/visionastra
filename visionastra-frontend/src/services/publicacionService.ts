import api from "./api";

export type PlataformaPublicacion =
  | "facebook"
  | "instagram"
  | "tiktok"
  | "linkedin"
  | "x"
  | "youtube";

export type PrivacidadPublicacion = "private" | "unlisted" | "public";

export type EstadoPublicacion =
  | "borrador"
  | "lista"
  | "programada"
  | "enviada"
  | "publicada"
  | "error"
  | "cancelada";

export type Publicacion = {
  idPublicacion: number;

  idCampana: number;
  nombreCampana: string;

  idRecurso: number | null;
  tituloRecurso: string | null;
  tipoRecurso: string | null;

  titulo: string;
  copyTexto: string | null;
  plataforma: PlataformaPublicacion;
  privacidad: PrivacidadPublicacion | null;
  estado: EstadoPublicacion;

  fechaProgramada: string | null;
  fechaPublicada: string | null;

  urlPublicacion: string | null;
  externalId: string | null;
  mensajeError: string | null;

  fechaCreacion: string | null;
  fechaActualizacion: string | null;
};

export type PublicacionRequest = {
  idCampana: number;
  idRecurso?: number | null;
  titulo: string;
  copyTexto?: string | null;
  plataforma: PlataformaPublicacion;
  privacidad?: PrivacidadPublicacion | null;
  estado?: EstadoPublicacion;
  fechaProgramada?: string | null;
};

export type PublicacionFiltros = {
  idCampana?: number;
  estado?: EstadoPublicacion;
  plataforma?: PlataformaPublicacion;
};

export async function obtenerPublicaciones(
  filtros?: PublicacionFiltros
): Promise<Publicacion[]> {
  const response = await api.get<Publicacion[]>("/publicaciones", {
    params: filtros,
  });

  return response.data;
}

export async function obtenerPublicacionPorId(
  idPublicacion: number
): Promise<Publicacion> {
  const response = await api.get<Publicacion>(
    `/publicaciones/${idPublicacion}`
  );

  return response.data;
}

export async function crearPublicacion(
  data: PublicacionRequest
): Promise<Publicacion> {
  const response = await api.post<Publicacion>("/publicaciones", data);
  return response.data;
}

export async function actualizarPublicacion(
  idPublicacion: number,
  data: PublicacionRequest
): Promise<Publicacion> {
  const response = await api.put<Publicacion>(
    `/publicaciones/${idPublicacion}`,
    data
  );

  return response.data;
}

export async function cancelarPublicacion(
  idPublicacion: number
): Promise<Publicacion> {
  const response = await api.patch<Publicacion>(
    `/publicaciones/${idPublicacion}/cancelar`
  );

  return response.data;
}

export async function enviarPublicacionAN8n(
  idPublicacion: number
): Promise<Publicacion> {
  const response = await api.patch<Publicacion>(
    `/publicaciones/${idPublicacion}/enviar-n8n`
  );

  return response.data;
}
