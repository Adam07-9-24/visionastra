import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import type { UsuarioEstado } from "@/types/adminUsers"

const STATUS_LABELS: Record<UsuarioEstado, string> = {
  activo: "Activo",
  bloqueado: "Bloqueado",
  pendiente: "Pendiente",
}

const STATUS_CLASSES: Record<UsuarioEstado, string> = {
  activo:
    "border-emerald-500/20 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  bloqueado: "",
  pendiente:
    "border-amber-500/25 bg-amber-500/10 text-amber-700 dark:text-amber-300",
}

type UserStatusBadgeProps = {
  estado: UsuarioEstado
}

export function UserStatusBadge({ estado }: UserStatusBadgeProps) {
  if (estado === "bloqueado") {
    return <Badge variant="destructive">{STATUS_LABELS[estado]}</Badge>
  }

  return (
    <Badge
      className={cn("capitalize", STATUS_CLASSES[estado])}
      variant={estado === "pendiente" ? "outline" : "secondary"}
    >
      {STATUS_LABELS[estado]}
    </Badge>
  )
}
