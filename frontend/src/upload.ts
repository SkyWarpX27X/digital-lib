// shortcut: no S3 yet, URLs are typed by hand in the form; the S3 owner implements this
// (upload the file, return its URL) and removes the temporary URL inputs in DigitizedBookFormPage.
export async function uploadFile(_file: File): Promise<string> {
  throw new Error('Завантаження файлів ще недоступне. Вкажіть посилання вручну.')
}