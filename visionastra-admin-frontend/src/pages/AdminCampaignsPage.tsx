import {
  AlertCircle,
  Megaphone,
  RefreshCw,
  Search,
  X,
} from "lucide-react"
import { type FormEvent, useCallback, useEffect, useRef, useState } from "react"
import { toast } from "sonner"

import { CampaignDetailDialog } from "@/components/campaigns/CampaignDetailDialog"
import { CampaignStatusBadge } from "@/components/campaigns/CampaignStatusBadge"
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
  obtenerCampana,
  obtenerCampanas,
  obtenerCampanasPropietarios,
} from "@/services/adminCampaignsService"
import type {
  CampanaAdmin,
  CampanaDetalle,
  CampanaEstado,
  CampanaPropietarioFiltro,
  CampanasPaginadas,
} from "@/types/adminCampaigns"

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

function ownerName(campaign: CampanaAdmin) {
  return `${campaign.propietario.nombres} ${campaign.propietario.apellidos}`.trim()
}

function ownerFilterName(owner: CampanaPropietarioFiltro) {
  return `${owner.nombres} ${owner.apellidos}`.trim()
}

type CampaignNameButtonProps = {
  campaign: CampanaAdmin
  onOpenDetail: (campaign: CampanaAdmin) => void
}

function CampaignNameButton({
  campaign,
  onOpenDetail,
}: CampaignNameButtonProps) {
  return (
    <button
      className="text-left text-sm font-medium text-foreground underline-offset-4 outline-none transition hover:text-primary hover:underline focus-visible:rounded-md focus-visible:ring-2 focus-visible:ring-ring"
      onClick={() => onOpenDetail(campaign)}
      type="button"
    >
      {campaign.nombre}
    </button>
  )
}

