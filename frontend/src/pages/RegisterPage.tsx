import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api, ApiError } from '../api'
import { useAuth } from '../auth/AuthContext'
import { HOME, takeRedirect } from '../auth/guards'
import Brand from '../components/Brand'
import { useTitle } from '../useTitle'

type Field = 'username' | 'password' | 'name' | 'surname' | 'patronymic'
type Form = Record<Field, string>
type Errors = Partial<Record<Field, string>>

// max lengths mirror the column sizes in user/User.java
const FIELDS: { id: Field; label: string; type: string; autoComplete: string; max: number; required: boolean }[] = [
  { id: 'username', label: 'Логін', type: 'text', autoComplete: 'username', max: 50, required: true },
  { id: 'password', label: 'Пароль', type: 'password', autoComplete: 'new-password', max: 100, required: true },
  { id: 'surname', label: 'Прізвище', type: 'text', autoComplete: 'family-name', max: 30, required: true },
  { id: 'name', label: "Ім'я", type: 'text', autoComplete: 'given-name', max: 30, required: true },
  { id: 'patronymic', label: 'По батькові', type: 'text', autoComplete: 'additional-name', max: 30, required: false },
]

function validate(form: Form): Errors {
  const errors: Errors = {}
  for (const { id, label, max, required } of FIELDS) {
    const value = form[id].trim()
    if (required && value === '') errors[id] = `Поле «${label}» має бути заповнене`
    else if (value.length > max) errors[id] = `Не більше ${max} символів`
  }
  return errors
}

export default function RegisterPage() {
  useTitle('Реєстрація читача')
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState<Form>({ username: '', password: '', name: '', surname: '', patronymic: '' })
  const [errors, setErrors] = useState<Errors>({})
  const [error, setError] = useState<string>()
  const [submitting, setSubmitting] = useState(false)

  if (user && !submitting) return <Navigate to={HOME} replace />

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    setError(undefined)
    if (Object.keys(validationErrors).length > 0) return

    setSubmitting(true)
    const username = form.username.trim()
    try {
      await api('/api/v1/users', {
        method: 'POST',
        body: JSON.stringify({
          username,
          password: form.password,
          name: form.name.trim(),
          surname: form.surname.trim(),
          patronymic: form.patronymic.trim() || null,
          role: 'READER',
          isActive: true,
        }),
      })
      await login(username, form.password)
      navigate(takeRedirect(), { replace: true })
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError(e.status === 409 ? 'Користувач із таким логіном уже існує' : 'Не вдалося зареєструватися. Перевірте введені дані')
      setSubmitting(false)
    }
  }

  return (
    <div className="centered-page">
      <Brand />
      <main className="centered-page__main">
        <h1 className="page-title">Реєстрація</h1>
        <p className="centered-page__text">Створіть обліковий запис читача</p>
        {error && <div className="alert alert--error" role="alert">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          {FIELDS.map(({ id, label, type, autoComplete, max }) => (
            <div className="field" key={id}>
              <label className="field__label" htmlFor={id}>{label}</label>
              <input className={'input' + (errors[id] ? ' input--invalid' : '')} type={type} id={id} name={id}
                     autoComplete={autoComplete} maxLength={max} autoFocus={id === 'username'}
                     value={form[id]} onChange={e => setForm({ ...form, [id]: e.target.value })} />
              {errors[id] && <span className="field__error">{errors[id]}</span>}
            </div>
          ))}

          <button className="btn btn--primary btn--block login__submit" type="submit" disabled={submitting}>
            Зареєструватися
          </button>
        </form>
        <p className="centered-page__text page-note">
          Уже маєте обліковий запис? <Link to="/login">Увійти</Link>
        </p>
      </main>
    </div>
  )
}