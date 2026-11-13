import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api, ApiError } from '../api'
import { useAuth } from '../auth/AuthContext'
import { STAFF_ROLES, type BookOrder } from '../bookOrders'
import Layout from '../components/Layout'
import { useTitle } from '../useTitle'

export default function BookOrdersPage() {
  const { hasAnyRole } = useAuth()
  const staff = hasAnyRole(...STAFF_ROLES)
  const title = staff ? 'Усі замовлення' : 'Мої замовлення'
  useTitle(title)
  const location = useLocation()
  const [message, setMessage] = useState<string | undefined>(location.state?.message)
  const [error, setError] = useState<string>()
  const [orders, setOrders] = useState<BookOrder[] | null>(null)
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')

  useEffect(() => {
    api<BookOrder[]>(staff ? '/api/v1/book-orders?onlyOpen=false' : '/api/v1/book-orders/my').then(setOrders)
  }, [staff])

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase()
    return (orders ?? [])
      .filter(order => !status || order.isOpen === (status === 'open'))
      .filter(order => !q || [order.bookName, order.bookAuthors, staff ? order.creatorName : null]
        .some(value => value?.toLowerCase().includes(q)))
      .sort((a, b) => Number(b.isOpen) - Number(a.isOpen))
  }, [orders, query, status, staff])

  const handleClose = async (order: BookOrder) => {
    try {
      const updated = await api<BookOrder>(`/api/v1/book-orders/${order.id}`,
        { method: 'PATCH', body: JSON.stringify({ isOpen: false }) })
      setOrders(current => current?.map(o => o.id === updated.id ? updated : o) ?? null)
      setMessage(staff ? 'Замовлення закрито' : 'Замовлення скасовано')
      setError(undefined)
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError(e.detail)
    }
  }

  const openCount = orders?.filter(o => o.isOpen).length ?? 0

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">{title}</h1>
          {orders && <p className="page-subtitle">Відкритих: {openCount} із {orders.length}</p>}
        </div>
        {!staff && <Link className="btn btn--primary" to="/book-orders/new">+ Нове замовлення</Link>}
      </div>
      {message && <div className="alert alert--success" role="status">{message}</div>}
      {error && <div className="alert alert--error" role="alert">{error}</div>}

      <div className="filters">
        <input className="input filters__search" type="search"
               placeholder={staff ? 'Пошук за книгою або читачем' : 'Пошук за назвою або автором'}
               aria-label="Пошук" value={query} onChange={e => setQuery(e.target.value)} />
        <select className="input filters__select" aria-label="Статус" value={status}
                onChange={e => setStatus(e.target.value)}>
          <option value="">Усі статуси</option>
          <option value="open">Відкрите</option>
          <option value="closed">Закрите</option>
        </select>
      </div>

      <div className="card">
        <table className="table">
          <thead>
          <tr>
            <th>Книга</th>
            {staff && <th>Читач</th>}
            <th>Email для доставки</th>
            <th>Статус</th>
            <th className="table__actions">Дії</th>
          </tr>
          </thead>
          <tbody>
          {visible.map(order => (
            <tr key={order.id}>
              <td>
                <div className="table__primary">{order.bookName ?? '—'}</div>
                {order.bookAuthors && <div className="field__hint">{order.bookAuthors}</div>}
              </td>
              {staff && <td>{order.creatorName}</td>}
              <td>{order.emailForDelivery}</td>
              <td>
                <span className={'badge ' + (order.isOpen ? 'badge--old' : 'badge--muted')}>
                  {order.isOpen ? 'Відкрите' : 'Закрите'}
                </span>
              </td>
              <td className="table__actions">
                {order.isOpen && (
                  <button className="btn btn--secondary" type="button" onClick={() => handleClose(order)}>
                    {staff ? '✓ Закрити' : '✕ Скасувати'}
                  </button>
                )}
              </td>
            </tr>
          ))}
          {orders && visible.length === 0 && (
            <tr className="table__empty">
              <td colSpan={staff ? 5 : 4}>Замовлення відсутні</td>
            </tr>
          )}
          </tbody>
        </table>
      </div>

      <p className="field__hint page-note">
        {staff
          ? 'Закрите замовлення не можна відкрити знову. Замовлення закривається автоматично, коли книгу оцифровано.'
          : 'Скасоване замовлення стає закритим і більше не відкривається. Замовлення також закривається автоматично, коли книгу оцифровано.'}
      </p>
    </Layout>
  )
}
