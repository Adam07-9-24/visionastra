import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
  type ReactNode,
} from "react";
import {
  BarChart3,
  CalendarDays,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  CircleDollarSign,
  Edit3,
  Megaphone,
  PauseCircle,
  PlayCircle,
  Plus,
  RefreshCw,
  Search,
  Target,
  Trash2,
  X,
} from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";

import {
  actualizarCampana,
  cambiarEstadoCampana,
  crearCampana,
  eliminarCampana,
  obtenerCampanas,
  type Campana,
  type CampanaRequest,
  type EstadoCampana,
} from "@/services/campanaService";

const estados: Array<EstadoCampana | "todas"> = [
  "todas",
  "borrador",
  "activa",
  "pausada",
  "finalizada",
];

const estadoLabel: Record<EstadoCampana | "todas", string> = {
  todas: "Todas",
  borrador: "Borrador",
  activa: "Activa",
  pausada: "Pausada",
  finalizada: "Finalizada",
};

const estadoStyles: Record<EstadoCampana, string> = {
  borrador: "border-border bg-card text-muted-foreground",
  activa: "border-primary/20 bg-primary/5 text-primary",
  pausada: "border-border bg-muted text-muted-foreground",
  finalizada: "border-border bg-card text-muted-foreground",
};

const estadoDot: Record<EstadoCampana, string> = {
  borrador: "bg-current",
  activa: "bg-current",
  pausada: "bg-current",
  finalizada: "bg-current",
};

const formInicial: CampanaRequest = {
  nombre: "",
  objetivo: "",
  descripcion: "",
  presupuesto: null,
  estado: "borrador",
  fechaInicio: null,
  fechaFin: null,
};

const inputClass =
  "h-9 w-full rounded-lg border border-border/70 bg-background/70 px-3 text-sm text-foreground outline-none transition " +
  "placeholder:text-muted-foreground/50 focus:border-primary/50 focus:ring-2 focus:ring-primary/10";

const textareaClass =
  "min-h-[74px] w-full resize-none rounded-lg border border-border/70 bg-background/70 px-3 py-2 text-sm text-foreground outline-none transition " +
  "placeholder:text-muted-foreground/50 focus:border-primary/50 focus:ring-2 focus:ring-primary/10";

const fechaInputValue = (fecha: string | null | undefined): string => {
  if (!fecha) return "";
  return fecha.slice(0, 10);
};

const actionButtonClass =
  "h-7 shrink-0 rounded-md px-2.5 text-[11px] leading-none";
const actionIconClass = "mr-1.5 h-3 w-3 shrink-0";

