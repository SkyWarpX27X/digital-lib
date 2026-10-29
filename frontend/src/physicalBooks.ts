export interface PhysicalBook {
  id: string
  name: string
  authors: string[]
  description: string
  topic: string
  publishingYear: string
  language: string
  resourceType: string
}

export type PhysicalBookRequest = Omit<PhysicalBook, 'id'>