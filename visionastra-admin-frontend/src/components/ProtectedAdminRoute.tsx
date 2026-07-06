import { Navigate, Outlet } from "react-router-dom"

import { Skeleton } from "@/components/ui/skeleton"
import { useAdminAuth } from "@/contexts/AdminAuthContext"

export function ProtectedAdminRoute() {
  const { admin, checkingSession } = useAdminAuth()

  if (checkingSession) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-background px-6">
        <div className="w-full max-w-sm space-y-4 text-center">
          <div className="mx-auto flex size-12 items-center justify-center rounded-xl border bg-card shadow-sm">
            <Skeleton className="size-6 rounded-full" />
          </div>
          <div className="space-y-2">
            <Skeleton className="mx-auto h-4 w-56" />
            <Skeleton className="mx-auto h-3 w-40" />
          </div>
          <p className="text-sm text-muted-foreground">
            Verificando sesión administrativa...
          </p>
        </div>
      </main>
    )
  }

  if (!admin) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}
