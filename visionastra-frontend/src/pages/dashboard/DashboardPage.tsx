import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  ArrowRight,
  Bot,
  FileUp,
  LayoutDashboard,
  Loader2,
  Megaphone,
  Video,
  WandSparkles,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { Campana, EstadoCampana } from "@/services/campanaService";
import { obtenerCampanas } from "@/services/campanaService";
import type { Recurso, TipoRecurso } from "@/services/recursoService";
import { obtenerRecursosPorCampana } from "@/services/recursoService";
import type {
  EstadoGeneracionIA,
  GeneracionIA,
} from "@/services/generacionIAService";
import { listarGeneracionesIA } from "@/services/generacionIAService";

type EstadoCarga = "loading" | "idle" | "error";

type ChartItem = {
  name: string;
  value: number;
  fill?: string;
};

type UsuarioLocal = {
  nombres?: unknown;
};

const estadosCampana: Array<{
  key: EstadoCampana;
  label: string;
  color: string;
}> = [
  { key: "borrador", label: "Borrador", color: "#64748b" },
  { key: "activa", label: "Activa", color: "#10b981" },
  { key: "pausada", label: "Pausada", color: "#f59e0b" },
  { key: "finalizada", label: "Finalizada", color: "#8b5cf6" },
];

const tiposRecurso: Array<{
  key: TipoRecurso;
  label: string;
  color: string;
}> = [
  { key: "imagen", label: "Imagen", color: "#3b82f6" },
  { key: "video", label: "Video", color: "#06b6d4" },
  { key: "copy", label: "Idea", color: "#8b5cf6" },
];

const estadosGeneracion: Array<{
  key: EstadoGeneracionIA;
  label: string;
  color: string;
  ocultarSiCero?: boolean;
}> = [
  { key: "pendiente", label: "Pendiente", color: "#f59e0b" },
  { key: "procesando", label: "Procesando", color: "#0ea5e9" },
  { key: "completado", label: "Completado", color: "#10b981" },
  { key: "error", label: "Fallidas", color: "#f43f5e", ocultarSiCero: true },
];

function formatearFecha(fecha: string | null | undefined) {
  if (!fecha) return "Sin fecha";

  try {
    return new Intl.DateTimeFormat("es-PE", {
      dateStyle: "medium",
    }).format(new Date(fecha));
  } catch {
    return "Sin fecha";
  }
}

function formatearPresupuesto(presupuesto: number | null | undefined) {
  if (presupuesto === null || presupuesto === undefined) {
    return "Sin presupuesto";
  }

  return new Intl.NumberFormat("es-PE", {
    style: "currency",
    currency: "PEN",
    maximumFractionDigits: 0,
  }).format(presupuesto);
}

function ordenarPorFechaDesc<T>(
  items: T[],
  getFecha: (item: T) => string | null | undefined
) {
  return [...items].sort((a, b) => {
    const fechaA = getFecha(a) ? new Date(getFecha(a) as string).getTime() : 0;
    const fechaB = getFecha(b) ? new Date(getFecha(b) as string).getTime() : 0;

    return fechaB - fechaA;
  });
}

function hayDatos(items: ChartItem[]) {
  return items.some((item) => item.value > 0);
}

function obtenerNombreUsuario() {
  try {
    const user = localStorage.getItem("user");

    if (!user) {
      return "Usuario";
    }

    const usuario = JSON.parse(user) as UsuarioLocal;
    const nombres =
      typeof usuario.nombres === "string" ? usuario.nombres.trim() : "";

    return nombres || "Usuario";
  } catch {
    return "Usuario";
  }
}

function ChartTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: Array<{ value?: number; name?: string }>;
  label?: string;
}) {
  if (!active || !payload?.length) {
    return null;
  }

  return (
    <div className="rounded-xl border border-border bg-card px-3 py-2 text-sm shadow-sm">
      <p className="font-medium text-foreground">{label || payload[0].name}</p>
      <p className="text-muted-foreground">{payload[0].value ?? 0}</p>
    </div>
  );
}

