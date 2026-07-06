import {
  AlertCircle,
  BadgeCheck,
  FolderOpen,
  Megaphone,
  RefreshCw,
  Send,
  Sparkles,
  UserCheck,
  Users,
} from "lucide-react"
import { useCallback, useEffect, useState } from "react"

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useAdminAuth } from "@/contexts/AdminAuthContext"
import { getAdminApiErrorMessage } from "@/lib/adminApiError"
import { obtenerDashboardResumen } from "@/services/adminDashboardService"
import type { DashboardResumen } from "@/types/adminDashboard"

type MetricDefinition = {
  key: keyof DashboardResumen
  title: string
  description: string
  icon: typeof Users
  accent: string
}

const METRICS: MetricDefinition[] = [
  {
    key: "totalUsuarios",
    title: "Total de usuarios",
    description: "Usuarios registrados",
    icon: Users,
    accent: "text-cyan-500",
  },
  {
    key: "usuariosActivos",
    title: "Usuarios activos",
    description: "Cuentas habilitadas",
    icon: UserCheck,
    accent: "text-emerald-500",
  },
  {
    key: "totalCampanas",
    title: "Total de campañas",
    description: "Campañas creadas",
    icon: Megaphone,
    accent: "text-violet-500",
  },
  {
    key: "totalRecursos",
    title: "Total de recursos",
    description: "Recursos disponibles",
    icon: FolderOpen,
    accent: "text-blue-500",
  },
  {
    key: "publicacionesEnviadas",
    title: "Publicaciones enviadas",
    description: "Publicaciones completadas",
    icon: Send,
    accent: "text-sky-500",
  },
  {
    key: "totalGeneracionesIa",
    title: "Generaciones IA realizadas",
    description: "Solicitudes procesadas",
    icon: Sparkles,
    accent: "text-fuchsia-500",
  },
]

function formatNumber(value: number) {
  return new Intl.NumberFormat("es-PE").format(value)
}

export function AdminDashboardPage() {
  const { admin } = useAdminAuth()
  const [resumen, setResumen] = useState<DashboardResumen | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  const loadResumen = useCallback(async () => {
    setLoading(true)
    setError("")

    try {
      const data = await obtenerDashboardResumen()
      setResumen(data)
    } catch (dashboardError) {
      setResumen(null)
      setError(
        getAdminApiErrorMessage(
          dashboardError,
          "No se pudo cargar el resumen administrativo."
        )
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void loadResumen()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadResumen])

  return (
    <div className="space-y-5">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="space-y-2">
          <Badge className="w-fit" variant="secondary">
            <BadgeCheck />
            Administrador
          </Badge>
          <div>
            <h2 className="text-2xl font-semibold tracking-normal sm:text-3xl">
              Bienvenido, {admin?.username}
            </h2>
            <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
              Supervisa el estado general de VisionAstra con información real
              del sistema.
            </p>
          </div>
        </div>
      </section>

      {error ? (
        <Alert variant="destructive">
          <AlertCircle />
          <AlertTitle>No se pudo cargar el dashboard</AlertTitle>
          <AlertDescription className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <span>{error}</span>
            <Button
              className="w-fit"
              onClick={() => void loadResumen()}
              size="sm"
              type="button"
              variant="outline"
            >
              <RefreshCw />
              Reintentar
            </Button>
          </AlertDescription>
        </Alert>
      ) : null}

      <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {METRICS.map((metric) => {
          const Icon = metric.icon
          const value = resumen ? resumen[metric.key] : null

          return (
            <Card className="rounded-lg" key={metric.key}>
              <CardHeader className="flex flex-row items-start justify-between gap-3 pb-2">
                <div>
                  <CardTitle className="text-sm">{metric.title}</CardTitle>
                  <CardDescription>{metric.description}</CardDescription>
                </div>
                <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-muted">
                  <Icon className={`size-4 ${metric.accent}`} />
                </div>
              </CardHeader>
              <CardContent>
                {loading ? (
                  <Skeleton className="h-8 w-24" />
                ) : value === null ? (
                  <p className="text-sm text-muted-foreground">
                    Sin información
                  </p>
                ) : (
                  <p className="text-3xl font-semibold tracking-tight">
                    {formatNumber(value)}
                  </p>
                )}
              </CardContent>
            </Card>
          )
        })}
      </section>
    </div>
  )
}
