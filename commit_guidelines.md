# Правила написания коммитов

## Общие правила
- Коммиты должны быть **маленькими и осмысленными** — один коммит = одна логическая задача.
- Сообщения коммитов пишутся на **английском языке**.
- Используется стиль **Conventional Commits**.
- Максимальная длина строки заголовка — **72 символа**.
- В теле коммита можно указывать детали, если нужно объяснить причину изменений.

---

## Формат сообщения
```
<type>(<scope>): <краткое описание>
<ПУСТАЯ СТРОКА>
<body>
<ПУСТАЯ СТРОКА>
<footer>
```

### Пример:
```
feat(auth): add JWT token refresh

Implemented automatic refresh of JWT tokens when expired.
This improves user experience and security.

BREAKING CHANGE: login endpoint now returns refresh token as well.
```

---

## Типы коммитов
- **feat** — новая функциональность  
  _пример: `feat(user): add profile picture upload`_

- **fix** — исправление ошибки  
  _пример: `fix(invoice): correct total sum calculation`_

- **docs** — изменения только в документации  
  _пример: `docs(readme): update installation guide`_

- **style** — изменения в коде, не влияющие на логику (пробелы, форматирование)  
  _пример: `style(ui): reformat button component`_

- **refactor** — рефакторинг без добавления нового функционала или исправления багов  
  _пример: `refactor(core): simplify service response logic`_

- **perf** — улучшение производительности  
  _пример: `perf(db): optimize query for invoices`_

- **test** — добавление или обновление тестов  
  _пример: `test(user): add unit tests for registration`_

- **chore** — рутинные задачи, обновления зависимостей, конфигурации  
  _пример: `chore(deps): update mongoose to 7.6.1`_

---

## Scope (область)
Указывается в скобках, если изменения относятся к определённому модулю:  
- `auth`, `user`, `invoice`, `ui`, `core`, `db` и т.д.  

Пример:  
```
feat(invoice): add export to CSV
```

---

## Body (тело коммита)
- Используется для **описания причины** изменения или деталей реализации.  
- Пишется в повелительном наклонении: *"add"*, *"fix"*, *"remove"* (не "added" или "fixed").  
- Допускается несколько абзацев.

---

## Footer (подвал коммита)
- Используется для ссылок на задачи или пометки о совместимости.  
- Ключевые слова:
  - `BREAKING CHANGE:` — важные изменения, нарушающие обратную совместимость.
  - `Closes #123` — автоматическое закрытие issue.

---

## Примеры
```
fix(auth): handle expired access tokens

Added middleware that catches expired tokens
and forces refresh instead of returning 401.

Closes #42
```

```
refactor(core): extract RequestContext into separate class
```

```
chore(repo): add commitlint and husky hooks
```

```
docs(readme): add usage examples
```
