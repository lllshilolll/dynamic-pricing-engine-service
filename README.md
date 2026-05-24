# Dynamic Pricing Engine

Система динамического ценообразования в реальном времени для вендинговых автоматов. Устройства отправляют телеметрию через Kafka; движок ценообразования потребляет события, применяет бизнес-правила и сохраняет обновлённые цены в MongoDB. Аналитика телеметрии — в ClickHouse. Управление правилами и устройствами — через admin-service с PostgreSQL.

---

## Архитектура

```
Устройство ──POST──▶ telemetry-ingestion ──produce──▶ Kafka ──consume──▶ pricing-engine
  (HTTP)               :8082 WebFlux          telemetry.events               :8081 WebFlux
                                                                            │
                                                                            ├── MongoDB (device_state)
                                                                            ├── Redis (price cache)
                                                                            └── ClickHouse (telemetry_events)

Администратор ──▶ admin-service :8083 MVC ──▶ PostgreSQL + Redis Pub/Sub (rules:updated)

Аналитик ──▶ analytics-service :8084 MVC ──▶ ClickHouse (JDBC)
```

### Поток данных

1. Устройство отправляет `POST /api/telemetry` с событием (`deviceId`, `eventType`, `payload`, `timestamp`)
2. `telemetry-ingestion-service` публикует событие в Kafka topic `telemetry.events` (key = `deviceId`)
3. `pricing-engine-service` читает события (consumer group `pricing-group`)
4. `PricingService` вычисляет новую цену, сохраняет в MongoDB (`device_state`), кэширует в Redis, записывает в ClickHouse
5. Администратор управляет правилами через `admin-service` → PostgreSQL → Redis Pub/Sub `rules:updated`

---

## Сервисы

| Сервис | Порт | Стек | Назначение |
|--------|------|------|------------|
| `telemetry-ingestion-service` | 8082 | WebFlux + Kafka | Приём телеметрии, Kafka producer |
| `pricing-engine-service` | 8081 | WebFlux + Kafka + MongoDB + Redis + ClickHouse | Потребление событий, расчёт цен |
| `admin-service` | 8083 | Spring MVC + PostgreSQL + Flyway + Redis | CRUD устройств, правил, экспериментов |

---

## API

### telemetry-ingestion-service (:8082)

```
POST /api/telemetry     — отправить событие телеметрии
GET  /api/test/send     — отправить тестовое событие
```

### admin-service (:8083)

```
POST   /api/devices              — зарегистрировать устройство
GET    /api/devices              — список устройств
GET    /api/devices/{externalId} — устройство по external ID
PUT    /api/devices/{id}         — обновить устройство

POST   /api/rules                — создать правило ценообразования
GET    /api/rules                — все правила
GET    /api/rules/active         — активные правила (по приоритету)
PUT    /api/rules/{id}           — обновить правило
DELETE /api/rules/{id}           — деактивировать правило

POST   /api/experiments          — создать A/B эксперимент
GET    /api/experiments          — список экспериментов
POST   /api/experiments/{id}/stop — завершить эксперимент
```

---

## Инфраструктура

```bash
docker compose up
```

| Сервис | Порт | Данные                                   |
|--------|------|------------------------------------------|
| Redis 7 | 6379 | кэш цен, pub/sub, bloom filter           |
| MongoDB 7.0 | 27017 | `root` / `password123`, auth-db `admin`  |
| Kafka (Confluent 7.7) | 9092 | topic `telemetry.events`, replication=1  |
| PostgreSQL 16 | 5432 | `admin` / `admin123`, db `pricing_admin` |
| ClickHouse 24.8 | 8123/9000 | таблица `telemetry_events`               |

---

## Сборка и запуск

```bash
# 1. Установить общий DTO
mvn install -pl dto-service

# 2. Собрать все сервисы
mvn package -pl telemetry-ingestion-service,pricing-engine-service,admin-service

# 3. Запустить инфраструктуру
docker compose up -d

# 4. Запустить сервисы (каждый в отдельном терминале)
java -jar telemetry-ingestion-service/target/*.jar
java -jar pricing-engine-service/target/*.jar
java -jar admin-service/target/*.jar
```

---

## Тесты

```bash
mvn test -pl telemetry-ingestion-service
mvn test -pl pricing-engine-service
mvn test -pl admin-service
```

---

## Структура проекта

```
dynamic-pricing-engine-service/
├── dto-service/                  Общий JAR: TelemetryEvent
├── telemetry-ingestion-service/  :8082 WebFlux — приём телеметрии → Kafka
├── pricing-engine-service/       :8081 WebFlux — Kafka consumer → расчёт цен
├── admin-service/                :8083 MVC — управление правилами, устройствами
├── compose.yaml                  Docker Compose (Redis, MongoDB, Kafka, PostgreSQL, ClickHouse)
└── pom.xml                       Aggregator POM
```

---

## Стек

- **Java 17**, **Spring Boot 3.4.3**
- **Spring WebFlux** (telemetry-ingestion, pricing-engine)
- **Spring MVC** (admin-service)
- **Spring Kafka** + **Reactor Kafka** (event streaming)
- **Spring Data MongoDB Reactive** (device_state)
- **Spring Data JPA** + **Flyway** (PostgreSQL)
- **Spring Data Redis Reactive** (кэш, pub/sub)
- **ClickHouse JDBC** (аналитика)
- **Lombok**
- **Docker Compose**
