import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, setUnauthorizedHandler, tokenStore } from '../api'

export type Role = 'READER' | 'DIGITAL_ARCHIVE_DEPARTMENT' | 'BOOK_DELIVERY_DEPARTMENT' | 'ADMIN'

export interface CurrentUser {
  id: string
  username: string
  name: string
  roles: Role[]
}

interface AuthResponse {
  token: string
  type: string
  expiresInSeconds: number
}

interface AuthContextValue {
  user: CurrentUser | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  hasAnyRole: (...roles: Role[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(() => tokenStore.get() !== null)

  const logout = useCallback(() => {
    tokenStore.clear()
    setUser(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    if (tokenStore.get()) {
      api<CurrentUser>('/api/v1/auth/me')
        .then(setUser)
        .catch(logout)
        .finally(() => setLoading(false))
    }
  }, [logout])

  const login = useCallback(async (username: string, password: string) => {
    const { token } = await api<AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
    tokenStore.set(token)
    setUser(await api<CurrentUser>('/api/v1/auth/me'))
  }, [])

  const hasAnyRole = useCallback(
    (...roles: Role[]) => user !== null && user.roles.some(role => roles.includes(role)),
    [user],
  )

  if (loading) return null

  return (
    <AuthContext.Provider value={{ user, login, logout, hasAnyRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}