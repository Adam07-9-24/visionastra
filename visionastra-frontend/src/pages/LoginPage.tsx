import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "@/services/authService";
import loginHero from "@/assets/visionastra-login-hero.png";
import VisionAstraLogo from "@/components/branding/VisionAstraLogo";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [darkMode, setDarkMode] = useState(true);
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const data = await login(email, password);

      localStorage.setItem("token", data.token);
      localStorage.setItem("refreshToken", data.refreshToken);

      localStorage.setItem(
        "user",
        JSON.stringify({
          idUsuario: data.idUsuario,
          nombres: data.nombres,
          apellidos: data.apellidos,
          email: data.email,
          rol: data.rol,
          estado: data.estado,
        })
      );

      navigate("/");
    } catch {
      alert("Credenciales incorrectas");
    }
  };

  return (
    <main
      className={`relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-10 transition-colors duration-300 sm:px-6 ${
        darkMode ? "bg-slate-950 text-white" : "bg-slate-100 text-slate-950"
      }`}
    >
      <div
        className={`pointer-events-none absolute inset-0 ${
          darkMode
            ? "bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.24),_transparent_38%),radial-gradient(circle_at_bottom_right,_rgba(139,92,246,0.28),_transparent_42%)]"
            : "bg-[radial-gradient(circle_at_top_left,_rgba(96,165,250,0.3),_transparent_38%),radial-gradient(circle_at_bottom_right,_rgba(196,181,253,0.42),_transparent_42%)]"
        }`}
      />
      <div
        className={`pointer-events-none absolute -left-24 top-1/4 h-72 w-72 rounded-full blur-3xl ${
          darkMode ? "bg-blue-500/15" : "bg-blue-400/25"
        }`}
      />
      <div
        className={`pointer-events-none absolute -right-20 bottom-0 h-80 w-80 rounded-full blur-3xl ${
          darkMode ? "bg-violet-500/15" : "bg-violet-300/35"
        }`}
      />

      <button
        type="button"
        onClick={() => setDarkMode((current) => !current)}
        className={`absolute right-5 top-5 z-10 rounded-lg border px-3 py-1.5 text-xs font-medium transition sm:right-7 sm:top-7 ${
          darkMode
            ? "border-white/15 bg-white/10 text-slate-200 hover:bg-white/15"
            : "border-slate-200 bg-white/80 text-slate-700 shadow-sm hover:bg-white"
        }`}
      >
        {darkMode ? "Claro" : "Oscuro"}
      </button>

      <div
        className={`relative grid w-full max-w-4xl overflow-hidden rounded-3xl border shadow-2xl backdrop-blur-xl transition-colors duration-300 md:grid-cols-[1.1fr_0.9fr] ${
          darkMode
            ? "border-white/10 bg-white/[0.06] shadow-black/30"
            : "border-white/70 bg-white/75 shadow-slate-400/20"
        }`}
      >
        <section
          className={`flex min-h-[280px] flex-col justify-center border-b p-8 sm:p-12 md:min-h-[460px] md:border-b-0 md:border-r ${
            darkMode ? "border-white/10" : "border-slate-200/80"
          }`}
        >
          <div className="mb-6">
            <VisionAstraLogo size={60} showText={false} />
          </div>
          <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">
            VisionAstra
          </h1>
          <p
            className={`mt-4 max-w-sm text-base leading-7 sm:text-lg ${
              darkMode ? "text-slate-300" : "text-slate-600"
            }`}
          >
            Marketing digital con IA para tiendas de peluches Pokémon.
          </p>
          <div className="mt-8 flex justify-center md:mt-10">
            <img
              src={loginHero}
              alt="Ilustración de VisionAstra"
              className="max-h-64 w-full max-w-sm object-contain sm:max-h-72 md:max-w-md"
            />
          </div>
        </section>

        <section
          className={`flex items-center p-5 sm:p-8 md:p-10 ${
            darkMode ? "bg-slate-950/25" : "bg-white/35"
          }`}
        >
          <Card className="w-full border-0 bg-transparent shadow-none">
            <CardContent className="space-y-6 p-0">
              <div>
                <h2
                  className={`text-2xl font-semibold tracking-tight ${
                    darkMode ? "text-white" : "text-slate-950"
                  }`}
                >
                  Iniciar sesión
                </h2>
              </div>

              <div className="space-y-4">
                <label
                  className={`block space-y-2 text-sm font-medium ${
                    darkMode ? "text-slate-200" : "text-slate-700"
                  }`}
                >
                  <span>Correo electrónico</span>
                  <input
                    type="email"
                    placeholder="correo@ejemplo.com"
                    className={`h-11 w-full rounded-xl border px-3 text-sm outline-none transition placeholder:text-slate-400 focus:border-blue-400/70 focus:ring-4 focus:ring-blue-400/10 ${
                      darkMode
                        ? "border-white/10 bg-white/10 text-white"
                        : "border-slate-200 bg-white text-slate-950 shadow-sm"
                    }`}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </label>

                <label
                  className={`block space-y-2 text-sm font-medium ${
                    darkMode ? "text-slate-200" : "text-slate-700"
                  }`}
                >
                  <span>Contraseña</span>
                  <input
                    type="password"
                    placeholder="Ingresa tu contraseña"
                    className={`h-11 w-full rounded-xl border px-3 text-sm outline-none transition placeholder:text-slate-400 focus:border-blue-400/70 focus:ring-4 focus:ring-blue-400/10 ${
                      darkMode
                        ? "border-white/10 bg-white/10 text-white"
                        : "border-slate-200 bg-white text-slate-950 shadow-sm"
                    }`}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </label>
              </div>

              <Button
                className="h-11 w-full rounded-xl bg-gradient-to-r from-blue-500 to-violet-500 font-medium text-white shadow-lg shadow-blue-950/30 transition hover:from-blue-400 hover:to-violet-400"
                onClick={handleLogin}
              >
                Ingresar
              </Button>
            </CardContent>
          </Card>
        </section>
      </div>
    </main>
  );
}
