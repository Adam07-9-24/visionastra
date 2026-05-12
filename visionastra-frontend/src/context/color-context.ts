import { createContext } from "react";
import type { ColorTheme } from "./ColorThemes";

export interface ColorContextValue {
  colorTheme: ColorTheme;
  setColorTheme: (c: ColorTheme) => void;
}

export const ColorContext = createContext<ColorContextValue>({
  colorTheme: "sage",
  setColorTheme: () => {},
});
