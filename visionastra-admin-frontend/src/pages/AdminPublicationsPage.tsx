import { AlertCircle, RefreshCw, Search, Send, X } from "lucide-react"
import { type FormEvent, useCallback, useEffect, useRef, useState } from "react"

import { PublicationStatusBadge } from "@/components/publications/PublicationStatusBadge"
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
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
import { getAdminApiErrorMessage } from "@/lib/adminApiError"
import { getPublicationErrorMessage } from "@/lib/publicationErrorMessage"
import {
  obtenerPublicaciones,
  obtenerPublicacionesUsuarios,
} from "@/services/adminPublicationsService"
import type {
  PublicacionAdmin,
  PublicacionEstado,
  PublicacionUsuarioFiltro,
  PublicacionesPaginadas,
} from "@/types/adminPublications"

const PAGE_SIZE = 20

const DATE_FORMATTER = new Intl.DateTimeFormat("es-PE", {
  dateStyle: "medium",
  timeStyle: "short",
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

function userName(publication: PublicacionAdmin) {
  return `${publication.usuario.nombres} ${publication.usuario.apellidos}`.trim()
}

function userFilterName(user: PublicacionUsuarioFiltro) {
  return `${user.nombres} ${user.apellidos}`.trim()
}

function errorMessage(publication: PublicacionAdmin) {
  if (publication.estado !== "error") {
    return ""
  }

  return getPublicationErrorMessage(publication.mensajeError) ?? ""
}

type PublicationStatusCellProps = {
  publication: PublicacionAdmin
}

function PublicationStatusCell({ publication }: PublicationStatusCellProps) {
  const message = errorMessage(publication)

  return (
    <div className="space-y-1.5">
      <PublicationStatusBadge estado={publication.estado} />
      {message ? (
        <p className="line-clamp-3 max-w-xs text-xs leading-5 text-destructive">
          {message}
        </p>
      ) : null}
    </div>
  )
}

export function AdminPublicationsPage() {
  const publicationsTopRef = useRef<HTMLDivElement | null>(null)
  const [publicaciones, setPublicaciones] =
    useState<PublicacionesPaginadas | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [searchInput, setSearchInput] = useState("")
  const [appliedSearch, setAppliedSearch] = useState("")
  const [estadoFiltro, setEstadoFiltro] = useState<PublicacionEstado | "">("")
  const [usuarios, setUsuarios] = useState<PublicacionUsuarioFiltro[]>([])
  const [usuariosLoading, setUsuariosLoading] = useState(true)
  const [usuariosError, setUsuariosError] = useState("")
  const [usuarioFiltro, setUsuarioFiltro] = useState<number | "">("")
  const [page, setPage] = useState(1)

  const totalPages = Math.max(
    1,
    Math.ceil((publicaciones?.count ?? 0) / PAGE_SIZE)
  )
  const hasAppliedSearch = Boolean(appliedSearch)
  const hasFilters =
    hasAppliedSearch || Boolean(estadoFiltro) || Boolean(usuarioFiltro)
  const hasClearableFilters = Boolean(searchInput.trim()) || hasFilters
  const selectedUsuario =
    typeof usuarioFiltro === "number"
      ? usuarios.find((user) => user.idUsuario === usuarioFiltro)
      : undefined
  const selectedUsuarioName = selectedUsuario
    ? userFilterName(selectedUsuario)
    : ""
  const publicationCount = publicaciones?.count ?? 0
  const publicationCountLabel =
    publicationCount === 1
      ? "1 publicación"
      : `${publicationCount} publicaciones`
  const publicationSummary = selectedUsuarioName
    ? `${publicationCountLabel} de ${selectedUsuarioName}`
    : publicationCountLabel

  const loadPublications = useCallback(async () => {
    setLoading(true)
    setError("")

    try {
      const data = await obtenerPublicaciones({
        search: appliedSearch,
        estado: estadoFiltro,
        usuario: usuarioFiltro,
        page,
      })
      setPublicaciones(data)
    } catch (publicationsError) {
      setPublicaciones(null)
      setError(
        getAdminApiErrorMessage(
          publicationsError,
          "No se pudieron cargar las publicaciones."
        )
      )
    } finally {
      setLoading(false)
    }
  }, [appliedSearch, estadoFiltro, page, usuarioFiltro])

  const loadUsuarios = useCallback(async () => {
    setUsuariosLoading(true)
    setUsuariosError("")

    try {
      const data = await obtenerPublicacionesUsuarios()
      setUsuarios(data)
    } catch (usersError) {
      setUsuarios([])
      setUsuariosError(
        getAdminApiErrorMessage(
          usersError,
          "No se pudieron cargar los usuarios."
        )
      )
    } finally {
      setUsuariosLoading(false)
    }
  }, [])

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void loadPublications()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadPublications])

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void loadUsuarios()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadUsuarios])

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAppliedSearch(searchInput.trim())
    setPage(1)
  }

  function handleClearSearch() {
    setSearchInput("")
    setAppliedSearch("")
    setEstadoFiltro("")
    setUsuarioFiltro("")
    setPage(1)
  }

  function handleEstadoChange(value: string) {
    setEstadoFiltro(value as PublicacionEstado | "")
    setPage(1)
  }

  function handleUsuarioChange(value: string) {
    setUsuarioFiltro(value ? Number(value) : "")
    setPage(1)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
    publicationsTopRef.current?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    })
  }

  const emptyMessage = hasFilters
    ? "No se encontraron publicaciones con los filtros seleccionados."
    : "No hay publicaciones registradas."

  return (
    <div className="space-y-5" ref={publicationsTopRef}>
      <section className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Badge className="w-fit" variant="secondary">
              <Send />
              Publicaciones
            </Badge>
          </div>
          <h2 className="mt-2 text-2xl font-semibold tracking-normal sm:text-3xl">
            Publicaciones
          </h2>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
            Consulta el estado de las publicaciones generadas por los usuarios.
          </p>
        </div>
        <div className="rounded-lg border bg-card px-3 py-2 text-sm shadow-sm">
          <span className="text-muted-foreground">Total: </span>
          <span className="font-semibold">{publicationCount}</span>
        </div>
      </section>

      <Card className="rounded-lg">
        <CardHeader>
          <CardTitle className="text-base">Filtros</CardTitle>
          <CardDescription>
            Busca por título, campaña, usuario o correo y filtra por estado o
            usuario.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px_260px]">
            <form
              className="flex flex-col gap-2 sm:flex-row"
              onSubmit={handleSearchSubmit}
            >
              <div className="min-w-0 flex-1 space-y-2">
                <Label htmlFor="publication-search">Buscar publicaciones</Label>
                <div className="relative">
                  <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    className="h-10 pl-9"
                    id="publication-search"
                    onChange={(event) => setSearchInput(event.target.value)}
                    placeholder="Buscar por título, campaña, usuario o correo"
                    value={searchInput}
                  />
                </div>
              </div>
              <div className="flex items-end gap-2">
                <Button className="h-10" type="submit">
                  Buscar
                </Button>
                {hasClearableFilters ? (
                  <Button
                    aria-label="Limpiar filtros"
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
              <Label htmlFor="publication-status-filter">Estado</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                id="publication-status-filter"
                onChange={(event) => handleEstadoChange(event.target.value)}
                value={estadoFiltro}
              >
                <option value="">Todos los estados</option>
                <option value="borrador">Borrador</option>
                <option value="lista">Lista</option>
                <option value="enviada">Publicada</option>
                <option value="error">Error</option>
                <option value="cancelada">Cancelada</option>
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="publication-user-filter">Usuario</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-60"
                disabled={usuariosLoading}
                id="publication-user-filter"
                onChange={(event) => handleUsuarioChange(event.target.value)}
                value={usuarioFiltro}
              >
                {usuariosLoading ? (
                  <option value="">Cargando usuarios...</option>
                ) : (
                  <>
                    <option value="">Todos los usuarios</option>
                    {usuarios.map((user) => (
                      <option key={user.idUsuario} value={user.idUsuario}>
                        {userFilterName(user)} ({user.totalPublicaciones})
                      </option>
                    ))}
                  </>
                )}
              </select>
              {usuariosError ? (
                <div className="flex flex-col gap-2 text-sm text-destructive sm:flex-row sm:items-center sm:justify-between">
                  <span>{usuariosError}</span>
                  <Button
                    className="h-8 w-fit"
                    onClick={() => void loadUsuarios()}
                    size="sm"
                    type="button"
                    variant="outline"
                  >
                    <RefreshCw />
                    Reintentar
                  </Button>
                </div>
              ) : null}
            </div>
          </div>
        </CardContent>
      </Card>

      {error ? (
        <Alert variant="destructive">
          <AlertCircle />
          <AlertTitle>No se pudieron cargar las publicaciones</AlertTitle>
          <AlertDescription className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <span>{error}</span>
            <Button
              className="w-fit"
              onClick={() => void loadPublications()}
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
            <CardTitle className="text-base">Listado de publicaciones</CardTitle>
            <CardDescription>
              Página {page} de {totalPages} · {publicationSummary}
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <div className="hidden overflow-x-auto md:block">
            <table className="w-full min-w-[820px] text-left text-sm">
              <thead className="border-b text-xs uppercase tracking-wide text-muted-foreground">
                <tr>
                  <th className="px-3 py-3 font-medium">Título</th>
                  <th className="px-3 py-3 font-medium">Campaña</th>
                  <th className="px-3 py-3 font-medium">Usuario</th>
                  <th className="px-3 py-3 font-medium">Estado</th>
                  <th className="px-3 py-3 font-medium">Fecha</th>
                </tr>
              </thead>
              <tbody>
                {loading
                  ? Array.from({ length: 6 }).map((_, index) => (
                      <tr className="border-b" key={index}>
                        {Array.from({ length: 5 }).map((__, cellIndex) => (
                          <td className="px-3 py-4" key={cellIndex}>
                            <Skeleton className="h-5 w-full" />
                          </td>
                        ))}
                      </tr>
                    ))
                  : publicaciones?.results.map((publication) => (
                      <tr
                        className="border-b transition-colors hover:bg-muted/50"
                        key={publication.idPublicacion}
                      >
                        <td className="max-w-[260px] px-3 py-4">
                          <span className="block truncate font-medium">
                            {publication.titulo}
                          </span>
                        </td>
                        <td className="max-w-[240px] px-3 py-4">
                          <span className="block truncate font-medium">
                            {publication.campana.nombre}
                          </span>
                        </td>
                        <td className="max-w-[240px] px-3 py-4">
                          <span className="block truncate font-medium">
                            {userName(publication)}
                          </span>
                          <span className="block truncate text-xs text-muted-foreground">
                            {publication.usuario.email}
                          </span>
                        </td>
                        <td className="px-3 py-4">
                          <PublicationStatusCell publication={publication} />
                        </td>
                        <td className="px-3 py-4">
                          {formatDateTime(publication.fechaCreacion)}
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
                    <Skeleton className="h-5 w-44" />
                    <Skeleton className="mt-3 h-4 w-full" />
                    <Skeleton className="mt-3 h-9 w-full" />
                  </div>
                ))
              : publicaciones?.results.map((publication) => (
                  <article
                    className="rounded-lg border p-4"
                    key={publication.idPublicacion}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium">
                          {publication.titulo}
                        </p>
                        <p className="mt-1 truncate text-xs text-muted-foreground">
                          {publication.campana.nombre}
                        </p>
                      </div>
                      <PublicationStatusBadge estado={publication.estado} />
                    </div>
                    {errorMessage(publication) ? (
                      <p className="mt-2 line-clamp-3 text-xs leading-5 text-destructive">
                        {errorMessage(publication)}
                      </p>
                    ) : null}
                    <dl className="mt-4 grid gap-2 text-sm">
                      <div>
                        <dt className="text-xs text-muted-foreground">
                          Usuario
                        </dt>
                        <dd className="font-medium">{userName(publication)}</dd>
                        <dd className="break-all text-xs text-muted-foreground">
                          {publication.usuario.email}
                        </dd>
                      </div>
                      <div>
                        <dt className="text-xs text-muted-foreground">Fecha</dt>
                        <dd className="font-medium">
                          {formatDateTime(publication.fechaCreacion)}
                        </dd>
                      </div>
                    </dl>
                  </article>
                ))}
          </div>

          {!loading && publicaciones?.results.length === 0 ? (
            <div className="rounded-lg border border-dashed p-8 text-center">
              <p className="font-medium">{emptyMessage}</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Ajusta la búsqueda o el filtro para volver a consultar.
              </p>
            </div>
          ) : null}

          <div className="mt-5 flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-muted-foreground">
              {publicationSummary} en total
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
    </div>
  )
}