export default function CampanasPage() {
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [estadoFiltro, setEstadoFiltro] = useState<EstadoCampana | "todas">(
    "todas"
  );
  const [busqueda, setBusqueda] = useState("");
  const [cargandoInicial, setCargandoInicial] = useState(true);
  const [refrescando, setRefrescando] = useState(false);
  const [eliminandoId, setEliminandoId] = useState<number | null>(null);
  const [cambiandoEstadoId, setCambiandoEstadoId] = useState<number | null>(
    null
  );
  const [detalleAbiertoId, setDetalleAbiertoId] = useState<number | null>(null);
  const [formAbierto, setFormAbierto] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [form, setForm] = useState<CampanaRequest>(formInicial);

  const estaEditando = editandoId !== null;

  const cargarCampanas = useCallback(
    async (opciones?: { silencioso?: boolean }) => {
      const silencioso = opciones?.silencioso ?? false;

      try {
        if (silencioso) {
          setRefrescando(true);
        } else {
          setCargandoInicial(true);
        }

        const data = await obtenerCampanas();
        setCampanas(data);
      } catch {
        toast.error("No se pudieron cargar las campañas", {
          description: "Intenta nuevamente o revisa tu conexión.",
        });
      } finally {
        if (silencioso) {
          setRefrescando(false);
        } else {
          setCargandoInicial(false);
        }
      }
    },
    []
  );

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void cargarCampanas();
    }, 0);

    return () => {
      window.clearTimeout(timer);
    };
  }, [cargarCampanas]);

  const campanasFiltradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();

    const porEstado =
      estadoFiltro === "todas"
        ? campanas
        : campanas.filter((c) => c.estado === estadoFiltro);

    if (!texto) return porEstado;

    return porEstado.filter((c) => {
      const nombre = c.nombre?.toLowerCase() ?? "";
      const objetivo = c.objetivo?.toLowerCase() ?? "";
      const descripcion = c.descripcion?.toLowerCase() ?? "";
      const estado = c.estado?.toLowerCase() ?? "";

      return (
        nombre.includes(texto) ||
        objetivo.includes(texto) ||
        descripcion.includes(texto) ||
        estado.includes(texto)
      );
    });
  }, [campanas, estadoFiltro, busqueda]);

  const resumen = useMemo(() => {
    const total = campanasFiltradas.length;

    const activas = campanasFiltradas.filter(
      (c) => c.estado === "activa"
    ).length;

    const borradores = campanasFiltradas.filter(
      (c) => c.estado === "borrador"
    ).length;

    const presupuestoTotal = campanasFiltradas
      .filter((c) => c.estado !== "finalizada")
      .reduce((acc, c) => acc + Number(c.presupuesto ?? 0), 0);

    return { total, activas, borradores, presupuestoTotal };
  }, [campanasFiltradas]);

  const formatearFecha = (fecha: string | null | undefined) => {
    if (!fecha) return "Sin fecha";

    const d = new Date(fecha);

    if (Number.isNaN(d.getTime())) return "Fecha invalida";

    return new Intl.DateTimeFormat("es-PE", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    }).format(d);
  };

  const formatearMoneda = (valor: number | null | undefined) => {
    if (valor === null || valor === undefined) return "Sin presupuesto";

    return new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: "PEN",
    }).format(valor);
  };

  const resetForm = () => setForm(formInicial);

  const abrirForm = () => {
    setEditandoId(null);
    resetForm();
    setFormAbierto(true);
  };

  const abrirEditar = (campana: Campana) => {
    if (campana.estado === "finalizada") {
      toast.error("No se puede editar una campaña finalizada", {
        description: "Las campañas finalizadas quedan como historial.",
      });
      return;
    }

    setEditandoId(campana.idCampana);
    setForm({
      nombre: campana.nombre ?? "",
      objetivo: campana.objetivo ?? "",
      descripcion: campana.descripcion ?? "",
      presupuesto: campana.presupuesto ?? null,
      estado: campana.estado ?? "borrador",
      fechaInicio: campana.fechaInicio ?? null,
      fechaFin: campana.fechaFin ?? null,
    });
    setFormAbierto(true);
  };

  const cerrarForm = () => {
    if (guardando) return;

    setFormAbierto(false);
    setEditandoId(null);
    resetForm();
  };

  const obtenerMensajeError = (error: unknown) => {
    let mensaje = "Revisa los datos ingresados e intenta nuevamente.";

    if (typeof error === "object" && error !== null && "response" in error) {
      const axiosError = error as {
        response?: { data?: { mensaje?: string } };
      };

      mensaje = axiosError.response?.data?.mensaje || mensaje;
    }

    return mensaje;
  };

  const validarActivacionRequest = (data: CampanaRequest) => {
    if (data.estado !== "activa") return null;

    if (!data.objetivo?.trim()) {
      return "Para activar la campaña debes completar el objetivo.";
    }

    if (data.presupuesto === null || data.presupuesto === undefined) {
      return "Para activar la campaña debes completar el presupuesto.";
    }

    if (Number(data.presupuesto) < 0) {
      return "El presupuesto no puede ser negativo.";
    }

    if (!data.fechaInicio) {
      return "Para activar la campaña debes completar la fecha de inicio.";
    }

    if (!data.fechaFin) {
      return "Para activar la campaña debes completar la fecha de fin.";
    }

    const inicio = new Date(data.fechaInicio).getTime();
    const fin = new Date(data.fechaFin).getTime();

    if (Number.isNaN(inicio) || Number.isNaN(fin)) {
      return "Las fechas de la campaña no son validas.";
    }

    if (fin < inicio) {
      return "La fecha de fin no puede ser menor que la fecha de inicio.";
    }

    return null;
  };

  const validarActivacionCampana = (campana: Campana) => {
    if (!campana.objetivo?.trim()) {
      return "Completa el objetivo antes de activar la campaña.";
    }

    if (campana.presupuesto === null || campana.presupuesto === undefined) {
      return "Completa el presupuesto antes de activar la campaña.";
    }

    if (Number(campana.presupuesto) < 0) {
      return "El presupuesto no puede ser negativo.";
    }

    if (!campana.fechaInicio) {
      return "Completa la fecha de inicio antes de activar la campaña.";
    }

    if (!campana.fechaFin) {
      return "Completa la fecha de fin antes de activar la campaña.";
    }

    const inicio = new Date(campana.fechaInicio).getTime();
    const fin = new Date(campana.fechaFin).getTime();

    if (fin < inicio) {
      return "La fecha de fin no puede ser menor que la fecha de inicio.";
    }

    return null;
  };

  const prepararRequest = (): CampanaRequest => {
    return {
      ...form,
      nombre: form.nombre.trim(),
      objetivo: form.objetivo?.trim() || undefined,
      descripcion: form.descripcion?.trim() || undefined,
      presupuesto:
        form.presupuesto === null || form.presupuesto === undefined
          ? null
          : Number(form.presupuesto),
      estado: form.estado || "borrador",
      fechaInicio: form.fechaInicio || null,
      fechaFin: form.fechaFin || null,
    };
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const request = prepararRequest();

    if (!request.nombre.trim()) {
      toast.error("El nombre es obligatorio", {
        description: "Escribe un nombre para la campaña.",
      });
      return;
    }

    const errorActivacion = validarActivacionRequest(request);

    if (errorActivacion) {
      toast.error("No se puede activar la campaña", {
        description: errorActivacion,
      });
      return;
    }

    try {
      setGuardando(true);

      if (editandoId !== null) {
        await actualizarCampana(editandoId, request);

        toast.success("Campaña actualizada", {
          description: "Los cambios fueron guardados correctamente.",
        });
      } else {
        await crearCampana(request);

        toast.success("Campaña creada", {
          description: "La campaña fue registrada correctamente.",
        });
      }

      setFormAbierto(false);
      setEditandoId(null);
      resetForm();
      await cargarCampanas();
    } catch (error: unknown) {
      toast.error(
        editandoId !== null
          ? "No se pudo actualizar la campaña"
          : "No se pudo crear la campaña",
        {
          description: obtenerMensajeError(error),
        }
      );
    } finally {
      setGuardando(false);
    }
  };

  const handleCambiarEstado = async (
    campana: Campana,
    nuevoEstado: EstadoCampana
  ) => {
    if (campana.estado === "finalizada") {
      toast.error("La campaña ya está finalizada", {
        description: "No se puede cambiar el estado de una campaña finalizada.",
      });
      return;
    }

    if (nuevoEstado === "activa") {
      const errorActivacion = validarActivacionCampana(campana);

      if (errorActivacion) {
        toast.error("No se puede activar la campaña", {
          description: errorActivacion,
        });
        return;
      }
    }

    try {
      setCambiandoEstadoId(campana.idCampana);

      const actualizada = await cambiarEstadoCampana(
        campana.idCampana,
        nuevoEstado
      );

      setCampanas((prev) =>
        prev.map((item) =>
          item.idCampana === campana.idCampana ? actualizada : item
        )
      );

      toast.success("Estado actualizado", {
        description: `La campaña ahora está en estado ${estadoLabel[
          nuevoEstado
        ].toLowerCase()}.`,
      });
    } catch (error: unknown) {
      toast.error("No se pudo cambiar el estado", {
        description: obtenerMensajeError(error),
      });
    } finally {
      setCambiandoEstadoId(null);
    }
  };

  const handleEliminar = async (idCampana: number) => {
    const confirmar = window.confirm(
      "¿Seguro que deseas eliminar esta campaña?"
    );

    if (!confirmar) return;

    try {
      setEliminandoId(idCampana);

      await eliminarCampana(idCampana);

      setCampanas((prev) => prev.filter((c) => c.idCampana !== idCampana));

      toast.success("Campaña eliminada", {
        description: "La campaña fue eliminada correctamente.",
      });
    } catch {
      toast.error("No se pudo eliminar la campaña", {
        description:
          "Verifica que la campaña no tenga recursos asociados o revisa tu conexión.",
      });
    } finally {
      setEliminandoId(null);
    }
  };

  const cambiarFiltro = (estado: EstadoCampana | "todas") =>
    setEstadoFiltro(estado);

  const toggleDetalles = (idCampana: number) => {
    setDetalleAbiertoId((actual) => (actual === idCampana ? null : idCampana));
  };

  const hayBusqueda = busqueda.trim().length > 0;
  const hayFiltroEstado = estadoFiltro !== "todas";

  const resumenItems = [
    {
      titulo: "Total campañas",
      valor: resumen.total.toString(),
      descripcion: "Campañas visibles",
      icono: <BarChart3 size={20} />,
      color: "var(--primary)",
      bg: "color-mix(in srgb, var(--primary) 12%, transparent)",
      border: "color-mix(in srgb, var(--primary) 25%, transparent)",
      compacto: false,
    },
    {
      titulo: "Activas",
      valor: resumen.activas.toString(),
      descripcion: "En ejecución",
      icono: <Target size={20} />,
      color: "var(--primary)",
      bg: "color-mix(in srgb, var(--primary) 12%, transparent)",
      border: "color-mix(in srgb, var(--primary) 25%, transparent)",
      compacto: false,
    },
    {
      titulo: "Borradores",
      valor: resumen.borradores.toString(),
      descripcion: "Pendientes",
      icono: <CalendarDays size={20} />,
      color: "var(--muted-foreground)",
      bg: "color-mix(in srgb, var(--muted-foreground) 12%, transparent)",
      border: "color-mix(in srgb, var(--muted-foreground) 25%, transparent)",
      compacto: false,
    },
    {
      titulo: "Presupuesto activo",
      valor: formatearMoneda(resumen.presupuestoTotal),
      descripcion: "Sin contar finalizadas",
      icono: <CircleDollarSign size={20} className="text-green-500" />,
      color: "var(--foreground)",
      bg: "color-mix(in srgb, var(--foreground) 8%, transparent)",
      border: "color-mix(in srgb, var(--foreground) 18%, transparent)",
      compacto: true,
    },
  ];

  const sectionStyle: React.CSSProperties = {
    border: "1px solid var(--border)",
    borderRadius: "24px",
    background: "var(--card)",
    boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
  };

  return (
    <div
      style={{
        width: "100%",
        maxWidth: "1240px",
        margin: "0 auto",
        display: "flex",
        flexDirection: "column",
        gap: "24px",
      }}
    >
      <style>
        {`
          .campanas-table-header,
          .campanas-table-row {
            display: grid;
            grid-template-columns: 1fr;
            gap: 8px;
          }

          @media (min-width: 1024px) {
            .campanas-table-header,
            .campanas-table-row {
              grid-template-columns: minmax(0, 1fr) 145px 118px minmax(430px, 0.95fr);
              align-items: center;
              gap: 12px;
            }
          }
        `}
      </style>

      {/* Header */}
      <section style={{ ...sectionStyle, padding: "24px 28px" }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            gap: "16px",
            flexWrap: "wrap",
          }}
        >
          <div style={{ display: "flex", gap: "16px", alignItems: "center" }}>
            <div
              style={{
                width: "48px",
                height: "48px",
                minWidth: "48px",
                borderRadius: "14px",
                background:
                  "color-mix(in srgb, var(--primary) 12%, transparent)",
                color: "var(--primary)",
                border:
                  "1px solid color-mix(in srgb, var(--primary) 25%, transparent)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Megaphone size={22} />
            </div>

            <div>
              <h1
                style={{
                  margin: 0,
                  fontSize: "26px",
                  lineHeight: "32px",
                  fontWeight: 700,
                  letterSpacing: "-0.03em",
                }}
              >
                Campañas
              </h1>

              <p
                style={{
                  margin: "4px 0 0",
                  color: "var(--muted-foreground)",
                  fontSize: "14px",
                }}
              >
                Gestiona campañas, estados y presupuesto de marketing.
              </p>
            </div>
          </div>

          <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => void cargarCampanas({ silencioso: true })}
              disabled={refrescando}
              style={{ minWidth: "138px", borderRadius: "10px" }}
            >
              <RefreshCw
                size={14}
                className={`mr-2 ${refrescando ? "animate-spin" : ""}`}
              />
              Refrescar
            </Button>

            <Button
              type="button"
              size="sm"
              onClick={abrirForm}
              style={{ minWidth: "138px", borderRadius: "10px" }}
            >
              <Plus size={15} className="mr-2" />
              Nueva campaña
            </Button>
          </div>
        </div>
      </section>

      {/* Metricas */}
      <section
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: "16px",
        }}
      >
        {resumenItems.map((item) => (
          <div key={item.titulo} style={{ ...sectionStyle, padding: "20px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "14px" }}>
              <div
                style={{
                  width: "44px",
                  height: "44px",
                  minWidth: "44px",
                  borderRadius: "13px",
                  background: item.bg,
                  color: item.color,
                  border: `1px solid ${item.border}`,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  flexShrink: 0,
                }}
              >
                {item.icono}
              </div>

              <div style={{ minWidth: 0 }}>
                <p
                  style={{
                    margin: 0,
                    fontSize: "12px",
                    color: "var(--muted-foreground)",
                  }}
                >
                  {item.titulo}
                </p>

                <p
                  style={{
                    margin: "3px 0 0",
                    fontSize: item.compacto ? "15px" : "22px",
                    fontWeight: 700,
                    lineHeight: "1.2",
                    color: "var(--foreground)",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {item.valor}
                </p>

                <p
                  style={{
                    margin: "2px 0 0",
                    fontSize: "11px",
                    color: "var(--muted-foreground)",
                    opacity: 0.6,
                  }}
                >
                  {item.descripcion}
                </p>
              </div>
            </div>
          </div>
        ))}
      </section>

      {/* Listado */}
      <section style={{ ...sectionStyle, padding: "24px" }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
            gap: "16px",
            flexWrap: "wrap",
            marginBottom: "16px",
          }}
        >
          <div>
            <h2
              style={{
                margin: 0,
                fontSize: "16px",
                fontWeight: 600,
                color: "var(--foreground)",
              }}
            >
              Listado de campañas
            </h2>

            <p
              style={{
                margin: "3px 0 0",
                fontSize: "12px",
                color: "var(--muted-foreground)",
              }}
            >
              Busca, filtra y administra las campañas registradas.
            </p>
          </div>

          <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
            {estados.map((estado) => (
              <button
                key={estado}
                type="button"
                onClick={() => cambiarFiltro(estado)}
                className={`rounded-full px-3 py-1 text-xs font-medium transition-all ${
                  estadoFiltro === estado
                    ? "bg-primary text-primary-foreground shadow-sm"
                    : "border border-border bg-transparent text-muted-foreground hover:border-primary/40 hover:text-foreground"
                }`}
              >
                {estadoLabel[estado]}
              </button>
            ))}
          </div>
        </div>

        <label className="mb-5 flex h-11 cursor-text items-center gap-3 rounded-xl border border-border bg-background px-4 text-sm transition hover:border-primary/40 focus-within:border-primary/60 focus-within:ring-2 focus-within:ring-primary/15">
          <Search className="h-4 w-4 shrink-0 text-muted-foreground" />

          <input
            className="h-full flex-1 bg-transparent text-sm outline-none placeholder:text-muted-foreground/40"
            placeholder="Buscar por nombre, objetivo, descripción o estado..."
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
          />
        </label>

        <div style={{ minHeight: "280px" }}>
          {cargandoInicial ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }).map((_, index) => (
                <div
                  key={index}
                  className="rounded-2xl border border-border/50 bg-background/40 p-4"
                >
                  <div className="grid gap-3 lg:grid-cols-[1.5fr_130px_170px_210px]">
                    <Skeleton className="h-10 rounded-lg" />
                    <Skeleton className="h-10 rounded-lg" />
                    <Skeleton className="h-10 rounded-lg" />
                    <Skeleton className="h-10 rounded-lg" />
                  </div>
                </div>
              ))}
            </div>
          ) : campanasFiltradas.length === 0 ? (
            <div className="flex min-h-[260px] flex-col items-center justify-center rounded-xl border border-dashed border-border/50 bg-background/30 p-8 text-center">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl border border-primary/20 bg-primary/10 text-primary">
                <Megaphone className="h-5 w-5" />
              </div>

              <h3 className="text-sm font-semibold text-foreground">
                {hayBusqueda
                  ? "No se encontraron campañas"
                  : hayFiltroEstado
                  ? `No hay campañas en estado ${estadoLabel[
                      estadoFiltro
                    ].toLowerCase()}`
                  : "Todavía no tienes campañas creadas"}
              </h3>

              <p className="mt-1.5 max-w-sm text-xs leading-5 text-muted-foreground">
                {hayBusqueda
                  ? "Prueba con otro nombre, objetivo, descripción o estado."
                  : "Crea tu primera campaña para organizar presupuesto, fechas y estado de ejecución."}
              </p>

              {!hayBusqueda && (
                <Button
                  type="button"
                  onClick={abrirForm}
                  size="sm"
                  className="mt-5 h-8 rounded-lg px-4"
                >
                  <Plus className="mr-2 h-3.5 w-3.5" />
                  Nueva campaña
                </Button>
              )}
            </div>
          ) : (
            <div className="space-y-2">
              <div className="campanas-table-header hidden rounded-lg border border-border/50 bg-muted/20 py-1.5 pl-4 pr-4 text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground/55 lg:grid">
                <span>Campaña</span>
                <span>Presupuesto</span>
                <span>Estado</span>
                <span className="text-left">Acciones</span>
              </div>

              {campanasFiltradas.map((campana) => {
                const detallesAbiertos = detalleAbiertoId === campana.idCampana;
                const esFinalizada = campana.estado === "finalizada";

                return (
                  <article
                    key={campana.idCampana}
                    className={`overflow-hidden rounded-lg border border-l-4 transition ${
                      detallesAbiertos
                        ? "mb-4 border-primary/20 border-l-primary bg-primary/5"
                        : "border-border/60 border-l-transparent bg-card hover:border-primary/35 hover:bg-primary/5"
                    }`}
                  >
                    <div className="campanas-table-row py-2.5 pl-4 pr-4">
                      <div className="min-w-0">
                        <h3 className="truncate text-[15px] font-semibold leading-5 text-foreground">
                          {campana.nombre || "Campaña sin nombre"}
                        </h3>

                        <p className="mt-0.5 line-clamp-1 text-[13px] leading-4 text-muted-foreground/75">
                          <span className="text-muted-foreground/55">
                            Objetivo:
                          </span>{" "}
                          {campana.objetivo || "Sin objetivo definido"}
                        </p>
                      </div>

                      <DatoFila
                        titulo="Presupuesto"
                        valor={formatearMoneda(campana.presupuesto)}
                      />

                      <div className="flex min-h-[44px] min-w-0 flex-col justify-center">
                        <p className="text-[10px] font-medium leading-none text-muted-foreground/60">
                          Estado
                        </p>
                        <div className="mt-2 flex items-center">
                          <Badge
                            variant="outline"
                            className={`inline-flex h-5 min-w-[76px] items-center justify-center rounded-full px-2 text-[11px] font-medium capitalize ${
                              estadoStyles[campana.estado]
                            }`}
                          >
                            <span
                              className={`mr-1.5 inline-block h-1.5 w-1.5 rounded-full ${
                                estadoDot[campana.estado]
                              }`}
                            />
                            {estadoLabel[campana.estado]}
                          </Badge>
                        </div>
                      </div>

                      <div className="flex min-w-0 flex-wrap items-center justify-start gap-1.5 lg:justify-start">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => toggleDetalles(campana.idCampana)}
                          className={`${actionButtonClass} min-w-[116px] border-border/60`}
                        >
                          {detallesAbiertos ? (
                            <>
                              <ChevronUp className={actionIconClass} />
                              Ocultar detalles
                            </>
                          ) : (
                            <>
                              <ChevronDown className={actionIconClass} />
                              Ver detalles
                            </>
                          )}
                        </Button>

                        <AccionesEstado
                          campana={campana}
                          cargando={cambiandoEstadoId === campana.idCampana}
                          onCambiarEstado={handleCambiarEstado}
                        />

                        {!esFinalizada && (
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => abrirEditar(campana)}
                            className={`${actionButtonClass} min-w-[76px] border-border/60 text-muted-foreground hover:text-foreground`}
                          >
                            <Edit3 className={actionIconClass} />
                            Editar
                          </Button>
                        )}

                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => void handleEliminar(campana.idCampana)}
                          disabled={eliminandoId === campana.idCampana}
                          className={`${actionButtonClass} min-w-[96px] border-destructive/30 text-destructive hover:bg-destructive/10 hover:text-destructive`}
                        >
                          <Trash2 className={actionIconClass} />
                          Eliminar
                        </Button>
                      </div>
                    </div>

                    {detallesAbiertos && (
                      <div className="border-t border-primary/20 px-4 py-2.5">
                        <div className="mb-2 flex items-center justify-between gap-3">
                          <h4 className="text-sm font-semibold text-foreground">
                            Detalles de campaña
                          </h4>

                          {esFinalizada && (
                            <span className="inline-flex h-6 items-center rounded-full border border-primary/20 bg-card px-2.5 text-[11px] text-primary">
                              Historial cerrado
                            </span>
                          )}
                        </div>

                        <div className="space-y-2">
                          <p className="text-sm leading-5 text-muted-foreground">
                            <span className="font-medium text-foreground">
                              Descripción:
                            </span>{" "}
                            {campana.descripcion ||
                              "Sin descripción registrada"}
                          </p>

                          <div className="grid gap-x-4 gap-y-1.5 text-[12px] leading-5 text-muted-foreground sm:grid-cols-2 lg:grid-cols-4">
                            <p>
                              <span className="font-medium text-foreground">
                                Inicio:
                              </span>{" "}
                              {formatearFecha(campana.fechaInicio)}
                            </p>
                            <p>
                              <span className="font-medium text-foreground">
                                Fin:
                              </span>{" "}
                              {formatearFecha(campana.fechaFin)}
                            </p>
                            <p>
                              <span className="font-medium text-foreground">
                                Creación:
                              </span>{" "}
                              {formatearFecha(campana.fechaCreacion)}
                            </p>
                            <p>
                              <span className="font-medium text-foreground">
                                Actualización:
                              </span>{" "}
                              {formatearFecha(campana.fechaActualizacion)}
                            </p>
                          </div>
                        </div>
                      </div>
                    )}
                  </article>
                );
              })}
            </div>
          )}
        </div>
      </section>

      {/* Formulario inline */}
      {formAbierto && (
        <section
          style={{
            ...sectionStyle,
            maxWidth: "880px",
            margin: "0 auto",
            width: "100%",
            boxShadow: "0 4px 24px rgba(0,0,0,0.12)",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              padding: "18px 24px",
              borderBottom: "1px solid var(--border)",
            }}
          >
            <div>
              <h2
                style={{
                  margin: 0,
                  fontSize: "14px",
                  fontWeight: 600,
                  color: "var(--foreground)",
                }}
              >
                {estaEditando ? "Editar campaña" : "Nueva campaña"}
              </h2>

              <p
                style={{
                  margin: "3px 0 0",
                  fontSize: "12px",
                  color: "var(--muted-foreground)",
                }}
              >
                {estaEditando
                  ? "Actualiza los datos principales de tu campaña."
                  : "Completa los datos principales de tu campaña."}
              </p>
            </div>

            <button
              type="button"
              onClick={cerrarForm}
              disabled={guardando}
              className="flex h-8 w-8 items-center justify-center rounded-lg border border-border/60 bg-background text-muted-foreground transition hover:bg-muted hover:text-foreground disabled:opacity-50"
              aria-label="Cerrar formulario"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          <form onSubmit={handleSubmit} style={{ padding: "18px 24px 20px" }}>
            <div className="space-y-4">
              <p className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground/60">
                Datos principales
              </p>

              <div className="grid gap-4 md:grid-cols-2">
                <FormField label="Nombre" required>
                  <input
                    className={inputClass}
                    placeholder="Ej: Campaña de Navidad"
                    value={form.nombre}
                    onChange={(e) =>
                      setForm((p) => ({ ...p, nombre: e.target.value }))
                    }
                  />
                </FormField>

                <FormField label="Objetivo">
                  <input
                    className={inputClass}
                    placeholder="Ej: Aumentar ventas en diciembre"
                    value={form.objetivo ?? ""}
                    onChange={(e) =>
                      setForm((p) => ({ ...p, objetivo: e.target.value }))
                    }
                  />
                </FormField>

                <div className="md:col-span-2">
                  <FormField label="Descripción">
                    <textarea
                      className={textareaClass}
                      placeholder="Describe brevemente la campaña..."
                      value={form.descripcion ?? ""}
                      onChange={(e) =>
                        setForm((p) => ({ ...p, descripcion: e.target.value }))
                      }
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="mt-5 space-y-4 border-t border-border/50 pt-4">
              <p className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground/60">
                Planificación
              </p>

              <div className="grid gap-4 md:grid-cols-2">
                <FormField label="Presupuesto (S/)">
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    className={inputClass}
                    placeholder="0.00"
                    value={form.presupuesto ?? ""}
                    onChange={(e) =>
                      setForm((p) => ({
                        ...p,
                        presupuesto: e.target.value
                          ? Number(e.target.value)
                          : null,
                      }))
                    }
                  />
                </FormField>

                <FormField label="Estado">
                  <select
                    className={inputClass}
                    value={form.estado}
                    onChange={(e) =>
                      setForm((p) => ({
                        ...p,
                        estado: e.target.value as EstadoCampana,
                      }))
                    }
                  >
                    <option value="borrador">Borrador</option>
                    <option value="activa">Activa</option>
                    <option value="pausada">Pausada</option>
                  </select>
                </FormField>

                <FormField label="Fecha de inicio">
                  <input
                    type="date"
                    className={inputClass}
                    value={fechaInputValue(form.fechaInicio)}
                    onChange={(e) =>
                      setForm((p) => ({
                        ...p,
                        fechaInicio: e.target.value
                          ? `${e.target.value}T00:00:00`
                          : null,
                      }))
                    }
                  />
                </FormField>

                <FormField label="Fecha de fin">
                  <input
                    type="date"
                    className={inputClass}
                    value={fechaInputValue(form.fechaFin)}
                    onChange={(e) =>
                      setForm((p) => ({
                        ...p,
                        fechaFin: e.target.value
                          ? `${e.target.value}T23:59:00`
                          : null,
                      }))
                    }
                  />
                </FormField>

                {form.estado === "activa" && (
                  <div className="rounded-xl border border-primary/20 bg-primary/5 px-3 py-2.5 text-xs leading-5 text-primary md:col-span-2">
                    Para activar esta campaña, completa objetivo, presupuesto,
                    fecha de inicio y fecha de fin.
                  </div>
                )}
              </div>
            </div>

            <div className="mt-5 flex gap-2 border-t border-border/50 pt-4">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={cerrarForm}
                disabled={guardando}
                className="h-9 flex-1 rounded-lg border-border/60 text-sm"
              >
                Cancelar
              </Button>

              <Button
                type="submit"
                size="sm"
                disabled={guardando}
                className="h-9 flex-1 rounded-lg bg-primary/75 text-sm text-primary-foreground shadow-none hover:bg-primary/85"
              >
                {guardando ? (
                  <>
                    <RefreshCw className="mr-2 h-3.5 w-3.5 animate-spin" />
                    Guardando...
                  </>
                ) : estaEditando ? (
                  <>
                    <Edit3 className="mr-2 h-3.5 w-3.5" />
                    Guardar cambios
                  </>
                ) : (
                  <>
                    <Plus className="mr-2 h-3.5 w-3.5" />
                    Crear campaña
                  </>
                )}
              </Button>
            </div>
          </form>
        </section>
      )}
    </div>
  );
}

