import {
  AlertCircle,
  RefreshCw,
  Search,
  Sparkles,
  X,
} from "lucide-react"
import { type FormEvent, useCallback, useEffect, useRef, useState } from "react"

import { AiGenerationStatusBadge } from "@/components/ai-generations/AiGenerationStatusBadge"
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
import {
  obtenerGeneracionesIA,
  obtenerGeneracionesIAUsuarios,
} from "@/services/adminAiGenerationsService"
import type {
  GeneracionIAAdmin,
  GeneracionIAEstado,
  GeneracionIAUsuarioFiltro,
  GeneracionesIAPaginadas,
} from "@/types/adminAiGenerations"

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

function userName(generation: GeneracionIAAdmin) {
  return `${generation.usuario.nombres} ${generation.usuario.apellidos}`.trim()
}

function userFilterName(user: GeneracionIAUsuarioFiltro) {
  return `${user.nombres} ${user.apellidos}`.trim()
}

export function AdminAiGenerationsPage() {
  const generationsTopRef = useRef<HTMLDivElement | null>(null)
  const [generaciones, setGeneraciones] =
    useState<GeneracionesIAPaginadas | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [searchInput, setSearchInput] = useState("")
  const [appliedSearch, setAppliedSearch] = useState("")
  const [estadoFiltro, setEstadoFiltro] = useState<GeneracionIAEstado | "">("")
  const [usuarios, setUsuarios] = useState<GeneracionIAUsuarioFiltro[]>([])
  const [usuariosLoading, setUsuariosLoading] = useState(true)
  const [usuariosError, setUsuariosError] = useState("")
  const [usuarioFiltro, setUsuarioFiltro] = useState<number | "">("")
  const [page, setPage] = useState(1)

  const totalPages = Math.max(
    1,
    Math.ceil((generaciones?.count ?? 0) / PAGE_SIZE)
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
  const generationCount = generaciones?.count ?? 0
  const generationCountLabel =
    generationCount === 1
      ? "1 generación IA"
      : `${generationCount} generaciones IA`
  const generationSummary = selectedUsuarioName
    ? `${generationCountLabel} de ${selectedUsuarioName}`
    : generationCountLabel

  const loadGenerations = useCallback(async () => {
    setLoading(true)
    setError("")

    try {
      const data = await obtenerGeneracionesIA({
        search: appliedSearch,
        estado: estadoFiltro,
        usuario: usuarioFiltro,
        page,
      })
      setGeneraciones(data)
    } catch (generationsError) {
      setGeneraciones(null)
      setError(
        getAdminApiErrorMessage(
          generationsError,
          "No se pudieron cargar las generaciones IA."
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
      const data = await obtenerGeneracionesIAUsuarios()
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
      void loadGenerations()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadGenerations])

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
    setEstadoFiltro(value as GeneracionIAEstado | "")
    setPage(1)
  }

  function handleUsuarioChange(value: string) {
    setUsuarioFiltro(value ? Number(value) : "")
    setPage(1)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
    generationsTopRef.current?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    })
  }

  const emptyMessage = hasFilters
    ? "No se encontraron generaciones IA con los filtros seleccionados."
    : "No hay generaciones IA registradas."

  return (
    <div className="space-y-5" ref={generationsTopRef}>
      <section className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Badge className="w-fit" variant="secondary">
              <Sparkles />
              Generaciones IA
            </Badge>
          </div>
          <h2 className="mt-2 text-2xl font-semibold tracking-normal sm:text-3xl">
            Generaciones IA
          </h2>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
            Consulta las generaciones de contenido realizadas por los usuarios.
          </p>
        </div>
        <div className="rounded-lg border bg-card px-3 py-2 text-sm shadow-sm">
          <span className="text-muted-foreground">Total: </span>
          <span className="font-semibold">{generationCount}</span>
        </div>
      </section>

      <Card className="rounded-lg">
        <CardHeader>
          <CardTitle className="text-base">Filtros</CardTitle>
          <CardDescription>
            Busca por campaña, usuario o correo y filtra por estado.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px_260px]">
            <form
              className="flex flex-col gap-2 sm:flex-row"
              onSubmit={handleSearchSubmit}
            >
              <div className="min-w-0 flex-1 space-y-2">
                <Label htmlFor="ai-generation-search">
                  Buscar generaciones IA
                </Label>
                <div className="relative">
                  <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    className="h-10 pl-9"
                    id="ai-generation-search"
                    onChange={(event) => setSearchInput(event.target.value)}
                    placeholder="Buscar por campaña, usuario o correo"
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
              <Label htmlFor="ai-generation-status-filter">Estado</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                id="ai-generation-status-filter"
                onChange={(event) => handleEstadoChange(event.target.value)}
                value={estadoFiltro}
              >
                <option value="">Todos los estados</option>
                <option value="pendiente">Pendiente</option>
                <option value="procesando">Procesando</option>
                <option value="completado">Completado</option>
                <option value="error">Error</option>
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="ai-generation-user-filter">Usuario</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-60"
                disabled={usuariosLoading}
                id="ai-generation-user-filter"
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
                        {userFilterName(user)} ({user.totalGeneraciones})
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
          <AlertTitle>No se pudieron cargar las generaciones IA</AlertTitle>
          <AlertDescription className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <span>{error}</span>
            <Button
              className="w-fit"
              onClick={() => void loadGenerations()}
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
            <CardTitle className="text-base">
              Listado de generaciones IA
            </CardTitle>
            <CardDescription>
              Página {page} de {totalPages} · {generationSummary}
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <div className="hidden overflow-x-auto md:block">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="border-b text-xs uppercase tracking-wide text-muted-foreground">
                <tr>
                  <th className="px-3 py-3 font-medium">Generación</th>
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
                  : generaciones?.results.map((generation) => (
                      <tr
                        className="border-b transition-colors hover:bg-muted/50"
                        key={generation.idGeneracion}
                      >
                        <td className="px-3 py-4">
                          <span className="text-sm font-medium">
                            #{generation.idGeneracion}
                          </span>
                        </td>
                        <td className="max-w-[260px] px-3 py-4">
                          <span className="block truncate font-medium">
                            {generation.campana.nombre}
                          </span>
                        </td>
                        <td className="max-w-[240px] px-3 py-4">
                          <span className="block truncate font-medium">
                            {userName(generation)}
                          </span>
                          <span className="block truncate text-xs text-muted-foreground">
                            {generation.usuario.email}
                          </span>
                        </td>
                        <td className="px-3 py-4">
                          <AiGenerationStatusBadge
                            estado={generation.estado}
                          />
                        </td>
                        <td className="px-3 py-4">
                          {formatDateTime(generation.fechaCreacion)}
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
                    <Skeleton className="h-5 w-32" />
                    <Skeleton className="mt-3 h-4 w-full" />
                    <Skeleton className="mt-3 h-9 w-full" />
                  </div>
                ))
              : generaciones?.results.map((generation) => (
                  <article
                    className="rounded-lg border p-4"
                    key={generation.idGeneracion}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium">
                          #{generation.idGeneracion}
                        </p>
                        <p className="mt-1 truncate text-xs text-muted-foreground">
                          {generation.campana.nombre}
                        </p>
                      </div>
                      <AiGenerationStatusBadge estado={generation.estado} />
                    </div>
                    <dl className="mt-4 grid gap-2 text-sm">
                      <div>
                        <dt className="text-xs text-muted-foreground">
                          Usuario
                        </dt>
                        <dd className="font-medium">{userName(generation)}</dd>
                        <dd className="break-all text-xs text-muted-foreground">
                          {generation.usuario.email}
                        </dd>
                      </div>
                      <div>
                        <dt className="text-xs text-muted-foreground">Fecha</dt>
                        <dd className="font-medium">
                          {formatDateTime(generation.fechaCreacion)}
                        </dd>
                      </div>
                    </dl>
                  </article>
                ))}
          </div>

          {!loading && generaciones?.results.length === 0 ? (
            <div className="rounded-lg border border-dashed p-8 text-center">
              <p className="font-medium">{emptyMessage}</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Ajusta la búsqueda o el filtro para volver a consultar.
              </p>
            </div>
          ) : null}

          <div className="mt-5 flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-muted-foreground">
              {generationSummary} en total
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
