import { useEffect, useMemo, useRef, useState } from "react";
import type { ElementType } from "react";
import { useNavigate } from "react-router-dom";

import {
  Bot,
  BrainCircuit,
  CheckCircle2,
  FileText,
  ImageIcon,
  Loader2,
  PlayCircle,
  Sparkles,
  Video,
  WandSparkles,
} from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

import type { Campana } from "@/services/campanaService";
import { obtenerCampanas } from "@/services/campanaService";

import type { Recurso } from "@/services/recursoService";
import {
  obtenerArchivoRecurso,
  obtenerRecursosPorCampana,
} from "@/services/recursoService";

import type { GeneracionIA } from "@/services/generacionIAService";
import {
  crearGeneracionIA,
  generarVideoIA,
  listarGeneracionesIA,
  prepararPromptIA,
} from "@/services/generacionIAService";

type EstadoCarga = "idle" | "loading" | "error";
type VistaResultadoIA = "explicacion" | "promptEspanol" | "promptVeo";

const tipoRecursoLabel: Record<string, string> = {
  copy: "Brief creativo",
  imagen: "Imagen",
  video: "Video",
  documento: "Documento",
};

const tipoRecursoIcon: Record<string, ElementType> = {
  copy: FileText,
  imagen: ImageIcon,
  video: Video,
  documento: FileText,
};

const estadoGeneracionStyles: Record<string, string> = {
  pendiente:
    "border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-500/25 dark:bg-amber-500/10 dark:text-amber-300",
  procesando:
    "border-sky-300 bg-sky-50 text-sky-700 dark:border-sky-500/25 dark:bg-sky-500/10 dark:text-sky-300",
  completado:
    "border-emerald-300 bg-emerald-50 text-emerald-700 dark:border-emerald-500/25 dark:bg-emerald-500/10 dark:text-emerald-300",
  error:
    "border-red-300 bg-red-50 text-red-700 dark:border-red-500/25 dark:bg-red-500/10 dark:text-red-300",
};

const estadoCampanaStyles: Record<string, string> = {
  borrador:
    "border-slate-300 bg-slate-100 text-slate-700 dark:border-slate-500/25 dark:bg-slate-500/10 dark:text-slate-300",
  activa:
    "border-emerald-300 bg-emerald-50 text-emerald-700 dark:border-emerald-500/25 dark:bg-emerald-500/10 dark:text-emerald-300",
  pausada:
    "border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-500/25 dark:bg-amber-500/10 dark:text-amber-300",
  finalizada:
    "border-sky-300 bg-sky-50 text-sky-700 dark:border-sky-500/25 dark:bg-sky-500/10 dark:text-sky-300",
};

