// src/pages/recursos/RecursosPage.tsx

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Archive,
  ChevronRight,
  Eye,
  FileText,
  ImageIcon,
  Layers,
  Loader2,
  Pencil,
  Plus,
  Search,
  Trash2,
  Video,
  X,
} from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

import {
  obtenerCampanas,
  type Campana,
  type EstadoCampana,
} from "@/services/campanaService";

import {
  actualizarRecurso,
  actualizarTituloRecurso,
  archivarRecurso,
  crearRecurso,
  crearUrlArchivoRecurso,
  desarchivarRecurso,
  eliminarRecurso,
  obtenerRecursosPorCampana,
  subirRecursoArchivo,
  type Recurso,
  type RecursoRequest,
  type TipoRecurso,
} from "@/services/recursoService";

type FormState = {
  tipo: TipoRecurso;
  titulo: string;
  nombreArchivo: string;
  urlArchivo: string;
  contenidoTexto: string;
  pesoMb: string;
  formato: string;
  archivo: File | null;
};

type FiltroEstadoRecurso = "todos" | "activo" | "archivado";

type PreviewRecurso = {
  recurso: Recurso;
  url: string;
};

type ApiErrorLike = {
  response?: {
    data?: {
      mensaje?: unknown;
    };
  };
};

const formInicial: FormState = {
  tipo: "imagen",
  titulo: "",
  nombreArchivo: "",
  urlArchivo: "",
  contenidoTexto: "",
  pesoMb: "",
  formato: "",
  archivo: null,
};

const estadosCampana: Record<EstadoCampana, string> = {
  borrador: "Borrador",
  activa: "Activa",
  pausada: "Pausada",
  finalizada: "Finalizada",
};

const tiposRecurso: Record<TipoRecurso, string> = {
  imagen: "Imagen",
  video: "Video",
  documento: "Archivo",
  copy: "Idea de campaña",
};

function generarIdentificadorRecurso(titulo: string) {
  const normalizado = titulo
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, "-")
    .replace(/[^a-z0-9-]/g, "")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "");

  return normalizado || `recurso-${Date.now()}`;
}

function obtenerMensajeError(error: unknown, mensajePorDefecto: string) {
  if (typeof error === "object" && error !== null && "response" in error) {
    const errorApi = error as ApiErrorLike;
    const mensaje = errorApi.response?.data?.mensaje;
    if (typeof mensaje === "string" && mensaje.trim().length > 0) {
      return mensaje;
    }
  }
  return mensajePorDefecto;
}

function obtenerIconoTipo(tipo: TipoRecurso) {
  if (tipo === "imagen") return <ImageIcon className="h-3.5 w-3.5" />;
  if (tipo === "video") return <Video className="h-3.5 w-3.5" />;
  return <FileText className="h-3.5 w-3.5" />;
}

function claseEstadoCampana(estado: EstadoCampana) {
  if (estado === "activa") {
    return "border-emerald-500/30 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400";
  }
  if (estado === "pausada") {
    return "border-amber-500/30 bg-amber-500/10 text-amber-600 dark:text-amber-400";
  }
  if (estado === "finalizada") {
    return "border-slate-500/30 bg-slate-500/10 text-slate-500 dark:text-slate-400";
  }
  return "border-sky-500/30 bg-sky-500/10 text-sky-600 dark:text-sky-400";
}

function claseEstadoDot(estado: EstadoCampana) {
  if (estado === "activa") return "bg-emerald-500";
  if (estado === "pausada") return "bg-amber-500";
  if (estado === "finalizada") return "bg-slate-400";
  return "bg-sky-500";
}

function mensajePorEstado(campana: Campana) {
  if (campana.estado === "borrador") {
    return "Campaña en borrador. Puedes preparar recursos antes de activarla.";
  }
  if (campana.estado === "activa") {
    return "Campaña activa. Puedes administrar sus recursos normalmente.";
  }
  if (campana.estado === "pausada") {
    return "Campaña pausada. Puedes preparar recursos, pero se reactiva desde Campañas.";
  }
  return "Campaña finalizada. Sus recursos están disponibles solo como historial.";
}

