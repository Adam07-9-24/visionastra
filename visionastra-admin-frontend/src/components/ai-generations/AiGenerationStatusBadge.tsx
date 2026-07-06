import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import type { GeneracionIAEstado } from "@/types/adminAiGenerations"

const STATUS_LABELS: Record<GeneracionIAEstado, string> = {
  pendiente: "Pendiente",
  procesando: "Procesando",
  completado: "Completado",
  error: "Error",
}

const STATUS_CLASSES: Record<GeneracionIAEstado, string> = {
  pendiente:
    "border-amber-500/25 bg-amber-500/10 text-amber-700 dark:text-amber-300",
  procesando:
    "border-sky-500/20 bg-sky-500/10 text-sky-700 dark:text-sky-300",
  completado:
    "border-emerald-500/20 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  error: "",
}

type AiGenerationStatusBadgeProps = {
  estado: GeneracionIAEstado
}

export function AiGenerationStatusBadge({
  estado,
}: AiGenerationStatusBadgeProps) {
  if (estado === "error") {
    return <Badge variant="destructive">{STATUS_LABELS[estado]}</Badge>
  }

  return (
    <Badge className={cn("capitalize", STATUS_CLASSES[estado])} variant="outline">
      {STATUS_LABELS[estado]}
    </Badge>
  )
}