export function AdminCampaignsPage() {
  const campaignsTopRef = useRef<HTMLDivElement | null>(null)
  const [campanas, setCampanas] = useState<CampanasPaginadas | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [searchInput, setSearchInput] = useState("")
  const [appliedSearch, setAppliedSearch] = useState("")
  const [estadoFiltro, setEstadoFiltro] = useState<CampanaEstado | "">("")
  const [propietarios, setPropietarios] = useState<CampanaPropietarioFiltro[]>(
    []
  )
  const [propietariosLoading, setPropietariosLoading] = useState(true)
  const [propietariosError, setPropietariosError] = useState("")
  const [propietarioFiltro, setPropietarioFiltro] = useState<number | "">("")
  const [page, setPage] = useState(1)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [selectedCampaign, setSelectedCampaign] =
    useState<CampanaDetalle | null>(null)

  const totalPages = Math.max(1, Math.ceil((campanas?.count ?? 0) / PAGE_SIZE))
  const hasAppliedSearch = Boolean(appliedSearch)
  const hasFilters =
    hasAppliedSearch || Boolean(estadoFiltro) || Boolean(propietarioFiltro)
  const hasClearableFilters = Boolean(searchInput.trim()) || hasFilters
  const selectedPropietario =
    typeof propietarioFiltro === "number"
      ? propietarios.find((owner) => owner.idUsuario === propietarioFiltro)
      : undefined
  const selectedPropietarioName = selectedPropietario
    ? ownerFilterName(selectedPropietario)
    : ""
  const campaignCount = campanas?.count ?? 0
  const campaignCountLabel =
    campaignCount === 1 ? "1 campaña" : `${campaignCount} campañas`
  const campaignSummary = selectedPropietarioName
    ? `${campaignCountLabel} de ${selectedPropietarioName}`
    : campaignCountLabel

  const loadCampaigns = useCallback(async () => {
    setLoading(true)
    setError("")

    try {
      const data = await obtenerCampanas({
        search: appliedSearch,
        estado: estadoFiltro,
        propietario: propietarioFiltro,
        page,
      })
      setCampanas(data)
    } catch (campaignsError) {
      setCampanas(null)
      setError(
        getAdminApiErrorMessage(
          campaignsError,
          "No se pudieron cargar las campañas."
        )
      )
    } finally {
      setLoading(false)
    }
  }, [appliedSearch, estadoFiltro, page, propietarioFiltro])

  const loadPropietarios = useCallback(async () => {
    setPropietariosLoading(true)
    setPropietariosError("")

    try {
      const data = await obtenerCampanasPropietarios()
      setPropietarios(data)
    } catch (ownersError) {
      setPropietarios([])
      setPropietariosError(
        getAdminApiErrorMessage(
          ownersError,
          "No se pudieron cargar los propietarios."
        )
      )
    } finally {
      setPropietariosLoading(false)
    }
  }, [])

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void loadCampaigns()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadCampaigns])

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void loadPropietarios()
    }, 0)

    return () => window.clearTimeout(timeout)
  }, [loadPropietarios])

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAppliedSearch(searchInput.trim())
    setPage(1)
  }

  function handleClearSearch() {
    setSearchInput("")
    setAppliedSearch("")
    setEstadoFiltro("")
    setPropietarioFiltro("")
    setPage(1)
  }

  function handleEstadoChange(value: string) {
    setEstadoFiltro(value as CampanaEstado | "")
    setPage(1)
  }

  function handlePropietarioChange(value: string) {
    setPropietarioFiltro(value ? Number(value) : "")
    setPage(1)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
    campaignsTopRef.current?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    })
  }

  async function handleOpenDetail(campaign: CampanaAdmin) {
    setDetailOpen(true)
    setDetailLoading(true)
    setSelectedCampaign(null)

    try {
      const data = await obtenerCampana(campaign.idCampana)
      setSelectedCampaign(data)
    } catch (detailError) {
      toast.error(
        getAdminApiErrorMessage(
          detailError,
          "No se pudo cargar el detalle de la campaña."
        )
      )
      setDetailOpen(false)
    } finally {
      setDetailLoading(false)
    }
  }

  const emptyMessage = hasFilters
    ? "No se encontraron campañas con los filtros seleccionados."
    : "No hay campañas registradas."

  return (
    <div className="space-y-5" ref={campaignsTopRef}>
      <section className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Badge className="w-fit" variant="secondary">
              <Megaphone />
              Campañas
            </Badge>
          </div>
          <h2 className="mt-2 text-2xl font-semibold tracking-normal sm:text-3xl">
            Campañas
          </h2>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
            Consulta campañas, propietarios, estado y volumen de contenido
            asociado.
          </p>
        </div>
        <div className="rounded-lg border bg-card px-3 py-2 text-sm shadow-sm">
          <span className="text-muted-foreground">Total: </span>
          <span className="font-semibold">{campanas?.count ?? 0}</span>
        </div>
      </section>

      <Card className="rounded-lg">
        <CardHeader>
          <CardTitle className="text-base">Filtros</CardTitle>
          <CardDescription>
            Busca por campaña, propietario o correo y filtra por estado.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px_260px]">
            <form
              className="flex flex-col gap-2 sm:flex-row"
              onSubmit={handleSearchSubmit}
            >
              <div className="min-w-0 flex-1 space-y-2">
                <Label htmlFor="campaign-search">Buscar campañas</Label>
                <div className="relative">
                  <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    className="h-10 pl-9"
                    id="campaign-search"
                    onChange={(event) => setSearchInput(event.target.value)}
                    placeholder="Buscar por campaña, propietario o correo"
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
              <Label htmlFor="campaign-status-filter">Estado</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                id="campaign-status-filter"
                onChange={(event) => handleEstadoChange(event.target.value)}
                value={estadoFiltro}
              >
                <option value="">Todos los estados</option>
                <option value="borrador">Borrador</option>
                <option value="activa">Activa</option>
                <option value="pausada">Pausada</option>
                <option value="finalizada">Finalizada</option>
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="campaign-owner-filter">Propietario</Label>
              <select
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-60"
                disabled={propietariosLoading}
                id="campaign-owner-filter"
                onChange={(event) =>
                  handlePropietarioChange(event.target.value)
                }
                value={propietarioFiltro}
              >
                {propietariosLoading ? (
                  <option value="">Cargando propietarios...</option>
                ) : (
                  <>
                    <option value="">Todos los propietarios</option>
                    {propietarios.map((owner) => (
                      <option key={owner.idUsuario} value={owner.idUsuario}>
                        {ownerFilterName(owner)} ({owner.totalCampanas})
                      </option>
                    ))}
                  </>
                )}
              </select>
              {propietariosError ? (
                <div className="flex flex-col gap-2 text-sm text-destructive sm:flex-row sm:items-center sm:justify-between">
                  <span>{propietariosError}</span>
                  <Button
                    className="h-8 w-fit"
                    onClick={() => void loadPropietarios()}
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
          <AlertTitle>No se pudieron cargar las campañas</AlertTitle>
          <AlertDescription className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <span>{error}</span>
            <Button
              className="w-fit"
              onClick={() => void loadCampaigns()}
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
            <CardTitle className="text-base">Listado de campañas</CardTitle>
            <CardDescription>
              Página {page} de {totalPages} · {campaignSummary}
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <div className="hidden overflow-x-auto md:block">
            <table className="w-full min-w-[800px] text-left text-sm">
              <thead className="border-b text-xs uppercase tracking-wide text-muted-foreground">
                <tr>
                  <th className="px-3 py-3 font-medium">Nombre</th>
                  <th className="px-3 py-3 font-medium">Propietario</th>
                  <th className="px-3 py-3 font-medium">Estado</th>
                  <th className="px-3 py-3 font-medium">Creación</th>
                  <th className="px-3 py-3 font-medium">Recursos</th>
                  <th className="px-3 py-3 font-medium">Publicaciones</th>
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
                  : campanas?.results.map((campaign) => (
                      <tr
                        className="border-b transition-colors hover:bg-muted/50"
                        key={campaign.idCampana}
                      >
                        <td className="max-w-[260px] px-3 py-4">
                          <CampaignNameButton
                            campaign={campaign}
                            onOpenDetail={(selected) => {
                              void handleOpenDetail(selected)
                            }}
                          />
                        </td>
                        <td className="max-w-[240px] px-3 py-4">
                          <span className="block truncate font-medium">
                            {ownerName(campaign)}
                          </span>
                          <span className="block truncate text-xs text-muted-foreground">
                            {campaign.propietario.email}
                          </span>
                        </td>
                        <td className="px-3 py-4">
                          <CampaignStatusBadge estado={campaign.estado} />
                        </td>
                        <td className="px-3 py-4">
                          {formatDateTime(campaign.fechaCreacion)}
                        </td>
                        <td className="px-3 py-4">
                          {campaign.totalRecursos.toLocaleString("es-PE")}
                        </td>
                        <td className="px-3 py-4">
                          {campaign.totalPublicaciones.toLocaleString("es-PE")}
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
              : campanas?.results.map((campaign) => (
                  <article
                    className="rounded-lg border p-4"
                    key={campaign.idCampana}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <CampaignNameButton
                          campaign={campaign}
                          onOpenDetail={(selected) => {
                            void handleOpenDetail(selected)
                          }}
                        />
                        <p className="mt-1 break-all text-xs text-muted-foreground">
                          {campaign.propietario.email}
                        </p>
                      </div>
                      <CampaignStatusBadge estado={campaign.estado} />
                    </div>
                    <dl className="mt-4 grid gap-2 text-sm">
                      <div>
                        <dt className="text-xs text-muted-foreground">
                          Propietario
                        </dt>
                        <dd className="font-medium">{ownerName(campaign)}</dd>
                      </div>
                      <div className="grid grid-cols-3 gap-3">
                        <div>
                          <dt className="text-xs text-muted-foreground">
                            Creación
                          </dt>
                          <dd className="font-medium">
                            {formatDateTime(campaign.fechaCreacion)}
                          </dd>
                        </div>
                        <div>
                          <dt className="text-xs text-muted-foreground">
                            Recursos
                          </dt>
                          <dd className="font-medium">
                            {campaign.totalRecursos.toLocaleString("es-PE")}
                          </dd>
                        </div>
                        <div>
                          <dt className="text-xs text-muted-foreground">
                            Publicaciones
                          </dt>
                          <dd className="font-medium">
                            {campaign.totalPublicaciones.toLocaleString(
                              "es-PE"
                            )}
                          </dd>
                        </div>
                      </div>
                    </dl>
                  </article>
                ))}
          </div>

          {!loading && campanas?.results.length === 0 ? (
            <div className="rounded-lg border border-dashed p-8 text-center">
              <p className="font-medium">{emptyMessage}</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Ajusta la búsqueda o el filtro para volver a consultar.
              </p>
            </div>
          ) : null}

          <div className="mt-5 flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-muted-foreground">
              {campaignSummary} en total
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

      <CampaignDetailDialog
        campaign={selectedCampaign}
        loading={detailLoading}
        onClose={() => setDetailOpen(false)}
        open={detailOpen}
      />
    </div>
  )
}
