export default function Brand() {
  return (
    <header className="brand">
      <div className="brand__logo">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <rect x="3" y="4" width="18" height="16" rx="2" />
          <line x1="12" y1="4" x2="12" y2="20" />
        </svg>
      </div>
      <div>
        <div className="brand__title">Електронна бібліотека</div>
        <div>ім. Л. Подерв'янського</div>
      </div>
    </header>
  )
}