# Dynamic Pricing Service 🚀

Интеллектуальная система динамического ценообразования в реальном времени. Сервис анализирует поток телеметрии от торговых автоматов и автоматически корректирует цены в зависимости от спроса.

---

## 🏗 Архитектура системы

Сервис построен на реактивном стеке **Spring WebFlux** и использует событийную модель:

1.  **События (Telemetry):** Устройства отправляют данные в **Redis Streams**.
2.  **Обработка (Pricing Engine):** Асинхронный слушатель вычитывает события, применяет бизнес-логику и рассчитывает коэффициенты цен.
3.  **Хранение (Persistence):** Результаты расчетов сохраняются в **MongoDB** для истории и аналитики.
4.  **Гарантия доставки:** Использование **Consumer Groups** и механизма **ACK** (подтверждения) гарантирует, что ни одно событие не будет потеряно при сбое.

---

## 🛠 Технологический стек

* **Java 17 / Spring Boot 3**
* **Spring Data Reactive Redis** — работа с Redis Streams.
* **Spring Data Reactive MongoDB** — хранение истории обновлений цен.
* **Docker & Docker Compose** — контейнеризация инфраструктуры.

---

## 🚀 Быстрый старт

### 1. Инфраструктура
Для запуска Redis и MongoDB используйте Docker Compose:

```bash
docker compose up
```
### 2. Конфигурация
Приложение настроено на работу с локальными контейнерами. Основные параметры в application.yml

## 📡 Описание потока данных (Redis Streams)
Сервис слушает поток telemetry:events.

### Пример структуры сообщения (MapRecord):

deviceId: Уникальный идентификатор устройства (String).

eventType: Тип события (HIGH_DEMAND, LOW_STOCK).

timestamp: UNIX-время события (Long).

### Бизнес-логика обработки:

Если eventType == "HIGH_DEMAND" → коэффициент цены 1.5.

В остальных случаях → коэффициент 1.0.

### 🗄 Хранение данных (MongoDB)
Все изменения цен фиксируются в коллекции price_updates.

### Пример документа в базе:

```JSON
{
  "_id": "6643bd8f9f1b2c3d4e5f6g7h",
  "deviceId": "device-123",
  "priceCoefficient": 1.5,
  "timestamp": "2026-05-14T21:13:19"
}
```
## 🛠 Полезные команды для отладки

Запуск redis консоли
```Bash
docker exec -it redis_service redis-cli
```
Создание группы для чтения топика telemetry:events
```Bash
XGROUP CREATE telemetry:events my-group 0 MKSTREAM
```
Просмотр сообщений в Redis:
```Bash
XREAD STREAM telemetry:events STREAMS telemetry:events 0
```
Просмотр «зависших» сообщений (Pending Entries List):
```Bash
XPENDING telemetry:events my-group
```
Удаление топика
```Bash
DEL telemetry:events
```