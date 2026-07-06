import {
  AlertCircle,
  Ban,
  CheckCircle2,
  Eye,
  MoreHorizontal,
  RefreshCw,
  Search,
  Users,
  X,
} from "lucide-react"
import { type FormEvent, useCallback, useEffect, useRef, useState } from "react"
import { toast } from "sonner"

import {
  UserActionConfirmDialog,
  type UserActionType,
} from "@/components/users/UserActionConfirmDialog"
import { UserDetailDialog } from "@/components/users/UserDetailDialog"
import { UserStatusBadge } from "@/components/users/UserStatusBadge"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
import { getAdminApiErrorMessage } from "@/lib/adminApiError"
import {
  activarUsuario,
  bloquearUsuario,
  obtenerUsuario,
  obtenerUsuarios,
} from "@/services/adminUsersService"
import type {
  UsuarioAdmin,
  UsuarioEstado,
  UsuariosPaginados,
} from "@/types/adminUsers"

const PAGE_SIZE = 20

const DATE_FORMATTER = new Intl.DateTimeFormat("es-PE", {
  dateStyle: "medium",
  timeStyle: "short",
})

function formatDateTime(value: string | null, emptyLabel = "Nunca") {
  if (!value) {
    return emptyLabel
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return emptyLabel
  }

  return DATE_FORMATTER.format(date)
}

function getFullName(user: UsuarioAdmin) {
  return `${user.nombres} ${user.apellidos}`.trim()
}

function getInitials(user: UsuarioAdmin) {
  const names = [user.nombres, user.apellidos]
    .join(" ")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((value) => value[0]?.toUpperCase())

  return names.join("") || "US"
}

function shouldBlockUser(user: UsuarioAdmin) {
  return user.estado === "activo" || user.estado === "pendiente"
}

function updateUserInResults(
  currentUsers: UsuariosPaginados | null,
  updatedUser: UsuarioAdmin
) {
  if (!currentUsers) {
    return currentUsers
  }

  return {
    ...currentUsers,
    results: currentUsers.results.map((user) =>
      user.idUsuario === updatedUser.idUsuario ? updatedUser : user
    ),
  }
}

type UserActionsProps = {
  user: UsuarioAdmin
  disabled: boolean
  onViewDetail: (user: UsuarioAdmin) => void
  onRequestAction: (user: UsuarioAdmin, action: UserActionType) => void
}

