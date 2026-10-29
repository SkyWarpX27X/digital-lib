import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth, type Role } from './AuthContext'
import ForbiddenPage from '../pages/ForbiddenPage'

export const REDIRECT_KEY = 'redirectAfterLogin'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) {
    sessionStorage.setItem(REDIRECT_KEY, location.pathname)
    return <Navigate to="/login" replace />
  }
  return children
}

export function RequireRole({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const { hasAnyRole } = useAuth()
  return hasAnyRole(...roles) ? children : <ForbiddenPage />
}