import ErrorPage from './ErrorPage'

export default function ForbiddenPage() {
  return <ErrorPage code={403} title="Доступ заборонено" text="У вас недостатньо прав для перегляду цієї сторінки." />
}