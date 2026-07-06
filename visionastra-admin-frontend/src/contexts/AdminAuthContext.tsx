/* eslint-disable react-refresh/only-export-components */

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react"
import { AxiosError } from "axios"

import {
  getCurrentAdmin,
  loginAdmin,
  logoutAdmin,
} from "@/services/adminAuthService"
import type { AdminUser } from "@/types/adminAuth"

type AdminAuthContextValue = {
  admin: AdminUser | null
  checkingSession: boolean
  loginLoading: boolean
  logoutLoading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshAdmin: () => Promise<void>
}

const AdminAuthContext = createContext<AdminAuthContextValue | undefined>(
  undefined
)

function isUnauthorizedSessionError(error: unknown) {
  return (
    error instanceof AxiosError &&
    (error.response?.status === 401 || error.response?.status === 403)
  )
}

export function AdminAuthProvider({ children }: { children: ReactNode }) {
  const [admin, setAdmin] = useState<AdminUser | null>(null)
  const [checkingSession, setCheckingSession] = useState(true)
  const [loginLoading, setLoginLoading] = useState(false)
  const [logoutLoading, setLogoutLoading] = useState(false)

  const refreshAdmin = useCallback(async () => {
    const currentAdmin = await getCurrentAdmin()
    setAdmin(currentAdmin)
  }, [])

  useEffect(() => {
    let isMounted = true

    async function checkSession() {
      setCheckingSession(true)

      try {
        const currentAdmin = await getCurrentAdmin()

        if (isMounted) {
          setAdmin(currentAdmin)
        }
      } catch (error) {
        if (isMounted && isUnauthorizedSessionError(error)) {
          setAdmin(null)
        }
      } finally {
        if (isMounted) {
          setCheckingSession(false)
        }
      }
    }

    void checkSession()

    return () => {
      isMounted = false
    }
  }, [])

  const login = useCallback(
    async (username: string, password: string) => {
      setLoginLoading(true)

      try {
        await loginAdmin({ username, password })
        await refreshAdmin()
      } finally {
        setLoginLoading(false)
      }
    },
    [refreshAdmin]
  )

  const logout = useCallback(async () => {
    setLogoutLoading(true)

    try {
      await logoutAdmin()
    } catch {
      setAdmin(null)
    } finally {
      setAdmin(null)
      setLogoutLoading(false)
    }
  }, [])

  const value = useMemo(
    () => ({
      admin,
      checkingSession,
      loginLoading,
      logoutLoading,
      login,
      logout,
      refreshAdmin,
    }),
    [
      admin,
      checkingSession,
      login,
      loginLoading,
      logout,
      logoutLoading,
      refreshAdmin,
    ]
  )

  return (
    <AdminAuthContext.Provider value={value}>
      {children}
    </AdminAuthContext.Provider>
  )
}

export function useAdminAuth() {
  const context = useContext(AdminAuthContext)

  if (!context) {
    throw new Error("useAdminAuth debe usarse dentro de AdminAuthProvider.")
  }

  return context
}
