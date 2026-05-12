import { useEffect, useState, useCallback } from "react";
import { COLOR_THEMES, type ColorTheme } from "./ColorThemes";
import { ColorContext } from "./color-context";

function applyColorToDom(c: ColorTheme) {
  const cfg = COLOR_THEMES[c];
  if (!cfg) return;

  const isDark = document.documentElement.classList.contains("dark");
  const root = document.documentElement;

  const primary = isDark ? cfg.primaryDark : cfg.primary;

  root.style.setProperty("--primary", primary);
  root.style.setProperty("--primary-foreground", "oklch(0.985 0 0)");
  root.style.setProperty("--ring", primary);
  root.style.setProperty("--sidebar-primary", primary);
  root.style.setProperty("--sidebar-ring", primary);
}

export function ColorProvider({ children }: { children: React.ReactNode }) {
  const [colorTheme, setColorThemeState] = useState<ColorTheme>(
    () => (localStorage.getItem("color-theme") as ColorTheme) ?? "sage"
  );

  const applyColor = useCallback(() => {
    applyColorToDom(colorTheme);
  }, [colorTheme]);

  useEffect(() => {
    applyColor();
  }, [applyColor]);

  useEffect(() => {
    const observer = new MutationObserver(() => {
      setTimeout(() => applyColorToDom(colorTheme), 0);
    });

    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ["class"],
    });

    return () => observer.disconnect();
  }, [colorTheme]);

  const setColorTheme = (c: ColorTheme) => {
    setColorThemeState(c);
    localStorage.setItem("color-theme", c);
    applyColorToDom(c);
  };

  return (
    <ColorContext.Provider value={{ colorTheme, setColorTheme }}>
      {children}
    </ColorContext.Provider>
  );
}
