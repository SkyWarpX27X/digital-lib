import { useEffect, useState, type ReactNode } from 'react'

type Field = 'name' | 'author' | 'topic' | 'yearFrom' | 'yearTo' | 'language' | 'resourceType'

const TEXT_FIELDS: { id: Field; label: string; wide?: boolean }[] = [
  { id: 'name', label: 'Назва', wide: true },
  { id: 'author', label: 'Автор' },
  { id: 'topic', label: 'Тема' },
  { id: 'language', label: 'Мова' },
]

const EMPTY: Record<Field, string> = { name: '', author: '', topic: '', yearFrom: '', yearTo: '', language: '', resourceType: '' }

export function resourceTypesOf(books: { resourceType: string }[]) {
  return [...new Set(books.map(b => b.resourceType))].sort((a, b) => a.localeCompare(b, 'uk'))
}

/** Catalog filters; reports a query string (without "?") 300 ms after the last change. */
export default function BookFilters({ onChange, resourceTypes, children }: {
  onChange: (query: string) => void
  resourceTypes: string[]
  children?: ReactNode
}) {
  const [values, setValues] = useState(EMPTY)

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
      {/* free text: physical books may have any type; the ones present in the catalog are suggested */}
      <input className="input filters__select" type="search" placeholder="Вид" aria-label="Вид ресурсу"
             list="book-filter-resource-types" value={values.resourceType} onChange={set('resourceType')} />
      <datalist id="book-filter-resource-types">
        {resourceTypes.map(t => <option key={t} value={t} />)}
      </datalist>
      {children}
    </div>
  )
}