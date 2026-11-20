import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth/AuthContext'
import BookFilters, { resourceTypesOf } from '../components/BookFilters'
import Layout from '../components/Layout'
import type { PhysicalBook } from '../physicalBooks'
import { useTitle } from '../useTitle'

export default function PhysicalBooksPage() {
  useTitle('Фізичні книги')
  const { hasAnyRole } = useAuth()
  const location = useLocation()
  const message: string | undefined = location.state?.message
  const [books, setBooks] = useState<PhysicalBook[] | null>(null)
  const [query, setQuery] = useState('')
  const [resourceTypes, setResourceTypes] = useState<string[]>([])

  useEffect(() => {
    let stale = false
    api<PhysicalBook[]>(`/api/v1/physical-books?${query}`).then(result => {
      if (stale) return
      setBooks(result)
      if (!query) setResourceTypes(resourceTypesOf(result))
    })
    return () => { stale = true }
  }, [query])

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">Фізичні книги</h1>
        </div>
        {hasAnyRole('BOOK_DELIVERY_DEPARTMENT', 'ADMIN') &&
          <Link className="btn btn--primary" to="/physical-books/new">Додати фізичну книгу</Link>}
      </div>
      {message && <div className="alert alert--success" role="status">{message}</div>}
      <BookFilters onChange={setQuery} resourceTypes={resourceTypes} />
      <div className="card">
        <table className="table">
          <thead>
          <tr>
            <th>Назва</th>
            <th>Автори</th>
            <th>Тема</th>
            <th>Рік публікації</th>
            <th>Мова</th>
            <th>Вид</th>
          </tr>
          </thead>
          <tbody>
          {books?.map(book => (
            <tr key={book.id}>
              <td className="table__primary">{book.name}</td>
              <td>{book.authors.join(', ')}</td>
              <td>{book.topic}</td>
              <td>{book.publishingYear}</td>
              <td>{book.language}</td>
              <td>{book.resourceType}</td>
            </tr>
          ))}
          {books?.length === 0 && (
            <tr className="table__empty">
              <td colSpan={6}>Книги відсутні</td>
            </tr>
          )}
          </tbody>
        </table>
      </div>
    </Layout>
  )
}