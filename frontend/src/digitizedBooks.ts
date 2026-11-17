export interface DigitizedBook {
  id: string
  name: string
  authors: string[]
  description: string
  topic: string
  publishingYear: string
  language: string
  resourceType: string
  isCopyrighted: boolean
  coverUrl: string | null
  pdfUrl: string
}

export type DigitizedBookRequest = Omit<DigitizedBook, 'id' | 'coverUrl' | 'pdfUrl'> & { physicalBookId?: string }

export const RESOURCE_TYPES = ['Книга', 'Стародрук']

export const LANGUAGES = ['Українська', 'Англійська', 'Німецька', 'Польська', 'Французька', 'Латинська', 'Інша']