function UserActions({
  user,
  disabled,
  onViewDetail,
  onRequestAction,
}: UserActionsProps) {
  const action = shouldBlockUser(user) ? "bloquear" : "activar"
  const isBlocking = action === "bloquear"

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        aria-label={`Abrir acciones de ${getFullName(user)}`}
        disabled={disabled}
        render={
          <Button size="icon" type="button" variant="ghost">
            <MoreHorizontal className="size-4" />
          </Button>
        }
      />
      <DropdownMenuContent align="end" className="w-44">
        <DropdownMenuItem onClick={() => onViewDetail(user)}>
          <Eye className="size-4" />
          Ver detalle
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={() => onRequestAction(user, action)}
          variant={isBlocking ? "destructive" : "default"}
        >
          {isBlocking ? (
            <Ban className="size-4" />
          ) : (
            <CheckCircle2 className="size-4" />
          )}
          {isBlocking ? "Bloquear" : "Activar"}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

type UserIdentityProps = {
  user: UsuarioAdmin
}

function UserIdentity({ user }: UserIdentityProps) {
  return (
    <div className="flex min-w-0 items-center gap-3">
      <Avatar className="size-9 border">
        <AvatarFallback className="text-xs font-semibold">
          {getInitials(user)}
        </AvatarFallback>
      </Avatar>
      <div className="min-w-0">
        <p className="truncate text-sm font-medium">{getFullName(user)}</p>
        <p className="text-xs text-muted-foreground">ID {user.idUsuario}</p>
      </div>
    </div>
  )
}

export function AdminUsersPage() {
  const usersTopRef = useRef<HTMLDivElement | null>(null)
  const [users, setUsers] = useState<UsuariosPaginados | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [searchInput, setSearchInput] = useState("")
  const [appliedSearch, setAppliedSearch] = useState("")
  const [estado, setEstado] = useState<UsuarioEstado | "">("")
  const [page, setPage] = useState(1)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailUser, setDetailUser] = useState<UsuarioAdmin | null>(null)
  const [selectedUser, setSelectedUser] = useState<UsuarioAdmin | null>(null)
  const [pendingAction, setPendingAction] = useState<UserActionType | null>(null)
  const [actionLoading, setActionLoading] = useState(false)

  const totalPages = Math.max(1, Math.ceil((users?.count ?? 0) / PAGE_SIZE))
  const hasAppliedSearch = Boolean(appliedSearch)
  const hasFilters = hasAppliedSearch || Boolean(estado)

  const loadUsers = useCallback(async () => {
    setLoading(true)
    setError("")

    try {
      const data = await obtenerUsuarios({
        search: appliedSearch,
        estado,
        page,
      })
      setUsers(data)
    } catch (usersError) {
      setUsers(null)
      setError(
        getAdminApiErrorMessage(
          usersError,
          "No se pudo cargar la lista de usuarios."
        )
      )
    } finally {
      setLoading(false)
    }
  }, [appliedSearch, estado, page])

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void loadUsers()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadUsers])

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAppliedSearch(searchInput.trim())
    setPage(1)
  }

  function handleClearSearch() {
    setSearchInput("")
    setAppliedSearch("")
    setPage(1)
  }

  function handleEstadoChange(value: string) {
    setEstado(value as UsuarioEstado | "")
    setPage(1)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
    usersTopRef.current?.scrollIntoView({ behavior: "smooth", block: "start" })
  }

  async function handleViewDetail(user: UsuarioAdmin) {
    setDetailOpen(true)
    setDetailLoading(true)
    setDetailUser(null)

    try {
      const data = await obtenerUsuario(user.idUsuario)
      setDetailUser(data)
    } catch (detailError) {
      toast.error(
        getAdminApiErrorMessage(
          detailError,
          "No se pudo cargar el detalle del usuario."
        )
      )
      setDetailOpen(false)
    } finally {
      setDetailLoading(false)
    }
  }

  function handleRequestAction(user: UsuarioAdmin, action: UserActionType) {
    setSelectedUser(user)
    setPendingAction(action)
  }

  function closeActionDialog() {
    if (!actionLoading) {
      setPendingAction(null)
      setSelectedUser(null)
    }
  }

  async function handleConfirmAction() {
    if (!selectedUser || !pendingAction || actionLoading) {
      return
    }

    setActionLoading(true)

    try {
      const response =
        pendingAction === "bloquear"
          ? await bloquearUsuario(selectedUser.idUsuario)
          : await activarUsuario(selectedUser.idUsuario)

      setUsers((currentUsers) =>
        updateUserInResults(currentUsers, response.usuario)
      )

      setDetailUser((currentDetailUser) =>
        currentDetailUser?.idUsuario === response.usuario.idUsuario
          ? response.usuario
          : currentDetailUser
      )

      toast.success(response.mensaje)
      setPendingAction(null)
      setSelectedUser(null)
    } catch (actionError) {
      toast.error(
        getAdminApiErrorMessage(
          actionError,
          "No se pudo completar la operación."
        )
      )
    } finally {
      setActionLoading(false)
    }
  }

  const emptyMessage = hasFilters
    ? "No se encontraron usuarios con los filtros seleccionados."
    : "No hay usuarios registrados."

  return (
    <div className="space-y-5" ref={usersTopRef}>
      <section className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Badge className="w-fit" variant="secondary">
              <Users />
              Usuarios
            </Badge>
          </div>
          <h2 className="mt-2 text-2xl font-semibold tracking-normal sm:text-3xl">
            Usuarios
          </h2>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
            Consulta, filtra y administra el estado de los usuarios registrados.
          </p>
        </div>
        <div className="rounded-lg border bg-card px-3 py-2 text-sm shadow-sm">
          <span className="text-muted-foreground">Total: </span>
          <span className="font-semibold">{users?.count ?? 0}</span>
        </div>
      </section>

      <Card className="rounded-lg">
        <CardHeader>
          <CardTitle className="text-base">Filtros</CardTitle>
          <CardDescription>
            Busca por nombre, apellido o correo y filtra por estado.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 lg:grid-cols-[1fr_220px]">
            <form className="flex flex-col gap-2 sm:flex-row" onSubmit={handleSearchSubmit}>
              <div className="min-w-0 flex-1 space-y-2">
                <Label htmlFor="user-search">Buscar usuarios</Label>
                <div className="relative">
                  <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    className="h-10 pl-9"
                    id="user-search"
                    onChange={(event) => setSearchInput(event.target.value)}
                    placeholder="Buscar por nombre, apellido o correo"
                    value={searchInput}
                  />
                </div>
              </div>
              <div className="flex items-end gap-2">
                <Button className="h-10" type="submit">
                  Buscar
                </Button>
                {hasAppliedSearch ? (
                  <Button
                    aria-label="Limpiar búsqueda"
                    className="h-10"
                    onClick={handleClearSearch}
                    type="button"
                    variant="outline"
                  >
                    <X className="size-4" />
                    Limpiar
                  </Button>
                ) : null}
              </div>
            </form>

            <div className="space-y-2">
              <Label htmlFor="user-status-filter">Estado</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                id="user-status-filter"
                onChange={(event) => handleEstadoChange(event.target.value)}
                value={estado}
              >
                <option value="">Todos los estados</option>
                <option value="activo">Activo</option>
                <option value="bloqueado">Bloqueado</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {error ? (
        <Alert variant="destructive">
          <AlertCircle />
          <AlertTitle>No se pudo cargar usuarios</AlertTitle>
          <AlertDescription className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <span>{error}</span>
            <Button
              className="w-fit"
              onClick={() => void loadUsers()}
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

      <Card className="rounded-lg">
        <CardHeader className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <CardTitle className="text-base">Listado de usuarios</CardTitle>
            <CardDescription>
              Página {page} de {totalPages} · {users?.count ?? 0} usuarios
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <div className="hidden overflow-x-auto md:block">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="border-b text-xs uppercase tracking-wide text-muted-foreground">
                <tr>
                  <th className="px-3 py-3 font-medium">Usuario</th>
                  <th className="px-3 py-3 font-medium">Correo</th>
                  <th className="px-3 py-3 font-medium">Rol</th>
                  <th className="px-3 py-3 font-medium">Estado</th>
                  <th className="px-3 py-3 font-medium">Registro</th>
                  <th className="px-3 py-3 text-right font-medium">
                    Acciones
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading
                  ? Array.from({ length: 6 }).map((_, index) => (
                      <tr className="border-b" key={index}>
                        {Array.from({ length: 6 }).map((__, cellIndex) => (
                          <td className="px-3 py-4" key={cellIndex}>
                            <Skeleton className="h-5 w-full" />
                          </td>
                        ))}
                      </tr>
                    ))
                  : users?.results.map((user) => (
                      <tr
                        className="border-b transition-colors hover:bg-muted/50"
                        key={user.idUsuario}
                      >
                        <td className="px-3 py-4">
                          <UserIdentity user={user} />
                        </td>
                        <td className="max-w-[220px] px-3 py-4">
                          <span className="block truncate">{user.email}</span>
                        </td>
                        <td className="px-3 py-4">{user.rol.nombre}</td>
                        <td className="px-3 py-4">
                          <UserStatusBadge estado={user.estado} />
                        </td>
                        <td className="px-3 py-4">
                          {formatDateTime(user.fechaCreacion, "Sin fecha")}
                        </td>
                        <td className="px-3 py-4 text-right">
                          <UserActions
                            disabled={actionLoading}
                            onRequestAction={handleRequestAction}
                            onViewDetail={(selected) => {
                              void handleViewDetail(selected)
                            }}
                            user={user}
                          />
                        </td>
                      </tr>
                    ))}
              </tbody>
            </table>
          </div>

          <div className="space-y-3 md:hidden">
            {loading
              ? Array.from({ length: 4 }).map((_, index) => (
                  <div className="rounded-lg border p-4" key={index}>
                    <Skeleton className="h-5 w-40" />
                    <Skeleton className="mt-3 h-4 w-full" />
                    <Skeleton className="mt-3 h-9 w-full" />
                  </div>
                ))
              : users?.results.map((user) => (
                  <article className="rounded-lg border p-4" key={user.idUsuario}>
                    <div className="flex items-start justify-between gap-3">
                      <UserIdentity user={user} />
                      <UserStatusBadge estado={user.estado} />
                    </div>
                    <dl className="mt-4 grid gap-2 text-sm">
                      <div>
                        <dt className="text-xs text-muted-foreground">Correo</dt>
                        <dd className="break-all font-medium">{user.email}</dd>
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <dt className="text-xs text-muted-foreground">Rol</dt>
                          <dd className="font-medium">{user.rol.nombre}</dd>
                        </div>
                        <div>
                          <dt className="text-xs text-muted-foreground">
                            Registro
                          </dt>
                          <dd className="font-medium">
                            {formatDateTime(user.fechaCreacion, "Sin fecha")}
                          </dd>
                        </div>
                      </div>
                    </dl>
                    <div className="mt-4 flex justify-end">
                      <UserActions
                        disabled={actionLoading}
                        onRequestAction={handleRequestAction}
                        onViewDetail={(selected) => {
                          void handleViewDetail(selected)
                        }}
                        user={user}
                      />
                    </div>
                  </article>
                ))}
          </div>

          {!loading && users?.results.length === 0 ? (
            <div className="rounded-lg border border-dashed p-8 text-center">
              <p className="font-medium">{emptyMessage}</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Ajusta la búsqueda o el filtro para volver a consultar.
              </p>
            </div>
          ) : null}

          <div className="mt-5 flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-muted-foreground">
              {users?.count ?? 0} usuarios en total
            </p>
            <div className="flex items-center justify-between gap-2 sm:justify-end">
              <Button
                disabled={loading || page <= 1}
                onClick={() => handlePageChange(page - 1)}
                type="button"
                variant="outline"
              >
                Anterior
              </Button>
              <span className="min-w-28 text-center text-sm font-medium">
                Página {page} de {totalPages}
              </span>
              <Button
                disabled={loading || page >= totalPages}
                onClick={() => handlePageChange(page + 1)}
                type="button"
                variant="outline"
              >
                Siguiente
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <UserDetailDialog
        formatDateTime={formatDateTime}
        loading={detailLoading}
        onClose={() => setDetailOpen(false)}
        open={detailOpen}
        user={detailUser}
      />

      <UserActionConfirmDialog
        action={pendingAction}
        isProcessing={actionLoading}
        onClose={closeActionDialog}
        onConfirm={() => {
          void handleConfirmAction()
        }}
        userName={selectedUser ? getFullName(selectedUser) : ""}
      />
    </div>
  )
}
