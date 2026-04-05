# Finance Dashboard Backend

A REST API backend for a finance dashboard system — built with **Java 17**, **Spring Boot 3**, and **MySQL**.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Default Credentials](#default-credentials)
- [Role & Permission Matrix](#role--permission-matrix)
- [API Reference](#api-reference)
- [Design Decisions & Assumptions](#design-decisions--assumptions)
- [Optional Enhancements Implemented](#optional-enhancements-implemented)

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| Framework    | Spring Boot 3.2                     |
| Security     | Spring Security + JWT (jjwt 0.11)   |
| Database     | MySQL 8                             |
| ORM          | Spring Data JPA / Hibernate         |
| Validation   | Jakarta Bean Validation             |
| Docs         | SpringDoc OpenAPI 3 (Swagger UI)    |
| Testing      | JUnit 5, Mockito, MockMvc, H2       |
| Build        | Maven                               |

---

## Project Structure

```
src/main/java/com/finance/dashboard/
├── config/                  # SecurityConfig, OpenApiConfig
├── controller/              # AuthController, UserController, TransactionController, DashboardController
├── dto/
│   ├── request/             # LoginRequest, RegisterRequest, TransactionRequest, etc.
│   └── response/            # ApiResponse<T>, PagedResponse<T>, AuthResponse, etc.
├── entity/                  # User, Transaction (JPA entities)
├── enums/                   # Role, UserStatus, TransactionType, TransactionCategory
├── exception/               # Custom exceptions + GlobalExceptionHandler
├── repository/              # UserRepository, TransactionRepository, TransactionSpecification
├── security/                # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
└── service/                 # AuthService, UserService, TransactionService, DashboardService
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### 1. Clone and configure

```bash
git clone https://github.com/your-org/finance-dashboard-backend.git
cd finance-dashboard-backend
```

Open `src/main/resources/application.yml` and update:

```yaml
spring:
  datasource:
    username: your_mysql_user
    password: your_mysql_password
```

> The database `finance_dashboard` is created automatically on first start (`createDatabaseIfNotExist=true`).

### 2. (Optional) Load seed data

The file `src/main/resources/data.sql` contains sample users and transactions for local testing.  
To enable it, add the following to `application.yml`:

```yaml
spring:
  sql:
    init:
      mode: always
```

> Make sure `ddl-auto` is set to `update` and the schema already exists before running the seed.

### 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

The server starts on **http://localhost:8081**

### 4. Explore the API

Open **http://localhost:8080/swagger-ui.html** in your browser.

Click **Authorize**, enter `Bearer <your_token>` after logging in.

---

## Default Credentials

These are seeded by `data.sql`:

| Email                  | Password     | Role     |
|------------------------|--------------|----------|
| admin@finance.com      | admin123     | ADMIN    |
| analyst@finance.com    | analyst123   | ANALYST  |
| viewer@finance.com     | viewer123    | VIEWER   |

> **Note:** The very first user to call `POST /api/auth/register` automatically receives the `ADMIN` role (so someone can bootstrap the system). All subsequent self-registrations default to `VIEWER`.

---

## Role & Permission Matrix

| Action                           | VIEWER | ANALYST | ADMIN |
|----------------------------------|:------:|:-------:|:-----:|
| Login / Register                 | ✅     | ✅      | ✅    |
| View transactions (list/detail)  | ✅     | ✅      | ✅    |
| Filter & search transactions     | ✅     | ✅      | ✅    |
| View dashboard summary           | ✅     | ✅      | ✅    |
| Create transaction               | ❌     | ✅      | ✅    |
| Update transaction               | ❌     | ✅      | ✅    |
| Delete transaction (soft)        | ❌     | ❌      | ✅    |
| List / view users                | ❌     | ❌      | ✅    |
| Create / update / delete users   | ❌     | ❌      | ✅    |

---

## API Reference

### Authentication — `POST /api/auth`

| Method | Endpoint             | Description                    |
|--------|----------------------|--------------------------------|
| POST   | `/api/auth/register` | Self-register (gets VIEWER)    |
| POST   | `/api/auth/login`    | Login, returns JWT token       |

**Login example:**
```json
POST /api/auth/login
{
  "email": "admin@finance.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "Admin User",
    "email": "admin@finance.com",
    "role": "ADMIN"
  }
}
```

---

### Transactions — `GET|POST|PUT|DELETE /api/transactions`

| Method | Endpoint                   | Role Required        | Description                  |
|--------|----------------------------|----------------------|------------------------------|
| GET    | `/api/transactions`        | Any authenticated    | List with filters + paging   |
| GET    | `/api/transactions/{id}`   | Any authenticated    | Get single transaction       |
| POST   | `/api/transactions`        | ANALYST, ADMIN       | Create new transaction       |
| PUT    | `/api/transactions/{id}`   | ANALYST, ADMIN       | Update existing transaction  |
| DELETE | `/api/transactions/{id}`   | ADMIN only           | Soft-delete transaction      |

**Query params for GET `/api/transactions`:**

| Param    | Type              | Example            | Description                  |
|----------|-------------------|--------------------|------------------------------|
| type     | INCOME / EXPENSE  | `type=INCOME`      | Filter by type               |
| category | enum              | `category=SALARY`  | Filter by category           |
| from     | YYYY-MM-DD        | `from=2024-03-01`  | Date range start             |
| to       | YYYY-MM-DD        | `to=2024-03-31`    | Date range end               |
| keyword  | string            | `keyword=salary`   | Search in description        |
| page     | int (0-based)     | `page=0`           | Page number                  |
| size     | int               | `size=10`          | Page size                    |
| sortBy   | field name        | `sortBy=amount`    | Sort field                   |
| sortDir  | asc / desc        | `sortDir=asc`      | Sort direction               |

**Create transaction body:**
```json
{
  "amount": 1500.00,
  "type": "INCOME",
  "category": "SALARY",
  "date": "2024-03-01",
  "description": "March salary",
  "notes": "Transferred to savings account"
}
```

Available categories: `SALARY`, `FREELANCE`, `INVESTMENT`, `RENT`, `FOOD`, `UTILITIES`, `HEALTHCARE`, `ENTERTAINMENT`, `TRAVEL`, `EDUCATION`, `SHOPPING`, `OTHER`

---

### Dashboard — `GET /api/dashboard`

| Method | Endpoint               | Description                                        |
|--------|------------------------|----------------------------------------------------|
| GET    | `/api/dashboard/summary` | Total income, expenses, net balance, category breakdown, recent activity, monthly trends |
| GET    | `/api/dashboard/weekly`  | All transactions in the last 7 days               |

**Query params for `/api/dashboard/summary`:**

| Param | Type | Default       | Description              |
|-------|------|---------------|--------------------------|
| year  | int  | current year  | Year for monthly trends  |

**Summary response structure:**
```json
{
  "totalIncome": 7850.00,
  "totalExpenses": 2275.50,
  "netBalance": 5574.50,
  "totalTransactions": 15,
  "categoryBreakdown": {
    "SALARY": 5000.00,
    "FREELANCE": 850.00,
    "INVESTMENT": 2000.00,
    "RENT": 1200.00,
    "FOOD": 320.50
  },
  "recentActivity": [ ... ],
  "monthlyTrends": [
    { "month": "Mar", "income": 7850.00, "expense": 2275.50 },
    { "month": "Apr", "income": 5500.00, "expense": 1610.00 }
  ]
}
```

---

### Users — `GET|POST|PUT|DELETE /api/users` *(ADMIN only)*

| Method | Endpoint           | Description                          |
|--------|--------------------|--------------------------------------|
| GET    | `/api/users`       | List all users (paged, filterable)   |
| GET    | `/api/users/{id}`  | Get user by ID                       |
| POST   | `/api/users`       | Create user with explicit role       |
| PUT    | `/api/users/{id}`  | Update name, role, status, password  |
| DELETE | `/api/users/{id}`  | Soft-delete (last admin protected)   |

**GET `/api/users` query params:**

| Param  | Type                    | Description           |
|--------|-------------------------|-----------------------|
| role   | VIEWER / ANALYST / ADMIN| Filter by role        |
| status | ACTIVE / INACTIVE       | Filter by status      |
| page   | int                     | Page number (0-based) |
| size   | int                     | Page size             |

---

## Design Decisions & Assumptions

**Soft Deletes**  
Both users and transactions use a `is_deleted` flag + `deleted_at` timestamp rather than physical deletion. This preserves audit history and allows recovery if needed. All queries filter on `deleted = false`.

**First-user bootstrap**  
The registration endpoint grants `ADMIN` to the very first user (when `users` table is empty). After that, self-registration always produces `VIEWER`. Admins can then elevate roles via the user management API.

**Analyst can write, not delete**  
The distinction between `ANALYST` and `ADMIN` on transactions is intentional. Analysts should be able to enter and correct records, but physical removal of financial data is a higher-privilege action.

**JPA Specification for filtering**  
`TransactionSpecification` builds a composable `Specification<Transaction>` from individual filter predicates. This avoids a long chain of `if/else` query methods and makes it easy to add new filters later.

**Dashboard aggregations in SQL**  
Rather than pulling all rows into Java and summing in memory, the summary endpoints use `@Query` with `SUM` / `GROUP BY` directly in the database. This keeps things efficient even with a large transaction table.

**No refresh token (yet)**  
JWT tokens expire after 24 hours. A refresh token flow is a natural next step but was out of scope for this assignment.

**Error response shape**  
All errors follow the same `ApiResponse<T>` envelope (`success`, `message`, optional `data`). Validation errors include a map of `field -> error message` so the frontend can highlight specific inputs.

---

## Optional Enhancements Implemented

| Enhancement         | Details                                                              |
|---------------------|----------------------------------------------------------------------|
| JWT Authentication  | Stateless auth via Bearer token, 24-hour expiry                      |
| Pagination          | All list endpoints support `page`, `size`, `sortBy`, `sortDir`       |
| Search              | `keyword` param does case-insensitive LIKE search on `description`   |
| Soft Delete         | Both `users` and `transactions` tables use `is_deleted` + timestamp  |
| Dynamic Filtering   | JPA Specification pattern — composable, null-safe filter predicates  |
| Swagger UI          | Full OpenAPI 3 docs at `/swagger-ui.html` with JWT auth support      |
| Unit Tests          | Service-layer tests with Mockito (TransactionService, UserService)   |
| Integration Tests   | MockMvc controller tests verifying role-based access                 |
| Audit Fields        | `created_at` / `updated_at` auto-managed via `@EnableJpaAuditing`    |
| DB Indexes          | Indexes on `date`, `type`, `category` for faster filtered queries    |
| Seed Data           | `data.sql` with 3 users and 15 sample transactions                   |

---