export default function RecursosPage() {
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [campanaSeleccionada, setCampanaSeleccionada] =
    useState<Campana | null>(null);

  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [cargandoCampanas, setCargandoCampanas] = useState(true);
  const [cargandoRecursos, setCargandoRecursos] = useState(false);
  const [guardando, setGuardando] = useState(false);

  const [busquedaCampana, setBusquedaCampana] = useState("");
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [recursoEditando, setRecursoEditando] = useState<Recurso | null>(null);
  const [form, setForm] = useState<FormState>(formInicial);
  const [mostrarTodasCampanas, setMostrarTodasCampanas] = useState(false);

  const [filtroEstado, setFiltroEstado] =
    useState<FiltroEstadoRecurso>("todos");

  const [previewRecurso, setPreviewRecurso] = useState<PreviewRecurso | null>(
    null
  );

  const [cargandoPreview, setCargandoPreview] = useState(false);

  const campanaFinalizada = campanaSeleccionada?.estado === "finalizada";
  const editandoVideo = recursoEditando?.tipo === "video";

  const recursosActivos = useMemo(
    () => recursos.filter((recurso) => recurso.estado === "activo"),
    [recursos]
  );

  const recursosArchivados = useMemo(
    () => recursos.filter((recurso) => recurso.estado === "archivado"),
    [recursos]
  );

  const recursosFiltrados = useMemo(() => {
    if (filtroEstado === "activo") return recursosActivos;
    if (filtroEstado === "archivado") return recursosArchivados;
    return recursos;
  }, [filtroEstado, recursos, recursosActivos, recursosArchivados]);

  const campanasFiltradas = useMemo(() => {
    const query = busquedaCampana.trim().toLowerCase();
    if (!query) return campanas;
    return campanas.filter((campana) => {
      const nombre = campana.nombre.toLowerCase();
      const objetivo = (campana.objetivo ?? "").toLowerCase();
      return nombre.includes(query) || objetivo.includes(query);
    });
  }, [busquedaCampana, campanas]);

  const LIMITE_CAMPANAS = 5;
  const campanasVisibles = useMemo(() => {
    return mostrarTodasCampanas
      ? campanasFiltradas
      : campanasFiltradas.slice(0, LIMITE_CAMPANAS);
  }, [campanasFiltradas, mostrarTodasCampanas]);

  const hayMasCampanas = campanasFiltradas.length > LIMITE_CAMPANAS;

  const cargarRecursos = useCallback(async (idCampana: number) => {
    try {
      setCargandoRecursos(true);
      const data = await obtenerRecursosPorCampana(idCampana);
      setRecursos(data);
    } catch {
      toast.error("No se pudieron cargar los recursos");
    } finally {
      setCargandoRecursos(false);
    }
  }, []);

  useEffect(() => {
    let cancelado = false;
    async function cargarCampanasIniciales() {
      try {
        const data = await obtenerCampanas();
        if (!cancelado) {
          setCampanas(data);
        }
      } catch {
        if (!cancelado) {
          toast.error("No se pudieron cargar las campañas");
        }
      } finally {
        if (!cancelado) {
          setCargandoCampanas(false);
        }
      }
    }
    void cargarCampanasIniciales();
    return () => {
      cancelado = true;
    };
  }, []);

  useEffect(() => {
    return () => {
      if (previewRecurso?.url) {
        URL.revokeObjectURL(previewRecurso.url);
      }
    };
  }, [previewRecurso]);

  async function seleccionarCampana(campana: Campana) {
    if (previewRecurso?.url) {
      URL.revokeObjectURL(previewRecurso.url);
    }
    setPreviewRecurso(null);
    setCampanaSeleccionada(campana);
    setMostrarFormulario(false);
    setRecursoEditando(null);
    setForm(formInicial);
    setFiltroEstado("todos");
    await cargarRecursos(campana.idCampana);
  }

  function abrirFormularioCrear() {
    if (!campanaSeleccionada) {
      toast.warning("Selecciona una campaña primero");
      return;
    }
    if (campanaSeleccionada.estado === "finalizada") {
      toast.warning(
        "Esta campaña está finalizada y solo se puede ver como historial"
      );
      return;
    }
    setRecursoEditando(null);
    setForm(formInicial);
    setMostrarFormulario(true);
  }

  function abrirFormularioEditar(recurso: Recurso) {
    if (campanaFinalizada) {
      toast.warning("No se pueden editar recursos de una campaña finalizada");
      return;
    }
    setRecursoEditando(recurso);
    setForm({
      tipo: recurso.tipo,
      titulo: recurso.titulo ?? "",
      nombreArchivo: recurso.nombreArchivo,
      urlArchivo: recurso.urlArchivo ?? "",
      contenidoTexto: recurso.contenidoTexto ?? "",
      pesoMb: recurso.pesoMb !== null ? String(recurso.pesoMb) : "",
      formato: recurso.formato ?? "",
      archivo: null,
    });
    setMostrarFormulario(true);
  }

  function cancelarFormulario() {
    setMostrarFormulario(false);
    setRecursoEditando(null);
    setForm(formInicial);
  }

  function validarFormulario() {
    if (!campanaSeleccionada) {
      toast.error("Selecciona una campaña");
      return false;
    }
    if (!form.tipo) {
      toast.error("Selecciona un tipo de recurso");
      return false;
    }
    if (!form.titulo.trim()) {
      toast.error("El título del recurso es obligatorio.");
      return false;
    }
    if (form.tipo === "copy") {
      if (!form.contenidoTexto.trim()) {
        toast.error("El contenido de la idea principal es obligatorio");
        return false;
      }
      return true;
    }
    if (recursoEditando) {
      return true;
    }
    if (!form.archivo) {
      toast.error("Selecciona un archivo desde tu PC");
      return false;
    }
    return true;
  }

  function construirRequest(): RecursoRequest {
    if (!campanaSeleccionada) {
      throw new Error("Campaña no seleccionada");
    }
    const esCopy = form.tipo === "copy";
    const nombreArchivo = esCopy
      ? generarIdentificadorRecurso(form.titulo)
      : form.nombreArchivo.trim() || generarIdentificadorRecurso(form.titulo);

    return {
      idCampana: campanaSeleccionada.idCampana,
      tipo: form.tipo,
      titulo: form.titulo.trim() || null,
      nombreArchivo,
      urlArchivo: esCopy ? null : form.urlArchivo.trim() || null,
      contenidoTexto: esCopy ? form.contenidoTexto.trim() : null,
      pesoMb: esCopy || !form.pesoMb ? null : Number(form.pesoMb),
      formato: form.formato.trim() || (esCopy ? "texto" : null),
    };
  }

  async function guardarRecurso() {
    if (!validarFormulario() || !campanaSeleccionada) return;
    try {
      setGuardando(true);
      if (recursoEditando) {
        if (recursoEditando.tipo === "video") {
          const titulo = form.titulo.trim();
          await actualizarTituloRecurso(recursoEditando.idRecurso, titulo);
          toast.success("Título del video actualizado correctamente");
        } else {
          const payload = construirRequest();
          await actualizarRecurso(recursoEditando.idRecurso, payload);
          toast.success("Recurso actualizado correctamente");
        }
      } else if (form.tipo === "copy") {
        const payload = construirRequest();
        await crearRecurso(payload);
        toast.success("Idea de campaña creada correctamente");
      } else {
        if (!form.archivo) {
          toast.error("Selecciona un archivo desde tu PC");
          return;
        }
        await subirRecursoArchivo({
          idCampana: campanaSeleccionada.idCampana,
          tipo: form.tipo,
          titulo: form.titulo.trim() || null,
          archivo: form.archivo,
        });
        toast.success("Archivo subido correctamente");
      }
      await cargarRecursos(campanaSeleccionada.idCampana);
      cancelarFormulario();
    } catch (error: unknown) {
      toast.error(obtenerMensajeError(error, "No se pudo guardar el recurso"));
    } finally {
      setGuardando(false);
    }
  }

  async function manejarArchivar(recurso: Recurso) {
    if (campanaFinalizada) {
      toast.warning("No se pueden archivar recursos de una campaña finalizada");
      return;
    }
    const confirmar = window.confirm("¿Deseas archivar este recurso?");
    if (!confirmar || !campanaSeleccionada) return;
    try {
      await archivarRecurso(recurso.idRecurso);
      toast.success("Recurso archivado correctamente");
      await cargarRecursos(campanaSeleccionada.idCampana);
    } catch (error: unknown) {
      toast.error(obtenerMensajeError(error, "No se pudo archivar el recurso"));
    }
  }

  async function manejarDesarchivar(recurso: Recurso) {
    if (campanaFinalizada) {
      toast.warning(
        "No se pueden desarchivar recursos de una campaña finalizada"
      );
      return;
    }
    const confirmar = window.confirm("¿Deseas desarchivar este recurso?");
    if (!confirmar || !campanaSeleccionada) return;
    try {
      await desarchivarRecurso(recurso.idRecurso);
      toast.success("Recurso desarchivado correctamente");
      await cargarRecursos(campanaSeleccionada.idCampana);
    } catch (error: unknown) {
      toast.error(
        obtenerMensajeError(error, "No se pudo desarchivar el recurso")
      );
    }
  }

  async function manejarEliminar(recurso: Recurso) {
    if (campanaFinalizada) {
      toast.warning("No se pueden eliminar recursos de una campaña finalizada");
      return;
    }
    const confirmar = window.confirm(
      "¿Seguro que deseas eliminar este recurso? Esta acción no se puede deshacer."
    );
    if (!confirmar || !campanaSeleccionada) return;
    try {
      await eliminarRecurso(recurso.idRecurso);
      toast.success("Recurso eliminado correctamente");
      await cargarRecursos(campanaSeleccionada.idCampana);
    } catch (error: unknown) {
      toast.error(obtenerMensajeError(error, "No se pudo eliminar el recurso"));
    }
  }

  async function verArchivoSeguro(recurso: Recurso) {
    if (recurso.tipo === "copy") {
      toast.info(
        "Este recurso es una idea de campaña, no tiene archivo para abrir"
      );
      return;
    }
    try {
      setCargandoPreview(true);
      if (previewRecurso?.url) {
        URL.revokeObjectURL(previewRecurso.url);
      }
      const urlTemporal = await crearUrlArchivoRecurso(recurso.idRecurso);
      setPreviewRecurso({ recurso, url: urlTemporal });
    } catch (error: unknown) {
      toast.error(obtenerMensajeError(error, "No se pudo abrir el archivo"));
    } finally {
      setCargandoPreview(false);
    }
  }

  function cerrarPreview() {
    if (previewRecurso?.url) {
      URL.revokeObjectURL(previewRecurso.url);
    }
    setPreviewRecurso(null);
  }

  function formatearFecha(fecha: string | null) {
    if (!fecha) return "—";
    return new Intl.DateTimeFormat("es-PE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(fecha));
  }

  return (
    <section className="mx-auto flex w-full max-w-[1500px] flex-col gap-6 px-4 py-6 md:px-6 lg:px-8">
      {/* ── HEADER ── */}
      <header className="relative overflow-hidden rounded-3xl border border-border/60 bg-card/80 shadow-sm backdrop-blur-sm">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-transparent pointer-events-none" />
        <div className="relative flex flex-col gap-4 p-6 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary ring-1 ring-primary/20 shadow-sm">
              <Layers className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-xl font-semibold tracking-tight text-foreground md:text-2xl">
                Recursos multimedia
              </h1>
              <p className="mt-0.5 text-sm text-muted-foreground">
                Administra imágenes, videos e ideas de campaña asociados a tus
                campañas.
              </p>
            </div>
          </div>

          <Button
            type="button"
            onClick={abrirFormularioCrear}
            disabled={!campanaSeleccionada || campanaFinalizada}
            className="h-10 gap-2 rounded-xl px-5 text-sm font-medium self-start md:self-center"
          >
            <Plus className="h-4 w-4" />
            Agregar recurso
          </Button>
        </div>
      </header>

      {/* ── CAMPAÑAS ── */}
      <section className="rounded-3xl border border-border/60 bg-card/80 shadow-sm backdrop-blur-sm">
        <div className="flex flex-col gap-5 p-6">
          {/* Encabezado campañas */}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex items-center gap-2.5">
                <h2 className="text-base font-semibold text-foreground">
                  Campañas
                </h2>
                <span className="inline-flex items-center justify-center rounded-full bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary ring-1 ring-primary/20">
                  {campanas.length}
                </span>
              </div>
              <p className="mt-0.5 text-sm text-muted-foreground">
                Selecciona una campaña para ver y gestionar sus recursos.
              </p>
            </div>

            <div className="relative w-full sm:max-w-xs">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground/50" />
              <input
                value={busquedaCampana}
                onChange={(e) => setBusquedaCampana(e.target.value)}
                placeholder="Buscar campaña..."
                className="h-10 w-full rounded-xl border border-border/70 bg-background/80 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground/50 outline-none transition-all focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
              />
            </div>
          </div>

          {/* Grid de campañas */}
          {cargandoCampanas ? (
            <div className="flex items-center gap-2.5 rounded-2xl border border-border/50 bg-muted/30 p-4 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin text-primary" />
              Cargando campañas...
            </div>
          ) : campanas.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-border/60 bg-muted/20 p-5 text-sm text-muted-foreground">
              No tienes campañas registradas todavía.
            </div>
          ) : campanasFiltradas.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-border/60 bg-muted/20 p-5 text-sm text-muted-foreground">
              No se encontraron campañas con esa búsqueda.
            </div>
          ) : (
            <>
              {/* Nota de límite */}
              {!mostrarTodasCampanas && hayMasCampanas && (
                <p className="text-xs text-muted-foreground -mb-1">
                  Mostrando tus últimas{" "}
                  <span className="font-medium text-foreground">
                    {LIMITE_CAMPANAS}
                  </span>{" "}
                  campañas recientes.
                </p>
              )}

              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
                {campanasVisibles.map((campana) => {
                  const seleccionada =
                    campanaSeleccionada?.idCampana === campana.idCampana;

                  return (
                    <button
                      key={campana.idCampana}
                      type="button"
                      onClick={() => seleccionarCampana(campana)}
                      className={[
                        "group relative flex flex-col justify-between rounded-2xl border p-4 text-left transition-all duration-200",
                        seleccionada
                          ? "border-primary/50 bg-primary/8 shadow-md shadow-primary/10 ring-1 ring-primary/20"
                          : "border-border/60 bg-background/60 hover:border-primary/30 hover:bg-primary/4 hover:shadow-sm",
                      ].join(" ")}
                    >
                      {/* Dot seleccionado */}
                      {seleccionada && (
                        <span className="absolute right-3 top-3 flex h-2 w-2">
                          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-60" />
                          <span className="relative inline-flex h-2 w-2 rounded-full bg-primary" />
                        </span>
                      )}

                      <div className="flex flex-col gap-3">
                        {/* Nombre + estado */}
                        <div className="flex items-start justify-between gap-2 pr-3">
                          <h3 className="min-w-0 flex-1 text-sm font-semibold leading-snug text-foreground line-clamp-2">
                            {campana.nombre}
                          </h3>
                        </div>

                        <div className="flex items-center gap-1.5">
                          <span
                            className={[
                              "h-1.5 w-1.5 rounded-full shrink-0",
                              claseEstadoDot(campana.estado),
                            ].join(" ")}
                          />
                          <span
                            className={[
                              "text-[11px] font-medium",
                              campana.estado === "activa"
                                ? "text-emerald-600 dark:text-emerald-400"
                                : campana.estado === "pausada"
                                ? "text-amber-600 dark:text-amber-400"
                                : campana.estado === "finalizada"
                                ? "text-slate-500 dark:text-slate-400"
                                : "text-sky-600 dark:text-sky-400",
                            ].join(" ")}
                          >
                            {estadosCampana[campana.estado]}
                          </span>
                        </div>

                        {/* Objetivo */}
                        <div className="rounded-xl border border-border/50 bg-muted/40 px-3 py-2">
                          <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground/60 mb-1">
                            Objetivo
                          </p>
                          <p className="line-clamp-2 text-xs leading-relaxed text-muted-foreground">
                            {campana.objetivo || "Objetivo pendiente"}
                          </p>
                        </div>
                      </div>

                      {campana.estado === "finalizada" && (
                        <div className="mt-3">
                          <span className="inline-flex items-center gap-1 rounded-full border border-slate-500/20 bg-slate-500/8 px-2 py-0.5 text-[10px] font-medium text-slate-500 dark:text-slate-400">
                            Solo historial
                          </span>
                        </div>
                      )}
                    </button>
                  );
                })}
              </div>

              {/* Botón ver más / ver menos */}
              {hayMasCampanas && (
                <div className="flex items-center justify-between pt-1">
                  <p className="text-xs text-muted-foreground">
                    {mostrarTodasCampanas
                      ? `Mostrando todas las ${campanasFiltradas.length} campañas`
                      : `${campanasFiltradas.length - LIMITE_CAMPANAS} campaña${
                          campanasFiltradas.length - LIMITE_CAMPANAS !== 1
                            ? "s"
                            : ""
                        } más disponible${
                          campanasFiltradas.length - LIMITE_CAMPANAS !== 1
                            ? "s"
                            : ""
                        }`}
                  </p>
                  <button
                    type="button"
                    onClick={() => setMostrarTodasCampanas((v) => !v)}
                    className="flex items-center gap-1.5 rounded-xl border border-border/60 bg-background/70 px-3 py-2 text-xs font-medium text-foreground transition hover:border-primary/40 hover:bg-primary/5 hover:text-primary"
                  >
                    {mostrarTodasCampanas ? (
                      <>Ver menos</>
                    ) : (
                      <>
                        Ver todas las campañas
                        <ChevronRight className="h-3.5 w-3.5" />
                      </>
                    )}
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </section>

      {/* ── PANEL PRINCIPAL ── */}
      <main className="rounded-3xl border border-border/60 bg-card/80 shadow-sm backdrop-blur-sm">
        {!campanaSeleccionada ? (
          <div className="flex min-h-[480px] flex-col items-center justify-center rounded-3xl p-10 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl border border-border/60 bg-muted/40 text-muted-foreground/50">
              <ImageIcon className="h-7 w-7" />
            </div>
            <h2 className="mt-5 text-base font-semibold text-foreground">
              Selecciona una campaña
            </h2>
            <p className="mt-2 max-w-sm text-sm text-muted-foreground">
              Elige una campaña de arriba para ver y gestionar sus recursos
              multimedia.
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-6 p-6">
            {/* Subheader campaña seleccionada */}
            <div className="flex flex-col gap-4 border-b border-border/50 pb-6 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2.5">
                  <h2 className="text-xl font-semibold tracking-tight text-foreground">
                    {campanaSeleccionada.nombre}
                  </h2>
                  <div className="flex items-center gap-1.5">
                    <span
                      className={[
                        "h-1.5 w-1.5 rounded-full",
                        claseEstadoDot(campanaSeleccionada.estado),
                      ].join(" ")}
                    />
                    <Badge
                      className={[
                        "px-2 py-0.5 text-[11px] font-medium",
                        claseEstadoCampana(campanaSeleccionada.estado),
                      ].join(" ")}
                    >
                      {estadosCampana[campanaSeleccionada.estado]}
                    </Badge>
                  </div>
                </div>
                <p className="mt-2 max-w-2xl text-sm leading-relaxed text-muted-foreground">
                  {mensajePorEstado(campanaSeleccionada)}
                </p>
              </div>

              {!campanaFinalizada && (
                <Button
                  type="button"
                  onClick={abrirFormularioCrear}
                  className="h-10 gap-2 shrink-0 rounded-xl px-4 text-sm font-medium"
                >
                  <Plus className="h-4 w-4" />
                  Agregar recurso
                </Button>
              )}
            </div>

            {/* Banner campaña finalizada */}
            {campanaFinalizada && (
              <div className="flex items-start gap-3 rounded-2xl border border-slate-500/20 bg-slate-500/8 px-4 py-3.5 text-sm text-muted-foreground">
                <div className="mt-0.5 h-1.5 w-1.5 rounded-full bg-slate-400 shrink-0" />
                <p>
                  <span className="font-semibold text-foreground">
                    Campaña finalizada.
                  </span>{" "}
                  Solo puedes ver los recursos. No es posible agregar, editar,
                  archivar ni eliminar.
                </p>
              </div>
            )}

            {/* Formulario */}
            {mostrarFormulario && !campanaFinalizada && (
              <div className="overflow-hidden rounded-2xl border border-border/60 bg-background/60 shadow-sm">
                <div className="border-b border-border/50 bg-muted/20 px-5 py-4">
                  <h3 className="text-sm font-semibold text-foreground">
                    {editandoVideo
                      ? "Editar título del video"
                      : recursoEditando
                      ? "Editar recurso"
                      : "Nuevo recurso"}
                  </h3>
                  <p className="mt-0.5 text-xs text-muted-foreground">
                    {editandoVideo
                      ? "Solo se actualizará el título del video."
                      : recursoEditando
                      ? "Puedes editar los datos del recurso. Para cambiar el archivo, crea un nuevo recurso."
                      : "Sube un archivo real desde tu PC o crea una idea de campaña."}
                  </p>
                </div>

                <div className="p-5">
                  <div className="grid gap-4 md:grid-cols-2">
                    {!editandoVideo && (
                    <div className="flex flex-col gap-1.5">
                      <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                        Tipo
                      </label>
                      <select
                        value={form.tipo}
                        disabled={!!recursoEditando}
                        onChange={(e) => {
                          const nuevoTipo = e.target.value as TipoRecurso;
                          setForm((prev) => ({
                            ...prev,
                            tipo: nuevoTipo,
                            archivo: null,
                            nombreArchivo:
                              nuevoTipo === "copy" ? prev.nombreArchivo : "",
                            urlArchivo: "",
                            contenidoTexto:
                              nuevoTipo === "copy" ? prev.contenidoTexto : "",
                            pesoMb: "",
                            formato: nuevoTipo === "copy" ? "texto" : "",
                          }));
                        }}
                        className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        <option value="copy">Idea de campaña</option>
                        <option value="imagen">Imagen</option>
                        <option value="video">Video</option>
                      </select>
                    </div>
                    )}

                    <div
                      className={[
                        "flex flex-col gap-1.5",
                        editandoVideo ? "md:col-span-2" : "",
                      ].join(" ")}
                    >
                      <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                        Título
                      </label>
                      <input
                        value={form.titulo}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            titulo: e.target.value,
                          }))
                        }
                        placeholder="Ej: Banner principal"
                        className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                      />
                    </div>

                    {!editandoVideo && (
                      <>
                    <div className="hidden">
                      <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                        Nombre del recurso
                      </label>
                      <input
                        value={form.nombreArchivo}
                        readOnly={form.tipo !== "copy"}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            nombreArchivo: e.target.value,
                          }))
                        }
                        placeholder={
                          form.tipo === "copy"
                            ? "Ej: texto-publicitario-campaña-juegos"
                            : "Se detecta automáticamente"
                        }
                        className={[
                          "h-10 rounded-xl border border-input px-3 text-sm outline-none focus:ring-2 focus:ring-ring",
                          form.tipo !== "copy"
                            ? "bg-muted/40 text-muted-foreground"
                            : "bg-background",
                        ].join(" ")}
                      />
                    </div>

                    <div className="flex flex-col gap-1.5">
                      <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                        Formato
                      </label>
                      <input
                        value={form.formato}
                        readOnly={form.tipo !== "copy"}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            formato: e.target.value,
                          }))
                        }
                        placeholder={
                          form.tipo === "copy" ? "texto" : "Automático"
                        }
                        className={[
                          "h-10 rounded-xl border border-input px-3 text-sm outline-none focus:ring-2 focus:ring-ring",
                          form.tipo !== "copy"
                            ? "bg-muted/40 text-muted-foreground"
                            : "bg-background",
                        ].join(" ")}
                      />
                    </div>

                    {form.tipo !== "copy" ? (
                      <>
                        {!recursoEditando && (
                          <div className="flex flex-col gap-1.5 md:col-span-2">
                            <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                              Archivo
                            </label>
                            <input
                              type="file"
                              accept={
                                form.tipo === "imagen"
                                  ? ".jpg,.jpeg,.png,.webp"
                                  : ".mp4,.webm,.mov"
                              }
                              onChange={(e) => {
                                const archivo = e.target.files?.[0] ?? null;
                                setForm((prev) => ({
                                  ...prev,
                                  archivo,
                                  nombreArchivo: archivo?.name ?? "",
                                  formato:
                                    archivo?.name
                                      .split(".")
                                      .pop()
                                      ?.toLowerCase() ?? "",
                                  pesoMb: archivo
                                    ? (archivo.size / (1024 * 1024)).toFixed(2)
                                    : "",
                                }));
                              }}
                              className="rounded-xl border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
                            />
                            <p className="text-xs text-muted-foreground">
                              Imagen: jpg, jpeg, png, webp · Video: mp4, webm,
                              mov.
                            </p>
                          </div>
                        )}

                        <div className="flex flex-col gap-1.5">
                          <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                            Peso detectado
                          </label>
                          <input
                            value={form.pesoMb ? `${form.pesoMb} MB` : ""}
                            readOnly
                            placeholder="Automático"
                            className="h-10 rounded-xl border border-input bg-muted/40 px-3 text-sm text-muted-foreground outline-none"
                          />
                        </div>
                      </>
                    ) : (
                      <div className="flex flex-col gap-1.5 md:col-span-2">
                        <label className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          IDEA PRINCIPAL
                        </label>
                        <textarea
                          value={form.contenidoTexto}
                          onChange={(e) =>
                            setForm((prev) => ({
                              ...prev,
                              contenidoTexto: e.target.value,
                            }))
                          }
                          placeholder="Describe qué quieres comunicar en esta campaña..."
                          rows={5}
                          className="rounded-xl border border-input bg-background px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-ring resize-none"
                        />
                      </div>
                    )}
                      </>
                    )}
                  </div>

                  <div className="mt-5 flex justify-end gap-2.5 border-t border-border/50 pt-4">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={cancelarFormulario}
                      disabled={guardando}
                      className="rounded-xl"
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="button"
                      onClick={guardarRecurso}
                      disabled={guardando}
                      className="gap-2 rounded-xl"
                    >
                      {guardando && (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      )}
                      {recursoEditando
                        ? "Guardar cambios"
                        : form.tipo === "copy"
                        ? "Crear idea de campaña"
                        : "Subir archivo"}
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {/* Stats */}
            <div className="grid gap-3 sm:grid-cols-3">
              <div className="rounded-2xl border border-border/50 bg-background/60 p-4">
                <p className="text-2xl font-bold text-foreground">
                  {recursos.length}
                </p>
                <p className="mt-1 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Total
                </p>
              </div>

              <div className="rounded-2xl border border-primary/20 bg-primary/8 p-4">
                <p className="text-2xl font-bold text-primary">
                  {recursosActivos.length}
                </p>
                <p className="mt-1 text-xs font-medium uppercase tracking-wider text-primary/70">
                  Activos
                </p>
              </div>

              <div className="rounded-2xl border border-border/50 bg-background/60 p-4">
                <p className="text-2xl font-bold text-foreground">
                  {recursosArchivados.length}
                </p>
                <p className="mt-1 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Archivados
                </p>
              </div>
            </div>

            {/* Filtros */}
            <div className="flex flex-col gap-3 rounded-2xl border border-border/50 bg-background/50 p-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 className="text-sm font-semibold text-foreground">
                  Filtrar recursos
                </h3>
                <p className="text-xs text-muted-foreground">
                  Cambia entre todos, activos o archivados.
                </p>
              </div>

              <div className="flex flex-wrap gap-2">
                {(
                  [
                    { key: "todos", label: "Todos", count: recursos.length },
                    {
                      key: "activo",
                      label: "Activos",
                      count: recursosActivos.length,
                    },
                    {
                      key: "archivado",
                      label: "Archivados",
                      count: recursosArchivados.length,
                    },
                  ] as const
                ).map(({ key, label, count }) => (
                  <button
                    key={key}
                    type="button"
                    onClick={() => setFiltroEstado(key)}
                    className={[
                      "flex items-center gap-2 rounded-xl border px-3 py-2 text-xs font-medium transition-all",
                      filtroEstado === key
                        ? "border-primary/40 bg-primary/10 text-primary shadow-sm"
                        : "border-border/60 bg-background text-muted-foreground hover:border-primary/30 hover:text-foreground",
                    ].join(" ")}
                  >
                    {label}
                    <span
                      className={[
                        "rounded-full px-1.5 py-0.5 text-[10px] font-semibold tabular-nums",
                        filtroEstado === key
                          ? "bg-primary/20 text-primary"
                          : "bg-muted text-muted-foreground",
                      ].join(" ")}
                    >
                      {count}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Preview */}
            {previewRecurso && (
              <div className="overflow-hidden rounded-2xl border border-border/60 bg-background/60 shadow-sm">
                <div className="flex flex-col gap-3 border-b border-border/50 bg-muted/20 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <h3 className="text-sm font-semibold text-foreground">
                      Vista previa
                    </h3>
                    <p className="text-xs text-muted-foreground">
                      {previewRecurso.recurso.titulo ||
                        previewRecurso.recurso.nombreArchivo}
                    </p>
                  </div>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={cerrarPreview}
                    className="gap-1.5 rounded-xl text-xs"
                  >
                    <X className="h-3.5 w-3.5" />
                    Cerrar
                  </Button>
                </div>

                <div className="p-4">
                  {previewRecurso.recurso.tipo === "imagen" && (
                    <div className="overflow-hidden rounded-2xl border border-border bg-muted/20">
                      <img
                        src={previewRecurso.url}
                        alt={previewRecurso.recurso.titulo || "Vista previa"}
                        className="mx-auto max-h-[520px] w-full object-contain"
                      />
                    </div>
                  )}
                  {previewRecurso.recurso.tipo === "video" && (
                    <div className="overflow-hidden rounded-2xl border border-border bg-black">
                      <video
                        src={previewRecurso.url}
                        controls
                        className="mx-auto max-h-[520px] w-full"
                      />
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Tabla de recursos */}
            <div className="overflow-hidden rounded-2xl border border-border/60 bg-background/50 shadow-sm">
              <div className="flex items-center justify-between border-b border-border/50 bg-muted/20 px-5 py-4">
                <div>
                  <h3 className="text-sm font-semibold text-foreground">
                    Recursos
                  </h3>
                  <p className="text-xs text-muted-foreground">
                    {recursosFiltrados.length}{" "}
                    {recursosFiltrados.length === 1
                      ? "resultado"
                      : "resultados"}
                  </p>
                </div>
              </div>

              {cargandoRecursos ? (
                <div className="flex items-center gap-2.5 p-6 text-sm text-muted-foreground">
                  <Loader2 className="h-4 w-4 animate-spin text-primary" />
                  Cargando recursos...
                </div>
              ) : recursosFiltrados.length === 0 ? (
                <div className="flex flex-col items-center justify-center px-5 py-16 text-center">
                  <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-primary/20 bg-primary/8 text-primary">
                    <ImageIcon className="h-6 w-6" />
                  </div>
                  <h4 className="mt-4 text-sm font-semibold text-foreground">
                    No hay recursos para el filtro seleccionado.
                  </h4>
                  <p className="mt-1.5 max-w-xs text-xs text-muted-foreground">
                    Intenta ajustar los filtros o selecciona otra campaña.
                  </p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[860px] text-sm">
                    <thead>
                      <tr className="border-b border-border/40 bg-muted/10">
                        <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          Recurso
                        </th>
                        <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          Tipo
                        </th>
                        <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          Formato
                        </th>
                        <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          Estado
                        </th>
                        <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          Fecha
                        </th>
                        <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                          Acciones
                        </th>
                      </tr>
                    </thead>

                    <tbody className="divide-y divide-border/40">
                      {recursosFiltrados.map((recurso) => (
                        <tr
                          key={recurso.idRecurso}
                          className="transition-colors hover:bg-muted/20"
                        >
                          <td className="px-5 py-3.5">
                            <div>
                              <p className="font-medium text-foreground leading-snug">
                                {recurso.titulo || (
                                  <span className="font-normal italic text-muted-foreground text-xs">
                                    Sin título
                                  </span>
                                )}
                              </p>
                              <p className="mt-0.5 max-w-[200px] truncate text-xs text-muted-foreground">
                                {recurso.nombreArchivo}
                              </p>
                            </div>
                          </td>

                          <td className="px-4 py-3.5">
                            <div className="flex items-center gap-1.5 text-muted-foreground">
                              {obtenerIconoTipo(recurso.tipo)}
                              <span className="text-xs font-medium">
                                {tiposRecurso[recurso.tipo]}
                              </span>
                            </div>
                          </td>

                          <td className="px-4 py-3.5 text-xs text-muted-foreground">
                            {recurso.formato ? (
                              <span className="rounded-md border border-border/60 bg-muted/40 px-2 py-0.5 font-mono text-[11px]">
                                {recurso.formato}
                              </span>
                            ) : (
                              "—"
                            )}
                          </td>

                          <td className="px-4 py-3.5">
                            <div className="flex items-center gap-1.5">
                              <span
                                className={[
                                  "h-1.5 w-1.5 rounded-full",
                                  recurso.estado === "activo"
                                    ? "bg-emerald-500"
                                    : "bg-slate-400",
                                ].join(" ")}
                              />
                              <span
                                className={[
                                  "text-xs font-medium",
                                  recurso.estado === "activo"
                                    ? "text-emerald-600 dark:text-emerald-400"
                                    : "text-slate-500 dark:text-slate-400",
                                ].join(" ")}
                              >
                                {recurso.estado === "activo"
                                  ? "Activo"
                                  : "Archivado"}
                              </span>
                            </div>
                          </td>

                          <td className="whitespace-nowrap px-4 py-3.5 text-xs text-muted-foreground">
                            {formatearFecha(recurso.fechaSubida)}
                          </td>

                          <td className="px-4 py-3.5">
                            <div className="flex items-center justify-end gap-1">
                              {(recurso.tipo === "imagen" ||
                                recurso.tipo === "video") && (
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  disabled={cargandoPreview}
                                  onClick={() => verArchivoSeguro(recurso)}
                                  className="h-8 gap-1 rounded-lg px-2.5 text-xs"
                                >
                                  {cargandoPreview ? (
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                  ) : (
                                    <Eye className="h-3.5 w-3.5" />
                                  )}
                                  Ver
                                </Button>
                              )}

                              {!campanaFinalizada && (
                                <>
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() =>
                                      abrirFormularioEditar(recurso)
                                    }
                                    className="h-8 gap-1 rounded-lg px-2.5 text-xs"
                                  >
                                    <Pencil className="h-3.5 w-3.5" />
                                    Editar
                                  </Button>

                                  {recurso.estado === "activo" ? (
                                    <Button
                                      variant="ghost"
                                      size="sm"
                                      onClick={() => manejarArchivar(recurso)}
                                      className="h-8 gap-1 rounded-lg px-2.5 text-xs"
                                    >
                                      <Archive className="h-3.5 w-3.5" />
                                      Archivar
                                    </Button>
                                  ) : (
                                    <Button
                                      variant="ghost"
                                      size="sm"
                                      onClick={() =>
                                        manejarDesarchivar(recurso)
                                      }
                                      className="h-8 gap-1 rounded-lg px-2.5 text-xs"
                                    >
                                      <Archive className="h-3.5 w-3.5" />
                                      Desarchivar
                                    </Button>
                                  )}

                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => manejarEliminar(recurso)}
                                    className="h-8 gap-1 rounded-lg px-2.5 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive"
                                  >
                                    <Trash2 className="h-3.5 w-3.5" />
                                    Eliminar
                                  </Button>
                                </>
                              )}

                              {campanaFinalizada && (
                                <span className="text-[11px] text-muted-foreground/60">
                                  Solo historial
                                </span>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </section>
  );
}
