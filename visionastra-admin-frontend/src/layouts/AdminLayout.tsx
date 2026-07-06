import {
  LayoutDashboard,
  LogOut,
  Megaphone,
  Menu,
  Send,
  Sparkles,
  Users,
} from "lucide-react"
import { NavLink, Outlet, useNavigate } from "react-router-dom"
import { toast } from "sonner"

import VisionAstraLogo from "@/components/branding/VisionAstraLogo"
import { ThemeToggle } from "@/components/theme-toggle"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Separator } from "@/components/ui/separator"
import { useAdminAuth } from "@/contexts/AdminAuthContext"
import { cn } from "@/lib/utils"

const NAV_ITEMS = [
  {
    label: "Dashboard",
    to: "/",
    icon: LayoutDashboard,
  },
  {
    label: "Usuarios",
    to: "/usuarios",
    icon: Users,
  },
  {
    label: "Campañas",
    to: "/campanas",
    icon: Megaphone,
  },
  {
    label: "Generaciones IA",
    to: "/generaciones-ia",
    icon: Sparkles,
  },
  {
    label: "Publicaciones",
    to: "/publicaciones",
    icon: Send,
  },
]

export function AdminLayout() {
  const navigate = useNavigate()
  const { admin, logout, logoutLoading } = useAdminAuth()

  async function handleLogout() {
    await logout()
    toast.info("Sesión administrativa cerrada")
    navigate("/login", { replace: true })
  }

  return (
    <div className="min-h-screen bg-muted/30 text-foreground dark:bg-background">
      <aside className="fixed inset-y-0 left-0 hidden min-h-screen w-64 border-r bg-card/90 px-4 py-5 backdrop-blur lg:flex lg:flex-col">
        <div className="flex items-center gap-3 px-1">
          <VisionAstraLogo
            size={48}
            bgOpacity={0.14}
            showText={false}
            className="text-sky-500 dark:text-sky-400"
          />

          <div className="min-w-0">
            <p className="truncate text-sm font-semibold">VisionAstra Admin</p>
            <p className="text-xs text-muted-foreground">
              Panel administrativo
            </p>
          </div>
        </div>

        <Separator className="my-5" />

        <nav className="space-y-1" aria-label="Navegación principal">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon

            return (
              <NavLink
                className={({ isActive }) =>
                  cn(
                    "flex h-9 items-center gap-2 rounded-lg px-3 text-sm font-medium transition-colors hover:bg-muted",
                    isActive
                      ? "bg-secondary text-secondary-foreground"
                      : "text-muted-foreground hover:text-foreground"
                  )
                }
                end={item.to === "/"}
                key={item.to}
                to={item.to}
              >
                <Icon className="size-4" />
                {item.label}
              </NavLink>
            )
          })}
        </nav>

        <div className="mt-auto">
          <Button
            className="w-full justify-start gap-2"
            disabled={logoutLoading}
            onClick={handleLogout}
            type="button"
            variant="outline"
          >
            <LogOut className="size-4" />
            {logoutLoading ? "Cerrando..." : "Cerrar sesión"}
          </Button>
        </div>
      </aside>

      <div className="lg:pl-64">
        <header className="sticky top-0 z-30 border-b bg-background/90 backdrop-blur">
          <div className="mx-auto flex h-16 max-w-[1400px] items-center justify-between px-4 sm:px-6 lg:justify-end lg:px-8">
            <div className="flex items-center gap-2 lg:hidden">
              <DropdownMenu>
                <DropdownMenuTrigger
                  render={
                    <Button
                      aria-label="Abrir navegación"
                      size="icon"
                      type="button"
                      variant="outline"
                    >
                      <Menu className="size-4" />
                    </Button>
                  }
                />
                <DropdownMenuContent align="start" className="w-48">
                  {NAV_ITEMS.map((item) => {
                    const Icon = item.icon

                    return (
                      <DropdownMenuItem
                        key={item.to}
                        onClick={() => navigate(item.to)}
                      >
                        <Icon className="size-4" />
                        {item.label}
                      </DropdownMenuItem>
                    )
                  })}
                </DropdownMenuContent>
              </DropdownMenu>
              <span className="text-sm font-semibold">VisionAstra Admin</span>
            </div>

            <div className="flex items-center gap-3">
              <span className="max-w-40 truncate text-sm font-semibold text-foreground">
                {admin?.username}
              </span>
              <ThemeToggle />
            </div>
          </div>
        </header>

        <main className="mx-auto max-w-[1400px] px-4 py-5 sm:px-6 lg:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
