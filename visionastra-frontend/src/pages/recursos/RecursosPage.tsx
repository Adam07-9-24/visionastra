// src/pages/recursos/RecursosPage.tsx

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Archive,
  FileText,
  ImageIcon,
  Loader2,
  Pencil,
  Plus,
  Trash2,
  Video,
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
  archivarRecurso,
  crearRecurso,
  eliminarRecurso,
  obtenerRecursosPorCampana,
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
  documento: "Documento",
  copy: "Copy",
};

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
  if (tipo === "imagen") return <ImageIcon className="h-4 w-4" />;
  if (tipo === "video") return <Video className="h-4 w-4" />;
  return <FileText className="h-4 w-4" />;
}

function claseEstadoCampana(estado: EstadoCampana) {
  if (estado === "activa") {
    return "border-emerald-500/30 bg-emerald-500/10 text-emerald-600 dark:text-emerald-300";
  }

  if (estado === "pausada") {
    return "border-amber-500/30 bg-amber-500/10 text-amber-600 dark:text-amber-300";
  }

  if (estado === "finalizada") {
    return "border-slate-500/30 bg-slate-500/10 text-slate-600 dark:text-slate-300";
  }

  return "border-sky-500/30 bg-sky-500/10 text-sky-600 dark:text-sky-300";
}

function mensajePorEstado(campana: Campana) {
  if (campana.estado === "borrador") {
    return "Esta campaña está en borrador. Puedes preparar recursos, pero se activa desde Campañas.";
  }

  if (campana.estado === "activa") {
    return "Campaña activa. Puedes administrar sus recursos normalmente.";
  }

  if (campana.estado === "pausada") {
    return "Esta campaña está pausada. Puedes preparar recursos, pero se reactiva desde Campañas.";
  }

  return "Esta campaña está finalizada. Sus recursos están disponibles solo como historial.";
}

