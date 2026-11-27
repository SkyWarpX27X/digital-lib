import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, ApiError } from '../api'
import { ROLE_LABELS, type Role } from '../auth/AuthContext'
import Layout from '../components/Layout'
import { useTitle } from '../useTitle'
import { FIELDS, validate, type Field, type Form } from './RegisterPage'
import { ROLES } from './UsersPage'

export default function UserFormPage() {
  useTitle('Новий користувач')
  const navigate = useNavigate()
  const [form, setForm] = useState<Form>({ username: '', password: '', name: '', surname: '', patronymic: '' })
  const [role, setRole] = useState<Role>('DIGITAL_ARCHIVE_DEPARTMENT')
  const [errors, setErrors] = useState<Partial<Record<Field, string>>>({})
  const [error, setError] = useState<string>()

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    setError(undefined)
    if (Object.keys(validationErrors).length > 0) return

    try {
      await api('/api/v1/users', {
        method: 'POST',
        body: JSON.stringify({
          username: form.username.trim(),
          password: form.password,
          name: form.name.trim(),
          surname: form.surname.trim(),
          patronymic: form.patronymic.trim() || null,
          role,
          isActive: true,
        }),
      })
      navigate('/users', { state: { message: 'Користувача створено' } })
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError(e.status === 409 ? 'Користувач із таким логіном уже існує' : 'Не вдалося створити користувача. Перевірте введені дані')
    }
  }

  return (
    <Layout narrow>
      <h1 className="page-title">Новий користувач</h1>
      {error && <div className="alert alert--error" role="alert">{error}</div>}

      <form className="card" onSubmit={handleSubmit} noValidate>
        <div className="form-card__body">
          {FIELDS.map(({ id, label, type, max }) => (
            <div className="field" key={id}>
              <label className="field__label" htmlFor={id}>{label}</label>
              <input className={'input' + (errors[id] ? ' input--invalid' : '')} type={type} id={id} name={id}
                     autoComplete="off" maxLength={max} value={form[id]}
                     onChange={e => setForm({ ...form, [id]: e.target.value })} />
              {errors[id] && <span className="field__error">{errors[id]}</span>}
            </div>
          ))}
          <div className="field">
            <label className="field__label" htmlFor="role">Роль</label>
            <select className="input" id="role" value={role} onChange={e => setRole(e.target.value as Role)}>
              {ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
            </select>
          </div>
        </div>

        <div className="form-card__actions">
          <Link className="btn btn--secondary" to="/users">Скасувати</Link>
          <button className="btn btn--primary" type="submit">Створити</button>
        </div>
      </form>
    </Layout>
  )
}
