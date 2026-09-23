# Електронна бібліотека - Бізнес правила

## 1. Bookmark (закладка)

| Операція | Правило | Exception |
|---|---|---|
| `addBookmark` | Користувач може мати лише одну закладку на книжку | `DuplicateException` якщо `(userId, bookId)` вже існує |
| `getBookmark` | Має існувати | `DataNotFoundException` |
| `deleteBookmark` | Має існувати | `DataNotFoundException` |
| `updatePageNumber` | Має існувати | `DataNotFoundException` |

`addBookmark` публікує `BookmarkAddedEvent(userId, bookId, pageNumber)`.

## 2. Book Order (замовлення книжки)
**Містить детермінований скінченний автомат (ДСА).**

**Стани:** `Open` -> `Closed` через прапорець `isOpen`.

| Операція | Правило | Exception |
|---|---|---|
| `addBookOrder` | Користувач має володіти карткою бібліотеки | `LibraryCardNotFoundException` |
| | Як `emailForDelivery` не передано, автоматично використовує електронну пошту, вказану в картці | - |
| | Id замовлення має бути унікальним | `DuplicateException` |
| | Книжка може мати лише одне активне замовлення | `DuplicateException` |
| | Замовлена книжка має мати фізичний відповідник | `DataNotFoundException` |
| `updateStatus` | Замовлення має існувати | `DataNotFoundException` |
| | Закрите замовлення не можна назад відкрити (`Closed -> Open` не є легальним переходом) | `InvalidOrderUpdateException` |
| | `Open -> Closed` завжди дозволено | - |

`addBookOrder` публікує `BookOrderCreatedEvent` для логування через `BookOrderAuditEventListener`.

**Event listener:** після створення `BookDigitizedEvent`, якщо існує замовлення для оцифрованої книги, його стан переходить в `Closed`.

## 3. Digitized Book (оцифрована книга)

| Операція | Правило | Exception |
|---|---|---|
| `getDigitizedBook` | Має існувати | `DataNotFoundException` |
| `addDigitizedBook` | Id має бути унікальним | `DuplicateException` |
| `updateDigitizedBook` | Має існувати | `DataNotFoundException` |
| `deleteDigitizedBook` | Має існувати | `DataNotFoundException` |

`addDigitizedBook` публікує `BookDigitizedEvent(digitizedBookId, physicalBookId)`, що є тригером для побічних ефектів поза модулем; для логування через `BookDigitizedAuditEventListener`.

## 4. Physical Book (фізична книга)

**Патерн стратегія:** Імплементації `ResourceTypeValidationStrategy` індексовані за 
`getResourceType()` в map. Під час операцій add/update, знаходиться відповідна стратегія `book.resourceType()`; якщо жодна не зареєстрована для типу, `"Книга"` використовується за замовчуванням.

| Тип ресурсу | Стратегія | Правило | Exception |
|---|---|---|---|
| `"Книга"` (modern book) | `ModernBookValidationStrategy` | Рік публікації має бути строго після 1830 | `IllegalResourceTypeException` якщо рік <= 1830 |
| `"Стародрук"` (old book) | `OldBookValidationStrategy` | Рік публікації має бути 1830 і нижче | `IllegalResourceTypeException` якщо рік > 1830 |

| Операція | Правило | Exception |
|---|---|---|
| `addPhysicalBook` | Id має бути унікальним | `DuplicateException` |
| | Має пройти перевірку на тип ресурсу | `IllegalResourceTypeException` |
| `updatePhysicalBook` | Має існувати | `DataNotFoundException` |
| | Має пройти перевірку на тип ресурсу | `IllegalResourceTypeException` |
| `getPhysicalBook` / `deletePhysicalBook` | Має існувати | `DataNotFoundException` |

`addPhysicalBook` публікує `PhysicalBookAddedEvent` для логування через `PhysicalBookAuditEventListener`.

**Event listener:** після створення `BookDigitizedEvent`, фізична книжка видаляється.

## 5. User (користувач)

| Операція | Правило | Exception |
|---|---|---|
| `getUser` | Має існувати | `DataNotFoundException` |
| `addUser` | Id має бути унікальним | `DuplicateException` |
| | login має бути унікальним | `DuplicateException` |
| `updateUser` | Має існувати | `DataNotFoundException` |
| `deleteUser` | Має існувати | `DataNotFoundException` |

`addUser` публікує `UserAddedEvent(id, name, surname, patronymic)` для логування через `UserAuditEventListener`.

## 6. Library Card (картка читача)

| Operation | Rule | Exception |
|---|---|---|
| `getLibraryCard` / `getEmail` | Має існувати | `DataNotFoundException` |
| `addLibraryCard` | Користувач-власник має існувати | `DataNotFoundException` |
| | Користувач може мати лише одну картку | `DuplicateException` |
| `updateLibraryCard` | Має існувати | `DataNotFoundException` |
| `deleteLibraryCard` | Має існувати | `DataNotFoundException` |

`addLibraryCard` публікує `LibraryCardAddedEvent(ownerId, email)` для логування через `LibraryCardAuditEventListener`.

## 7. Список доменних Exception

- `DataNotFoundException` - загальний "сутність не знайдено" для get/update/delete у всіх модулях.
- `DuplicateException` - порушення унікальності, у всіх модулях.
- `LibraryCardNotFoundException` - замовник не має картки читача.
- `InvalidOrderUpdateException` - спроба заново відкрити закрите замовлення.
- `IllegalResourceTypeException` — рік публікації фізичної книжки не підлягає наявній стратегії типу ресурсу.