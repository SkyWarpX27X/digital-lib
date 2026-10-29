import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { REDIRECT_KEY } from '../auth/guards'
import Brand from '../components/Brand'
import { useTitle } from '../useTitle'

export default function LoginPage() {
  useTitle('Вхід до бібліотеки')
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  if (user && !submitting) return <Navigate to="/physical-books" replace />

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    try {
      await login(username, password)
      const target = sessionStorage.getItem(REDIRECT_KEY) ?? '/physical-books'
      sessionStorage.removeItem(REDIRECT_KEY)
      navigate(target, { replace: true })
    } catch {
      setError(true)
      setSubmitting(false)
    }
  }

  return (
    <div className="centered-page">
      <Brand />
      <main className="centered-page__main">
        <h1 className="page-title">Вхід</h1>
        {error && <div className="alert alert--error" role="alert">Невірний логін або пароль</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label className="field__label" htmlFor="username">Логін</label>
            <input className="input" type="text" id="username" name="username" placeholder="Ваш логін"
                   autoComplete="username" required autoFocus
                   value={username} onChange={e => setUsername(e.target.value)} />
          </div>

          <div className="field">
            <label className="field__label" htmlFor="password">Пароль</label>
            <input className="input" type="password" id="password" name="password" placeholder="Ваш пароль"
                   autoComplete="current-password" required
                   value={password} onChange={e => setPassword(e.target.value)} />
          </div>

          <button className="btn btn--primary btn--block login__submit" type="submit" disabled={submitting}>Увійти</button>
        </form>
      </main>
    </div>
  )
}