function formatearFecha(fecha: string | null) {
  if (!fecha) return "Sin fecha";

  try {
    return new Intl.DateTimeFormat("es-PE", {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(new Date(fecha));
  } catch {
    return fecha;
  }
}

function normalizarTexto(texto: string | null | undefined) {
  return texto && texto.trim().length > 0 ? texto : "Sin información";
}

function obtenerEstadoGeneracionVisible(generacion: GeneracionIA) {
  const tieneResultado =
    generacion.resumenContexto?.trim() ||
    generacion.guionGenerado?.trim() ||
    generacion.promptFinal?.trim();

  if (generacion.estado === "procesando" && tieneResultado) {
    return "Prompt preparado";
  }

  return generacion.estado;
}

const estadosIniciadosGeneracion = new Set(["pendiente", "procesando"]);
const mensajeLimiteGeneraciones =
  "Ya tienes 2 generaciones iniciadas para esta campaña y recurso. Continúa con una de ellas antes de crear otra.";

function normalizarIdsRecursos(ids: number[]) {
  return [...ids].sort((a, b) => a - b).join(",");
}

function generacionCoincideConSeleccion(
  generacion: GeneracionIA,
  idCampana: number,
  idsRecursosSeleccionados: number[]
) {
  if (
    generacion.idCampana !== idCampana ||
    !estadosIniciadosGeneracion.has(generacion.estado)
  ) {
    return false;
  }

  const idsRecursosGeneracion =
    generacion.recursosEntrada?.map((recurso) => recurso.idRecurso) ?? [];

  if (idsRecursosGeneracion.length === 0) {
    return true;
  }

  return (
    normalizarIdsRecursos(idsRecursosGeneracion) ===
    normalizarIdsRecursos(idsRecursosSeleccionados)
  );
}

export default function GeneradorIAPage() {
  const navigate = useNavigate();
  const limiteAvisadoKeyRef = useRef<string | null>(null);

  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [generaciones, setGeneraciones] = useState<GeneracionIA[]>([]);

  const [estadoCarga, setEstadoCarga] = useState<EstadoCarga>("loading");
  const [cargandoRecursos, setCargandoRecursos] = useState(false);
  const [creando, setCreando] = useState(false);
  const [preparandoId, setPreparandoId] = useState<number | null>(null);
  const [generandoVideo, setGenerandoVideo] = useState(false);
  const [cargandoVideoGenerado, setCargandoVideoGenerado] = useState(false);
  const [urlVideoGenerado, setUrlVideoGenerado] = useState<string | null>(null);
  const [idUrlVideoGenerado, setIdUrlVideoGenerado] = useState<number | null>(
    null
  );
  const [errorVideoGenerado, setErrorVideoGenerado] = useState<string | null>(
    null
  );
  const [vistaResultadoIA, setVistaResultadoIA] =
    useState<VistaResultadoIA>("explicacion");

  const [idCampanaSeleccionada, setIdCampanaSeleccionada] = useState<
    number | null
  >(null);

  const [idsRecursosSeleccionados, setIdsRecursosSeleccionados] = useState<
    number[]
  >([]);

  const [prompt, setPrompt] = useState(
    "Crea un video promocional realista y profesional para redes sociales usando la información seleccionada. Muestra escenas naturales, personas reales, ambiente cotidiano y una estética limpia. El video debe transmitir confianza, motivación y valor para el público objetivo."
  );

  const [generacionActiva, setGeneracionActiva] = useState<GeneracionIA | null>(
    null
  );

  const campanaSeleccionada = useMemo(
    () =>
      campanas.find((campana) => campana.idCampana === idCampanaSeleccionada) ??
      null,
    [campanas, idCampanaSeleccionada]
  );
  const campanaActiva = campanaSeleccionada?.estado === "activa";
  const idRecursoVideoGenerado =
    generacionActiva?.estado === "completado"
      ? generacionActiva.idRecursoResultado
      : null;
  const tituloVideoGenerado =
    generacionActiva?.tituloRecursoResultado || "Video IA generado";

  const recursosDisponiblesIA = useMemo(() => {
    return recursos.filter(
      (recurso) =>
        recurso.estado === "activo" &&
        (recurso.tipo === "copy" ||
          recurso.tipo === "imagen" ||
          recurso.tipo === "video")
    );
  }, [recursos]);

  const recursosBrief = useMemo(
    () => recursosDisponiblesIA.filter((recurso) => recurso.tipo === "copy"),
    [recursosDisponiblesIA]
  );

  const generacionesIniciadasMismaSeleccion = useMemo(() => {
    if (!campanaSeleccionada || idsRecursosSeleccionados.length === 0) {
      return 0;
    }

    return generaciones.filter((generacion) =>
      generacionCoincideConSeleccion(
        generacion,
        campanaSeleccionada.idCampana,
        idsRecursosSeleccionados
      )
    ).length;
  }, [campanaSeleccionada, generaciones, idsRecursosSeleccionados]);

  const limiteGeneracionesIniciadasAlcanzado =
    generacionesIniciadasMismaSeleccion >= 2;

  useEffect(() => {
    if (
      !campanaSeleccionada ||
      idsRecursosSeleccionados.length === 0 ||
      !limiteGeneracionesIniciadasAlcanzado
    ) {
      limiteAvisadoKeyRef.current = null;
      return;
    }

    const seleccionKey = `${campanaSeleccionada.idCampana}:${normalizarIdsRecursos(
      idsRecursosSeleccionados
    )}`;

    if (limiteAvisadoKeyRef.current === seleccionKey) {
      return;
    }

    limiteAvisadoKeyRef.current = seleccionKey;
    toast.warning(mensajeLimiteGeneraciones);
  }, [
    campanaSeleccionada,
    idsRecursosSeleccionados,
    limiteGeneracionesIniciadasAlcanzado,
  ]);

  useEffect(() => {
    if (!idRecursoVideoGenerado) {
      return;
    }

    let cancelado = false;
    let objectUrl: string | null = null;

    const cargarVideoGenerado = async () => {
      try {
        setCargandoVideoGenerado(true);
        setErrorVideoGenerado(null);
        setUrlVideoGenerado(null);
        setIdUrlVideoGenerado(null);

        const archivo = await obtenerArchivoRecurso(idRecursoVideoGenerado);

        if (cancelado) return;

        objectUrl = URL.createObjectURL(archivo);
        setUrlVideoGenerado(objectUrl);
        setIdUrlVideoGenerado(idRecursoVideoGenerado);
      } catch (error) {
        console.error(error);

        if (cancelado) return;

        setErrorVideoGenerado("No se pudo cargar el video generado.");
      } finally {
        if (!cancelado) {
          setCargandoVideoGenerado(false);
        }
      }
    };

    void cargarVideoGenerado();

    return () => {
      cancelado = true;

      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [idRecursoVideoGenerado]);

  async function cargarRecursosDeCampana(idCampana: number) {
    try {
      setCargandoRecursos(true);
      setIdsRecursosSeleccionados([]);

      const recursosData = await obtenerRecursosPorCampana(idCampana);
      setRecursos(recursosData);
    } catch (error) {
      console.error(error);
      toast.error("No se pudieron cargar los recursos de la campaña.");
    } finally {
      setCargandoRecursos(false);
    }
  }

  useEffect(() => {
    let cancelado = false;

    const iniciar = async () => {
      try {
        const [campanasData, generacionesData] = await Promise.all([
          obtenerCampanas(),
          listarGeneracionesIA(),
        ]);

        if (cancelado) return;

        setCampanas(campanasData);
        setGeneraciones(generacionesData);

        if (campanasData.length > 0) {
          const primeraActiva =
            campanasData.find((campana) => campana.estado === "activa") ??
            campanasData[0];

          setIdCampanaSeleccionada(primeraActiva.idCampana);
          setCargandoRecursos(true);
          setIdsRecursosSeleccionados([]);

          const recursosData = await obtenerRecursosPorCampana(
            primeraActiva.idCampana
          );

          if (cancelado) return;

          setRecursos(recursosData);
          setCargandoRecursos(false);
        }

        setEstadoCarga("idle");
      } catch (error) {
        console.error(error);

        if (cancelado) return;

        setEstadoCarga("error");
        setCargandoRecursos(false);
        toast.error("No se pudieron cargar los datos del Generador IA.");
      }
    };

    void iniciar();

    return () => {
      cancelado = true;
    };
  }, []);

  async function handleSeleccionarCampana(idCampana: number) {
    setIdCampanaSeleccionada(idCampana);
    await cargarRecursosDeCampana(idCampana);
  }

  function alternarRecurso(idRecurso: number) {
    setIdsRecursosSeleccionados((actuales) =>
      actuales.includes(idRecurso)
        ? actuales.filter((id) => id !== idRecurso)
        : [...actuales, idRecurso]
    );
  }

  async function handleCrearGeneracion() {
    if (!campanaSeleccionada) {
      toast.warning("Selecciona una campaña primero.");
      return;
    }

    if (!campanaActiva) {
      toast.warning(
        "Esta campaña debe estar activa para usar el Generador IA."
      );
      return;
    }

    if (!prompt.trim() || prompt.trim().length < 10) {
      toast.warning("Escribe una idea inicial más clara para la IA.");
      return;
    }

    if (idsRecursosSeleccionados.length === 0) {
      toast.warning("Selecciona al menos un recurso para la generación IA.");
      return;
    }

    const tieneBrief = idsRecursosSeleccionados.some((idRecurso) =>
      recursosBrief.some((recurso) => recurso.idRecurso === idRecurso)
    );

    if (!tieneBrief) {
      toast.warning(
        "Para esta fase, selecciona al menos un Brief creativo o texto publicitario."
      );
      return;
    }

    if (limiteGeneracionesIniciadasAlcanzado) {
      toast.warning(mensajeLimiteGeneraciones);
      return;
    }

    if (generacionesIniciadasMismaSeleccion === 1) {
      toast.info(
        "Puedes crear una variante adicional o continuar preparando el prompt."
      );
    }

    try {
      setCreando(true);

      const nuevaGeneracion = await crearGeneracionIA({
        idCampana: campanaSeleccionada.idCampana,
        idAgente: null,
        prompt: prompt.trim(),
        tipoSalida: "video",
        idsRecursos: idsRecursosSeleccionados,
      });

      setGeneracionActiva(nuevaGeneracion);
      setGeneraciones((actuales) => [nuevaGeneracion, ...actuales]);

      toast.success("Generación IA iniciada. Continúa preparando el prompt.");
    } catch (error) {
      console.error(error);
      toast.error("Selecciona al menos un recurso para continuar.");
    } finally {
      setCreando(false);
    }
  }

  async function handlePrepararPrompt(idGeneracion: number) {
    if (!campanaActiva) {
      toast.warning(
        "Esta campaña debe estar activa para usar el Generador IA."
      );
      return;
    }

    try {
      setPreparandoId(idGeneracion);

      const actualizada = await prepararPromptIA(idGeneracion);

      setGeneracionActiva(actualizada);
      setGeneraciones((actuales) =>
        actuales.map((generacion) =>
          generacion.idGeneracion === idGeneracion ? actualizada : generacion
        )
      );

      toast.success("OpenAI preparó el resumen, guion y prompt final.");
    } catch (error) {
      console.error(error);
      toast.error("No se pudo preparar el prompt con OpenAI.");
    } finally {
      setPreparandoId(null);
    }
  }

  async function handleGenerarVideo() {
    if (!generacionActiva) {
      return;
    }

    if (!generacionActiva.promptFinal?.trim()) {
      toast.warning("Primero debes preparar el prompt final.");
      return;
    }

    if (!campanaActiva) {
      toast.warning(
        "Esta campaña debe estar activa para usar el Generador IA."
      );
      return;
    }

    if (generacionActiva.estado === "completado") {
      toast.warning("Esta generación IA ya fue completada.");
      return;
    }

    setGenerandoVideo(true);
    const toastId = toast.loading("Generando video con Google Veo...");

    try {
      const actualizada = await generarVideoIA(generacionActiva.idGeneracion);

      setGeneracionActiva(actualizada);
      setGeneraciones((actuales) =>
        actuales.map((generacion) =>
          generacion.idGeneracion === actualizada.idGeneracion
            ? actualizada
            : generacion
        )
      );

      try {
        const generacionesData = await listarGeneracionesIA();
        setGeneraciones(generacionesData);
      } catch (error) {
        console.error(error);
      }

      toast.success("Video generado correctamente.", { id: toastId });
    } catch (error) {
      console.error(error);
      toast.error("No se pudo generar el video.", { id: toastId });
    } finally {
      setGenerandoVideo(false);
    }
  }

  function seleccionarGeneracion(generacion: GeneracionIA) {
    setGeneracionActiva(generacion);
    setIdCampanaSeleccionada(generacion.idCampana);
  }

  if (estadoCarga === "loading") {
    return (
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 md:px-6">
        <Card className="border-border bg-card">
          <CardContent className="flex min-h-[260px] items-center justify-center">
            <div className="flex items-center gap-3 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Cargando Generador IA...
            </div>
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 md:px-6">
      <section className="rounded-3xl border border-border bg-card p-6 shadow-sm">
        <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
              <WandSparkles className="h-7 w-7" />
            </div>

            <div>
              <h1 className="text-2xl font-bold tracking-tight text-foreground">
                Generador IA
              </h1>
              <p className="mt-1 max-w-3xl text-sm leading-6 text-muted-foreground">
                Crea una solicitud IA usando campaña, brief creativo e imágenes.
                OpenAI prepara el resumen, guion y prompt final para generar
                video con Google Veo más adelante.
              </p>
            </div>
          </div>

          <Badge className="w-fit border-primary/20 bg-primary/10 px-3 py-1 text-primary">
            OpenAI conectado
          </Badge>
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[420px_minmax(0,1fr)]">
        <div className="flex flex-col gap-6">
          <Card className="border-border bg-card">
            <CardContent className="p-5">
              <div className="mb-4 flex items-center justify-between gap-3">
                <div>
                  <h2 className="text-lg font-semibold text-foreground">
                    1. Selecciona campaña
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Usa una campaña activa para generar contenido IA.
                  </p>
                </div>
                <Sparkles className="h-5 w-5 text-primary" />
              </div>

              <div className="space-y-3">
                {campanas.length === 0 ? (
                  <div className="rounded-2xl border border-dashed border-border p-4 text-sm text-muted-foreground">
                    No tienes campañas disponibles.
                  </div>
                ) : (
                  campanas.map((campana) => {
                    const seleccionada =
                      campana.idCampana === idCampanaSeleccionada;

                    return (
                      <button
                        key={campana.idCampana}
                        type="button"
                        onClick={() =>
                          void handleSeleccionarCampana(campana.idCampana)
                        }
                        className={`w-full rounded-2xl border p-4 text-left transition hover:border-primary/40 hover:bg-primary/5 ${
                          seleccionada
                            ? "border-primary/40 bg-primary/10"
                            : "border-border bg-background/40"
                        }`}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <p className="font-semibold text-foreground">
                              {campana.nombre}
                            </p>
                            <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                              {normalizarTexto(campana.objetivo)}
                            </p>
                          </div>

                          <Badge
                            variant="outline"
                            className={
                              estadoCampanaStyles[campana.estado] ??
                              estadoCampanaStyles.borrador
                            }
                          >
                            {campana.estado}
                          </Badge>
                        </div>
                      </button>
                    );
                  })
                )}
              </div>
            </CardContent>
          </Card>

          <Card className="border-border bg-card">
            <CardContent className="p-5">
              <div className="mb-4">
                <h2 className="text-lg font-semibold text-foreground">
                  2. Recursos para IA
                </h2>
                <p className="text-sm text-muted-foreground">
                  Para esta fase usa principalmente Brief creativo. Imagen y
                  video se dejan como referencia.
                </p>
              </div>

              {cargandoRecursos ? (
                <div className="flex items-center gap-2 rounded-2xl border border-border p-4 text-sm text-muted-foreground">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Cargando recursos...
                </div>
              ) : recursosDisponiblesIA.length === 0 ? (
                <div className="rounded-2xl border border-dashed border-border p-4 text-sm leading-6 text-muted-foreground">
                  Esta campaña no tiene recursos activos compatibles para IA.
                  Agrega un <strong>Brief creativo</strong> desde Recursos.
                </div>
              ) : (
                <div className="space-y-3">
                  {recursosDisponiblesIA.map((recurso) => {
                    const Icono = tipoRecursoIcon[recurso.tipo] ?? FileText;
                    const seleccionado = idsRecursosSeleccionados.includes(
                      recurso.idRecurso
                    );

                    return (
                      <button
                        key={recurso.idRecurso}
                        type="button"
                        onClick={() => alternarRecurso(recurso.idRecurso)}
                        className={`w-full rounded-2xl border p-4 text-left transition hover:border-primary/40 hover:bg-primary/5 ${
                          seleccionado
                            ? "border-primary/40 bg-primary/10"
                            : "border-border bg-background/40"
                        }`}
                      >
                        <div className="flex items-start gap-3">
                          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-border bg-card text-primary">
                            <Icono className="h-5 w-5" />
                          </div>

                          <div className="min-w-0 flex-1">
                            <div className="flex flex-wrap items-center gap-2">
                              <p className="font-medium text-foreground">
                                {recurso.titulo ?? recurso.nombreArchivo}
                              </p>
                              <Badge variant="outline">
                                {tipoRecursoLabel[recurso.tipo]}
                              </Badge>
                            </div>

                            <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                              {recurso.tipo === "copy"
                                ? normalizarTexto(recurso.contenidoTexto)
                                : recurso.nombreArchivo}
                            </p>
                          </div>

                          {seleccionado && (
                            <CheckCircle2 className="h-5 w-5 shrink-0 text-primary" />
                          )}
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <div className="flex flex-col gap-6">
          <Card className="border-border bg-card">
            <CardContent className="p-5">
              <div className="mb-4 flex items-center justify-between gap-3">
                <div>
                  <h2 className="text-lg font-semibold text-foreground">
                    3. Idea inicial
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Escribe qué quieres lograr. OpenAI lo convertirá en guion y
                    prompt profesional.
                  </p>
                </div>
                <BrainCircuit className="h-5 w-5 text-primary" />
              </div>

              {campanaSeleccionada && (
                <div className="mb-4 rounded-2xl border border-border bg-background/50 p-4">
                  <div>
                    <p className="text-sm font-medium text-foreground">
                      Campaña seleccionada
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {campanaSeleccionada.nombre}
                    </p>
                  </div>

                  {(campanaSeleccionada.objetivo?.trim() ||
                    campanaSeleccionada.descripcion?.trim()) && (
                    <div className="mt-4 grid gap-3">
                      {campanaSeleccionada.objetivo?.trim() && (
                        <div>
                          <p className="mb-1.5 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                            Objetivo
                          </p>
                          <div className="max-h-32 overflow-y-auto rounded-xl border border-border bg-card p-3">
                            <p className="whitespace-pre-line text-sm leading-6 text-foreground">
                              {campanaSeleccionada.objetivo}
                            </p>
                          </div>
                        </div>
                      )}

                      {campanaSeleccionada.descripcion?.trim() && (
                        <div>
                          <p className="mb-1.5 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                            Descripción
                          </p>
                          <div className="max-h-32 overflow-y-auto rounded-xl border border-border bg-card p-3">
                            <p className="whitespace-pre-line text-sm leading-6 text-foreground">
                              {campanaSeleccionada.descripcion}
                            </p>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}

              {campanaSeleccionada && !campanaActiva && (
                <div className="mb-4 rounded-2xl border border-primary/20 bg-primary/10 p-4 text-sm text-primary">
                  Esta campaña debe estar activa para usar el Generador IA.
                </div>
              )}

              <textarea
                value={prompt}
                onChange={(event) => setPrompt(event.target.value)}
                placeholder="Ej: Crea un video promocional realista para vender este curso, mostrando personas usando el producto o servicio en situaciones naturales."
                className="min-h-[130px] w-full resize-none rounded-2xl border border-border bg-background px-4 py-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground focus:border-primary/50 focus:ring-4 focus:ring-primary/10"
              />

              <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <p className="text-xs text-muted-foreground">
                  Recursos seleccionados: {idsRecursosSeleccionados.length}
                </p>

                <div className="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    onClick={handleCrearGeneracion}
                    disabled={
                      creando ||
                      !campanaActiva ||
                      limiteGeneracionesIniciadasAlcanzado
                    }
                    className="rounded-xl"
                  >
                    {creando ? (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    ) : (
                      <Bot className="mr-2 h-4 w-4" />
                    )}
                    Crear generación IA
                  </Button>

                  {generacionActiva && (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() =>
                        void handlePrepararPrompt(generacionActiva.idGeneracion)
                      }
                      disabled={
                        preparandoId === generacionActiva.idGeneracion ||
                        !campanaActiva
                      }
                      className="rounded-xl"
                    >
                      {preparandoId === generacionActiva.idGeneracion ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Sparkles className="mr-2 h-4 w-4" />
                      )}
                      Preparar prompt
                    </Button>
                  )}
                </div>
              </div>

              {limiteGeneracionesIniciadasAlcanzado && (
                <p className="mt-3 text-xs text-muted-foreground">
                  {mensajeLimiteGeneraciones}
                </p>
              )}
            </CardContent>
          </Card>

          <Card className="border-border bg-card">
            <CardContent className="p-5">
              <div className="mb-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                <div>
                  <h2 className="text-lg font-semibold text-foreground">
                    Resultado IA
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Revisa lo que OpenAI preparó antes de generar video.
                  </p>
                </div>

                {generacionActiva && (
                  <div className="flex items-center gap-2">
                    <Badge
                      variant="outline"
                      className={
                        estadoGeneracionStyles[generacionActiva.estado] ??
                        estadoGeneracionStyles.pendiente
                      }
                    >
                      {obtenerEstadoGeneracionVisible(generacionActiva)}
                    </Badge>
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => setGeneracionActiva(null)}
                      className="h-8 rounded-xl border-border px-3 text-xs text-muted-foreground"
                    >
                      Ocultar
                    </Button>
                  </div>
                )}
              </div>

              {!generacionActiva ? (
                <div className="flex min-h-[260px] flex-col items-center justify-center rounded-2xl border border-dashed border-border p-6 text-center">
                  <WandSparkles className="mb-3 h-8 w-8 text-muted-foreground" />
                  <p className="font-medium text-foreground">
                    Aún no hay generación seleccionada
                  </p>
                  <p className="mt-1 max-w-md text-sm text-muted-foreground">
                    Crea una generación IA y luego prepara el prompt con OpenAI.
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="rounded-2xl border border-border bg-background/50 p-4">
                    <div className="mb-4 flex flex-wrap gap-2">
                      <Button
                        type="button"
                        size="sm"
                        variant={
                          vistaResultadoIA === "explicacion"
                            ? "default"
                            : "outline"
                        }
                        onClick={() => setVistaResultadoIA("explicacion")}
                        className={`rounded-xl ${
                          vistaResultadoIA === "explicacion"
                            ? "border-primary/20 bg-primary/10 text-primary hover:bg-primary/10"
                            : "border-border text-muted-foreground"
                        }`}
                      >
                        Ver explicación
                      </Button>

                      <Button
                        type="button"
                        size="sm"
                        variant={
                          vistaResultadoIA === "promptEspanol"
                            ? "default"
                            : "outline"
                        }
                        onClick={() => setVistaResultadoIA("promptEspanol")}
                        className={`rounded-xl ${
                          vistaResultadoIA === "promptEspanol"
                            ? "border-primary/20 bg-primary/10 text-primary hover:bg-primary/10"
                            : "border-border text-muted-foreground"
                        }`}
                      >
                        Prompt técnico en español
                      </Button>

                      <Button
                        type="button"
                        size="sm"
                        variant={
                          vistaResultadoIA === "promptVeo"
                            ? "default"
                            : "outline"
                        }
                        onClick={() => setVistaResultadoIA("promptVeo")}
                        className={`rounded-xl ${
                          vistaResultadoIA === "promptVeo"
                            ? "border-primary/20 bg-primary/10 text-primary hover:bg-primary/10"
                            : "border-border text-muted-foreground"
                        }`}
                      >
                        Prompt técnico para Veo
                      </Button>
                    </div>

                    {vistaResultadoIA === "explicacion" ? (
                      <div className="space-y-4">
                        <div>
                          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                            Resumen del contexto
                          </p>
                          <p className="whitespace-pre-line text-sm leading-6 text-foreground">
                            {normalizarTexto(generacionActiva.resumenContexto)}
                          </p>
                        </div>

                        <div>
                          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                            Guion generado
                          </p>
                          <p className="whitespace-pre-line text-sm leading-6 text-foreground">
                            {normalizarTexto(generacionActiva.guionGenerado)}
                          </p>
                        </div>

                        <div className="rounded-2xl border border-primary/20 bg-primary/10 p-4">
                          <p className="text-sm leading-6 text-primary">
                            El video se generará usando el prompt técnico en
                            inglés optimizado para Google Veo.
                          </p>
                        </div>
                      </div>
                    ) : vistaResultadoIA === "promptEspanol" ? (
                      <div className="space-y-4">
                        <div>
                          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                            Prompt técnico en español
                          </p>
                          <p className="whitespace-pre-line text-sm leading-6 text-foreground">
                            {generacionActiva.promptFinalEspanol?.trim() ||
                              "Esta generación todavía no tiene prompt técnico en español. Vuelve a preparar el prompt o crea una nueva generación."}
                          </p>
                        </div>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        <div className="rounded-2xl border border-primary/20 bg-primary/10 p-4">
                          <p className="text-sm leading-6 text-primary">
                            Este es el prompt técnico en inglés que se enviará
                            a Google Veo.
                          </p>
                        </div>

                        <div>
                          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                            Prompt técnico para Veo · Inglés optimizado
                          </p>
                          <p className="whitespace-pre-line text-sm leading-6 text-foreground">
                            {normalizarTexto(generacionActiva.promptFinal)}
                          </p>
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="flex flex-col gap-3 rounded-2xl border border-border bg-background/50 p-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <p className="text-sm font-medium text-foreground">
                        Generación de video
                      </p>
                      <p className="text-sm text-muted-foreground">
                        Google Veo generará el video y lo guardará como recurso.
                      </p>
                    </div>

                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleGenerarVideo}
                      disabled={
                        !generacionActiva ||
                        !generacionActiva.promptFinal?.trim() ||
                        !campanaActiva ||
                        generacionActiva.estado === "completado" ||
                        generandoVideo ||
                        creando ||
                        preparandoId !== null
                      }
                      className="rounded-xl"
                    >
                      {generandoVideo ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <PlayCircle className="mr-2 h-4 w-4" />
                      )}
                      {generandoVideo ? "Generando video..." : "Generar video"}
                    </Button>
                  </div>

                  {(generandoVideo || idRecursoVideoGenerado) && (
                    <div className="rounded-2xl border border-primary/20 bg-primary/10 p-4">
                      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                        <div>
                          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-primary">
                            Video generado
                          </p>
                          {generandoVideo ? (
                            <p className="text-sm text-muted-foreground">
                              Generando video con Google Veo...
                            </p>
                          ) : (
                            <div className="space-y-1.5">
                              <h3 className="text-base font-semibold text-foreground">
                                {tituloVideoGenerado}
                              </h3>
                              <p className="max-w-xl text-sm leading-6 text-muted-foreground">
                                Tu video generado está listo. Puedes
                                reproducirlo aquí o verlo en Recursos.
                              </p>
                            </div>
                          )}
                        </div>

                        {idRecursoVideoGenerado && (
                          <Button
                            type="button"
                            onClick={() => navigate("/recursos")}
                            className="rounded-xl"
                          >
                            Ir a Recursos
                          </Button>
                        )}
                      </div>

                      {idRecursoVideoGenerado && (
                        <div className="overflow-hidden rounded-2xl border border-border bg-card">
                          {cargandoVideoGenerado ? (
                            <div className="flex min-h-[220px] items-center justify-center gap-2 p-6 text-sm text-muted-foreground">
                              <Loader2 className="h-4 w-4 animate-spin" />
                              Cargando video generado...
                            </div>
                          ) : errorVideoGenerado ? (
                            <div className="flex min-h-[180px] items-center justify-center p-6 text-center text-sm text-muted-foreground">
                              {errorVideoGenerado}
                            </div>
                          ) : urlVideoGenerado &&
                            idUrlVideoGenerado === idRecursoVideoGenerado ? (
                            <video
                              controls
                              src={urlVideoGenerado}
                              className="aspect-video w-full bg-card"
                            >
                              Tu navegador no puede reproducir este video.
                            </video>
                          ) : null}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="border-border bg-card">
            <CardContent className="p-5">
              <div className="mb-4 flex items-center justify-between">
                <div>
                  <h2 className="text-lg font-semibold text-foreground">
                    Historial reciente
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Generaciones IA creadas por tu usuario.
                  </p>
                </div>
              </div>

              {generaciones.length === 0 ? (
                <div className="rounded-2xl border border-dashed border-border p-4 text-sm text-muted-foreground">
                  Todavía no tienes generaciones IA.
                </div>
              ) : (
                <div className="space-y-3">
                  {generaciones.slice(0, 6).map((generacion) => (
                    <button
                      key={generacion.idGeneracion}
                      type="button"
                      onClick={() => seleccionarGeneracion(generacion)}
                      className={`w-full rounded-2xl border p-4 text-left transition hover:border-primary/40 hover:bg-primary/5 ${
                        generacionActiva?.idGeneracion ===
                        generacion.idGeneracion
                          ? "border-primary/40 bg-primary/10"
                          : "border-border bg-background/40"
                      }`}
                    >
                      <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                        <div className="min-w-0">
                          <p className="line-clamp-1 font-medium text-foreground">
                            {generacion.nombreCampana}
                          </p>
                          <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                            {generacion.prompt}
                          </p>
                          <p className="mt-2 text-xs text-muted-foreground">
                            {formatearFecha(generacion.fechaCreacion)}
                          </p>
                        </div>

                        <Badge
                          variant="outline"
                          className={
                            estadoGeneracionStyles[generacion.estado] ??
                            estadoGeneracionStyles.pendiente
                          }
                        >
                          {generacion.estado}
                        </Badge>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </section>
    </main>
  );
}
