import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Ban,
  CalendarDays,
  Edit3,
  ExternalLink,
  FileText,
  ImageIcon,
  Loader2,
  PlayCircle,
  Plus,
  Search,
  Send,
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
  crearUrlArchivoRecurso,
  obtenerRecursosPorCampana,
  type Recurso,
} from "@/services/recursoService";
import {
  actualizarPublicacion,
  cancelarPublicacion,
  crearPublicacion,
  enviarPublicacionAN8n,
  obtenerPublicaciones,
  type EstadoPublicacion,
  type PlataformaPublicacion,
  type PrivacidadPublicacion,
  type Publicacion,
  type PublicacionRequest,
} from "@/services/publicacionService";

type FormState = {
  idRecurso: string;
  titulo: string;
  copyTexto: string;
  plataforma: PlataformaPublicacion;
  privacidad: PrivacidadPublicacion;
  estado: Extract<EstadoPublicacion, "borrador" | "lista">;
};

type PreviewState = {
  recurso: Recurso;
  url: string | null;
  error: string | null;
};

type ApiErrorLike = {
  response?: {
    data?: {
      mensaje?: unknown;
    };
  };
};

const formInicial: FormState = {
  idRecurso: "",
  titulo: "",
  copyTexto: "",
  plataforma: "youtube",
  privacidad: "private",
  estado: "borrador",
};

const estadosCampana: Record<EstadoCampana, string> = {
  borrador: "Borrador",
  activa: "Activa",
  pausada: "Pausada",
  finalizada: "Finalizada",
};

const tiposRecurso: Record<Recurso["tipo"], string> = {
  imagen: "Imagen",
  video: "Video",
  documento: "Archivo",
  copy: "Copy",
};

const estadoLabel: Record<EstadoPublicacion, string> = {
  borrador: "Borrador",
  lista: "Lista",
  programada: "Programada",
  enviada: "Enviada",
  publicada: "Publicada",
  error: "Error",
  cancelada: "Cancelada",
};

const estadosBloqueados: EstadoPublicacion[] = [
  "enviada",
  "publicada",
  "cancelada",
];

function obtenerMensajeError(error: unknown, mensajePorDefecto: string) {
  if (typeof error === "object" && error !== null && "response" in error) {
    const apiError = error as ApiErrorLike;
    const mensaje = apiError.response?.data?.mensaje;

    if (typeof mensaje === "string" && mensaje.trim().length > 0) {
      return mensaje;
    }
  }

  return mensajePorDefecto;
}

