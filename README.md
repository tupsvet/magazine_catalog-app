# Каталог журналов — Android Client
Мобильное приложение для просмотра, поиска и публикации журналов.
Разработано в рамках курсовой работы Samsung Innovation Campus / RTU MIREA.
## Стек технологий
| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Архитектура | Clean Architecture (data/domain/presentation) |
| DI | Hilt |
| Сеть | Retrofit 2 + OkHttp |
| Локальная БД | Room |
| Изображения | Coil |
| Асинхронность | Coroutines + Flow |
| Навигация | Navigation Compose |
## Функционал
- Каталог журналов с поиском и фильтрацией по категориям
- Детальный просмотр журнала с выпусками и отзывами
- Избранное с офлайн-кешем (Room)
- Загрузка собственных журналов и выпусков
- Просмотр PDF-выпусков
- Система отзывов и рейтингов
- Профиль пользователя
- JWT-аутентификация
- Панель администратора (модерация)
## Требования
- Android 8.0+ (minSdk 26)
- Android Studio Hedgehog или новее
- Запущенный бэкенд ([magazine-app-backend](https://github.com/tupsvet/magazine-app-backend))
## Установка и запуск
### 1. Клонируй репозиторий
``` bash git clone https://github.com/tupsvet/magazine_catalog_app.git cd magazine_catalog_app ```
### 2. Настрой BASE_URL
Создай файл `local.properties` в корне проекта:
```
properties
Для эмулятора: BASE_URL=http://10.0.2.2:8080/
Для реального устройства (подставь IP своей машины): BASE_URL=http://192.168.x.x:8080/
```
### 3. Запусти бэкенд
Убедись что бэкенд запущен. Инструкция в репозитории бэкенда.
### 4. Запусти приложение
В Android Studio нажми **▶️ Run** или:
``` bash ./gradlew installDebug ```
## Структура проекта
```
app/src/main/java/com/magazines/catalog/
│
├── data/
│ ├── local/ # Room БД, DataStore
│ ├── mapper/ # DTO → Domain маперы
│ ├── remote/ # Retrofit API, DTO
│ └── repository/ # Реализации репозиториев
│
├── di/ # Hilt модули
│
├── domain/
│ ├── model/ # Domain модели
│ ├── repository/ # Интерфейсы репозиториев
│ └── usecase/ # Use cases
│
└── presentation/
├── auth/ # Логин, Регистрация
├── catalog/ # Каталог журналов
├── detail/ # Детали журнала, PDF
├── favorites/ # Избранное (Room кеш)
├── mymagazines/ # Мои журналы, загрузка
├── profile/ # Профиль
├── admin/ # Панель администратора
├── navigation/ # NavGraph, BottomNav
├── components/ # Общие компоненты
└── theme/ # Цвета, типографика
```
## Связанные репозитории
- 🔗 [Бэкенд (Ktor)](https://github.com/tupsvet/magazine-app-backend)
EOF