function AccionesEstado({
  campana,
  cargando,
  onCambiarEstado,
}: {
  campana: Campana;
  cargando: boolean;
  onCambiarEstado: (campana: Campana, nuevoEstado: EstadoCampana) => void;
}) {
  if (campana.estado === "finalizada") {
    return null;
  }

  if (campana.estado === "borrador") {
    return (
      <Button
        type="button"
        variant="outline"
        size="sm"
        disabled={cargando}
        onClick={() => onCambiarEstado(campana, "activa")}
        className={`${actionButtonClass} min-w-[82px] border-border/60`}
      >
        {cargando ? (
          <RefreshCw className={`${actionIconClass} animate-spin`} />
        ) : (
          <PlayCircle className={actionIconClass} />
        )}
        Activar
      </Button>
    );
  }

  if (campana.estado === "activa") {
    return (
      <>
        <Button
          type="button"
          variant="outline"
          size="sm"
          disabled={cargando}
          onClick={() => onCambiarEstado(campana, "pausada")}
          className={`${actionButtonClass} min-w-[82px] border-border/60`}
        >
          {cargando ? (
            <RefreshCw className={`${actionIconClass} animate-spin`} />
          ) : (
            <PauseCircle className={actionIconClass} />
          )}
          Pausar
        </Button>

        <Button
          type="button"
          variant="outline"
          size="sm"
          disabled={cargando}
          onClick={() => onCambiarEstado(campana, "finalizada")}
          className={`${actionButtonClass} min-w-[88px] border-border/60`}
        >
          <CheckCircle2 className={actionIconClass} />
          Finalizar
        </Button>
      </>
    );
  }

  return (
    <>
      <Button
        type="button"
        variant="outline"
        size="sm"
        disabled={cargando}
        onClick={() => onCambiarEstado(campana, "activa")}
        className={`${actionButtonClass} min-w-[92px] border-border/60`}
      >
        {cargando ? (
          <RefreshCw className={`${actionIconClass} animate-spin`} />
        ) : (
          <PlayCircle className={actionIconClass} />
        )}
        Reactivar
      </Button>

      <Button
        type="button"
        variant="outline"
        size="sm"
        disabled={cargando}
        onClick={() => onCambiarEstado(campana, "finalizada")}
        className={`${actionButtonClass} min-w-[88px] border-border/60`}
      >
        <CheckCircle2 className={actionIconClass} />
        Finalizar
      </Button>
    </>
  );
}

function DatoFila({ titulo, valor }: { titulo: string; valor: string }) {
  return (
    <div className="flex min-h-[44px] min-w-0 flex-col justify-center">
      <p className="truncate text-[10px] font-medium leading-none text-muted-foreground/60">
        {titulo}
      </p>
      <div className="mt-2">
        <p className="truncate text-[13px] font-medium text-foreground/90">
          {valor}
        </p>
      </div>
    </div>
  );
}

function FormField({
  label,
  required,
  children,
}: {
  label: string;
  required?: boolean;
  children: ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <label className="mb-1 block text-xs font-medium text-muted-foreground">
        {label}
        {required && <span className="ml-1 text-destructive">*</span>}
      </label>
      {children}
    </div>
  );
}
