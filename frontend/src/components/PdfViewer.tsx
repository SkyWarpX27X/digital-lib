import { useEffect, useRef, useState } from 'react'
import { getDocument, GlobalWorkerOptions, type PDFDocumentProxy } from 'pdfjs-dist'
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url'

GlobalWorkerOptions.workerSrc = workerUrl

const MIN_ZOOM = 50
const MAX_ZOOM = 300
const ZOOM_STEP = 25

export default function PdfViewer({ url }: { url: string }) {
  const containerRef = useRef<HTMLDivElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [pdf, setPdf] = useState<PDFDocumentProxy | null>(null)
  const [error, setError] = useState(false)
  const [page, setPage] = useState(1)
  const [pageInput, setPageInput] = useState('1')
  const [zoom, setZoom] = useState(100)

  useEffect(() => {
    setPdf(null)
    setError(false)
    setPage(1)
    const task = getDocument(url)
    task.promise.then(setPdf, () => setError(true))
    return () => { task.destroy() }
  }, [url])

  useEffect(() => {
    if (!pdf || !canvasRef.current) return
    const canvas = canvasRef.current
    let cancelled = false
    let renderTask: ReturnType<Awaited<ReturnType<PDFDocumentProxy['getPage']>>['render']> | null = null

    pdf.getPage(page).then(pdfPage => {
      if (cancelled) return
      const viewport = pdfPage.getViewport({ scale: zoom / 100 })
      const ratio = window.devicePixelRatio || 1
      canvas.width = viewport.width * ratio
      canvas.height = viewport.height * ratio
      canvas.style.width = `${viewport.width}px`
      canvas.style.height = `${viewport.height}px`
      renderTask = pdfPage.render({
        canvasContext: canvas.getContext('2d')!,
        viewport,
        transform: ratio === 1 ? undefined : [ratio, 0, 0, ratio, 0, 0],
      })
      renderTask.promise.catch(() => {})
    })
    return () => {
      cancelled = true
      renderTask?.cancel()
    }
  }, [pdf, page, zoom])

  useEffect(() => setPageInput(String(page)), [page])

  const total = pdf?.numPages ?? 0
  const goTo = (target: number) => {
    if (target >= 1 && target <= total) setPage(target)
    else setPageInput(String(page))
  }

  if (error) return <div className="alert alert--error">Не вдалося завантажити файл книги.</div>

  return (
    <div className="card pdf" ref={containerRef}>
      <div className="pdf__toolbar">
        <div className="pdf__group">
          <button className="icon-btn" type="button" onClick={() => goTo(page - 1)} disabled={page <= 1}
                  aria-label="Попередня сторінка">‹</button>
          <input className="input pdf__page" value={pageInput} inputMode="numeric" aria-label="Сторінка"
                 onChange={e => setPageInput(e.target.value)}
                 onBlur={() => goTo(Number(pageInput))}
                 onKeyDown={e => { if (e.key === 'Enter') goTo(Number(pageInput)) }} />
          <span className="pdf__total">з {total || '…'}</span>
          <button className="icon-btn" type="button" onClick={() => goTo(page + 1)} disabled={page >= total}
                  aria-label="Наступна сторінка">›</button>
        </div>
        <div className="pdf__group">
          <button className="icon-btn" type="button" onClick={() => setZoom(Math.max(MIN_ZOOM, zoom - ZOOM_STEP))}
                  disabled={zoom <= MIN_ZOOM} aria-label="Зменшити">−</button>
          <span className="pdf__zoom">{zoom}%</span>
          <button className="icon-btn" type="button" onClick={() => setZoom(Math.min(MAX_ZOOM, zoom + ZOOM_STEP))}
                  disabled={zoom >= MAX_ZOOM} aria-label="Збільшити">+</button>
          <button className="icon-btn" type="button" onClick={() => containerRef.current?.requestFullscreen()}
                  aria-label="На весь екран">⛶</button>
        </div>
      </div>
      <div className="pdf__stage">
        {pdf ? <canvas ref={canvasRef} className="pdf__canvas" /> : <span className="pdf__loading">Завантаження…</span>}
      </div>
    </div>
  )
}