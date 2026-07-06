import { Moon, Sun } from "lucide-react"
import { useEffect, useState } from "react"
import { useTheme } from "next-themes"

import { Button } from "@/components/ui/button"

export function ThemeToggle() {
  const [mounted, setMounted] = useState(false)
  const { theme, setTheme } = useTheme()
  const currentTheme = theme === "light" ? "light" : "dark"
  const isDark = currentTheme === "dark"

  useEffect(() => {
    const timeout = window.setTimeout(() => setMounted(true), 0)

    return () => window.clearTimeout(timeout)
  }, [])

  if (!mounted) {
    return (
      <Button
        aria-label="Cambiar tema"
        className="pointer-events-auto size-9 cursor-pointer rounded-full"
        disabled
        size="icon"
        title="Cambiar tema"
        type="button"
        variant="outline"
      />
    )
  }

  return (
    <Button
      aria-label={isDark ? "Activar modo claro" : "Activar modo oscuro"}
      className="pointer-events-auto size-9 cursor-pointer rounded-full border-border bg-background/85 shadow-sm backdrop-blur transition hover:bg-muted focus-visible:ring-2 focus-visible:ring-ring"
      onClick={() => setTheme(isDark ? "light" : "dark")}
      size="icon"
      title={isDark ? "Activar modo claro" : "Activar modo oscuro"}
      type="button"
      variant="outline"
    >
      {isDark ? <Sun className="size-4" /> : <Moon className="size-4" />}
    </Button>
  )
}
