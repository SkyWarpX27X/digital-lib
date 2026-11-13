export interface BookOrder {
  id: string
  creatorId: string
  creatorName: string
  emailForDelivery: string
  book: string
  bookName: string | null
  bookAuthors: string | null
  isOpen: boolean
}

export interface BookOrderRequest {
  emailForDelivery: string | null
  book: string
  isOpen: true
}

export const STAFF_ROLES = ['BOOK_DELIVERY_DEPARTMENT', 'ADMIN'] as const
