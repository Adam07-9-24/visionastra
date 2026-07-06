import type { DashboardResumen } from "@/types/adminDashboard"

import adminApi from "./adminApi"

export async function obtenerDashboardResumen(): Promise<DashboardResumen> {
  const response = await adminApi.get<DashboardResumen>("/dashboard/resumen/")

  return response.data
}
