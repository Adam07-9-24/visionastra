import { Routes, Route } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import SesionesPage from "./pages/sesiones/SesionesPage";
import LoginPage from "./pages/LoginPage"; // 🔥 NUEVO
import { Toaster } from "@/components/ui/sonner";

function App() {
  return (
    <>
      <Routes>
        {/* 🔓 Ruta pública */}
        <Route path="/login" element={<LoginPage />} />

        {/* 🔒 Rutas protegidas */}
        <Route element={<MainLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/sesiones" element={<SesionesPage />} />
        </Route>
      </Routes>

      <Toaster richColors position="top-right" />
    </>
  );
}

export default App;
