import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api, ApiError } from '../api'
import { useAuth } from '../auth/AuthContext'
import BookCover from '../components/BookCover'
import Layout from '../components/Layout'
import { RESOURCE_TYPES, type DigitizedBook } from '../digitizedBooks'
import { useTitle } from '../useTitle'

export const EDITOR_ROLES = ['DIGITAL_ARCHIVE_DEPARTMENT', 'BOOK_DELIVERY_DEPARTMENT', 'ADMIN'] as const

function booksWord(count: number) {
  const mod10 = count % 10
  const mod100 = count % 100
  if (mod10 === 1 && mod100 !== 11) return 'книга'
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return 'книги'
  return 'книг'
}

export default function DigitizedBooksPage() {
  useTitle('Оцифровані книги')
  const { hasAnyRole } = useAuth()
  const location = useLocation()
  const [message, setMessage] = useState<string | undefined>(location.state?.message)
  const [error, setError] = useState<string>()
  const [books, setBooks] = useState<DigitizedBook[] | null>(null)
  const [query, setQuery] = useState('')
  const [type, setType] = useState('')
  const [newestFirst, setNewestFirst] = useState(true)

  useEffect(() => {
    api<DigitizedBook[]>('/api/v1/digitized-books').then(setBooks)
  }, [])

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase()
    return (books ?? [])
      .filter(book => !type || book.resourceType === type)
      .filter(book => !q || book.name.toLowerCase().includes(q)
        || book.authors.some(author => author.toLowerCase().includes(q)))
      .sort((a, b) => (Number(a.publishingYear) - Number(b.publishingYear)) * (newestFirst ? -1 : 1))
  }, [books, query, type, newestFirst])

  const canEdit = hasAnyRole(...EDITOR_ROLES)
  const canDelete = hasAnyRole('ADMIN')

  const handleDelete = async (book: DigitizedBook) => {
    if (!confirm(`Видалити «${book.name}»?`)) return
    try {
      await api<void>(`/api/v1/digitized-books/${book.id}`, { method: 'DELETE' })
      setBooks(current => current?.filter(b => b.id !== book.id) ?? null)
      setMessage('Книгу видалено')
      setError(undefined)
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError(e.detail)
    }
  }

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">Оцифровані книги</h1>
          {books && <p className="page-subtitle">{books.length} {booksWord(books.length)} у цифровому фонді</p>}
        </div>
        {canEdit && <Link className="btn btn--primary" to="/digitized-books/new">+ Додати оцифровану книгу</Link>}
      </div>
      {message && <div className="alert alert--success" role="status">{message}</div>}
      {error && <div className="alert alert--error" role="alert">{error}</div>}

      <div className="filters">
        <input className="input filters__search" type="search" placeholder="Пошук за назвою або автором"
               aria-label="Пошук за назвою або автором" value={query} onChange={e => setQuery(e.target.value)} />
        <select className="input filters__select" aria-label="Тип ресурсу" value={type}
                onChange={e => setType(e.target.value)}>
          <option value="">Усі типи ресурсу</option>
          {RESOURCE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
        <select className="input filters__select" aria-label="Сортування" value={newestFirst ? 'new' : 'old'}
                onChange={e => setNewestFirst(e.target.value === 'new')}>
          <option value="new">Спочатку нові</option>
          <option value="old">Спочатку старі</option>
        </select>
      </div>

      <div className="card">
        <table className="table">
          <thead>
          <tr>
            <th>Назва</th>
            <th>Автори</th>
            <th>Тип ресурсу</th>
            <th>Рік публікації</th>
            <th>Мова</th>
            <th>Авторське право</th>
            <th className="table__actions">Дії</th>
          </tr>
          </thead>
          <tbody>
          {visible.map(book => (
            <tr key={book.id}>
              <td>
                <div className="book-cell">
                  <BookCover id={book.id} name={book.name} authors={book.authors} coverUrl={book.coverUrl} />
                  <span className="table__primary">{book.name}</span>
                </div>
              </td>
              <td>{book.authors.join(', ')}</td>
              <td><TypeBadge type={book.resourceType} /></td>
              <td>{book.publishingYear}</td>
              <td>{book.language}</td>
              <td><CopyrightBadge copyrighted={book.isCopyrighted} /></td>
              <td className="table__actions">
                <Link className="icon-btn" to={`/digitized-books/${book.id}`} aria-label={`Переглянути «${book.name}»`}>👁</Link>
                {canEdit && <Link className="icon-btn" to={`/digitized-books/${book.id}/edit`}
                                  aria-label={`Редагувати «${book.name}»`}>✎</Link>}
                {canDelete && <button className="icon-btn icon-btn--danger" type="button" onClick={() => handleDelete(book)}
                                      aria-label={`Видалити «${book.name}»`}>🗑</button>}
              </td>
            </tr>
          ))}
          {books && visible.length === 0 && (
            <tr className="table__empty">
              <td colSpan={7}>Книги відсутні</td>
            </tr>
          )}
          </tbody>
        </table>
      </div>
    </Layout>
  )
}

export function TypeBadge({ type }: { type: string }) {
  return <span className={'badge ' + (type === 'Стародрук' ? 'badge--old' : 'badge--primary')}>{type}</span>
}

export function CopyrightBadge({ copyrighted }: { copyrighted: boolean }) {
  return <span className={'badge ' + (copyrighted ? 'badge--danger' : 'badge--muted')}>{copyrighted ? 'Так' : 'Ні'}</span>
}