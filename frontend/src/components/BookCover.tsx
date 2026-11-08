const COLORS = ['#2b44c4', '#16181d', '#7a3324', '#1f5c45', '#e5dccb', '#4a2a6b']

function colorFor(id: string) {
  let hash = 0
  for (const char of id) hash = (hash * 31 + char.charCodeAt(0)) | 0
  return COLORS[Math.abs(hash) % COLORS.length]
}

interface Props {
  id: string
  name: string
  authors: string[]
  coverUrl: string | null
  large?: boolean
}

export default function BookCover({ id, name, authors, coverUrl, large = false }: Props) {
  const className = 'cover' + (large ? ' cover--large' : '')
  if (coverUrl) return <img className={className} src={coverUrl} alt={`Обкладинка: ${name}`} />

  const background = colorFor(id)
  const light = background === '#e5dccb'
  return (
    <div className={className + (light ? ' cover--light' : '')} style={{ background }} aria-hidden="true">
      <span className="cover__lines" />
      {large && (
        <div className="cover__text">
          <div className="cover__title">{name}</div>
          <div className="cover__author">{authors.join(', ')}</div>
        </div>
      )}
      <span className="cover__circle" />
      {large && <span className="cover__circle cover__circle--second" />}
    </div>
  )
}