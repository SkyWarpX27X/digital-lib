import { useEffect, useState, type ChangeEvent, type DragEvent, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, ApiError } from '../api'
import Layout from '../components/Layout'
import { LANGUAGES, RESOURCE_TYPES, type DigitizedBook, type DigitizedBookRequest } from '../digitizedBooks'
import type { PhysicalBook } from '../physicalBooks'
import { uploadFile } from '../upload'
import { useTitle } from '../useTitle'

type Field = 'physicalBookId' | 'name' | 'authors' | 'publishingYear' | 'language' | 'resourceType' | 'coverUrl' | 'fileUrl'
interface Form extends Record<Field, string> {
  description: string
  topic: string
  isCopyrighted: boolean
}
type Errors = Partial<Record<Field, string>>

const EMPTY_FORM: Form = {
  physicalBookId: '', name: '', authors: '', publishingYear: '', language: '', resourceType: '',
  coverUrl: '', fileUrl: '', description: '', topic: '', isCopyrighted: false,
}

function splitAuthors(authors: string) {
  return authors.split(',').map(author => author.trim()).filter(author => author !== '')
}

function validate(form: Form, creating: boolean): Errors {
  const errors: Errors = {}
  if (creating && !form.physicalBookId) errors.physicalBookId = 'Оберіть фізичну книгу'
  if (form.name.trim() === '') errors.name = 'Назва має бути вказана'
  if (splitAuthors(form.authors).length === 0) errors.authors = 'Автори мають бути вказані'

  const year = form.publishingYear.trim()
  if (year === '') errors.publishingYear = 'Рік має бути вказаний'
  else if (!/^\d{1,4}$/.test(year)) errors.publishingYear = 'Рік має бути числом'
  else if (Number(year) > new Date().getFullYear()) errors.publishingYear = 'Рік публікації не може бути у майбутньому'

  if (!form.language) errors.language = 'Мова має бути вказана'
  if (!form.resourceType) errors.resourceType = 'Тип ресурсу має бути вказаний'
  if (form.fileUrl.trim() === '') errors.fileUrl = 'Файл книги має бути вказаний'
  return errors
}

