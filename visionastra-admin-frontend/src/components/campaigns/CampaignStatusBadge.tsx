import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import type { CampanaEstado } from "@/types/adminCampaigns"

const STATUS_LABELS: Record<CampanaEstado, string> = {
  borrador: "Borrador",
  activa: "Activa",
  pausada: "Pausada",
  finalizada: "Finalizada",
}

const STATUS_CLASSES: Record<CampanaEstado, string> = {
  borrador: "border-border bg-background text-muted-foreground",
  activa:
    "border-emerald-500/20 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  pausada:
    "border-amber-500/25 bg-amber-500/10 text-amber-700 dark:text-amber-300",
  finalizada: "bg-secondary text-secondary-foreground",
}

type CampaignStatusBadgeProps = {
  estado: CampanaEstado
}

export function CampaignStatusBadge({ estado }: CampaignStatusBadgeProps) {
  return (
    <Badge
      className={cn("capitalize", STATUS_CLASSES[estado])}
      variant={estado === "borrador" ? "outline" : "secondary"}
    >
      {STATUS_LABELS[estado]}
    </Badge>
  )
}
