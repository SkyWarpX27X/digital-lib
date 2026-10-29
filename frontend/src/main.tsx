import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { RequireAuth, RequireRole } from './auth/guards'
import AddPhysicalBookPage from './pages/AddPhysicalBookPage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import PhysicalBooksPage from './pages/PhysicalBooksPage'
import './app.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<Navigate to="/physical-books" replace />} />
          <Route path="/physical-books" element={<RequireAuth><PhysicalBooksPage /></RequireAuth>} />
          <Route path="/physical-books/new" element={
            <RequireAuth>
              <RequireRole roles={['BOOK_DELIVERY_DEPARTMENT', 'ADMIN']}><AddPhysicalBookPage /></RequireRole>
            </RequireAuth>
          } />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)