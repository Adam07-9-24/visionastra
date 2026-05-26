import { useEffect, useState } from "react";
import { NavLink, Outlet, Navigate, useLocation } from "react-router-dom";
import { ModeToggle } from "@/components/mode-toggle";
import ColorModal from "@/components/ColorModal";
import VisionAstraLogo from "@/components/branding/VisionAstraLogo";
import { useHeartbeat } from "@/hooks/useHeartbeat";
import { useSilentRefresh } from "@/hooks/useSilentRefresh";
import {
  LayoutDashboard,
  Shield,
  Megaphone,
  FolderOpen,
  ChevronLeft,
  ChevronRight,
  Palette,
} from "lucide-react";

const navItems = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/sesiones", label: "Sesiones", icon: Shield },
  { to: "/campanas", label: "Campañas", icon: Megaphone },
  { to: "/recursos", label: "Recursos", icon: FolderOpen },
];

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [openColor, setOpenColor] = useState(false);

  const location = useLocation();
  const token = localStorage.getItem("token");

  useHeartbeat();
  useSilentRefresh();

  useEffect(() => {
    const main = document.getElementById("main-scroll");

    main?.scrollTo({
      top: 0,
      left: 0,
      behavior: "auto",
    });
  }, [location.pathname]);

  if (!token) {
    return <Navigate to="/login" />;
  }

  return (
    <div className="flex h-screen flex-col overflow-hidden bg-background">
      {/* ── HEADER ── */}
      <header className="z-10 flex h-16 shrink-0 items-center justify-between border-b bg-background/80 px-6 backdrop-blur-md">
        <div
          style={{
            display: "flex",
            alignItems: "center",
            paddingLeft: "18px",
            gap: "10px",
            marginBottom: "4px",
          }}
        >
          <VisionAstraLogo size={40} showText={true} />
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <ModeToggle />

          <button
            onClick={() => setOpenColor(true)}
            title="Personalizar color"
            style={{
              width: "36px",
              height: "36px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              borderRadius: "8px",
              border: "1px solid var(--border)",
              backgroundColor: "transparent",
              cursor: "pointer",
              color: "var(--muted-foreground)",
              transition: "all 0.2s",
            }}
            className="hover:bg-accent hover:text-accent-foreground"
          >
            <Palette size={16} />
          </button>
        </div>
      </header>

      {/* ── BODY ── */}
      <div
        style={{
          display: "flex",
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
        }}
      >
        {/* ── SIDEBAR ── */}
        <aside
          style={{
            width: collapsed ? "72px" : "260px",
            minWidth: collapsed ? "72px" : "260px",
            height: "100%",
            position: "relative",
            transition: "width 0.4s ease, min-width 0.4s ease",
            flexShrink: 0,
          }}
        >
          {/* Fondo */}
          <div
            style={{
              position: "absolute",
              inset: 0,
              borderRight: "1px solid var(--border)",
              backgroundColor: "var(--background)",
            }}
          />

          {/* Contenido */}
          <div
            style={{
              position: "relative",
              height: "100%",
              display: "flex",
              flexDirection: "column",
              padding: "24px 0",
            }}
          >
            {!collapsed && (
              <p
                style={{
                  fontSize: "10px",
                  fontWeight: 600,
                  letterSpacing: "0.15em",
                  textTransform: "uppercase",
                  padding: "0 20px",
                  marginBottom: "8px",
                }}
                className="text-muted-foreground"
              >
                Overview
              </p>
            )}

            <nav
              style={{
                display: "flex",
                flexDirection: "column",
                gap: "4px",
                padding: "0 10px",
              }}
            >
              {navItems.map(({ to, label, icon: Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  end
                  style={{ textDecoration: "none" }}
                >
                  {({ isActive }) => (
                    <div
                      style={{
                        position: "relative",
                        display: "flex",
                        alignItems: "center",
                        gap: collapsed ? 0 : "12px",
                        justifyContent: collapsed ? "center" : "flex-start",
                        padding: collapsed ? "10px 0" : "10px 12px",
                        borderRadius: "12px",
                        backgroundColor: isActive
                          ? "color-mix(in srgb, var(--primary) 12%, transparent)"
                          : "transparent",
                        cursor: "pointer",
                        transition: "all 0.2s ease",
                      }}
                      className={!isActive ? "hover:bg-muted/60" : ""}
                    >
                      {isActive && !collapsed && (
                        <div
                          style={{
                            position: "absolute",
                            left: 0,
                            top: "50%",
                            transform: "translateY(-50%)",
                            width: "3px",
                            height: "60%",
                            borderRadius: "0 4px 4px 0",
                            backgroundColor: "var(--primary)",
                          }}
                        />
                      )}

                      {isActive && (
                        <div
                          style={{
                            position: "absolute",
                            inset: 0,
                            borderRadius: "12px",
                            boxShadow:
                              "inset 0 0 0 1px color-mix(in srgb, var(--primary) 30%, transparent)",
                            pointerEvents: "none",
                          }}
                        />
                      )}

                      <div
                        style={{
                          width: "36px",
                          height: "36px",
                          minWidth: "36px",
                          borderRadius: "8px",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          backgroundColor: isActive
                            ? "color-mix(in srgb, var(--primary) 20%, transparent)"
                            : "var(--muted)",
                          color: isActive
                            ? "var(--primary)"
                            : "var(--muted-foreground)",
                          transition: "all 0.2s",
                        }}
                      >
                        <Icon size={17} />
                      </div>

                      {!collapsed && (
                        <span
                          style={{
                            fontSize: "14px",
                            fontWeight: isActive ? 600 : 400,
                            color: isActive
                              ? "var(--primary)"
                              : "var(--muted-foreground)",
                            whiteSpace: "nowrap",
                          }}
                        >
                          {label}
                        </span>
                      )}
                    </div>
                  )}
                </NavLink>
              ))}
            </nav>
          </div>

          {/* BOTÓN COLAPSAR */}
          <button
            onClick={() => setCollapsed(!collapsed)}
            style={{
              position: "absolute",
              top: "50%",
              right: "-16px",
              transform: "translateY(-50%)",
              width: "32px",
              height: "32px",
              borderRadius: "50%",
              border: "1px solid var(--border)",
              backgroundColor: "var(--background)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              cursor: "pointer",
              boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
              zIndex: 20,
              transition: "all 0.3s",
            }}
            className="hover:bg-muted"
          >
            {collapsed ? <ChevronRight size={14} /> : <ChevronLeft size={14} />}
          </button>
        </aside>

        {/* ── MAIN ── */}
        <main
          id="main-scroll"
          style={{
            flex: 1,
            minWidth: 0,
            minHeight: 0,
            overflowY: "auto",
            padding: "40px",
            position: "relative",
          }}
          className="bg-background"
        >
          <Outlet />
        </main>
      </div>

      <ColorModal open={openColor} onClose={() => setOpenColor(false)} />
    </div>
  );
}
