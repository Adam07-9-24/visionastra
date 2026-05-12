import { useEffect, useState } from "react";
import { toast } from "sonner";
import api from "@/services/api";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Monitor,
  Smartphone,
  Tablet,
  ShieldCheck,
  LogOut,
  Clock3,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Laptop,
} from "lucide-react";

type Sesion = {
  idSesion: number;
  dispositivo: string;
  fechaInicio: string;
  estado: string;
  actual: boolean;
};

export default function SesionesPage() {
  const [sesiones, setSesiones] = useState<Sesion[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [closingId, setClosingId] = useState<number | null>(null);

  const obtenerSesiones = async (silent = false, showToast = false) => {
    try {
      if (silent) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      const res = await api.get("/sesiones/activas-v2");
      setSesiones(res.data);

      if (showToast) {
        toast.info("Sesiones actualizadas", {
          description: "La lista de dispositivos conectados fue actualizada.",
        });
      }
    } catch {
      toast.error("No se pudieron cargar las sesiones", {
        description: "Intenta nuevamente o revisa tu conexión.",
      });
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const cerrarSesion = async (sesion: Sesion) => {
    if (sesion.actual) return;

    try {
      setClosingId(sesion.idSesion);

      await api.patch(`/sesiones/${sesion.idSesion}/cerrar`);
      await obtenerSesiones(true);

      toast.success("Sesión cerrada correctamente", {
        description: "El dispositivo fue desconectado de forma segura.",
      });
    } catch {
      toast.error("No se pudo cerrar la sesión", {
        description: "Intenta nuevamente o revisa tu conexión.",
      });
    } finally {
      setClosingId(null);
    }
  };

  useEffect(() => {
    let mounted = true;

    api
      .get("/sesiones/activas-v2")
      .then((res) => {
        if (mounted) {
          setSesiones(res.data);
        }
      })
      .catch(() => {
        if (mounted) {
          toast.error("No se pudieron cargar las sesiones", {
            description: "Intenta nuevamente o revisa tu conexión.",
          });
        }
      })
      .finally(() => {
        if (mounted) {
          setLoading(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, []);

  const formatearFecha = (fecha: string) =>
    fecha
      ? new Date(fecha).toLocaleString("es-PE", {
          day: "2-digit",
          month: "short",
          year: "numeric",
          hour: "2-digit",
          minute: "2-digit",
        })
      : "No disponible";

  const obtenerIconoDispositivo = (dispositivo: string) => {
    const d = dispositivo?.toLowerCase() ?? "";

    if (d.includes("mobile") || d.includes("android") || d.includes("iphone")) {
      return <Smartphone size={22} />;
    }

    if (d.includes("tablet") || d.includes("ipad")) {
      return <Tablet size={22} />;
    }

    if (d.includes("chrome") || d.includes("windows")) {
      return <Laptop size={22} />;
    }

    return <Monitor size={22} />;
  };

  const sesionesActivas = sesiones.filter((s) => s.estado === "activa").length;
  const haySesionActual = sesiones.some((s) => s.actual);
  const otrosDispositivos = sesiones.filter((s) => !s.actual).length;

  const resumen = [
    {
      icon: <Monitor size={19} />,
      label: "Sesiones activas",
      value: sesionesActivas,
      color: "var(--primary)",
      bg: "color-mix(in srgb, var(--primary) 12%, transparent)",
    },
    {
      icon: <CheckCircle2 size={19} />,
      label: "Sesión actual",
      value: haySesionActual ? "Activa" : "—",
      color: "var(--primary)",
      bg: "color-mix(in srgb, var(--primary) 12%, transparent)",
    },
    {
      icon: <AlertTriangle size={19} />,
      label: "Otros dispositivos",
      value: otrosDispositivos,
      color: "var(--primary)",
      bg: "color-mix(in srgb, var(--primary) 12%, transparent)",
    },
  ];

  return (
    <div
      style={{
        width: "100%",
        maxWidth: "1050px",
        margin: "0 auto",
      }}
    >
      {/* HEADER */}
      <section
        style={{
          border: "1px solid var(--border)",
          borderRadius: "24px",
          background: "var(--card)",
          padding: "28px",
          marginBottom: "24px",
          boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
            gap: "24px",
            flexWrap: "wrap",
          }}
        >
          <div
            style={{
              display: "flex",
              gap: "18px",
              alignItems: "flex-start",
            }}
          >
            <div
              style={{
                width: "52px",
                height: "52px",
                minWidth: "52px",
                borderRadius: "16px",
                background:
                  "color-mix(in srgb, var(--primary) 12%, transparent)",
                color: "var(--primary)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                border:
                  "1px solid color-mix(in srgb, var(--primary) 25%, transparent)",
              }}
            >
              <ShieldCheck size={25} />
            </div>

            <div>
              <h1
                style={{
                  margin: 0,
                  fontSize: "30px",
                  lineHeight: "36px",
                  fontWeight: 700,
                  letterSpacing: "-0.04em",
                }}
              >
                Sesiones activas
              </h1>

              <p
                style={{
                  margin: "10px 0 0",
                  color: "var(--muted-foreground)",
                  fontSize: "15px",
                  lineHeight: "24px",
                  maxWidth: "620px",
                }}
              >
                Revisa dónde está abierta tu cuenta y cierra accesos que no
                reconozcas.
              </p>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            style={{
              borderRadius: "999px",
              paddingLeft: "16px",
              paddingRight: "16px",
              display: "inline-flex",
              alignItems: "center",
              gap: "8px",
              width: "auto",
            }}
            onClick={() => obtenerSesiones(true, true)}
            disabled={refreshing || loading}
          >
            <RefreshCw size={16} className={refreshing ? "animate-spin" : ""} />
            Actualizar
          </Button>
        </div>
      </section>

      {/* RESUMEN */}
      <section
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(230px, 1fr))",
          gap: "16px",
          marginBottom: "32px",
        }}
      >
        {resumen.map((item) => (
          <div
            key={item.label}
            style={{
              border: "1px solid var(--border)",
              borderRadius: "20px",
              background: "var(--card)",
              padding: "20px",
              boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
            }}
          >
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "14px",
              }}
            >
              <div
                style={{
                  width: "44px",
                  height: "44px",
                  borderRadius: "14px",
                  background: item.bg,
                  color: item.color,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  flexShrink: 0,
                }}
              >
                {item.icon}
              </div>

              <div>
                <div
                  style={{
                    fontSize: "13px",
                    color: "var(--muted-foreground)",
                    marginBottom: "4px",
                  }}
                >
                  {item.label}
                </div>

                <div
                  style={{
                    fontSize: "24px",
                    lineHeight: "30px",
                    fontWeight: 500,
                    color: "var(--foreground)",
                  }}
                >
                  {loading ? <Skeleton className="h-7 w-12" /> : item.value}
                </div>
              </div>
            </div>
          </div>
        ))}
      </section>

      {/* SUBTÍTULO */}
      <section style={{ marginBottom: "18px" }}>
        <h2
          style={{
            margin: 0,
            fontSize: "21px",
            lineHeight: "28px",
            fontWeight: 650,
            letterSpacing: "-0.03em",
          }}
        >
          Dispositivos conectados
        </h2>

        <p
          style={{
            margin: "6px 0 0",
            color: "var(--muted-foreground)",
            fontSize: "14px",
          }}
        >
          Tu sesión actual aparece resaltada.
        </p>
      </section>

      {/* LOADING */}
      {loading && (
        <div style={{ display: "grid", gap: "14px" }}>
          {[...Array(3)].map((_, i) => (
            <div
              key={i}
              style={{
                border: "1px solid var(--border)",
                borderRadius: "20px",
                background: "var(--card)",
                padding: "20px",
              }}
            >
              <div style={{ display: "flex", gap: "16px" }}>
                <Skeleton className="h-12 w-12 rounded-xl" />

                <div style={{ flex: 1 }}>
                  <Skeleton className="mb-3 h-5 w-52" />
                  <Skeleton className="mb-3 h-4 w-72" />
                  <Skeleton className="h-7 w-40 rounded-full" />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* LISTA */}
      {!loading && sesiones.length > 0 && (
        <section style={{ display: "grid", gap: "16px" }}>
          {sesiones.map((s) => (
            <article
              key={s.idSesion}
              style={{
                display: "flex",
                overflow: "hidden",
                border: s.actual
                  ? "1px solid color-mix(in srgb, var(--primary) 35%, transparent)"
                  : "1px solid var(--border)",
                borderRadius: "22px",
                background: s.actual
                  ? "color-mix(in srgb, var(--primary) 7%, var(--card))"
                  : "var(--card)",
                boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
              }}
            >
              <div
                style={{
                  width: "6px",
                  background: s.actual ? "var(--primary)" : "var(--border)",
                  flexShrink: 0,
                }}
              />

              <div
                style={{
                  flex: 1,
                  padding: "22px",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  gap: "24px",
                  flexWrap: "wrap",
                }}
              >
                <div
                  style={{
                    display: "flex",
                    gap: "16px",
                    alignItems: "flex-start",
                  }}
                >
                  <div
                    style={{
                      width: "48px",
                      height: "48px",
                      minWidth: "48px",
                      borderRadius: "15px",
                      background: s.actual
                        ? "color-mix(in srgb, var(--primary) 14%, transparent)"
                        : "var(--muted)",
                      color: s.actual
                        ? "var(--primary)"
                        : "var(--muted-foreground)",
                      border: s.actual
                        ? "1px solid color-mix(in srgb, var(--primary) 25%, transparent)"
                        : "1px solid var(--border)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    {obtenerIconoDispositivo(s.dispositivo)}
                  </div>

                  <div>
                    <h3
                      style={{
                        margin: 0,
                        fontSize: "18px",
                        lineHeight: "25px",
                        fontWeight: 650,
                      }}
                    >
                      {s.dispositivo || "Dispositivo desconocido"}
                    </h3>

                    <p
                      style={{
                        margin: "5px 0 0",
                        color: "var(--muted-foreground)",
                        fontSize: "14px",
                        lineHeight: "22px",
                      }}
                    >
                      {s.actual
                        ? "Esta es la sesión que estás usando ahora."
                        : "Sesión iniciada en otro dispositivo o cliente."}
                    </p>

                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        flexWrap: "wrap",
                        marginTop: "14px",
                      }}
                    >
                      <Badge
                        variant={s.actual ? "secondary" : "outline"}
                        style={{
                          borderRadius: "999px",
                          padding: "4px 10px",
                          fontSize: "12px",
                        }}
                      >
                        {s.actual ? "Este dispositivo" : "Otro dispositivo"}
                      </Badge>

                      {s.estado === "activa" && (
                        <Badge
                          style={{
                            borderRadius: "999px",
                            padding: "4px 10px",
                            fontSize: "12px",
                            background:
                              "color-mix(in srgb, var(--primary) 12%, transparent)",
                            color: "var(--primary)",
                            border:
                              "1px solid color-mix(in srgb, var(--primary) 24%, transparent)",
                          }}
                        >
                          Activa
                        </Badge>
                      )}

                      {s.estado === "cerrada" && (
                        <Badge
                          variant="destructive"
                          style={{
                            borderRadius: "999px",
                            padding: "4px 10px",
                            fontSize: "12px",
                          }}
                        >
                          Cerrada
                        </Badge>
                      )}

                      {s.estado === "expirada" && (
                        <Badge
                          variant="outline"
                          style={{
                            borderRadius: "999px",
                            padding: "4px 10px",
                            fontSize: "12px",
                          }}
                        >
                          Expirada
                        </Badge>
                      )}
                    </div>

                    <div
                      style={{
                        display: "inline-flex",
                        alignItems: "center",
                        gap: "7px",
                        marginTop: "14px",
                        border: "1px solid var(--border)",
                        borderRadius: "999px",
                        background: "var(--background)",
                        padding: "6px 11px",
                        color: "var(--muted-foreground)",
                        fontSize: "13px",
                      }}
                    >
                      <Clock3 size={14} />
                      <span>Inicio: {formatearFecha(s.fechaInicio)}</span>
                    </div>
                  </div>
                </div>

                <div>
                  {s.actual ? (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled
                      style={{
                        borderRadius: "999px",
                        paddingLeft: "16px",
                        paddingRight: "16px",
                      }}
                    >
                      Sesión actual
                    </Button>
                  ) : (
                    <Button
                      size="sm"
                      className="
    inline-flex
    h-9
    items-center
    justify-center
    gap-2
    rounded-xl
    border
    border-border
    bg-card
    px-7
    text-[13.5px]
    font-medium
    text-foreground
    shadow-none
    transition-all
    duration-200
    hover:-translate-y-[1px]
    hover:bg-muted
    hover:text-foreground
    disabled:opacity-60
    dark:border-border
    dark:bg-card
    dark:text-foreground
    dark:hover:bg-muted
    dark:hover:text-foreground
  "
                      onClick={() => cerrarSesion(s)}
                      disabled={closingId === s.idSesion}
                    >
                      <LogOut size={14} strokeWidth={2.2} />
                      {closingId === s.idSesion
                        ? "Cerrando..."
                        : "Cerrar sesión"}
                    </Button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </section>
      )}

      {/* SIN SESIONES */}
      {!loading && sesiones.length === 0 && (
        <section
          style={{
            border: "1px dashed var(--border)",
            borderRadius: "22px",
            padding: "48px",
            textAlign: "center",
          }}
        >
          <div
            style={{
              width: "54px",
              height: "54px",
              borderRadius: "16px",
              background: "var(--muted)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 14px",
              color: "var(--muted-foreground)",
            }}
          >
            <Monitor size={22} />
          </div>

          <p
            style={{
              margin: 0,
              fontSize: "15px",
              fontWeight: 600,
            }}
          >
            No tienes sesiones activas
          </p>

          <p
            style={{
              margin: "6px 0 0",
              fontSize: "13px",
              color: "var(--muted-foreground)",
            }}
          >
            Cuando inicies sesión desde un dispositivo, aparecerá aquí.
          </p>
        </section>
      )}
    </div>
  );
}
