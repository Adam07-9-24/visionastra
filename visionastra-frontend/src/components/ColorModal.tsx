import { X } from "lucide-react";
import { COLOR_THEMES, type ColorTheme } from "@/context/ColorThemes";
import { useColorTheme } from "@/hooks/useColorTheme";
import { cn } from "@/lib/utils";

interface ColorModalProps {
  open: boolean;
  onClose: () => void;
}

export default function ColorModal({ open, onClose }: ColorModalProps) {
  const { colorTheme, setColorTheme } = useColorTheme();

  return (
    <>
      {/* Overlay */}
      {open && (
        <div
          onClick={onClose}
          style={{
            position: "fixed",
            inset: 0,
            zIndex: 40,
            backgroundColor: "rgba(0,0,0,0.3)",
            backdropFilter: "blur(4px)",
          }}
        />
      )}

      {/* Panel lateral derecho */}
      <div
        style={{
          position: "fixed",
          top: 0,
          right: 0,
          height: "100%",
          width: "300px",
          zIndex: 50,
          transform: open ? "translateX(0)" : "translateX(100%)",
          transition: "transform 0.3s ease-in-out",
          display: "flex",
          flexDirection: "column",
        }}
        className="bg-card border-l shadow-2xl"
      >
        {/* Header */}
        <div className="flex items-start justify-between p-6 border-b">
          <div>
            <h2 className="text-base font-semibold">Personalizar</h2>
            <p className="text-xs text-muted-foreground mt-1">
              Personaliza la experiencia de tu panel de control.
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground transition-colors"
          >
            <X size={16} />
          </button>
        </div>

        {/* Contenido */}
        <div className="flex-1 overflow-y-auto p-6">
          <p className="text-sm font-semibold mb-3">Color</p>

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "8px",
            }}
          >
            {(
              Object.entries(COLOR_THEMES) as [
                ColorTheme,
                (typeof COLOR_THEMES)[ColorTheme]
              ][]
            ).map(([key, cfg]) => (
              <button
                key={key}
                onClick={() => setColorTheme(key)}
                style={{ display: "flex", alignItems: "center", gap: "10px" }}
                className={cn(
                  "px-3 py-3 rounded-xl border text-sm font-medium transition-all duration-200",
                  colorTheme === key
                    ? "border-primary text-primary bg-primary/5"
                    : "border-border text-muted-foreground hover:text-foreground"
                )}
              >
                {/* Círculo de color — con style inline para garantizar render */}
                <span
                  style={{
                    width: "18px",
                    height: "18px",
                    minWidth: "18px",
                    borderRadius: "50%",
                    backgroundColor: cfg.color,
                    display: "inline-block",
                    boxShadow: "0 1px 3px rgba(0,0,0,0.2)",
                    outline:
                      colorTheme === key ? `2px solid ${cfg.color}` : "none",
                    outlineOffset: "2px",
                  }}
                />
                {cfg.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}
