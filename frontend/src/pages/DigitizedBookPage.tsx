import { useEffect, useRef, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { api, ApiError } from '../api'
import { REDIRECT_KEY } from '../auth/guards'
import BookCover from '../components/BookCover'
import Layout from '../components/Layout'
import PdfViewer from '../components/PdfViewer'
import type { DigitizedBook } from '../digitizedBooks'
import { useTitle } from '../useTitle'
import NotFoundPage from './NotFoundPage'
import { TypeBadge } from './DigitizedBooksPage'

export default function DigitizedBookPage() {
  const { id } = useParams()
  const [book, setBook] = useState<DigitizedBook | null>(null)
  const [status, setStatus] = useState<number>()
  const readerRef = useRef<HTMLDivElement>(null)
  useTitle(book?.name ?? 'Оцифрована книга')

  useEffect(() => {
    api<DigitizedBook>(`/api/v1/digitized-books/${id}`).then(setBook, e => {
      if (!(e instanceof ApiError)) throw e
      setStatus(e.status)
    })
  }, [id])

  // copyrighted books are returned only to logged-in users
  if (status === 401) {
    sessionStorage.setItem(REDIRECT_KEY, `/digitized-books/${id}`)
    return <Navigate to="/login" replace />
  }
  if (status === 404) return <NotFoundPage />
  if (!book) return <Layout>{null}</Layout>

  return (
    <Layout>
      <nav className="breadcrumbs" aria-label="Навігація">
        <Link to="/digitized-books">Оцифровані книги</Link> / <span>{book.name}</span>
      </nav>

      <div className="book-hero">
        <BookCover id={book.id} name={book.name} authors={book.authors} coverUrl={book.coverUrl} large />
        <div>
          <div className="book-hero__badges">
            <TypeBadge type={book.resourceType} />
            {!book.isCopyrighted && <span className="badge badge--muted">Вільний доступ</span>}
          </div>
          <h1 className="page-title">{book.name}</h1>
          <p className="book-hero__authors">{book.authors.join(', ')}</p>

          <dl className="card details">
            <div><dt>Рік публікації</dt><dd>{book.publishingYear}</dd></div>
            <div><dt>Мова</dt><dd>{book.language}</dd></div>
            <div><dt>Тип ресурсу</dt><dd>{book.resourceType}</dd></div>
            <div><dt>Авторське право</dt><dd>{book.isCopyrighted ? 'Так' : 'Ні'}</dd></div>
          </dl>

          <div className="book-hero__actions">
            <button className="btn btn--primary" type="button"
                    onClick={() => readerRef.current?.scrollIntoView({ behavior: 'smooth' })}>Читати</button>
            <button className="btn btn--secondary" type="button" disabled title="Незабаром">♡ Додати в улюблене</button>
          </div>
        </div>
      </div>

      <div ref={readerRef}>
        <PdfViewer url={book.fileUrl} />
      </div>
    </Layout>
  )
}