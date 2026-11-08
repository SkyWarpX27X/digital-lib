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
  fileUrl: string
}

export type DigitizedBookRequest = Omit<DigitizedBook, 'id'> & { physicalBookId?: string }

export const RESOURCE_TYPES = ['Книга', 'Стародрук']

export const LANGUAGES = ['Українська', 'Англійська', 'Німецька', 'Польська', 'Французька', 'Латинська', 'Інша']