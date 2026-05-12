// Solo datos — sin componentes ni hooks para evitar el warning de react-refresh

export type ColorTheme =
  | "coral"
  | "teal"
  | "blue"
  | "violet"
  | "sage"
  | "amber";

export interface ColorConfig {
  label: string;
  color: string;
  primary: string;
  primaryDark: string;
  ring: string;
}

export const COLOR_THEMES: Record<ColorTheme, ColorConfig> = {
  coral: {
    label: "Coral",
    color: "#e85d4a",
    primary: "oklch(0.6 0.2 29)",
    primaryDark: "oklch(0.65 0.2 29)",
    ring: "oklch(0.6 0.2 29)",
  },
  teal: {
    label: "Verde azulado",
    color: "#2bb5a0",
    primary: "oklch(0.6 0.15 180)",
    primaryDark: "oklch(0.65 0.15 180)",
    ring: "oklch(0.6 0.15 180)",
  },
  blue: {
    label: "Azul",
    color: "#3b82f6",
    primary: "oklch(0.6 0.2 240)",
    primaryDark: "oklch(0.65 0.2 240)",
    ring: "oklch(0.6 0.2 240)",
  },
  violet: {
    label: "Violeta",
    color: "#8b5cf6",
    primary: "oklch(0.58 0.22 290)",
    primaryDark: "oklch(0.63 0.22 290)",
    ring: "oklch(0.58 0.22 290)",
  },
  sage: {
    label: "Sabio",
    color: "#4ade80",
    primary: "oklch(0.6 0.15 150)",
    primaryDark: "oklch(0.65 0.15 150)",
    ring: "oklch(0.6 0.15 150)",
  },
  amber: {
    label: "Ámbar",
    color: "#f59e0b",
    primary: "oklch(0.7 0.18 80)",
    primaryDark: "oklch(0.72 0.18 80)",
    ring: "oklch(0.7 0.18 80)",
  },
};
