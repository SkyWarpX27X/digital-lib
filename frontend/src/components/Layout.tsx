import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import Brand from './Brand'

const navClass = ({ isActive }: { isActive: boolean }) => 'nav__link' + (isActive ? ' nav__link--active' : '')

export default function Layout({ children, narrow = false }: { children: ReactNode; narrow?: boolean }) {
  const { user, logout, hasAnyRole } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <Brand />

        <nav className="nav">
          <NavLink className={navClass} to="/digitized-books">Оцифровані книги</NavLink>
          <NavLink className={navClass} to="/physical-books">Фізичні книги</NavLink>
          {hasAnyRole('BOOK_DELIVERY_DEPARTMENT', 'ADMIN') &&
            <NavLink className={navClass} to="/book-orders">Замовлення</NavLink>}
          {hasAnyRole('ADMIN') && <NavLink className={navClass} to="/users">Користувачі</NavLink>}
          {hasAnyRole('ADMIN') && <NavLink className={navClass} to="/library-cards">Картки читачів</NavLink>}
        </nav>

        {user && (
          <div className="sidebar__user">
            <div>
              <div className="sidebar__user-name">{user.name}</div>
              <div className="sidebar__user-role">{user.username}</div>
            </div>
            <button className="icon-btn" type="button" onClick={handleLogout}>Вийти</button>
          </div>
        )}
      </aside>

      <main className={narrow ? 'content content--narrow' : 'content'}>{children}</main>
    </div>
  )
}