function EmptyState({ children }: { children: string }) {
  return (
    <div className="flex min-h-[220px] items-center justify-center rounded-2xl border border-dashed border-border bg-muted/30 p-6 text-center text-sm text-muted-foreground">
      {children}
    </div>
  );
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const [estadoCarga, setEstadoCarga] = useState<EstadoCarga>("loading");
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [generaciones, setGeneraciones] = useState<GeneracionIA[]>([]);
  const nombreUsuario = useMemo(() => obtenerNombreUsuario(), []);

  useEffect(() => {
    let cancelado = false;

    const cargarDashboard = async () => {
      try {
        setEstadoCarga("loading");

        const [campanasData, generacionesData] = await Promise.all([
          obtenerCampanas(),
          listarGeneracionesIA(),
        ]);

        const recursosPorCampana = await Promise.all(
          campanasData.map(async (campana) => {
            try {
              return await obtenerRecursosPorCampana(campana.idCampana);
            } catch (error) {
              console.error(
                `No se pudieron cargar recursos de la campaña ${campana.idCampana}`,
                error
              );
              return [];
            }
          })
        );

        if (cancelado) return;

        setCampanas(campanasData);
        setGeneraciones(generacionesData);
        setRecursos(recursosPorCampana.flat());
        setEstadoCarga("idle");
      } catch (error) {
        console.error(error);

        if (!cancelado) {
          setEstadoCarga("error");
        }
      }
    };

    void cargarDashboard();

    return () => {
      cancelado = true;
    };
  }, []);

  const metricas = useMemo(() => {
    const recursosVideoIds = new Set(
      recursos
        .filter((recurso) => recurso.tipo === "video")
        .map((recurso) => recurso.idRecurso)
    );

    generaciones.forEach((generacion) => {
      if (generacion.estado === "completado" && generacion.idRecursoResultado) {
        recursosVideoIds.add(generacion.idRecursoResultado);
      }
    });

    return {
      campanasActivas: campanas.filter((campana) => campana.estado === "activa")
        .length,
      videosGenerados: recursosVideoIds.size,
      generacionesIA: generaciones.length,
    };
  }, [campanas, generaciones, recursos]);

  const campanasPorEstado = useMemo<ChartItem[]>(
    () =>
      estadosCampana.map((estado) => ({
        name: estado.label,
        value: campanas.filter((campana) => campana.estado === estado.key)
          .length,
        fill: estado.color,
      })),
    [campanas]
  );

  const recursosPorTipo = useMemo<ChartItem[]>(
    () =>
      tiposRecurso.map((tipo) => ({
        name: tipo.label,
        value: recursos.filter((recurso) => recurso.tipo === tipo.key).length,
        fill: tipo.color,
      })),
    [recursos]
  );

  const generacionesPorEstado = useMemo<ChartItem[]>(
    () =>
      estadosGeneracion
        .map((estado) => ({
          name: estado.label,
          value: generaciones.filter(
            (generacion) => generacion.estado === estado.key
          ).length,
          fill: estado.color,
          ocultarSiCero: estado.ocultarSiCero,
        }))
        .filter((estado) => !estado.ocultarSiCero || estado.value > 0),
    [generaciones]
  );

  const ultimasCampanas = useMemo(
    () =>
      ordenarPorFechaDesc(campanas, (campana) => campana.fechaCreacion).slice(
        0,
        5
      ),
    [campanas]
  );

  const ultimosVideosGenerados = useMemo(
    () =>
      ordenarPorFechaDesc(
        generaciones.filter(
          (generacion) =>
            generacion.estado === "completado" &&
            generacion.idRecursoResultado &&
            generacion.tipoSalida === "video"
        ),
        (generacion) => generacion.fechaCreacion
      ).slice(0, 5),
    [generaciones]
  );

  const resumenCards = [
    {
      label: "Campañas activas",
      value: metricas.campanasActivas,
      icon: Megaphone,
    },
    {
      label: "Videos generados",
      value: metricas.videosGenerados,
      icon: Video,
    },
    {
      label: "Generaciones IA",
      value: metricas.generacionesIA,
      icon: Bot,
    },
  ];

  const accionesRapidas = [
    {
      label: "Crear campaña",
      to: "/campanas",
      icon: Megaphone,
    },
    {
      label: "Subir recurso",
      to: "/recursos",
      icon: FileUp,
    },
    {
      label: "Generar video IA",
      to: "/generador-ia",
      icon: WandSparkles,
    },
  ];

  if (estadoCarga === "loading") {
    return (
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 md:px-6">
        <Card className="border-border bg-card">
          <CardContent className="flex min-h-[320px] items-center justify-center">
            <div className="flex items-center gap-3 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Cargando dashboard...
            </div>
          </CardContent>
        </Card>
      </main>
    );
  }

  if (estadoCarga === "error") {
    return (
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 md:px-6">
        <Card className="border-border bg-card">
          <CardContent className="flex min-h-[320px] flex-col items-center justify-center gap-3 text-center">
            <LayoutDashboard className="h-8 w-8 text-muted-foreground" />
            <div>
              <h1 className="text-lg font-semibold text-foreground">
                No se pudo cargar el dashboard
              </h1>
              <p className="mt-1 text-sm text-muted-foreground">
                Intenta actualizar la página en unos segundos.
              </p>
            </div>
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 md:px-6">
      <section className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            Bienvenido de nuevo, {nombreUsuario}
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Resumen general de tus campañas, recursos y contenido generado con
            IA.
          </p>
        </div>
      </section>

      {campanas.length === 0 && (
        <div className="rounded-2xl border border-border bg-muted/30 p-4 text-sm text-muted-foreground">
          Aún no tienes campañas registradas. Crea una campaña para empezar.
        </div>
      )}

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {resumenCards.map(({ label, value, icon: Icon }) => (
          <Card key={label} className="border-border bg-card">
            <CardContent className="p-5">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm text-muted-foreground">{label}</p>
                  <p className="mt-2 text-3xl font-semibold text-foreground">
                    {value}
                  </p>
                </div>
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <Icon className="h-5 w-5" />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        {accionesRapidas.map(({ label, to, icon: Icon }) => (
          <button
            key={label}
            type="button"
            onClick={() => navigate(to)}
            className="group rounded-2xl border border-border bg-card p-4 text-left transition hover:bg-muted"
          >
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <Icon className="h-5 w-5" />
                </div>
                <span className="font-medium text-foreground">{label}</span>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground transition group-hover:text-foreground" />
            </div>
          </button>
        ))}
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        <Card className="border-border bg-card">
          <CardContent className="p-5">
            <h2 className="text-lg font-semibold text-foreground">
              Campañas por estado
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Distribución actual de tus campañas.
            </p>

            <div className="mt-4 h-[260px]">
              {hayDatos(campanasPorEstado) ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={campanasPorEstado}
                    layout="vertical"
                    margin={{ top: 4, right: 12, bottom: 4, left: 8 }}
                  >
                    <CartesianGrid stroke="var(--border)" horizontal={false} />
                    <XAxis
                      type="number"
                      allowDecimals={false}
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                    />
                    <YAxis
                      dataKey="name"
                      type="category"
                      width={78}
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                    />
                    <Tooltip content={<ChartTooltip />} />
                    <Bar dataKey="value" radius={[0, 8, 8, 0]} barSize={18}>
                      {campanasPorEstado.map((entry) => (
                        <Cell key={entry.name} fill={entry.fill} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState>
                  Aún no tienes campañas registradas para visualizar.
                </EmptyState>
              )}
            </div>
          </CardContent>
        </Card>

        <Card className="border-border bg-card">
          <CardContent className="p-5">
            <h2 className="text-lg font-semibold text-foreground">
              Recursos por tipo
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Archivos e ideas disponibles para tus campañas.
            </p>

            <div className="mt-4 h-[260px]">
              {hayDatos(recursosPorTipo) ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Tooltip content={<ChartTooltip />} />
                    <Pie
                      data={recursosPorTipo}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={58}
                      outerRadius={86}
                      paddingAngle={3}
                    >
                      {recursosPorTipo.map((entry) => (
                        <Cell key={entry.name} fill={entry.fill} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState>
                  Aún no tienes recursos subidos para visualizar.
                </EmptyState>
              )}
            </div>

            <div className="mt-3 grid grid-cols-2 gap-2">
              {recursosPorTipo.map((item) => (
                <div
                  key={item.name}
                  className="flex items-center gap-2 text-sm"
                >
                  <span
                    className="h-2.5 w-2.5 rounded-full"
                    style={{ backgroundColor: item.fill }}
                  />
                  <span className="text-muted-foreground">
                    {item.name}: {item.value}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="border-border bg-card">
          <CardContent className="p-5">
            <h2 className="text-lg font-semibold text-foreground">
              Generaciones IA por estado
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Seguimiento del contenido generado con IA.
            </p>

            <div className="mt-4 h-[260px]">
              {hayDatos(generacionesPorEstado) ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={generacionesPorEstado}>
                    <CartesianGrid stroke="var(--border)" vertical={false} />
                    <XAxis
                      dataKey="name"
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                    />
                    <YAxis
                      allowDecimals={false}
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                    />
                    <Tooltip content={<ChartTooltip />} />
                    <Bar dataKey="value" radius={[8, 8, 0, 0]}>
                      {generacionesPorEstado.map((entry) => (
                        <Cell key={entry.name} fill={entry.fill} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState>
                  Aún no tienes generaciones IA para visualizar.
                </EmptyState>
              )}
            </div>
          </CardContent>
        </Card>
      </section>

      <section className="grid gap-6 lg:grid-cols-2">
        <Card className="border-border bg-card">
          <CardContent className="p-5">
            <div className="mb-4 flex items-center justify-between gap-3">
              <div>
                <h2 className="text-lg font-semibold text-foreground">
                  Últimas campañas
                </h2>
                <p className="text-sm text-muted-foreground">
                  Actividad reciente de tus campañas.
                </p>
              </div>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/campanas")}
                className="rounded-xl"
              >
                Ver campañas
              </Button>
            </div>

            {ultimasCampanas.length === 0 ? (
              <EmptyState>
                Aún no tienes campañas registradas. Crea una campaña para
                empezar.
              </EmptyState>
            ) : (
              <div className="space-y-3">
                {ultimasCampanas.map((campana) => (
                  <div
                    key={campana.idCampana}
                    className="rounded-2xl border border-border bg-muted/30 p-4"
                  >
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div className="min-w-0">
                        <p className="truncate font-medium text-foreground">
                          {campana.nombre}
                        </p>
                        <p className="mt-1 text-sm text-muted-foreground">
                          {formatearPresupuesto(campana.presupuesto)}
                        </p>
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {formatearFecha(campana.fechaCreacion)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="border-border bg-card">
          <CardContent className="p-5">
            <div className="mb-4 flex items-center justify-between gap-3">
              <div>
                <h2 className="text-lg font-semibold text-foreground">
                  Últimos videos generados
                </h2>
                <p className="text-sm text-muted-foreground">
                  Videos IA listos para revisar o reutilizar.
                </p>
              </div>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/recursos")}
                className="rounded-xl"
              >
                Ver recursos
              </Button>
            </div>

            {ultimosVideosGenerados.length === 0 ? (
              <EmptyState>Aún no tienes videos generados con IA.</EmptyState>
            ) : (
              <div className="space-y-3">
                {ultimosVideosGenerados.map((generacion) => (
                  <div
                    key={generacion.idGeneracion}
                    className="rounded-2xl border border-border bg-muted/30 p-4"
                  >
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div className="min-w-0">
                        <p className="truncate font-medium text-foreground">
                          {generacion.tituloRecursoResultado ||
                            "Video IA generado"}
                        </p>
                        <p className="mt-1 truncate text-sm text-muted-foreground">
                          {generacion.nombreCampana || "Campaña sin nombre"}
                        </p>
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {formatearFecha(generacion.fechaCreacion)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </section>
    </main>
  );
}
