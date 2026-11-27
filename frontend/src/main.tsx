import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { HOME, RequireAuth, RequireRole } from './auth/guards'
import { STAFF_ROLES } from './bookOrders'
import AddPhysicalBookPage from './pages/AddPhysicalBookPage'
import BookOrdersPage from './pages/BookOrdersPage'
import NewBookOrderPage from './pages/NewBookOrderPage'
import DigitizedBookFormPage from './pages/DigitizedBookFormPage'
import DigitizedBookPage from './pages/DigitizedBookPage'
import DigitizedBooksPage, { EDITOR_ROLES } from './pages/DigitizedBooksPage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import PhysicalBooksPage from './pages/PhysicalBooksPage'
import ProfilePage from './pages/ProfilePage'
import RegisterPage from './pages/RegisterPage'
import LibraryCardsPage from './pages/LibraryCardsPage'
import UserFormPage from './pages/UserFormPage'
import UsersPage from './pages/UsersPage'
import './app.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<Navigate to={HOME} replace />} />
          <Route path="/profile" element={<RequireAuth><ProfilePage /></RequireAuth>} />
          <Route path="/physical-books" element={<RequireAuth><PhysicalBooksPage /></RequireAuth>} />
          <Route path="/physical-books/new" element={
            <RequireAuth>
              <RequireRole roles={['BOOK_DELIVERY_DEPARTMENT', 'ADMIN']}><AddPhysicalBookPage /></RequireRole>
            </RequireAuth>
          } />
          {/* public, like GET /api/v1/digitized-books; copyrighted books redirect to login from the page */}
          <Route path="/digitized-books" element={<DigitizedBooksPage />} />
          <Route path="/digitized-books/:id" element={<DigitizedBookPage />} />
          {['/digitized-books/new', '/digitized-books/:id/edit'].map(path => (
            <Route key={path} path={path} element={
              <RequireAuth>
                <RequireRole roles={[...EDITOR_ROLES]}><DigitizedBookFormPage /></RequireRole>
              </RequireAuth>
            } />
          ))}
          <Route path="/book-orders" element={
            <RequireAuth>
              <RequireRole roles={['READER', ...STAFF_ROLES]}><BookOrdersPage /></RequireRole>
            </RequireAuth>
          } />
          <Route path="/book-orders/new" element={
            <RequireAuth>
              <RequireRole roles={['READER']}><NewBookOrderPage /></RequireRole>
            </RequireAuth>
          } />
          <Route path="/users" element={
            <RequireAuth>
              <RequireRole roles={['ADMIN']}><UsersPage /></RequireRole>
            </RequireAuth>
          } />
          <Route path="/users/new" element={
            <RequireAuth>
              <RequireRole roles={['ADMIN']}><UserFormPage /></RequireRole>
            </RequireAuth>
          } />
          <Route path="/library-cards" element={
            <RequireAuth>
              <RequireRole roles={['ADMIN']}><LibraryCardsPage /></RequireRole>
            </RequireAuth>
          } />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)