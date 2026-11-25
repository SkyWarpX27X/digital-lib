import { useEffect, useRef, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { api, ApiError } from '../api'
import { useAuth } from '../auth/AuthContext'
import { REDIRECT_KEY } from '../auth/guards'
import BookCover from '../components/BookCover'
import Layout from '../components/Layout'
import PdfViewer from '../components/PdfViewer'
import type { Bookmark, DigitizedBook } from '../digitizedBooks'
import { useTitle } from '../useTitle'
import NotFoundPage from './NotFoundPage'
import { TypeBadge } from './DigitizedBooksPage'

export default function DigitizedBookPage() {
  const { id } = useParams()
  const [book, setBook] = useState<DigitizedBook | null>(null)
  const [status, setStatus] = useState<number>()
  const { user, hasAnyRole } = useAuth()
  const reader = hasAnyRole('READER')
  const readerRef = useRef<HTMLDivElement>(null)
  const initialPage = Number(useSearchParams()[0].get('page')) || 1
  const [isFavorite, setIsFavorite] = useState(false)
  // null = no bookmark for this book yet
  const [bookmarkPage, setBookmarkPage] = useState<number | null>(null)
  const [message, setMessage] = useState<string>()
  useTitle(book?.name ?? 'Оцифрована книга')

  useEffect(() => {
    api<DigitizedBook>(`/api/v1/digitized-books/${id}`).then(setBook, e => {
      if (!(e instanceof ApiError)) throw e
      setStatus(e.status)
    })
  }, [id])

  const bookmarkUrl = `/api/v1/bookmark/${user?.id}/${id}`
  useEffect(() => {
    if (!reader) return
    api<DigitizedBook[]>('/api/v1/favorites').then(list => setIsFavorite(list.some(b => b.id === id)))
    api<Bookmark>(bookmarkUrl).then(b => setBookmarkPage(b.pageNumber), e => {
      if (!(e instanceof ApiError) || e.status !== 404) throw e
      setBookmarkPage(null)
    })
  }, [reader, id, bookmarkUrl])

  const toggleFavorite = async () => {
    await api(`/api/v1/favorites/${id}`, { method: isFavorite ? 'DELETE' : 'PUT' })
    setIsFavorite(!isFavorite)
  }

  const saveBookmark = async (pageNumber: number) => {
    if (bookmarkPage === pageNumber) return setMessage(`Закладка вже стоїть на сторінці ${pageNumber}`)
    if (bookmarkPage !== null && !window.confirm(
      `У вас уже є закладка в цій книзі на сторінці ${bookmarkPage}. Нова закладка на сторінці ${pageNumber} замінить її. Продовжити?`))
      return
    if (bookmarkPage === null)
      await api('/api/v1/bookmark', { method: 'POST', body: JSON.stringify({ bookId: id, pageNumber }) })
    else
      await api(bookmarkUrl, { method: 'PATCH', body: JSON.stringify({ pageNumber }) })
    setBookmarkPage(pageNumber)
    setMessage(`Закладку збережено на сторінці ${pageNumber}`)
  }

  const removeBookmark = async () => {
    if (!window.confirm(`Видалити закладку на сторінці ${bookmarkPage}?`)) return
    await api(bookmarkUrl, { method: 'DELETE' })
    setBookmarkPage(null)
    setMessage('Закладку видалено')
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
            {reader && (
              <button className="btn btn--secondary" type="button" aria-pressed={isFavorite} onClick={toggleFavorite}>
                {isFavorite ? '♥ В улюбленому' : '♡ Додати в улюблене'}
              </button>
            )}
          </div>
        </div>
      </div>

      <div ref={readerRef}>
        {message && <div className="alert alert--success" role="status">{message}</div>}
        {book.pdfUrl
          ? <PdfViewer url={book.pdfUrl} initialPage={initialPage}
                       bookmarkPage={reader ? bookmarkPage : undefined}
                       onSaveBookmark={reader ? saveBookmark : undefined}
                       onRemoveBookmark={reader ? removeBookmark : undefined} />
          : !user
            ? <div className="alert alert--info">
                Книга захищена авторським правом. <Link to="/login"
                  onClick={() => sessionStorage.setItem(REDIRECT_KEY, `/digitized-books/${id}`)}>Увійдіть, щоб читати</Link>.
              </div>
            : hasAnyRole('READER') && (
              <div className="alert alert--info">
                Книга захищена авторським правом і доступна лише читачам із читацьким квитком.{' '}
                <Link to="/profile">Оформіть читацький квиток</Link>.
              </div>
            )}
      </div>
    </Layout>
  )
}