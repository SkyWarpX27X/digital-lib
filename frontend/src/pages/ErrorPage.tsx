import { Link } from 'react-router-dom'
import Brand from '../components/Brand'
import { useTitle } from '../useTitle'

interface ErrorPageProps {
  code: number
  title: string
  text: string
}

export default function ErrorPage({ code, title, text }: ErrorPageProps) {
  useTitle(title)

  return (
    <div className="centered-page error-page">
      <Brand />
      <main className="centered-page__main">
        <div className="error-page__code">{code}</div>
        <h1 className="page-title">{title}</h1>
        <p className="centered-page__text">{text}</p>
        <div>
          <Link className="btn btn--primary" to="/physical-books">На головну</Link>
        </div>
      </main>
    </div>
  )
}