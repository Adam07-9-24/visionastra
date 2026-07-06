import { Loader2 } from "lucide-react"
import { useEffect } from "react"

import { Button } from "@/components/ui/button"

export type UserActionType = "bloquear" | "activar"

type UserActionConfirmDialogProps = {
  action: UserActionType | null
  userName: string
  isProcessing: boolean
  onClose: () => void
  onConfirm: () => void
}

const ACTION_CONTENT: Record<
  UserActionType,
  { title: string; description: string; confirmLabel: string }
> = {
  bloquear: {
    title: "Bloquear usuario",
    description:
      "¿Seguro que deseas bloquear a este usuario? Se cerrarán todas sus sesiones activas.",
    confirmLabel: "Bloquear usuario",
  },
  activar: {
    title: "Activar usuario",
    description:
      "¿Seguro que deseas activar a este usuario? Tendrá que iniciar sesión nuevamente.",
    confirmLabel: "Activar usuario",
  },
}

export function UserActionConfirmDialog({
  action,
  userName,
  isProcessing,
  onClose,
  onConfirm,
}: UserActionConfirmDialogProps) {
  useEffect(() => {
    if (!action) {
      return undefined
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !isProcessing) {
        onClose()
      }
    }

    document.addEventListener("keydown", handleKeyDown)

    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [action, isProcessing, onClose])

  if (!action) {
    return null
  }

  const content = ACTION_CONTENT[action]
  const titleId = "user-action-confirm-title"
  const descriptionId = "user-action-confirm-description"
  const isBlocking = action === "bloquear"

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 px-4 backdrop-blur-sm"
      onMouseDown={() => {
        if (!isProcessing) {
          onClose()
        }
      }}
    >
      <section
        aria-describedby={descriptionId}
        aria-labelledby={titleId}
        aria-modal="true"
        className="w-full max-w-md rounded-xl border bg-card p-5 text-card-foreground shadow-xl"
        onMouseDown={(event) => event.stopPropagation()}
        role="alertdialog"
      >
        <div className="space-y-2">
          <h2 className="text-lg font-semibold" id={titleId}>
            {content.title}
          </h2>
          <p className="text-sm text-muted-foreground" id={descriptionId}>
            {content.description}
          </p>
          <p className="text-sm font-medium">{userName}</p>
        </div>

        <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <Button
            disabled={isProcessing}
            onClick={onClose}
            type="button"
            variant="outline"
          >
            Cancelar
          </Button>
          <Button
            disabled={isProcessing}
            onClick={onConfirm}
            type="button"
            variant={isBlocking ? "destructive" : "default"}
          >
            {isProcessing ? (
              <>
                <Loader2 className="animate-spin" />
                Procesando...
              </>
            ) : (
              content.confirmLabel
            )}
          </Button>
        </div>
      </section>
    </div>
  )
}
