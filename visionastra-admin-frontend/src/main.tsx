import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import { BrowserRouter } from "react-router-dom"

import App from "./App.tsx"
import { ThemeProvider } from "./components/theme-provider"
import { Toaster } from "./components/ui/sonner"
import { AdminAuthProvider } from "./contexts/AdminAuthContext"
import "./index.css"

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <BrowserRouter>
        <AdminAuthProvider>
          <App />
          <Toaster richColors position="top-right" />
        </AdminAuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>
)
