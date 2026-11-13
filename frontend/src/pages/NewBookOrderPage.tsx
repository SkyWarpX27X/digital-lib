import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, ApiError } from '../api'
import { useAuth } from '../auth/AuthContext'
import type { BookOrder, BookOrderRequest } from '../bookOrders'
import Layout from '../components/Layout'
import type { PhysicalBook } from '../physicalBooks'
import { useTitle } from '../useTitle'

export default function NewBookOrderPage() {
  useTitle('Нове замовлення')
  const { user } = useAuth()
  const navigate = useNavigate()
  const [cardEmail, setCardEmail] = useState<string | null>()
  const [books, setBooks] = useState<PhysicalBook[]>([])
  const [book, setBook] = useState('')
  const [email, setEmail] = useState('')
  const [error, setError] = useState<string>()

  useEffect(() => {
    api<{ email: string }>(`/api/v1/users/${user!.id}/library-card`)
      .then(card => setCardEmail(card.email))
      .catch(e => {
        if (!(e instanceof ApiError) || e.status !== 404) throw e
        setCardEmail(null)
      })
    api<PhysicalBook[]>('/api/v1/physical-books').then(setBooks)
  }, [user])

  const noCard = cardEmail === null

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (!book) return setError('Оберіть книгу')
    const request: BookOrderRequest = { book, emailForDelivery: email.trim() || null, isOpen: true }
    try {
      await api<BookOrder>('/api/v1/book-orders', { method: 'POST', body: JSON.stringify(request) })
      navigate('/book-orders', { state: { message: 'Замовлення створено' } })
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError(Object.values(e.errors)[0] ?? e.detail)
    }
  }

  return (
    <Layout>
      <nav className="breadcrumbs" aria-label="Навігація">
        <Link to="/book-orders">Мої замовлення</Link> / <span>Нове замовлення</span>
      </nav>
      <h1 className="page-title">Замовити електронну доставку книги</h1>

      <div className="form-layout">
        <form className="card form-card" onSubmit={handleSubmit}>
          <div className="form-card__body">
            {noCard && <div className="alert alert--error" role="alert">Замовлення доступне лише читачам із карткою бібліотеки.</div>}
            {error && <div className="alert alert--error" role="alert">{error}</div>}

            <dl className="details reader-summary">
              <div><dt>Читач</dt><dd>{user?.name}</dd></div>
              <div><dt>Пошта з картки читача</dt><dd>{cardEmail ?? '—'}</dd></div>
            </dl>

            <div className="field">
              <label className="field__label" htmlFor="book">Книга</label>
              <select id="book" className="input" value={book} disabled={noCard}
                      onChange={e => setBook(e.target.value)}>
                <option value="">Оберіть книгу…</option>
                {books.map(b => (
                  <option key={b.id} value={b.id}>{b.name} — {b.authors.join(', ')}, {b.publishingYear}</option>
                ))}
              </select>
              <p className="field__hint">Показано книги, яких ще немає в оцифрованому вигляді.</p>
            </div>

            <div className="field">
              <label className="field__label" htmlFor="email">Електронна пошта для доставки</label>
              <input id="email" className="input" type="email" value={email} disabled={noCard}
                     placeholder={cardEmail ?? undefined} onChange={e => setEmail(e.target.value)} />
              <p className="field__hint">Необов'язково. Якщо залишити порожнім, книгу буде надіслано на пошту з вашої картки.</p>
            </div>
          </div>

          <div className="form-card__actions">
            <Link className="btn btn--secondary" to="/book-orders">Скасувати</Link>
            <button className="btn btn--primary" type="submit" disabled={noCard}>Замовити</button>
          </div>
        </form>

        <aside className="info-box info-box--side">
          <strong>ⓘ Як працює замовлення</strong>
          <ul>
            <li>Замовлення доступне читачам із карткою бібліотеки.</li>
            <li>Нове замовлення одразу має статус «Відкрите».</li>
            <li>Доки книгу не оцифровано, її можуть замовити й інші читачі, і кожен отримає її на свою пошту.</li>
            <li>Коли книгу оцифрують, замовлення закриється автоматично.</li>
            <li>Закрите замовлення не можна відкрити знову.</li>
          </ul>
        </aside>
      </div>
    </Layout>
  )
}