export default function DigitizedBookFormPage() {
  const { id } = useParams()
  const creating = id === undefined
  useTitle(creating ? 'Додати оцифровану книгу' : 'Редагувати оцифровану книгу')
  const navigate = useNavigate()
  const [form, setForm] = useState<Form>(EMPTY_FORM)
  const [errors, setErrors] = useState<Errors>({})
  const [physicalBooks, setPhysicalBooks] = useState<PhysicalBook[]>([])
  const [uploadError, setUploadError] = useState<string>()

  useEffect(() => {
    if (creating) {
      api<PhysicalBook[]>('/api/v1/physical-books').then(setPhysicalBooks)
    } else {
      api<DigitizedBook>(`/api/v1/digitized-books/${id}`).then(book => setForm({
        ...EMPTY_FORM, ...book, authors: book.authors.join(', '), coverUrl: book.coverUrl ?? '',
      }))
    }
  }, [creating, id])

  const set = (patch: Partial<Form>) => setForm(current => ({ ...current, ...patch }))

  const choosePhysicalBook = (bookId: string) => {
    const book = physicalBooks.find(b => b.id === bookId)
    if (!book) return set({ physicalBookId: '' })
    // description and topic aren't on the design, but the backend requires them: carry them over
    set({
      physicalBookId: book.id, name: book.name, authors: book.authors.join(', '),
      publishingYear: book.publishingYear, language: book.language, resourceType: book.resourceType,
      description: book.description, topic: book.topic,
    })
  }

  const upload = async (file: File | undefined, field: 'coverUrl' | 'fileUrl') => {
    if (!file) return
    try {
      set({ [field]: await uploadFile(file) })
      setUploadError(undefined)
    } catch (e) {
      setUploadError((e as Error).message)
    }
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(form, creating)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    const request: DigitizedBookRequest = {
      name: form.name,
      authors: splitAuthors(form.authors),
      description: form.description,
      topic: form.topic,
      publishingYear: form.publishingYear.trim().padStart(4, '0'),
      language: form.language,
      resourceType: form.resourceType,
      isCopyrighted: form.isCopyrighted,
      coverUrl: form.coverUrl.trim() || null,
      fileUrl: form.fileUrl.trim(),
      physicalBookId: creating ? form.physicalBookId : undefined,
    }
    try {
      await api<DigitizedBook>(creating ? '/api/v1/digitized-books' : `/api/v1/digitized-books/${id}`,
        { method: creating ? 'POST' : 'PUT', body: JSON.stringify(request) })
      navigate('/digitized-books', { state: { message: creating ? 'Книгу успішно оцифровано' : 'Зміни збережено' } })
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setErrors(Object.keys(e.errors).length > 0 ? e.errors : { name: e.detail })
    }
  }

  const inputClass = (field: Field) => 'input' + (errors[field] ? ' input--invalid' : '')
  const error = (field: Field) => errors[field] && <span className="field__error">{errors[field]}</span>
  const text = (field: Field, label: string) => (
    <div className="field">
      <label className="field__label" htmlFor={field}>{label}</label>
      <input id={field} name={field} className={inputClass(field)} value={form[field]}
             inputMode={field === 'publishingYear' ? 'numeric' : undefined}
             onChange={(e: ChangeEvent<HTMLInputElement>) => set({ [field]: e.target.value })} />
      {error(field)}
    </div>
  )
  const select = (field: Field, label: string, options: string[]) => (
    <div className="field">
      <label className="field__label" htmlFor={field}>{label}</label>
      <select id={field} name={field} className={inputClass(field)} value={form[field]}
              onChange={e => set({ [field]: e.target.value })}>
        <option value="">Оберіть…</option>
        {/* keep an existing value that isn't in the list selectable */}
        {[...new Set([...options, form[field]].filter(Boolean))].map(o => <option key={o} value={o}>{o}</option>)}
      </select>
      {error(field)}
    </div>
  )

  return (
    <Layout>
      <nav className="breadcrumbs" aria-label="Навігація">
        <Link to="/digitized-books">Оцифровані книги</Link> / <span>{creating ? 'Нова книга' : form.name}</span>
      </nav>
      <h1 className="page-title">{creating ? 'Додати оцифровану книгу' : 'Редагувати оцифровану книгу'}</h1>

      <form className="card form-card" onSubmit={handleSubmit} noValidate>
        <div className="form-card__body">
          {uploadError && <div className="alert alert--error" role="alert">{uploadError}</div>}

          {creating && (
            <div className="field">
              <label className="field__label" htmlFor="physicalBookId">Фізична книга</label>
              <select id="physicalBookId" className={inputClass('physicalBookId')} value={form.physicalBookId}
                      onChange={e => choosePhysicalBook(e.target.value)}>
                <option value="">Оберіть книгу…</option>
                {physicalBooks.map(b => (
                  <option key={b.id} value={b.id}>{b.name} — {b.authors.join(', ')}, {b.publishingYear}</option>
                ))}
              </select>
              {error('physicalBookId')}
              <p className="field__hint">
                Показано книги, які ще не оцифровані. Дані нижче підставляються з обраної книги, їх можна змінити.
              </p>
            </div>
          )}

          <h2 className="form-section">Дані книги</h2>
          {text('name', 'Назва')}
          <div className="field">
            <label className="field__label" htmlFor="authors">Автори</label>
            <input id="authors" className={inputClass('authors')} value={form.authors}
                   onChange={e => set({ authors: e.target.value })} />
            {error('authors')}
            <p className="field__hint">Якщо авторів кілька, перелічіть їх через кому.</p>
          </div>

          <div className="form-row">
            {text('publishingYear', 'Рік публікації')}
            {select('language', 'Мова', LANGUAGES)}
          </div>
          <div className="form-row">
            <div>
              {select('resourceType', 'Тип ресурсу', RESOURCE_TYPES)}
              <p className="field__hint field__hint--tight">
                «Книга» — видано після 1830 року, «Стародрук» — у 1830 році або раніше.
              </p>
            </div>
            <fieldset className="field radio-group">
              <legend className="field__label">Книга під авторськими правами?</legend>
              {[true, false].map(value => (
                <label key={String(value)} className={'radio' + (form.isCopyrighted === value ? ' radio--checked' : '')}>
                  <input type="radio" name="isCopyrighted" checked={form.isCopyrighted === value}
                         onChange={() => set({ isCopyrighted: value })} />
                  {value ? 'Так' : 'Ні'}
                </label>
              ))}
            </fieldset>
          </div>

          <div className="field">
            <span className="field__label">Обкладинка</span>
            <div className="cover-picker">
              {form.coverUrl
                ? <img className="cover-picker__preview" src={form.coverUrl} alt="Обкладинка" />
                : <div className="cover-picker__preview cover-picker__preview--empty" aria-hidden="true">🖼</div>}
              <div>
                <label className="btn btn--secondary">
                  Обрати зображення
                  <input type="file" accept="image/jpeg,image/png" hidden
                         onChange={e => upload(e.target.files?.[0], 'coverUrl')} />
                </label>
                <p className="field__hint">Необов'язково. JPG або PNG, співвідношення сторін 2:3.</p>
              </div>
            </div>
            {/* temporary until S3 upload exists: see upload.ts */}
            <input className="input input--temp" aria-label="Посилання на обкладинку" placeholder="або посилання на обкладинку"
                   value={form.coverUrl} onChange={e => set({ coverUrl: e.target.value })} />
          </div>

          <div className="field">
            <span className="field__label">Файл книги</span>
            <label className={'dropzone' + (errors.fileUrl ? ' dropzone--invalid' : '')}
                   onDragOver={(e: DragEvent) => e.preventDefault()}
                   onDrop={(e: DragEvent) => { e.preventDefault(); upload(e.dataTransfer.files[0], 'fileUrl') }}>
              <span className="dropzone__icon" aria-hidden="true">⇪</span>
              <strong>Перетягніть PDF-файл сюди</strong>
              <span className="field__hint">Підтримується лише формат PDF</span>
              <span className="btn btn--secondary">Обрати файл</span>
              <input type="file" accept="application/pdf" hidden onChange={e => upload(e.target.files?.[0], 'fileUrl')} />
            </label>
            {/* temporary until S3 upload exists: see upload.ts */}
            <input className={inputClass('fileUrl') + ' input--temp'} aria-label="Посилання на PDF-файл"
                   placeholder="або посилання на PDF-файл" value={form.fileUrl}
                   onChange={e => set({ fileUrl: e.target.value })} />
            {error('fileUrl')}
          </div>
        </div>

        <div className="form-card__actions">
          <Link className="btn btn--secondary" to="/digitized-books">Скасувати</Link>
          <button className="btn btn--primary" type="submit">Зберегти</button>
        </div>

        {creating && (
          <div className="info-box">
            <strong>ⓘ Що станеться після збереження</strong>
            <ul>
              <li>Фізичну книгу буде видалено з каталогу фізичних книг.</li>
              <li>Відкрите замовлення на цю книгу, якщо воно є, буде автоматично закрите.</li>
            </ul>
          </div>
        )}
      </form>
    </Layout>
  )
}