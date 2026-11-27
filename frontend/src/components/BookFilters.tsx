import { useEffect, useState, type ReactNode } from 'react'
import { api } from '../api'

type Field = 'name' | 'author' | 'topic' | 'yearFrom' | 'yearTo' | 'language' | 'resourceType'

const TEXT_FIELDS: { id: Field; label: string; wide?: boolean }[] = [
  { id: 'name', label: 'Назва', wide: true },
  { id: 'author', label: 'Автор' },
]

export type FilterOptions = { topics: string[]; languages: string[]; resourceTypes: string[] }

const SELECT_FIELDS: { id: Field; label: string; options: keyof FilterOptions }[] = [
  { id: 'topic', label: 'Тема', options: 'topics' },
  { id: 'language', label: 'Мова', options: 'languages' },
  { id: 'resourceType', label: 'Вид', options: 'resourceTypes' },
]

const EMPTY: Record<Field, string> = { name: '', author: '', topic: '', yearFrom: '', yearTo: '', language: '', resourceType: '' }

/** Catalog filters; reports a query string (without "?") 300 ms after the last change. */
export default function BookFilters({ onChange, optionsUrl, children }: {
  onChange: (query: string) => void
  /** Endpoint returning the distinct values for the dropdowns. */
  optionsUrl: string
  children?: ReactNode
}) {
  const [values, setValues] = useState(EMPTY)
  const [options, setOptions] = useState<FilterOptions>({ topics: [], languages: [], resourceTypes: [] })

  useEffect(() => {
    api<FilterOptions>(optionsUrl).then(setOptions)
  }, [optionsUrl])

  useEffect(() => {
    const timer = setTimeout(() => {
      const params = new URLSearchParams()
      for (const [key, value] of Object.entries(values)) {
        if (value.trim()) params.set(key, value.trim())
      }
      onChange(params.toString())
    }, 300)
    return () => clearTimeout(timer)
  }, [values, onChange])

  const set = (field: Field) => (e: { target: { value: string } }) => setValues(v => ({ ...v, [field]: e.target.value }))

  return (
    <div className="filters">
      {TEXT_FIELDS.map(({ id, label, wide }) => (
        <input key={id} className={'input ' + (wide ? 'filters__search' : 'filters__select')} type="search"
               placeholder={label} aria-label={label} value={values[id]} onChange={set(id)} />
      ))}
      <input className="input filters__select" type="number" placeholder="Рік від" aria-label="Рік публікації від"
             value={values.yearFrom} onChange={set('yearFrom')} />
      <input className="input filters__select" type="number" placeholder="Рік до" aria-label="Рік публікації до"
             value={values.yearTo} onChange={set('yearTo')} />
      {SELECT_FIELDS.map(({ id, label, options: key }) => (
        <select key={id} className="input filters__select" aria-label={label} value={values[id]} onChange={set(id)}>
          <option value="">{label}: усі</option>
          {options[key].map(value => <option key={value} value={value}>{value}</option>)}
        </select>
      ))}
      {children}
    </div>
  )
}