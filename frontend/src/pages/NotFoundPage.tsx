import ErrorPage from './ErrorPage'

export default function NotFoundPage() {
  return <ErrorPage code={404} title="Сторінку не знайдено" text="Сторінка, яку ви шукаєте, не існує або була переміщена." />
}