import { X } from "lucide-react"
import { useEffect } from "react"

import { UserStatusBadge } from "@/components/users/UserStatusBadge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import type { UsuarioAdmin } from "@/types/adminUsers"

type UserDetailDialogProps = {
  open: boolean
  user: UsuarioAdmin | null
  loading: boolean
  onClose: () => void
  formatDateTime: (value: string | null, emptyLabel?: string) => string
}

function fullName(user: UsuarioAdmin) {
  return `${user.nombres} ${user.apellidos}`.trim()
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

export function UserDetailDialog({
  open,
  user,
  loading,
  onClose,
  formatDateTime,
}: UserDetailDialogProps) {
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

  const titleId = "user-detail-title"

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 px-4 py-6 backdrop-blur-sm"
      onMouseDown={onClose}
    >
      <section
        aria-labelledby={titleId}
        aria-modal="true"
        className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-xl border bg-card text-card-foreground shadow-xl"
        onMouseDown={(event) => event.stopPropagation()}
        role="dialog"
      >
        <header className="flex items-start justify-between gap-4 border-b p-5">
          <div>
            <h2 className="text-lg font-semibold" id={titleId}>
              Detalle del usuario
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Información administrativa del usuario seleccionado.
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
              <Skeleton className="h-6 w-48" />
              <div className="grid gap-3 sm:grid-cols-2">
                {Array.from({ length: 8 }).map((_, index) => (
                  <Skeleton className="h-16 rounded-lg" key={index} />
                ))}
              </div>
            </div>
          ) : user ? (
            <div className="space-y-5">
              <div className="flex flex-col gap-3 rounded-lg border bg-background/60 p-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-base font-semibold">{fullName(user)}</p>
                  <p className="break-all text-sm text-muted-foreground">
                    {user.email}
                  </p>
                </div>
                <UserStatusBadge estado={user.estado} />
              </div>

              <dl className="grid gap-3 sm:grid-cols-2">
                <DetailItem label="Nombres" value={user.nombres} />
                <DetailItem label="Apellidos" value={user.apellidos} />
                <DetailItem label="Correo" value={user.email} />
                <DetailItem label="Rol" value={user.rol.nombre} />
                <DetailItem label="Estado" value={user.estado} />
                <DetailItem
                  label="Fecha de registro"
                  value={formatDateTime(user.fechaCreacion, "Sin fecha")}
                />
                <DetailItem
                  label="Última actualización"
                  value={formatDateTime(user.fechaActualizacion, "Sin fecha")}
                />
                <DetailItem
                  label="Último acceso"
                  value={formatDateTime(user.ultimoLogin, "Nunca")}
                />
              </dl>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              No se pudo cargar el detalle del usuario.
            </p>
          )}
        </div>
      </section>
    </div>
  )
}
