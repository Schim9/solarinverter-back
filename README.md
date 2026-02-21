# solarinverter-back

REST API backend for a solar inverter monitoring application.
Frontend: https://github.com/Schim9/solarinverter-front

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 (Data JPA, Web Services) |
| Database | MySQL 8 — database `inverter` |
| ORM | Spring Data JPA / Hibernate |
| HTTP client | Apache HttpClient 4.5.13 |
| JSON parsing | GSON 2.9.0 |
| Utilities | Google Guava 31.0.1 |
| Boilerplate | Lombok |
| Logging | Log4j2 (rolling file, 10 MB / 10 backups) |
| Notifications | PushBullet |
| Testing | JUnit 5 + Mockito (via `spring-boot-starter-test`) |
| Build | Maven (wrapper included) |

---

## Architecture

The application polls a solar inverter HTTP API (Basic Auth) and stores daily production data in MySQL. It exposes a REST API consumed by the frontend SPA.

```
src/main/java/lu/kaminski/inverter/
├── controler/
│   ├── MainController.java         # REST endpoints
│   └── ShutdownController.java     # Graceful shutdown
├── service/
│   ├── InverterService.java        # Inverter HTTP API client
│   ├── SyncService.java            # Schedulers + business logic
│   └── DataService.java            # Database query layer
├── dao/
│   └── DailyProdDAO.java           # JPA repository
├── model/
│   ├── entity/DailyProdEntity.java # DB entity: date (PK), value (kWh)
│   └── rest/ProdRestModel.java     # REST DTO
└── util/
    └── NotifUtil.java              # PushBullet notifications
```

---

## REST API

Base path: `/api`
CORS allowed origins: `https://solar-app.kaminski.lu`, `http://localhost:4200`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/status` | Health check |
| GET | `/api/inverter-status` | Trigger inverter status check + notification |
| GET | `/api/daily-prod?start=YYYY-MM-DD&end=YYYY-MM-DD` | Historical production from DB |
| GET | `/api/livedata` | Live data from inverter + sync to DB. Response includes `contractProd` (kWh produced since last contract anniversary date) |
| GET | `/api/update?nbDays=5` | Manual sync (default: last 5 days) |
| POST | `/shutdownContext` | Graceful application shutdown |

---

## Schedulers

| Property | Default schedule | Action |
|---|---|---|
| `schedule.task.syncProductionData` | `0 0 10 * * *` (10:00 AM) | Sync last 5 days of production data |
| `schedule.task.checkInverterStatus` | `0 0 14 * * *` (2:00 PM) | Check inverter status + send notification |

---

## Configuration

Fill in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inverter?serverTimezone=UTC
    username: root
    password:

inverter:
  url:      # Inverter base URL
  token:    # Base64-encoded Basic Auth token

pushbullet:
  api-key:  # PushBullet API key

contract:
  anniversary-day: 25    # Day of the contract anniversary date
  anniversary-month: 1   # Month of the contract anniversary date

schedule.task:
  syncProductionData: 0 0 10 * * *
  checkInverterStatus: 0 0 14 * * *
```

---

## Database

Table `daily_prod`:

| Column | Type | Description |
|---|---|---|
| `date` | DATE (PK) | Production date |
| `value` | DECIMAL | Production in kWh |

---

## Build & Run

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

26 unit tests covering `DataService`, `SyncService`, and `MainController`.
