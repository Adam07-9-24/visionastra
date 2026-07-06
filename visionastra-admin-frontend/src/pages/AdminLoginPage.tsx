import { AxiosError } from "axios"
import {
  AlertCircle,
  Loader2,
  LockKeyhole,
  LogIn,
  ShieldCheck,
  UserRound,
} from "lucide-react"
import { type FormEvent, useState } from "react"
import { Navigate, useNavigate } from "react-router-dom"
import { toast } from "sonner"

import VisionAstraLogo from "@/components/branding/VisionAstraLogo"
import { ThemeToggle } from "@/components/theme-toggle"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useAdminAuth } from "@/contexts/AdminAuthContext"

function extractLoginError(error: unknown) {
  if (error instanceof AxiosError) {
    const data = error.response?.data as
      | { mensaje?: string; detail?: string }
      | undefined

    return (
      data?.mensaje ??
      data?.detail ??
      "No se pudo iniciar sesión. Verifica tus credenciales."
    )
  }

  if (error instanceof Error) {
    return error.message
  }

  return "No se pudo iniciar sesión. Verifica tus credenciales."
}

export function AdminLoginPage() {
  const navigate = useNavigate()
  const { admin, checkingSession, login, loginLoading } = useAdminAuth()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")

  if (!checkingSession && admin) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError("")

    const cleanUsername = username.trim()

    if (!cleanUsername || !password) {
      setError("Usuario y contraseña son obligatorios")
      return
    }

    try {
      await login(cleanUsername, password)
      toast.success("Inicio de sesión administrativo correcto")
      setPassword("")
      navigate("/", { replace: true })
    } catch (loginError) {
      const message = extractLoginError(loginError)
      setError(message)
      toast.error(message)
      setPassword("")
    }
  }

  return (
    <main className="relative min-h-screen overflow-hidden bg-background text-foreground">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_18%_18%,rgba(14,165,233,0.16),transparent_30%),radial-gradient(circle_at_84%_24%,rgba(139,92,246,0.13),transparent_28%),linear-gradient(135deg,rgba(14,165,233,0.06),transparent_42%,rgba(139,92,246,0.06))] dark:bg-[radial-gradient(circle_at_18%_18%,rgba(14,165,233,0.18),transparent_32%),radial-gradient(circle_at_84%_24%,rgba(139,92,246,0.20),transparent_30%),linear-gradient(135deg,rgba(14,165,233,0.08),transparent_44%,rgba(139,92,246,0.10))]" />
      <div className="pointer-events-none absolute left-8 top-28 hidden h-40 w-40 rounded-full border border-sky-500/15 blur-sm md:block" />
      <div className="pointer-events-none absolute bottom-12 right-10 hidden h-56 w-56 rounded-full bg-violet-500/10 blur-3xl md:block" />

      <div className="absolute right-4 top-4 z-20 sm:right-6 sm:top-6">
        <ThemeToggle />
      </div>

      <div className="relative z-10 mx-auto grid min-h-screen w-full max-w-6xl items-center gap-8 px-5 py-20 lg:grid-cols-[1.1fr_0.9fr] lg:px-8">
        <section className="order-2 mx-auto w-full max-w-2xl space-y-7 text-center lg:order-1 lg:mx-0 lg:text-left">
          <div className="flex items-center justify-center gap-4 lg:justify-start">
            <VisionAstraLogo
              size={58}
              bgOpacity={0.14}
              showText={false}
              className="text-sky-500 dark:text-sky-400"
            />
            <div>
              <p className="text-xl font-semibold leading-none">
                VisionAstra Admin
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                Panel administrativo
              </p>
            </div>
          </div>

          <div className="inline-flex items-center gap-2 rounded-full border border-sky-500/20 bg-sky-500/10 px-3 py-1 text-sm font-medium text-sky-700 dark:text-sky-300">
            <ShieldCheck className="size-4" />
            Acceso interno autorizado
          </div>

          <div>
            <h1 className="max-w-xl text-3xl font-bold leading-[1.12] tracking-tight text-foreground sm:text-4xl lg:text-[46px]">
              Gestión y supervisión de VisionAstra
            </h1>
            <p className="mx-auto mt-6 max-w-xl text-base leading-7 text-muted-foreground sm:text-lg lg:mx-0">
              Administra usuarios, campañas, publicaciones y recursos desde un
              entorno seguro.
            </p>
          </div>
        </section>

        <section className="order-1 flex justify-center lg:order-2 lg:justify-end">
          <Card className="w-full max-w-[460px] rounded-2xl border-border bg-card/95 text-card-foreground shadow-xl shadow-sky-950/5 backdrop-blur dark:shadow-sky-950/20">
            <CardHeader className="space-y-3">
              <div className="flex size-12 items-center justify-center rounded-xl bg-sky-500/10 text-sky-600 ring-1 ring-sky-500/20 dark:text-sky-400">
                <LockKeyhole className="size-5" />
              </div>
              <div>
                <CardTitle className="text-2xl">
                  Acceso administrativo
                </CardTitle>
                <CardDescription className="mt-1">
                  Ingresa con tu cuenta interna autorizada.
                </CardDescription>
              </div>
            </CardHeader>

            <form onSubmit={handleSubmit}>
              <CardContent className="space-y-5 pb-6">
                {error ? (
                  <Alert variant="destructive">
                    <AlertCircle />
                    <AlertTitle>No se pudo iniciar sesión</AlertTitle>
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                ) : null}

                <div className="space-y-2">
                  <Label htmlFor="username">Usuario</Label>
                  <div className="relative">
                    <UserRound className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                      autoComplete="username"
                      className="h-11 cursor-text bg-background pl-9"
                      disabled={loginLoading}
                      id="username"
                      onChange={(event) => setUsername(event.target.value)}
                      placeholder="Usuario interno"
                      value={username}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="password">Contraseña</Label>
                  <div className="relative">
                    <LockKeyhole className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                      autoComplete="current-password"
                      className="h-11 cursor-text bg-background pl-9"
                      disabled={loginLoading}
                      id="password"
                      onChange={(event) => setPassword(event.target.value)}
                      placeholder="Contraseña"
                      type="password"
                      value={password}
                    />
                  </div>
                </div>
              </CardContent>

              <CardFooter className="border-t-0 bg-transparent pt-0">
                <Button
                  className="h-12 w-full cursor-pointer bg-sky-500 font-semibold text-white shadow-sm transition-colors hover:bg-sky-600 focus-visible:ring-sky-500 disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={loginLoading}
                  type="submit"
                >
                  {loginLoading ? (
                    <>
                      <Loader2 className="animate-spin" />
                      Ingresando...
                    </>
                  ) : (
                    <>
                      <LogIn />
                      Iniciar sesión
                    </>
                  )}
                </Button>
              </CardFooter>
            </form>
          </Card>
        </section>
      </div>
    </main>
  )
}
