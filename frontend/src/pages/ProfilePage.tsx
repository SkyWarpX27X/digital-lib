import { useEffect, useState, type FormEvent } from 'react'
import { api, ApiError } from '../api'
import { useAuth, type Role } from '../auth/AuthContext'
import Layout from '../components/Layout'
import { useTitle } from '../useTitle'

interface UserProfile {
  id: string
  name: string
  surname: string
  patronymic?: string | null
  role: Role
  isActive: boolean
}

type CardField = 'email' | 'postcode' | 'birthYear' | 'livingAddress' | 'workOrStudyAddress' | 'organisation' | 'workPosition'
type LibraryCard = Partial<Record<CardField, string | null>>
type CardForm = Record<CardField, string>
type Errors = Partial<Record<CardField, string>>

const ROLE_LABELS: Record<Role, string> = {
  READER: 'Читач',
  DIGITAL_ARCHIVE_DEPARTMENT: 'Відділ цифрового архіву',
  BOOK_DELIVERY_DEPARTMENT: 'Відділ доставки книг',
  ADMIN: 'Адміністратор',
}

const CARD_FIELDS: { id: CardField; label: string; required: boolean }[] = [
  { id: 'email', label: 'Електронна пошта', required: true },
  { id: 'birthYear', label: 'Рік народження', required: true },
  { id: 'postcode', label: 'Поштовий індекс', required: true },
  { id: 'livingAddress', label: 'Адреса проживання', required: true },
  { id: 'workOrStudyAddress', label: 'Адреса роботи або навчання', required: false },
  { id: 'organisation', label: 'Організація', required: false },
  { id: 'workPosition', label: 'Посада', required: false },
]

function toForm(card: LibraryCard | null): CardForm {
  return Object.fromEntries(CARD_FIELDS.map(({ id }) => [id, card?.[id] ?? ''])) as CardForm
}

function validate(form: CardForm): Errors {
  const errors: Errors = {}
  for (const { id, label, required } of CARD_FIELDS) {
    if (required && form[id].trim() === '') errors[id] = `Поле «${label}» має бути заповнене`
  }
  if (!errors.email && !/^[^\s@]+@[^\s@]+$/.test(form.email.trim())) errors.email = 'Некоректна адреса пошти'
  const year = form.birthYear.trim()
  if (!errors.birthYear) {
    if (!/^\d{4}$/.test(year)) errors.birthYear = 'Рік має складатися з 4 цифр'
    else if (Number(year) > new Date().getFullYear()) errors.birthYear = 'Рік народження не може бути у майбутньому'
  }
  return errors
}

export default function ProfilePage() {
  useTitle('Профіль')
  const { user, hasAnyRole } = useAuth()
  const [profile, setProfile] = useState<UserProfile>()

  useEffect(() => {
    api<UserProfile>(`/api/v1/users/${user!.id}`).then(setProfile)
  }, [user])

  return (
    <Layout narrow>
      <h1 className="page-title">Профіль</h1>

      <section className="card">
        <dl className="details">
          <div><dt>Прізвище</dt><dd>{profile?.surname ?? '—'}</dd></div>
          <div><dt>Ім'я</dt><dd>{profile?.name ?? '—'}</dd></div>
          <div><dt>По батькові</dt><dd>{profile?.patronymic || '—'}</dd></div>
          <div><dt>Логін</dt><dd>{user?.username}</dd></div>
          <div><dt>Роль</dt><dd>{profile ? ROLE_LABELS[profile.role] : '—'}</dd></div>
        </dl>
      </section>

      {hasAnyRole('READER') && <LibraryCardSection userId={user!.id} />}
    </Layout>
  )
}

function LibraryCardSection({ userId }: { userId: string }) {
  const url = `/api/v1/users/${userId}/library-card`
  // undefined = loading, null = reader has no card yet
  const [card, setCard] = useState<LibraryCard | null>()
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState<CardForm>(toForm(null))
  const [errors, setErrors] = useState<Errors>({})
  const [error, setError] = useState<string>()
  const [message, setMessage] = useState<string>()

  useEffect(() => {
    api<LibraryCard>(url)
      .then(setCard)
      .catch(e => {
        if (!(e instanceof ApiError) || e.status !== 404) throw e
        setCard(null)
      })
  }, [url])

  if (card === undefined) return null

  const startEditing = () => {
    setForm(toForm(card))
    setErrors({})
    setError(undefined)
    setMessage(undefined)
    setEditing(true)
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    setError(undefined)
    if (Object.keys(validationErrors).length > 0) return

    const request = Object.fromEntries(CARD_FIELDS.map(({ id }) => {
      const value = form[id].trim()
      return [id, id === 'birthYear' ? Number(value) : value || null]
    }))
    try {
      const saved = await api<LibraryCard>(url, { method: card ? 'PUT' : 'POST', body: JSON.stringify(request) })
      setMessage(card ? 'Картку читача оновлено' : 'Картку читача оформлено')
      setCard(saved)
      setEditing(false)
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setError('Не вдалося зберегти картку. Перевірте введені дані')
    }
  }

  if (card && !editing) {
    return (
      <section className="card page-note">
        <div className="form-card__body">
          <h2 className="form-section">Картка читача</h2>
          {message && <div className="alert alert--success" role="status">{message}</div>}
          <dl className="details reader-summary">
            {CARD_FIELDS.map(({ id, label }) => (
              <div key={id}><dt>{label}</dt><dd>{card[id] || '—'}</dd></div>
            ))}
          </dl>
        </div>
        <div className="form-card__actions">
          <button className="btn btn--secondary" type="button" onClick={startEditing}>Редагувати</button>
        </div>
      </section>
    )
  }

  return (
    <form className="card page-note" onSubmit={handleSubmit} noValidate>
      <div className="form-card__body">
        <h2 className="form-section">{card ? 'Редагування картки читача' : 'Оформлення картки читача'}</h2>
        {!card && (
          <p className="field__hint field__hint--tight">
            Картка потрібна, щоб замовляти оцифрування книг і читати книги під авторським правом.
          </p>
        )}
        {error && <div className="alert alert--error" role="alert">{error}</div>}

        {CARD_FIELDS.map(({ id, label, required }) => (
          <div className="field" key={id}>
            <label className="field__label" htmlFor={id}>{label}{!required && ' (необов\'язково)'}</label>
            <input id={id} name={id} className={'input' + (errors[id] ? ' input--invalid' : '')}
                   type={id === 'email' ? 'email' : 'text'} inputMode={id === 'birthYear' ? 'numeric' : undefined}
                   value={form[id]} onChange={e => setForm({ ...form, [id]: e.target.value })} />
            {errors[id] && <span className="field__error">{errors[id]}</span>}
          </div>
        ))}
      </div>

      <div className="form-card__actions">
        {card && <button className="btn btn--secondary" type="button" onClick={() => setEditing(false)}>Скасувати</button>}
        <button className="btn btn--primary" type="submit">{card ? 'Зберегти' : 'Оформити картку'}</button>
      </div>
    </form>
  )
}