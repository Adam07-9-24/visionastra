import { Routes, Route } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import DashboardPage from "./pages/dashboard/DashboardPage";
import SesionesPage from "./pages/sesiones/SesionesPage";
import LoginPage from "./pages/LoginPage";
import { Toaster } from "@/components/ui/sonner";
import CampanasPage from "@/pages/campanas/CampanasPage";
import RecursosPage from "./pages/recursos/RecursosPage";
import GeneradorIAPage from "./pages/generador-ia/GeneradorIAPage";

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
          <Route path="/campanas" element={<CampanasPage />} />
          <Route path="/recursos" element={<RecursosPage />} />
          <Route path="/generador-ia" element={<GeneradorIAPage />} />
        </Route>
      </Routes>

      <Toaster richColors position="top-right" />
    </>
  );
}

export default App;
