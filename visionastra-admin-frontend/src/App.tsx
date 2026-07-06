import { Navigate, Route, Routes } from "react-router-dom"

import { ProtectedAdminRoute } from "@/components/ProtectedAdminRoute"
import { AdminLayout } from "@/layouts/AdminLayout"
import { AdminAiGenerationsPage } from "@/pages/AdminAiGenerationsPage"
import { AdminCampaignsPage } from "@/pages/AdminCampaignsPage"
import { AdminDashboardPage } from "@/pages/AdminDashboardPage"
import { AdminLoginPage } from "@/pages/AdminLoginPage"
import { AdminPublicationsPage } from "@/pages/AdminPublicationsPage"
import { AdminUsersPage } from "@/pages/AdminUsersPage"

import "./App.css"

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<AdminLoginPage />} />
      <Route element={<ProtectedAdminRoute />}>
        <Route element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="usuarios" element={<AdminUsersPage />} />
          <Route path="campanas" element={<AdminCampaignsPage />} />
          <Route
            path="generaciones-ia"
            element={<AdminAiGenerationsPage />}
          />
          <Route path="publicaciones" element={<AdminPublicationsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