export default function RecursosPage() {
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [campanaSeleccionada, setCampanaSeleccionada] =
    useState<Campana | null>(null);

  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [cargandoCampanas, setCargandoCampanas] = useState(true);
  const [cargandoRecursos, setCargandoRecursos] = useState(false);
  const [guardando, setGuardando] = useState(false);

  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [recursoEditando, setRecursoEditando] = useState<Recurso | null>(null);
  const [form, setForm] = useState<FormState>(formInicial);

  const campanaFinalizada = campanaSeleccionada?.estado === "finalizada";

  const recursosActivos = useMemo(
    () => recursos.filter((recurso) => recurso.estado === "activo"),
    [recursos]
  );

  const recursosArchivados = useMemo(
    () => recursos.filter((recurso) => recurso.estado === "archivado"),
    [recursos]
  );

  const cargarCampanas = useCallback(async () => {
    try {
      setCargandoCampanas(true);
      const data = await obtenerCampanas();
      setCampanas(data);
    } catch {
      toast.error("No se pudieron cargar las campañas");
    } finally {
      setCargandoCampanas(false);
    }
  }, []);

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
    const iniciarCarga = async () => {
      await cargarCampanas();
    };

    void iniciarCarga();
  }, [cargarCampanas]);

  async function seleccionarCampana(campana: Campana) {
    setCampanaSeleccionada(campana);
    setMostrarFormulario(false);
    setRecursoEditando(null);
    setForm(formInicial);
    await cargarRecursos(campana.idCampana);
  }

  function abrirFormularioCrear() {
    if (!campanaSeleccionada) return;

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

    if (!form.nombreArchivo.trim()) {
      toast.error("El nombre del recurso es obligatorio");
      return false;
    }

    if (form.tipo === "copy") {
      if (!form.contenidoTexto.trim()) {
        toast.error("El contenido del copy es obligatorio");
        return false;
      }

      return true;
    }

    if (!form.urlArchivo.trim()) {
      toast.error("La URL del archivo es obligatoria");
      return false;
    }

    return true;
  }

  function construirRequest(): RecursoRequest {
    if (!campanaSeleccionada) {
      throw new Error("Campaña no seleccionada");
    }

    const esCopy = form.tipo === "copy";

    return {
      idCampana: campanaSeleccionada.idCampana,
      tipo: form.tipo,
      titulo: form.titulo.trim() || null,
      nombreArchivo: form.nombreArchivo.trim(),
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

      const payload = construirRequest();

      if (recursoEditando) {
        await actualizarRecurso(recursoEditando.idRecurso, payload);
        toast.success("Recurso actualizado correctamente");
      } else {
        await crearRecurso(payload);
        toast.success("Recurso creado correctamente");
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

  function formatearFecha(fecha: string | null) {
    if (!fecha) return "Sin fecha";

    return new Intl.DateTimeFormat("es-PE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(fecha));
  }

  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6">
      <header className="rounded-3xl border border-border/70 bg-card/70 p-6 shadow-sm backdrop-blur">
        <div className="flex flex-col gap-2">
          <p className="text-sm font-medium text-primary">
            VisionAstra / Recursos
          </p>
          <h1 className="text-2xl font-semibold tracking-tight text-foreground md:text-3xl">
            Recursos multimedia
          </h1>
          <p className="max-w-3xl text-sm text-muted-foreground">
            Administra imágenes, videos, documentos y textos publicitarios
            asociados a tus campañas. Las campañas finalizadas se muestran como
            historial bloqueado.
          </p>
        </div>
      </header>

      <div className="grid gap-6 xl:grid-cols-[360px_1fr]">
        <aside className="rounded-3xl border border-border/70 bg-card/70 p-4 shadow-sm backdrop-blur">
          <div className="mb-4">
            <h2 className="text-base font-semibold text-foreground">
              Campañas
            </h2>
            <p className="text-sm text-muted-foreground">
              Selecciona una campaña para ver sus recursos.
            </p>
          </div>

          {cargandoCampanas ? (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Cargando campañas...
            </div>
          ) : campanas.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-border p-4 text-sm text-muted-foreground">
              No tienes campañas registradas todavía.
            </div>
          ) : (
            <div className="flex max-h-[620px] flex-col gap-3 overflow-y-auto pr-1">
              {campanas.map((campana) => {
                const seleccionada =
                  campanaSeleccionada?.idCampana === campana.idCampana;

                return (
                  <button
                    key={campana.idCampana}
                    type="button"
                    onClick={() => seleccionarCampana(campana)}
                    className={[
                      "rounded-2xl border p-4 text-left transition hover:border-primary/50 hover:bg-primary/5",
                      seleccionada
                        ? "border-primary/60 bg-primary/10"
                        : "border-border bg-background/40",
                    ].join(" ")}
                  >
                    <div className="mb-2 flex items-start justify-between gap-3">
                      <h3 className="text-sm font-semibold text-foreground">
                        {campana.nombre}
                      </h3>
                      <Badge className={claseEstadoCampana(campana.estado)}>
                        {estadosCampana[campana.estado]}
                      </Badge>
                    </div>

                    <p className="text-xs text-muted-foreground">
                      {campana.objetivo || "Sin objetivo definido"}
                    </p>

                    {campana.estado === "finalizada" && (
                      <p className="mt-2 text-xs font-medium text-muted-foreground">
                        Historial bloqueado
                      </p>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </aside>

        <main className="rounded-3xl border border-border/70 bg-card/70 p-4 shadow-sm backdrop-blur md:p-6">
          {!campanaSeleccionada ? (
            <div className="flex min-h-[420px] flex-col items-center justify-center rounded-3xl border border-dashed border-border bg-background/40 p-8 text-center">
              <FileText className="mb-4 h-10 w-10 text-muted-foreground" />
              <h2 className="text-lg font-semibold text-foreground">
                Selecciona una campaña
              </h2>
              <p className="mt-2 max-w-md text-sm text-muted-foreground">
                Al elegir una campaña podrás ver, agregar y administrar sus
                recursos multimedia.
              </p>
            </div>
          ) : (
            <div className="flex flex-col gap-5">
              <div className="flex flex-col gap-4 border-b border-border pb-5 lg:flex-row lg:items-start lg:justify-between">
                <div>
                  <div className="mb-2 flex flex-wrap items-center gap-2">
                    <h2 className="text-xl font-semibold text-foreground">
                      Recursos de: {campanaSeleccionada.nombre}
                    </h2>
                    <Badge
                      className={claseEstadoCampana(campanaSeleccionada.estado)}
                    >
                      {estadosCampana[campanaSeleccionada.estado]}
                    </Badge>
                  </div>

                  <p className="max-w-3xl text-sm text-muted-foreground">
                    {mensajePorEstado(campanaSeleccionada)}
                  </p>
                </div>

                {!campanaFinalizada && (
                  <Button onClick={abrirFormularioCrear} className="gap-2">
                    <Plus className="h-4 w-4" />
                    Agregar recurso
                  </Button>
                )}
              </div>

              {campanaFinalizada && (
                <div className="rounded-2xl border border-slate-500/20 bg-slate-500/10 p-4 text-sm text-muted-foreground">
                  Esta campaña está finalizada. Puedes revisar sus recursos,
                  pero no agregar, editar, archivar ni eliminar.
                </div>
              )}

              {mostrarFormulario && !campanaFinalizada && (
                <div className="rounded-3xl border border-border bg-background/60 p-4">
                  <div className="mb-4">
                    <h3 className="text-base font-semibold text-foreground">
                      {recursoEditando ? "Editar recurso" : "Agregar recurso"}
                    </h3>
                    <p className="text-sm text-muted-foreground">
                      Completa los datos del recurso para esta campaña.
                    </p>
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="flex flex-col gap-2">
                      <label className="text-sm font-medium text-foreground">
                        Tipo
                      </label>
                      <select
                        value={form.tipo}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            tipo: e.target.value as TipoRecurso,
                            urlArchivo:
                              e.target.value === "copy" ? "" : prev.urlArchivo,
                            contenidoTexto:
                              e.target.value === "copy"
                                ? prev.contenidoTexto
                                : "",
                            pesoMb:
                              e.target.value === "copy" ? "" : prev.pesoMb,
                            formato:
                              e.target.value === "copy"
                                ? "texto"
                                : prev.formato,
                          }))
                        }
                        className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value="imagen">Imagen</option>
                        <option value="video">Video</option>
                        <option value="documento">Documento</option>
                        <option value="copy">Copy</option>
                      </select>
                    </div>

                    <div className="flex flex-col gap-2">
                      <label className="text-sm font-medium text-foreground">
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

                    <div className="flex flex-col gap-2">
                      <label className="text-sm font-medium text-foreground">
                        Nombre del recurso
                      </label>
                      <input
                        value={form.nombreArchivo}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            nombreArchivo: e.target.value,
                          }))
                        }
                        placeholder="Ej: banner-juegos.png"
                        className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                      />
                    </div>

                    <div className="flex flex-col gap-2">
                      <label className="text-sm font-medium text-foreground">
                        Formato
                      </label>
                      <input
                        value={form.formato}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            formato: e.target.value,
                          }))
                        }
                        placeholder={
                          form.tipo === "copy" ? "texto" : "png, mp4, pdf"
                        }
                        className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                      />
                    </div>

                    {form.tipo !== "copy" ? (
                      <>
                        <div className="flex flex-col gap-2 md:col-span-2">
                          <label className="text-sm font-medium text-foreground">
                            URL del archivo
                          </label>
                          <input
                            value={form.urlArchivo}
                            onChange={(e) =>
                              setForm((prev) => ({
                                ...prev,
                                urlArchivo: e.target.value,
                              }))
                            }
                            placeholder="https://example.com/recurso.png"
                            className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                          />
                        </div>

                        <div className="flex flex-col gap-2">
                          <label className="text-sm font-medium text-foreground">
                            Peso MB
                          </label>
                          <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={form.pesoMb}
                            onChange={(e) =>
                              setForm((prev) => ({
                                ...prev,
                                pesoMb: e.target.value,
                              }))
                            }
                            placeholder="Ej: 2.5"
                            className="h-10 rounded-xl border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                          />
                        </div>
                      </>
                    ) : (
                      <div className="flex flex-col gap-2 md:col-span-2">
                        <label className="text-sm font-medium text-foreground">
                          Contenido del copy
                        </label>
                        <textarea
                          value={form.contenidoTexto}
                          onChange={(e) =>
                            setForm((prev) => ({
                              ...prev,
                              contenidoTexto: e.target.value,
                            }))
                          }
                          placeholder="Escribe el texto publicitario..."
                          rows={5}
                          className="rounded-xl border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
                        />
                      </div>
                    )}
                  </div>

                  <div className="mt-5 flex flex-wrap justify-end gap-3">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={cancelarFormulario}
                      disabled={guardando}
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="button"
                      onClick={guardarRecurso}
                      disabled={guardando}
                      className="gap-2"
                    >
                      {guardando && (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      )}
                      {recursoEditando ? "Guardar cambios" : "Crear recurso"}
                    </Button>
                  </div>
                </div>
              )}

              <div className="grid gap-4 md:grid-cols-3">
                <div className="rounded-2xl border border-border bg-background/50 p-4">
                  <p className="text-sm text-muted-foreground">
                    Total recursos
                  </p>
                  <p className="mt-1 text-2xl font-semibold text-foreground">
                    {recursos.length}
                  </p>
                </div>
                <div className="rounded-2xl border border-border bg-background/50 p-4">
                  <p className="text-sm text-muted-foreground">Activos</p>
                  <p className="mt-1 text-2xl font-semibold text-foreground">
                    {recursosActivos.length}
                  </p>
                </div>
                <div className="rounded-2xl border border-border bg-background/50 p-4">
                  <p className="text-sm text-muted-foreground">Archivados</p>
                  <p className="mt-1 text-2xl font-semibold text-foreground">
                    {recursosArchivados.length}
                  </p>
                </div>
              </div>

              <div className="overflow-hidden rounded-3xl border border-border">
                <div className="border-b border-border bg-background/60 px-4 py-3">
                  <h3 className="text-sm font-semibold text-foreground">
                    Lista de recursos
                  </h3>
                </div>

                {cargandoRecursos ? (
                  <div className="flex items-center gap-2 p-5 text-sm text-muted-foreground">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Cargando recursos...
                  </div>
                ) : recursos.length === 0 ? (
                  <div className="p-6 text-sm text-muted-foreground">
                    Esta campaña todavía no tiene recursos registrados.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full min-w-[820px] text-sm">
                      <thead className="bg-muted/40 text-left text-xs uppercase tracking-wide text-muted-foreground">
                        <tr>
                          <th className="px-4 py-3">Tipo</th>
                          <th className="px-4 py-3">Título</th>
                          <th className="px-4 py-3">Nombre</th>
                          <th className="px-4 py-3">Formato</th>
                          <th className="px-4 py-3">Estado</th>
                          <th className="px-4 py-3">Fecha</th>
                          <th className="px-4 py-3 text-right">Acciones</th>
                        </tr>
                      </thead>

                      <tbody>
                        {recursos.map((recurso) => (
                          <tr
                            key={recurso.idRecurso}
                            className="border-t border-border"
                          >
                            <td className="px-4 py-3">
                              <div className="flex items-center gap-2 text-foreground">
                                {obtenerIconoTipo(recurso.tipo)}
                                {tiposRecurso[recurso.tipo]}
                              </div>
                            </td>

                            <td className="px-4 py-3 text-foreground">
                              {recurso.titulo || "Sin título"}
                            </td>

                            <td className="px-4 py-3 text-muted-foreground">
                              {recurso.nombreArchivo}
                            </td>

                            <td className="px-4 py-3 text-muted-foreground">
                              {recurso.formato || "Sin formato"}
                            </td>

                            <td className="px-4 py-3">
                              <Badge
                                className={
                                  recurso.estado === "activo"
                                    ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-600 dark:text-emerald-300"
                                    : "border-slate-500/30 bg-slate-500/10 text-slate-600 dark:text-slate-300"
                                }
                              >
                                {recurso.estado === "activo"
                                  ? "Activo"
                                  : "Archivado"}
                              </Badge>
                            </td>

                            <td className="px-4 py-3 text-muted-foreground">
                              {formatearFecha(recurso.fechaSubida)}
                            </td>

                            <td className="px-4 py-3">
                              <div className="flex justify-end gap-2">
                                {!campanaFinalizada && (
                                  <>
                                    <Button
                                      variant="outline"
                                      size="sm"
                                      onClick={() =>
                                        abrirFormularioEditar(recurso)
                                      }
                                      className="gap-1"
                                    >
                                      <Pencil className="h-3.5 w-3.5" />
                                      Editar
                                    </Button>

                                    {recurso.estado === "activo" && (
                                      <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => manejarArchivar(recurso)}
                                        className="gap-1"
                                      >
                                        <Archive className="h-3.5 w-3.5" />
                                        Archivar
                                      </Button>
                                    )}

                                    <Button
                                      variant="outline"
                                      size="sm"
                                      onClick={() => manejarEliminar(recurso)}
                                      className="gap-1 text-destructive hover:text-destructive"
                                    >
                                      <Trash2 className="h-3.5 w-3.5" />
                                      Eliminar
                                    </Button>
                                  </>
                                )}

                                {campanaFinalizada && (
                                  <span className="text-xs text-muted-foreground">
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
      </div>
    </section>
  );
}
