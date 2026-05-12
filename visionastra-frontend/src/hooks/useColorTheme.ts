import { useContext } from "react";
import { ColorContext } from "@/context/color-context";

export function useColorTheme() {
  return useContext(ColorContext);
}
