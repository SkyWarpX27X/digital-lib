import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, ApiError } from '../api'
import Layout from '../components/Layout'
import type { PhysicalBook, PhysicalBookRequest } from '../physicalBooks'
import { useTitle } from '../useTitle'

type Field = 'name' | 'authors' | 'description' | 'topic' | 'publishingYear' | 'language' | 'resourceType'
type Form = Record<Field, string>
type Errors = Partial<Record<Field, string>>

const EMPTY_FORM: Form = {
  name: '', authors: '', description: '', topic: '', publishingYear: '', language: '', resourceType: '',
}

function splitAuthors(authors: string) {
  return authors.split(',').map(author => author.trim()).filter(author => author !== '')
}

function validate(form: Form): Errors {
  const errors: Errors = {}
  const blank = (value: string) => value.trim() === ''

  if (blank(form.name)) errors.name = "Ім'я має бути вказане"
  if (splitAuthors(form.authors).length === 0) errors.authors = 'Автори мають бути вказані'
  if (blank(form.description)) errors.description = 'Опис має бути вказаний'
  if (blank(form.topic)) errors.topic = 'Тема має бути вказана'

  const year = form.publishingYear.trim()
  if (year === '') errors.publishingYear = 'Рік має бути вказаний'
  else if (!/^\d{1,4}$/.test(year)) errors.publishingYear = 'Рік має бути числом'
  else if (Number(year) > new Date().getFullYear()) errors.publishingYear = 'Рік публікації не може бути у майбутньому'

  const language = form.language.trim()
  if (language === '') errors.language = 'Мова має бути вказана'
  else if (language.length < 3 || language.length > 30) errors.language = 'Довжина має бути від 3 до 30 символів'

  if (blank(form.resourceType)) errors.resourceType = 'Вид має бути вказано'
  return errors
}

export default function AddPhysicalBookPage() {
  useTitle('Додати фізичну книгу')
  const navigate = useNavigate()
  const [form, setForm] = useState<Form>(EMPTY_FORM)
  const [errors, setErrors] = useState<Errors>({})

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    const request: PhysicalBookRequest = {
      name: form.name,
      authors: splitAuthors(form.authors),
      description: form.description,
      topic: form.topic,
      publishingYear: form.publishingYear.trim().padStart(4, '0'),
      language: form.language,
      resourceType: form.resourceType,
    }
    try {
      await api<PhysicalBook>('/api/v1/physical-books', { method: 'POST', body: JSON.stringify(request) })
      navigate('/physical-books', { state: { message: 'Книгу успішно додано' } })
    } catch (e) {
      if (!(e instanceof ApiError)) throw e
      setErrors(e.status === 422 ? { resourceType: e.detail } : e.errors)
    }
  }

  const field = (id: Field, label: string, multiline = false) => {
    const props = {
      id,
      name: id,
      className: (multiline ? 'input input--textarea' : 'input') + (errors[id] ? ' input--invalid' : ''),
      value: form[id],
      onChange: (e: { target: { value: string } }) => setForm({ ...form, [id]: e.target.value }),
    }
    return (
      <>
        <label className="field__label" htmlFor={id}>{label}</label>
        {multiline
          ? <textarea {...props} rows={6} />
          : <input {...props} type="text" inputMode={id === 'publishingYear' ? 'numeric' : undefined} />}
        {errors[id] && <span className="field__error">{errors[id]}</span>}
      </>
    )
  }

  return (
    <Layout narrow>
      <h1 className="page-title">Додати фізичну книгу</h1>

      <form className="card" onSubmit={handleSubmit} noValidate>
        <div className="form-card__body">
          {field('name', 'Назва')}
          {field('authors', 'Автори')}
          <p className="field__hint">Якщо авторів кілька, перелічіть їх через кому.</p>
          {field('description', 'Опис', true)}
          {field('topic', 'Тема')}
          {field('publishingYear', 'Рік публікації')}
          {field('language', 'Мова')}
          {field('resourceType', 'Вид')}
        </div>

        <div>
          <Link className="btn btn--secondary" to="/physical-books">Скасувати</Link>
          <button className="btn btn--primary" type="submit">Зберегти</button>
        </div>
      </form>
    </Layout>
  )
}