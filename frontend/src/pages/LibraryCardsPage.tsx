import { useEffect, useMemo, useState } from 'react'
import { api } from '../api'
import Layout from '../components/Layout'
import { useTitle } from '../useTitle'
import { fullName } from './UsersPage'

interface LibraryCardOverview {
  ownerId: string
  surname: string
  name: string
  patronymic?: string | null
  email: string
  birthYear: string
  livingAddress: string
  workOrStudyAddress?: string | null
}

export default function LibraryCardsPage() {
  useTitle('Картки читачів')
  const [cards, setCards] = useState<LibraryCardOverview[] | null>(null)
  const [query, setQuery] = useState('')

  useEffect(() => {
    api<LibraryCardOverview[]>('/api/v1/library-cards').then(setCards)
  }, [])

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase()
    return (cards ?? []).filter(c => !q || [fullName(c), c.email].some(value => value.toLowerCase().includes(q)))
  }, [cards, query])

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">Картки читачів</h1>
          {cards && <p className="page-subtitle">Усього: {cards.length}</p>}
        </div>
      </div>

      <div className="filters">
        <input className="input filters__search" type="search" placeholder="ПІБ або пошта"
               aria-label="Пошук за ПІБ або поштою" value={query} onChange={e => setQuery(e.target.value)} />
      </div>

      <div className="card">
        <table className="table">
          <thead>
          <tr>
            <th>ПІБ</th>
            <th>Електронна пошта</th>
            <th>Рік народження</th>
            <th>Адреса проживання</th>
            <th>Адреса роботи або навчання</th>
          </tr>
          </thead>
          <tbody>
          {visible.map(card => (
            <tr key={card.ownerId}>
              <td className="table__primary">{fullName(card)}</td>
              <td>{card.email}</td>
              <td>{card.birthYear}</td>
              <td>{card.livingAddress}</td>
              <td>{card.workOrStudyAddress ?? '—'}</td>
            </tr>
          ))}
          {cards && visible.length === 0 && (
            <tr className="table__empty">
              <td colSpan={5}>Картки відсутні</td>
            </tr>
          )}
          </tbody>
        </table>
      </div>
    </Layout>
  )
}