function formatearFecha(fecha: string | null) {
  if (!fecha) return "Sin fecha";

  const date = new Date(fecha);
  if (Number.isNaN(date.getTime())) return "Fecha invalida";

  return new Intl.DateTimeFormat("es-PE", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
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

function claseEstadoPublicacion(estado: EstadoPublicacion) {
  if (estado === "lista") {
    return "border-emerald-500/30 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400";
  }
  if (estado === "programada" || estado === "enviada") {
    return "border-sky-500/30 bg-sky-500/10 text-sky-600 dark:text-sky-400";
  }
  if (estado === "publicada") {
    return "border-primary/30 bg-primary/10 text-primary";
  }
  if (estado === "error") {
    return "border-destructive/30 bg-destructive/10 text-destructive";
  }
  if (estado === "cancelada") {
    return "border-slate-500/30 bg-slate-500/10 text-slate-500 dark:text-slate-400";
  }
  return "border-border bg-muted/40 text-muted-foreground";
}

function obtenerIconoRecurso(tipo: Recurso["tipo"] | string | null) {
  if (tipo === "imagen") return <ImageIcon className="h-3.5 w-3.5" />;
  if (tipo === "video") return <Video className="h-3.5 w-3.5" />;
  if (tipo === "copy") return <FileText className="h-3.5 w-3.5" />;
  return <FileText className="h-3.5 w-3.5" />;
}

function puedeEditarPublicacion(publicacion: Publicacion) {
  return !estadosBloqueados.includes(publicacion.estado);
}

function puedeCancelarPublicacion(publicacion: Publicacion) {
  return !estadosBloqueados.includes(publicacion.estado);
}

function puedeEnviarPublicacionAN8n(publicacion: Publicacion) {
  return (
    publicacion.estado === "lista" &&
    Boolean(publicacion.idRecurso) &&
    publicacion.tipoRecurso === "video" &&
    Boolean(publicacion.copyTexto?.trim())
  );
}

export default function PublicacionesPage() {
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [campanaSeleccionada, setCampanaSeleccionada] =
    useState<Campana | null>(null);
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [publicaciones, setPublicaciones] = useState<Publicacion[]>([]);

  const [cargandoCampanas, setCargandoCampanas] = useState(true);
  const [cargandoRecursos, setCargandoRecursos] = useState(false);
  const [cargandoPublicaciones, setCargandoPublicaciones] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [cancelandoId, setCancelandoId] = useState<number | null>(null);
  const [enviandoId, setEnviandoId] = useState<number | null>(null);
  const [cargandoPreview, setCargandoPreview] = useState(false);

  const [busquedaCampana, setBusquedaCampana] = useState("");
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [publicacionEditando, setPublicacionEditando] =
    useState<Publicacion | null>(null);
  const [form, setForm] = useState<FormState>(formInicial);
  const [preview, setPreview] = useState<PreviewState | null>(null);

  const videosDisponibles = useMemo(
    () =>
      recursos.filter(
        (recurso) => recurso.tipo === "video" && recurso.estado === "activo"
      ),
    [recursos]
  );

  const recursoSeleccionado = useMemo(() => {
    const idRecurso = Number(form.idRecurso);
    if (!idRecurso) return null;
    return (
      videosDisponibles.find((recurso) => recurso.idRecurso === idRecurso) ??
      null
    );
  }, [form.idRecurso, videosDisponibles]);

  const campanasFiltradas = useMemo(() => {
    const query = busquedaCampana.trim().toLowerCase();
    if (!query) return campanas;

    return campanas.filter((campana) => {
      const nombre = campana.nombre.toLowerCase();
      const objetivo = (campana.objetivo ?? "").toLowerCase();
      return nombre.includes(query) || objetivo.includes(query);
    });
  }, [busquedaCampana, campanas]);

  const publicacionesFiltradas = useMemo(() => {
    if (!campanaSeleccionada) return [];

    return publicaciones.filter(
      (publicacion) => publicacion.idCampana === campanaSeleccionada.idCampana
    );
  }, [campanaSeleccionada, publicaciones]);

  const yaExisteEnvioMismoVideoPlataforma = useCallback(
    (publicacion: Publicacion) =>
      Boolean(publicacion.idRecurso) &&
      publicaciones.some(
        (otra) =>
          otra.idPublicacion !== publicacion.idPublicacion &&
          otra.idRecurso === publicacion.idRecurso &&
          otra.plataforma === publicacion.plataforma &&
          (otra.estado === "enviada" || otra.estado === "publicada")
      ),
    [publicaciones]
  );

  const obtenerCampanaDePublicacion = useCallback(
    (idCampana: number) =>
      campanas.find((campana) => campana.idCampana === idCampana),
    [campanas]
  );

  const esCampanaActivaParaPublicacion = useCallback(
    (publicacion: Publicacion) => {
      const campana = obtenerCampanaDePublicacion(publicacion.idCampana);
      return campana?.estado?.toLowerCase() === "activa";
    },
    [obtenerCampanaDePublicacion]
  );

  const cargarPublicaciones = useCallback(async (idCampana: number) => {
    try {
      setCargandoPublicaciones(true);
      const data = await obtenerPublicaciones({ idCampana });
      setPublicaciones(
        data.filter((publicacion) => publicacion.idCampana === idCampana)
      );
    } catch (error: unknown) {
      toast.error(
        obtenerMensajeError(error, "No se pudieron cargar las publicaciones")
      );
    } finally {
      setCargandoPublicaciones(false);
    }
  }, []);

  const cargarRecursos = useCallback(async (idCampana: number) => {
    try {
      setCargandoRecursos(true);
      const data = await obtenerRecursosPorCampana(idCampana);
      setRecursos(data);
    } catch (error: unknown) {
      toast.error(
        obtenerMensajeError(error, "No se pudieron cargar los recursos")
      );
    } finally {
      setCargandoRecursos(false);
    }
  }, []);

  useEffect(() => {
    let cancelado = false;

    async function cargarCampanas() {
      try {
        const data = await obtenerCampanas();
        if (!cancelado) {
          setCampanas(data);
        }
      } catch (error: unknown) {
        if (!cancelado) {
          toast.error(
            obtenerMensajeError(error, "No se pudieron cargar las campañas")
          );
        }
      } finally {
        if (!cancelado) {
          setCargandoCampanas(false);
        }
      }
    }

    void cargarCampanas();

    return () => {
      cancelado = true;
    };
  }, []);

  useEffect(() => {
    let cancelado = false;
    let urlTemporal: string | null = null;

    async function cargarPreview(recurso: Recurso) {
      if (recurso.tipo === "copy" || recurso.tipo === "documento") {
        setPreview({ recurso, url: null, error: null });
        return;
      }

      try {
        setCargandoPreview(true);
        const url = await crearUrlArchivoRecurso(recurso.idRecurso);
        urlTemporal = url;

        if (!cancelado) {
          setPreview({ recurso, url, error: null });
        }
      } catch {
        if (!cancelado) {
          setPreview({
            recurso,
            url: null,
            error: "No se pudo cargar la vista previa del recurso.",
          });
        }
      } finally {
        if (!cancelado) {
          setCargandoPreview(false);
        }
      }
    }

    if (recursoSeleccionado) {
      void cargarPreview(recursoSeleccionado);
    }

    return () => {
      cancelado = true;
      if (urlTemporal) {
        URL.revokeObjectURL(urlTemporal);
      }
    };
  }, [recursoSeleccionado]);

  async function seleccionarCampana(campana: Campana) {
    setCampanaSeleccionada(campana);
    setRecursos([]);
    setPublicaciones([]);
    setMostrarFormulario(false);
    setPublicacionEditando(null);
    setForm(formInicial);
    setPreview(null);

    await Promise.all([
      cargarRecursos(campana.idCampana),
      cargarPublicaciones(campana.idCampana),
    ]);
  }

  function handleCambiarRecurso(idRecurso: string) {
    setPreview(null);
    setCargandoPreview(false);
    setForm((prev) => ({
      ...prev,
      idRecurso,
    }));
  }

  function abrirFormularioCrear() {
    if (!campanaSeleccionada) {
      toast.error("Selecciona una campaña antes de crear una publicación");
      return;
    }

    setPublicacionEditando(null);
    setPreview(null);
    setCargandoPreview(false);
    setForm(formInicial);
    setMostrarFormulario(true);
  }

  function abrirFormularioEditar(publicacion: Publicacion) {
    if (!puedeEditarPublicacion(publicacion)) {
      toast.error("Esta publicación ya no se puede editar", {
        description:
          "Las publicaciones enviadas, publicadas o canceladas quedan bloqueadas.",
      });
      return;
    }

    setPublicacionEditando(publicacion);
    setPreview(null);
    setCargandoPreview(false);
    setForm({
      idRecurso: publicacion.idRecurso ? String(publicacion.idRecurso) : "",
      titulo: publicacion.titulo ?? "",
      copyTexto: publicacion.copyTexto ?? "",
      plataforma: "youtube",
      privacidad: "private",
      estado:
        publicacion.estado === "lista" || publicacion.estado === "borrador"
          ? publicacion.estado
          : "borrador",
    });
    setMostrarFormulario(true);
  }

  function cancelarFormulario() {
    if (guardando) return;

    setMostrarFormulario(false);
    setPublicacionEditando(null);
    setPreview(null);
    setCargandoPreview(false);
    setForm(formInicial);
  }

  function validarFormulario() {
    if (!campanaSeleccionada) {
      toast.error("Selecciona una campaña");
      return false;
    }

    if (!form.titulo.trim()) {
      toast.error("El título es obligatorio");
      return false;
    }

    if (!form.plataforma) {
      toast.error("La plataforma es obligatoria");
      return false;
    }

    if (form.plataforma === "youtube" && !form.privacidad) {
      toast.error("La privacidad de YouTube es obligatoria");
      return false;
    }

    if (!form.estado) {
      toast.error("El estado es obligatorio");
      return false;
    }

    if (form.estado !== "borrador" && form.estado !== "lista") {
      toast.error("Solo puedes guardar publicaciones como borrador o lista");
      return false;
    }

    if (form.estado === "lista" && !form.idRecurso) {
      toast.error("Selecciona un video asociado para marcarla como lista");
      return false;
    }

    if (form.estado === "lista" && !form.copyTexto.trim()) {
      toast.error(
        "Agrega una descripción antes de dejar la publicación lista."
      );
      return false;
    }

    return true;
  }

  function construirRequest(): PublicacionRequest {
    if (!campanaSeleccionada) {
      throw new Error("Campaña no seleccionada");
    }

    return {
      idCampana: campanaSeleccionada.idCampana,
      idRecurso: form.idRecurso ? Number(form.idRecurso) : null,
      titulo: form.titulo.trim(),
      copyTexto: form.copyTexto.trim() || null,
      plataforma: form.plataforma,
      privacidad: "private",
      estado: form.estado,
      fechaProgramada: null,
    };
  }

  async function guardarPublicacion() {
    if (!validarFormulario() || !campanaSeleccionada) return;

    try {
      setGuardando(true);
      const payload = construirRequest();

      if (publicacionEditando) {
        await actualizarPublicacion(publicacionEditando.idPublicacion, payload);
        toast.success("Publicación actualizada correctamente");
      } else {
        await crearPublicacion(payload);
        toast.success("Publicación creada correctamente");
      }

      cancelarFormulario();
      await cargarPublicaciones(campanaSeleccionada.idCampana);
    } catch (error: unknown) {
      toast.error(
        obtenerMensajeError(error, "No se pudo guardar la publicación")
      );
    } finally {
      setGuardando(false);
    }
  }

  async function manejarCancelarPublicacion(publicacion: Publicacion) {
    if (!puedeCancelarPublicacion(publicacion)) {
      toast.error("Esta publicación no se puede cancelar", {
        description:
          "Las publicaciones enviadas, publicadas o canceladas quedan bloqueadas.",
      });
      return;
    }

    const confirmar = window.confirm(
      "¿Seguro que deseas cancelar esta publicación?"
    );

    if (!confirmar || !campanaSeleccionada) return;

    try {
      setCancelandoId(publicacion.idPublicacion);
      await cancelarPublicacion(publicacion.idPublicacion);
      toast.success("Publicación cancelada correctamente");
      await cargarPublicaciones(campanaSeleccionada.idCampana);
    } catch (error: unknown) {
      toast.error(
        obtenerMensajeError(error, "No se pudo cancelar la publicación")
      );
    } finally {
      setCancelandoId(null);
    }
  }

  async function manejarEnviarPublicacionAN8n(publicacion: Publicacion) {
    if (enviandoId !== null) return;

    if (!esCampanaActivaParaPublicacion(publicacion)) {
      toast.error("Solo puedes enviar publicaciones de campañas activas.");
      return;
    }

    if (estadosBloqueados.includes(publicacion.estado)) {
      toast.error("Esta publicación no se puede enviar a n8n");
      return;
    }

    if (!publicacion.copyTexto?.trim()) {
      toast.error("La descripción es obligatoria para enviar a n8n.");
      return;
    }

    if (yaExisteEnvioMismoVideoPlataforma(publicacion)) {
      toast.error("Este video ya fue enviado a esta plataforma.");
      return;
    }

    if (!puedeEnviarPublicacionAN8n(publicacion)) {
      toast.error(
        "Solo puedes enviar publicaciones listas con video asociado."
      );
      return;
    }

    try {
      setEnviandoId(publicacion.idPublicacion);
      const publicacionActualizada = await enviarPublicacionAN8n(
        publicacion.idPublicacion
      );
      const publicacionParaMostrar: Publicacion =
        publicacionActualizada.mensajeError &&
        publicacionActualizada.estado !== "error"
          ? { ...publicacionActualizada, estado: "error" }
          : publicacionActualizada;

      setPublicaciones((prev) =>
        prev.map((item) =>
          item.idPublicacion === publicacionParaMostrar.idPublicacion
            ? publicacionParaMostrar
            : item
        )
      );

      if (
        publicacionParaMostrar.estado === "error" ||
        publicacionParaMostrar.mensajeError
      ) {
        toast.error(
          publicacionParaMostrar.mensajeError ||
            "No se pudo enviar la publicación a n8n."
        );
        return;
      }

      toast.success("Publicación enviada a n8n correctamente.");
    } catch (error: unknown) {
      toast.error(
        obtenerMensajeError(error, "No se pudo enviar la publicación a n8n.")
      );
    } finally {
      setEnviandoId(null);
    }
  }

  return (
    <section className="mx-auto flex w-full max-w-[1500px] flex-col gap-6 px-4 py-6 md:px-6 lg:px-8">
      <header className="relative overflow-hidden rounded-3xl border border-border/60 bg-card/80 shadow-sm backdrop-blur-sm">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-transparent" />
        <div className="relative flex flex-col gap-4 p-6 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary ring-1 ring-primary/20 shadow-sm">
              <Send className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-xl font-semibold tracking-tight text-foreground md:text-2xl">
                Publicaciones
              </h1>
              <p className="mt-0.5 text-sm text-muted-foreground">
                Crea publicaciones usando tus recursos y videos generados con
                IA.
              </p>
            </div>
          </div>
        </div>
      </header>

      <section className="rounded-3xl border border-border/60 bg-card/80 shadow-sm backdrop-blur-sm">
        <div className="flex flex-col gap-5 p-6">
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
                Selecciona una campaña para gestionar sus publicaciones.
              </p>
            </div>

            <div className="relative w-full sm:max-w-xs">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground/50" />
              <input
                value={busquedaCampana}
                onChange={(event) => setBusquedaCampana(event.target.value)}
                placeholder="Buscar campaña..."
                className="h-10 w-full rounded-xl border border-border/70 bg-background/80 pl-10 pr-4 text-sm text-foreground outline-none transition-all placeholder:text-muted-foreground/50 focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
              />
            </div>
          </div>

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
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {campanasFiltradas.map((campana) => {
                const seleccionada =
                  campanaSeleccionada?.idCampana === campana.idCampana;

                return (
                  <button
                    key={campana.idCampana}
                    type="button"
                    onClick={() => void seleccionarCampana(campana)}
                    className={[
                      "group rounded-2xl border p-4 text-left transition-all",
                      seleccionada
                        ? "border-primary/50 bg-primary/10 shadow-sm"
                        : "border-border/60 bg-background/60 hover:border-primary/30 hover:bg-primary/5",
                    ].join(" ")}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="truncate text-sm font-semibold text-foreground">
                          {campana.nombre || "Campaña sin nombre"}
                        </p>
                        <p className="mt-1 line-clamp-2 text-xs leading-5 text-muted-foreground">
                          {campana.objetivo || "Sin objetivo definido"}
                        </p>
                      </div>

                      <Badge
                        variant="outline"
                        className={`shrink-0 rounded-full px-2 text-[10px] font-medium ${claseEstadoCampana(
                          campana.estado
                        )}`}
                      >
                        {estadosCampana[campana.estado]}
                      </Badge>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </section>

      <section className="rounded-3xl border border-border/60 bg-card/80 shadow-sm backdrop-blur-sm">
        <div className="flex flex-col gap-5 p-6">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-base font-semibold text-foreground">
                Publicaciones de campaña
              </h2>
              <p className="mt-0.5 text-sm text-muted-foreground">
                {campanaSeleccionada
                  ? campanaSeleccionada.nombre
                  : "Selecciona una campaña para ver sus publicaciones."}
              </p>
            </div>

            {campanaSeleccionada && (
              <div className="flex flex-wrap items-center gap-2">
                <div className="flex items-center gap-2 rounded-xl border border-border/60 bg-background/60 px-3 py-2 text-xs text-muted-foreground">
                  <CalendarDays className="h-3.5 w-3.5" />
                  {publicacionesFiltradas.length}{" "}
                  {publicacionesFiltradas.length === 1
                    ? "publicación"
                    : "publicaciones"}
                </div>
                <Button
                  type="button"
                  size="sm"
                  onClick={abrirFormularioCrear}
                  className="h-9 gap-1.5 rounded-xl px-3 text-xs font-medium"
                >
                  <Plus className="h-3.5 w-3.5" />
                  Nueva publicación
                </Button>
              </div>
            )}
          </div>

          {!campanaSeleccionada ? (
            <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border/60 bg-muted/20 px-5 py-16 text-center">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
                <Send className="h-6 w-6" />
              </div>
              <h3 className="mt-4 text-sm font-semibold text-foreground">
                Selecciona una campaña
              </h3>
              <p className="mt-1.5 max-w-sm text-xs text-muted-foreground">
                Las publicaciones se organizan por campaña y usan los recursos
                asociados a ella.
              </p>
            </div>
          ) : (
            <>
              {campanaSeleccionada.estado.toLowerCase() !== "activa" && (
                <div className="flex items-center gap-2 rounded-2xl border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-700 dark:text-amber-300">
                  Campaña inactiva. Actívala para publicar.
                </div>
              )}

              {mostrarFormulario && (
                <div className="overflow-hidden rounded-2xl border border-border/60 bg-background/70 shadow-sm">
                  <div className="flex items-start justify-between gap-4 border-b border-border/50 bg-muted/20 px-5 py-4">
                    <div>
                      <h3 className="text-sm font-semibold text-foreground">
                        {publicacionEditando
                          ? "Editar publicación"
                          : "Nueva publicación"}
                      </h3>
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        Configura el contenido inicial para YouTube.
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={cancelarFormulario}
                      disabled={guardando}
                      className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-border/60 bg-background text-muted-foreground transition hover:bg-muted hover:text-foreground disabled:opacity-50"
                      aria-label="Cerrar formulario"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>

                  <div className="grid gap-5 p-5 lg:grid-cols-[minmax(0,1fr)_360px]">
                    <div className="space-y-4">
                      <div className="grid gap-4 md:grid-cols-2">
                        <Field label="Campaña">
                          <input
                            value={campanaSeleccionada.nombre}
                            readOnly
                            className="h-10 w-full rounded-xl border border-border/70 bg-muted/40 px-3 text-sm text-muted-foreground outline-none"
                          />
                        </Field>

                        <Field label="Video asociado">
                          <select
                            value={form.idRecurso}
                            onChange={(event) =>
                              handleCambiarRecurso(event.target.value)
                            }
                            disabled={cargandoRecursos}
                            className="h-10 w-full rounded-xl border border-border/70 bg-background px-3 text-sm text-foreground outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 disabled:opacity-60"
                          >
                            <option value="">Sin video asociado</option>
                            {videosDisponibles.map((recurso) => (
                              <option
                                key={recurso.idRecurso}
                                value={recurso.idRecurso}
                              >
                                {recurso.titulo ||
                                  recurso.nombreArchivo ||
                                  tiposRecurso[recurso.tipo]}
                              </option>
                            ))}
                          </select>
                          {!cargandoRecursos &&
                            videosDisponibles.length === 0 && (
                              <span className="text-xs text-muted-foreground">
                                Esta campaña todavía no tiene videos
                                disponibles.
                              </span>
                            )}
                        </Field>

                        <Field label="Título" required>
                          <input
                            value={form.titulo}
                            onChange={(event) =>
                              setForm((prev) => ({
                                ...prev,
                                titulo: event.target.value,
                              }))
                            }
                            placeholder="Ej: Video promocional de campaña"
                            className="h-10 w-full rounded-xl border border-border/70 bg-background px-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/50 focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
                          />
                        </Field>

                        <Field label="Plataforma" required>
                          <select
                            value={form.plataforma}
                            onChange={(event) =>
                              setForm((prev) => ({
                                ...prev,
                                plataforma: event.target
                                  .value as PlataformaPublicacion,
                              }))
                            }
                            className="h-10 w-full rounded-xl border border-border/70 bg-background px-3 text-sm text-foreground outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
                          >
                            <option value="youtube">YouTube</option>
                          </select>
                        </Field>

                        <Field label="Privacidad" required>
                          <select
                            value={form.privacidad}
                            onChange={() =>
                              setForm((prev) => ({
                                ...prev,
                                privacidad: "private",
                              }))
                            }
                            className="h-10 w-full rounded-xl border border-border/70 bg-background px-3 text-sm text-foreground outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
                          >
                            <option value="private">Privado</option>
                          </select>
                        </Field>

                        <div className="md:col-span-2">
                          <Field
                            label="Descripción / caption"
                            required={form.estado === "lista"}
                          >
                            <textarea
                              value={form.copyTexto}
                              onChange={(event) =>
                                setForm((prev) => ({
                                  ...prev,
                                  copyTexto: event.target.value,
                                }))
                              }
                              rows={4}
                              placeholder="Escribe el texto que acompañará la publicación..."
                              className="w-full resize-none rounded-xl border border-border/70 bg-background px-3 py-2.5 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/50 focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
                            />
                          </Field>
                        </div>

                        <Field label="Estado" required>
                          <select
                            value={form.estado}
                            onChange={(event) =>
                              setForm((prev) => ({
                                ...prev,
                                estado: event.target
                                  .value as FormState["estado"],
                              }))
                            }
                            className="h-10 w-full rounded-xl border border-border/70 bg-background px-3 text-sm text-foreground outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10"
                          >
                            <option value="borrador">Borrador</option>
                            <option value="lista">Lista</option>
                          </select>
                        </Field>
                      </div>

                      <div className="rounded-2xl border border-dashed border-border/60 bg-muted/20 px-4 py-3 text-xs text-muted-foreground">
                        Para dejar una publicación lista debes seleccionar un
                        video activo y agregar una descripción.
                      </div>

                      <div className="flex flex-col gap-2 border-t border-border/50 pt-4 sm:flex-row sm:justify-end">
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
                          onClick={() => void guardarPublicacion()}
                          disabled={guardando}
                          className="gap-2 rounded-xl"
                        >
                          {guardando ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : publicacionEditando ? (
                            <Edit3 className="h-4 w-4" />
                          ) : (
                            <Plus className="h-4 w-4" />
                          )}
                          {publicacionEditando
                            ? "Guardar cambios"
                            : "Crear publicación"}
                        </Button>
                      </div>
                    </div>

                    <PreviewPanel
                      preview={preview}
                      cargando={cargandoPreview}
                      recursoSeleccionado={recursoSeleccionado}
                    />
                  </div>
                </div>
              )}

              {cargandoPublicaciones ? (
                <div className="flex items-center gap-2.5 rounded-2xl border border-border/50 bg-muted/30 p-4 text-sm text-muted-foreground">
                  <Loader2 className="h-4 w-4 animate-spin text-primary" />
                  Cargando publicaciones...
                </div>
              ) : publicacionesFiltradas.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border/60 bg-muted/20 px-5 py-16 text-center">
                  <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
                    <Send className="h-6 w-6" />
                  </div>
                  <h3 className="mt-4 text-sm font-semibold text-foreground">
                    No hay publicaciones para esta campaña.
                  </h3>
                  <p className="mt-1.5 max-w-sm text-xs text-muted-foreground">
                    Crea una publicación usando un recurso de la campaña
                    seleccionada.
                  </p>
                </div>
              ) : (
                <div className="overflow-hidden rounded-2xl border border-border/60 bg-background/50 shadow-sm">
                  <div className="overflow-x-auto">
                    <table className="w-full min-w-[820px] text-sm">
                      <thead>
                        <tr className="border-b border-border/40 bg-muted/20">
                          <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                            Publicación
                          </th>
                          <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                            Recurso
                          </th>
                          <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                            Plataforma
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
                        {publicacionesFiltradas.map((publicacion) => {
                          const puedeEnviarAN8n =
                            puedeEnviarPublicacionAN8n(publicacion);
                          const campanaActiva =
                            esCampanaActivaParaPublicacion(publicacion);
                          const envioDuplicado =
                            yaExisteEnvioMismoVideoPlataforma(publicacion);

                          return (
                            <tr
                              key={publicacion.idPublicacion}
                              className="transition-colors hover:bg-muted/20"
                            >
                              <td className="px-5 py-3.5">
                                <p className="font-medium leading-snug text-foreground">
                                  {publicacion.titulo || "Sin título"}
                                </p>
                                <p className="mt-0.5 line-clamp-1 max-w-[260px] text-xs text-muted-foreground">
                                  {publicacion.copyTexto ||
                                    "Sin descripción registrada"}
                                </p>
                              </td>

                              <td className="px-4 py-3.5">
                                <div className="flex items-center gap-1.5 text-muted-foreground">
                                  {obtenerIconoRecurso(publicacion.tipoRecurso)}
                                  <span className="max-w-[180px] truncate text-xs font-medium">
                                    {publicacion.tituloRecurso ||
                                      "Sin recurso asociado"}
                                  </span>
                                </div>
                              </td>

                              <td className="px-4 py-3.5">
                                <span className="inline-flex items-center gap-1.5 rounded-lg border border-border/60 bg-muted/30 px-2 py-1 text-xs font-medium capitalize text-muted-foreground">
                                  <PlayCircle className="h-3.5 w-3.5" />
                                  {publicacion.plataforma}
                                </span>
                              </td>

                              <td className="px-4 py-3.5">
                                <Badge
                                  variant="outline"
                                  className={`rounded-full px-2.5 text-[11px] font-medium ${claseEstadoPublicacion(
                                    publicacion.estado
                                  )}`}
                                >
                                  {estadoLabel[publicacion.estado]}
                                </Badge>
                                {publicacion.urlPublicacion && (
                                  <a
                                    href={publicacion.urlPublicacion}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="mt-1.5 inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline"
                                  >
                                    Ver publicación
                                    <ExternalLink className="h-3 w-3" />
                                  </a>
                                )}
                                {publicacion.mensajeError && (
                                  <button
                                    type="button"
                                    title={publicacion.mensajeError}
                                    className="mt-1.5 block text-xs font-medium text-destructive hover:underline"
                                  >
                                    Ver detalle
                                  </button>
                                )}
                              </td>

                              <td className="whitespace-nowrap px-4 py-3.5 text-xs text-muted-foreground">
                                {formatearFecha(publicacion.fechaCreacion)}
                              </td>

                              <td className="px-4 py-3.5">
                                <div className="flex items-center justify-end gap-1">
                                  {puedeEnviarAN8n && (
                                    <div className="flex flex-col items-end gap-1">
                                      <Button
                                        type="button"
                                        variant="ghost"
                                        size="sm"
                                        onClick={() =>
                                          void manejarEnviarPublicacionAN8n(
                                            publicacion
                                          )
                                        }
                                        disabled={
                                          enviandoId ===
                                            publicacion.idPublicacion ||
                                          envioDuplicado ||
                                          !campanaActiva
                                        }
                                        className="h-8 gap-1 rounded-lg px-2.5 text-xs text-primary hover:bg-primary/10 hover:text-primary disabled:text-muted-foreground"
                                      >
                                        {enviandoId ===
                                        publicacion.idPublicacion ? (
                                          <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                        ) : (
                                          <Send className="h-3.5 w-3.5" />
                                        )}
                                        {enviandoId ===
                                        publicacion.idPublicacion
                                          ? "Publicando..."
                                          : "Publicar"}
                                      </Button>
                                      {envioDuplicado && (
                                        <span className="text-[11px] text-muted-foreground">
                                          Video ya enviado
                                        </span>
                                      )}
                                    </div>
                                  )}

                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="sm"
                                    onClick={() =>
                                      abrirFormularioEditar(publicacion)
                                    }
                                    disabled={
                                      !puedeEditarPublicacion(publicacion)
                                    }
                                    className="h-8 gap-1 rounded-lg px-2.5 text-xs"
                                  >
                                    <Edit3 className="h-3.5 w-3.5" />
                                    Editar
                                  </Button>

                                  {puedeCancelarPublicacion(publicacion) && (
                                    <Button
                                      type="button"
                                      variant="ghost"
                                      size="sm"
                                      onClick={() =>
                                        void manejarCancelarPublicacion(
                                          publicacion
                                        )
                                      }
                                      disabled={
                                        cancelandoId ===
                                        publicacion.idPublicacion
                                      }
                                      className="h-8 gap-1 rounded-lg px-2.5 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive"
                                    >
                                      {cancelandoId ===
                                      publicacion.idPublicacion ? (
                                        <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                      ) : (
                                        <Ban className="h-3.5 w-3.5" />
                                      )}
                                      Cancelar
                                    </Button>
                                  )}
                                </div>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </section>
    </section>
  );
}

function Field({
  label,
  required,
  children,
}: {
  label: string;
  required?: boolean;
  children: React.ReactNode;
}) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
        {required && <span className="ml-1 text-destructive">*</span>}
      </span>
      {children}
    </label>
  );
}

function PreviewPanel({
  preview,
  cargando,
  recursoSeleccionado,
}: {
  preview: PreviewState | null;
  cargando: boolean;
  recursoSeleccionado: Recurso | null;
}) {
  return (
    <aside className="rounded-2xl border border-border/60 bg-card/70 p-4">
      <div className="mb-3 flex items-center justify-between gap-3">
        <div>
          <h4 className="text-sm font-semibold text-foreground">
            Vista previa
          </h4>
          <p className="text-xs text-muted-foreground">
            Video seleccionado para la publicación.
          </p>
        </div>
        {recursoSeleccionado && (
          <Badge
            variant="outline"
            className="rounded-full px-2 text-[10px] font-medium capitalize"
          >
            {tiposRecurso[recursoSeleccionado.tipo]}
          </Badge>
        )}
      </div>

      {!recursoSeleccionado ? (
        <div className="flex min-h-[220px] flex-col items-center justify-center rounded-2xl border border-dashed border-border/60 bg-muted/20 p-5 text-center">
          <FileText className="h-6 w-6 text-muted-foreground/60" />
          <p className="mt-3 text-sm font-medium text-foreground">
            Sin video asociado
          </p>
          <p className="mt-1 text-xs text-muted-foreground">
            Puedes guardar la publicación como borrador sin video por ahora.
          </p>
        </div>
      ) : cargando ? (
        <div className="flex min-h-[220px] items-center justify-center gap-2 rounded-2xl border border-border/60 bg-muted/20 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin text-primary" />
          Cargando preview...
        </div>
      ) : preview?.error ? (
        <div className="flex min-h-[220px] flex-col items-center justify-center rounded-2xl border border-dashed border-border/60 bg-muted/20 p-5 text-center">
          <FileText className="h-6 w-6 text-muted-foreground/60" />
          <p className="mt-3 text-sm font-medium text-foreground">
            Preview no disponible
          </p>
          <p className="mt-1 text-xs text-muted-foreground">{preview.error}</p>
        </div>
      ) : preview?.recurso.tipo === "imagen" && preview.url ? (
        <div className="overflow-hidden rounded-2xl border border-border bg-muted/20">
          <img
            src={preview.url}
            alt={preview.recurso.titulo || "Vista previa"}
            className="max-h-[360px] w-full object-contain"
          />
        </div>
      ) : preview?.recurso.tipo === "video" && preview.url ? (
        <div className="overflow-hidden rounded-2xl border border-border bg-black">
          <video src={preview.url} controls className="max-h-[360px] w-full" />
        </div>
      ) : preview?.recurso.tipo === "copy" ? (
        <div className="min-h-[220px] rounded-2xl border border-border/60 bg-background/70 p-4">
          <p className="text-sm leading-6 text-foreground">
            {preview.recurso.contenidoTexto ||
              "Este copy no tiene contenido registrado."}
          </p>
        </div>
      ) : (
        <div className="flex min-h-[220px] flex-col items-center justify-center rounded-2xl border border-border/60 bg-muted/20 p-5 text-center">
          <FileText className="h-6 w-6 text-muted-foreground/60" />
          <p className="mt-3 text-sm font-medium text-foreground">
            Archivo asociado
          </p>
          <p className="mt-1 text-xs text-muted-foreground">
            La vista previa está disponible para imágenes, videos y copy.
          </p>
        </div>
      )}
    </aside>
  );
}
