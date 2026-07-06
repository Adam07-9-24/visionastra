import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import type { PublicacionEstado } from "@/types/adminPublications"

const STATUS_LABELS: Record<PublicacionEstado, string> = {
  borrador: "Borrador",
  lista: "Lista",
  programada: "Programada",
  enviada: "Publicada",
  publicada: "Publicada",
  error: "Error",
  cancelada: "Cancelada",
}

const STATUS_CLASSES: Record<PublicacionEstado, string> = {
  borrador: "border-border bg-background text-muted-foreground",
  lista: "border-sky-500/20 bg-sky-500/10 text-sky-700 dark:text-sky-300",
  programada:
    "border-violet-500/20 bg-violet-500/10 text-violet-700 dark:text-violet-300",
  enviada:
    "border-emerald-500/20 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  publicada:
    "border-emerald-500/20 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  error: "",
  cancelada: "bg-secondary text-secondary-foreground",
}

type PublicationStatusBadgeProps = {
  estado: PublicacionEstado
}

export function PublicationStatusBadge({
  estado,
}: PublicationStatusBadgeProps) {
  if (estado === "error") {
    return <Badge variant="destructive">{STATUS_LABELS[estado]}</Badge>
  }

  return (
    <Badge
      className={cn("capitalize", STATUS_CLASSES[estado])}
      variant={estado === "borrador" ? "outline" : "secondary"}
    >
      {STATUS_LABELS[estado]}
    </Badge>
  )
}
