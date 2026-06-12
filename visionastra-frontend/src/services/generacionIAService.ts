import api from "./api";

export type EstadoGeneracionIA =
  | "pendiente"
  | "procesando"
  | "completado"
  | "error";

export type TipoSalidaIA = "copy" | "imagen" | "video";

export interface RecursoEntradaIA {
  idRecurso: number;
  titulo: string | null;
  tipo: string;
  nombreArchivo: string | null;
  rolRecurso: string;
}

export interface GeneracionIA {
  idGeneracion: number;
  idUsuario: number;
  nombreUsuario: string | null;

  idCampana: number;
  nombreCampana: string | null;

  idAgente: number | null;
  nombreAgente: string | null;

  prompt: string;
  resumenContexto: string | null;
  guionGenerado: string | null;
  promptFinalEspanol: string | null;
  promptFinal: string | null;

  proveedorPrompt: string | null;
  proveedorVideo: string | null;

  tipoSalida: TipoSalidaIA;
  estado: EstadoGeneracionIA;
  mensajeError: string | null;

  idRecursoResultado: number | null;
  tituloRecursoResultado: string | null;
  tipoRecursoResultado: string | null;

  recursosEntrada: RecursoEntradaIA[];

  fechaCreacion: string | null;
  fechaActualizacion: string | null;
}

export interface CrearGeneracionIARequest {
  idCampana: number;
  idAgente?: number | null;
  prompt: string;
  tipoSalida: TipoSalidaIA;
  idsRecursos: number[];
}

export async function listarGeneracionesIA(): Promise<GeneracionIA[]> {
  const { data } = await api.get<GeneracionIA[]>("/generaciones-ia");
  return data;
}

export async function obtenerGeneracionIA(
  idGeneracion: number
): Promise<GeneracionIA> {
  const { data } = await api.get<GeneracionIA>(
    `/generaciones-ia/${idGeneracion}`
  );
  return data;
}

export async function crearGeneracionIA(
  request: CrearGeneracionIARequest
): Promise<GeneracionIA> {
  const { data } = await api.post<GeneracionIA>("/generaciones-ia", request);
  return data;
}

export async function prepararPromptIA(
  idGeneracion: number
): Promise<GeneracionIA> {
  const { data } = await api.patch<GeneracionIA>(
    `/generaciones-ia/${idGeneracion}/preparar-prompt`
  );
  return data;
}

export async function generarVideoIA(
  idGeneracion: number
): Promise<GeneracionIA> {
  const { data } = await api.patch<GeneracionIA>(
    `/generaciones-ia/${idGeneracion}/generar-video`
  );
  return data;
}

export async function marcarGeneracionComoProcesando(
  idGeneracion: number
): Promise<GeneracionIA> {
  const { data } = await api.patch<GeneracionIA>(
    `/generaciones-ia/${idGeneracion}/procesar`
  );
  return data;
}

export async function marcarGeneracionComoError(
  idGeneracion: number,
  mensajeError: string
): Promise<GeneracionIA> {
  const { data } = await api.patch<GeneracionIA>(
    `/generaciones-ia/${idGeneracion}/error`,
    { mensajeError }
  );
  return data;
}

export async function eliminarGeneracionIA(
  idGeneracion: number
): Promise<void> {
  await api.delete(`/generaciones-ia/${idGeneracion}`);
}
