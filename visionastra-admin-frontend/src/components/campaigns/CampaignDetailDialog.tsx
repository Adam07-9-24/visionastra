import { X } from "lucide-react"
import { useEffect } from "react"

import { CampaignStatusBadge } from "@/components/campaigns/CampaignStatusBadge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import type { CampanaDetalle } from "@/types/adminCampaigns"

type CampaignDetailDialogProps = {
  open: boolean
  campaign: CampanaDetalle | null
  loading: boolean
  onClose: () => void
}

const DATE_FORMATTER = new Intl.DateTimeFormat("es-PE", {
  dateStyle: "medium",
  timeStyle: "short",
})

const NUMBER_FORMATTER = new Intl.NumberFormat("es-PE", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

function formatDateTime(value: string | null) {
  if (!value) {
    return "Sin fecha"
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return "Sin fecha"
  }

  return DATE_FORMATTER.format(date)
}

function formatBudget(value: string | null) {
  if (!value) {
    return "No especificado"
  }

  const parsedValue = Number(value)

  if (Number.isNaN(parsedValue)) {
    return value.trim() || "No especificado"
  }

  return NUMBER_FORMATTER.format(parsedValue)
}

function ownerName(campaign: CampanaDetalle) {
  return `${campaign.propietario.nombres} ${campaign.propietario.apellidos}`.trim()
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border bg-background/60 p-3">
      <dt className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
        {label}
      </dt>
      <dd className="mt-1 break-words text-sm font-medium">{value}</dd>
    </div>
  )
}

export function CampaignDetailDialog({
  open,
  campaign,
  loading,
  onClose,
}: CampaignDetailDialogProps) {
  useEffect(() => {
    if (!open) {
      return undefined
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose()
      }
    }

    document.addEventListener("keydown", handleKeyDown)

    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [open, onClose])

  if (!open) {
    return null
  }

  const titleId = "campaign-detail-title"

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 px-4 py-6 backdrop-blur-sm"
      onMouseDown={onClose}
    >
      <section
        aria-labelledby={titleId}
        aria-modal="true"
        className="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-xl border bg-card text-card-foreground shadow-xl"
        onMouseDown={(event) => event.stopPropagation()}
        role="dialog"
      >
        <header className="flex items-start justify-between gap-4 border-b p-5">
          <div>
            <h2 className="text-lg font-semibold" id={titleId}>
              Detalle de campaña
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Información administrativa de la campaña seleccionada.
            </p>
          </div>
          <Button
            aria-label="Cerrar detalle"
            onClick={onClose}
            size="icon"
            type="button"
            variant="ghost"
          >
            <X className="size-4" />
          </Button>
        </header>

        <div className="p-5">
          {loading ? (
            <div className="space-y-4">
              <Skeleton className="h-6 w-56" />
              <div className="grid gap-3 sm:grid-cols-2">
                {Array.from({ length: 12 }).map((_, index) => (
                  <Skeleton className="h-16 rounded-lg" key={index} />
                ))}
              </div>
            </div>
          ) : campaign ? (
            <div className="space-y-5">
              <div className="flex flex-col gap-3 rounded-lg border bg-background/60 p-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-base font-semibold">{campaign.nombre}</p>
                  <p className="break-all text-sm text-muted-foreground">
                    {ownerName(campaign)} · {campaign.propietario.email}
                  </p>
                </div>
                <CampaignStatusBadge estado={campaign.estado} />
              </div>

              <dl className="grid gap-3 sm:grid-cols-2">
                <DetailItem label="Nombre" value={campaign.nombre} />
                <DetailItem label="Propietario" value={ownerName(campaign)} />
                <DetailItem
                  label="Correo del propietario"
                  value={campaign.propietario.email}
                />
                <DetailItem
                  label="Objetivo"
                  value={campaign.objetivo || "No especificado"}
                />
                <DetailItem
                  label="Descripción"
                  value={campaign.descripcion || "Sin descripción"}
                />
                <DetailItem
                  label="Presupuesto"
                  value={formatBudget(campaign.presupuesto)}
                />
                <DetailItem label="Estado" value={campaign.estado} />
                <DetailItem
                  label="Fecha de inicio"
                  value={formatDateTime(campaign.fechaInicio)}
                />
                <DetailItem
                  label="Fecha de fin"
                  value={formatDateTime(campaign.fechaFin)}
                />
                <DetailItem
                  label="Fecha de creación"
                  value={formatDateTime(campaign.fechaCreacion)}
                />
                <DetailItem
                  label="Última actualización"
                  value={formatDateTime(campaign.fechaActualizacion)}
                />
                <DetailItem
                  label="Total de recursos"
                  value={campaign.totalRecursos.toLocaleString("es-PE")}
                />
                <DetailItem
                  label="Total de publicaciones"
                  value={campaign.totalPublicaciones.toLocaleString("es-PE")}
                />
              </dl>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              No se pudo cargar el detalle de la campaña.
            </p>
          )}
        </div>
      </section>
    </div>
  )
}
