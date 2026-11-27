import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api, ApiError } from '../api'
import { ROLE_LABELS, useAuth, type Role } from '../auth/AuthContext'
import Layout from '../components/Layout'
import { useTitle } from '../useTitle'

interface User {
  id: string
  username: string
  name: string
  surname: string
  patronymic?: string | null
  role: Role
  isActive: boolean
}

export const ROLES = Object.keys(ROLE_LABELS) as Role[]

export function fullName(person: { surname: string; name: string; patronymic?: string | null }) {
  return [person.surname, person.name, person.patronymic].filter(Boolean).join(' ')
}

export default function UsersPage() {
  useTitle('Користувачі')
  const { user: me } = useAuth()
  const location = useLocation()
  const [message, setMessage] = useState<string | undefined>(location.state?.message)
  const [error, setError] = useState<string>()
  const [users, setUsers] = useState<User[] | null>(null)
  const [query, setQuery] = useState('')
  const [role, setRole] = useState('')

  useEffect(() => {
    api<User[]>('/api/v1/users').then(setUsers)
  }, [])

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase()
    return (users ?? [])
      .filter(u => !role || u.role === role)
      .filter(u => !q || [fullName(u), u.username].some(value => value.toLowerCase().includes(q)))
      .sort((a, b) => fullName(a).localeCompare(fullName(b), 'uk'))
  }, [users, query, role])

  const update = async (target: User, change: { role?: Role; isActive?: boolean }, success: string) => {
    try {
      const updated = await api<User>(`/api/v1/users/${target.id}`, { method: 'PATCH', body: JSON.stringify(change) })
      setUsers(current => current?.map(u => u.id === updated.id ? updated : u) ?? null)
      setMessage(success)
      setError(undefined)
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError(e.detail)
      setMessage(undefined)
    }
  }

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">Користувачі</h1>
          {users && <p className="page-subtitle">Усього: {users.length}</p>}
        </div>
        <Link className="btn btn--primary" to="/users/new">+ Новий користувач</Link>
      </div>
      {message && <div className="alert alert--success" role="status">{message}</div>}
      {error && <div className="alert alert--error" role="alert">{error}</div>}

      <div className="filters">
        <input className="input filters__search" type="search" placeholder="ПІБ або логін"
               aria-label="Пошук за ПІБ або логіном" value={query} onChange={e => setQuery(e.target.value)} />
        <select className="input filters__select" aria-label="Роль" value={role} onChange={e => setRole(e.target.value)}>
          <option value="">Роль: усі</option>
          {ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
        </select>
      </div>

      <div className="card">
        <table className="table">
          <thead>
          <tr>
            <th>ПІБ</th>
            <th>Логін</th>
            <th>Роль</th>
            <th>Статус</th>
            <th className="table__actions">Дії</th>
          </tr>
          </thead>
          <tbody>
          {visible.map(u => {
            // the admin can't demote or deactivate themselves
            const self = u.id === me?.id
            return (
              <tr key={u.id}>
                <td className="table__primary">{fullName(u)}</td>
                <td>{u.username}</td>
                <td>
                  <select className="input" aria-label={`Роль користувача ${u.username}`} value={u.role} disabled={self}
                          onChange={e => update(u, { role: e.target.value as Role }, 'Роль змінено')}>
                    {ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
                  </select>
                </td>
                <td>
                  <span className={'badge ' + (u.isActive ? 'badge--primary' : 'badge--muted')}>
                    {u.isActive ? 'Активний' : 'Деактивований'}
                  </span>
                </td>
                <td className="table__actions">
                  <button className="btn btn--secondary" type="button" disabled={self}
                          onClick={() => update(u, { isActive: !u.isActive },
                            u.isActive ? 'Обліковий запис деактивовано' : 'Обліковий запис активовано')}>
                    {u.isActive ? 'Деактивувати' : 'Активувати'}
                  </button>
                </td>
              </tr>
            )
          })}
          {users && visible.length === 0 && (
            <tr className="table__empty">
              <td colSpan={5}>Користувачів не знайдено</td>
            </tr>
          )}
          </tbody>
        </table>
      </div>
    </Layout>
  )
}
