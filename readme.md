# Finance Dashboard Backend

A role-based finance management backend built with Spring Boot. The system allows an organization to manage financial records, control user access by role, and view company-wide analytics through a dashboard.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Setup and Running](#setup-and-running)
- [Authentication](#authentication)
- [Roles and Permissions](#roles-and-permissions)
- [API Reference](#api-reference)
    - [Auth](#auth-endpoints)
    - [Admin — User Management](#admin-endpoints-admin-only)
    - [Financial Records](#financial-record-endpoints)
    - [Dashboard](#dashboard-endpoints)
    - [User Profile](#user-profile-endpoint)
- [Data Models](#data-models)
- [Design Decisions and Assumptions](#design-decisions-and-assumptions)
- [Error Handling](#error-handling)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (access token) + Refresh Token (HttpOnly cookie) |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| Utilities | Lombok |

---

## Project Structure

```
src/main/java/com/auth/
├── config/
│   ├── AppConfig.java             # BCryptPasswordEncoder bean
│   └── DataInitializer.java       # Seeds admin user on startup
├── controller/
│   ├── AdminController.java       # User management (Admin only)
│   ├── AuthController.java        # Login, refresh, logout
│   ├── DashboardController.java   # Company-wide analytics
│   ├── FinancialRecordController.java
│   └── UserController.java        # Current user profile
├── dto/
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── CategorySummary.java
│   ├── CreateFinancialRecordRequest.java
│   ├── CreateUserRequest.java
│   ├── DashboardResponse.java
│   ├── FinancialRecordResponse.java
│   ├── LoginRequest.java
│   ├── MonthlyTrend.java
│   ├── RecentActivity.java
│   ├── UpdateUserRequest.java
│   └── UserResponse.java
├── entities/
│   ├── FinancialRecord.java
│   ├── RecordType.java            # Enum: INCOME, EXPENSE
│   ├── RefreshToken.java
│   ├── Role.java                  # Enum: ADMIN, ANALYST, VIEWER
│   └── User.java
├── exceptions/
│   ├── AuthException.java
│   ├── GlobalExceptionHandler.java
│   └── NotFoundException.java
├── repository/
│   ├── FinancialRecordRepo.java
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── SecurityConfig.java
│   └── TokenService.java
└── service/
    ├── AdminService.java
    ├── AuthService.java
    ├── DashboardService.java
    ├── FinancialRecordService.java
    └── UserService.java
```

---

## Setup and Running

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd finance-dashboard-backend
```

### 2. Create the database

```sql
CREATE DATABASE finance_db;
```

### 3. Configure `application.properties`

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=your-secret-key-minimum-32-characters-long
app.jwt.access-token-validity-seconds=900
app.jwt.refresh-token-validity-seconds=604800

# Default Admin (seeded automatically on first run)
app.admin.email=admin@gmail.com
app.admin.password=admin123
app.admin.name=Admin
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

On first startup, the admin account is automatically created using the credentials in `application.properties`.

---

## Authentication

This project uses a **dual-token strategy**:

| Token | Type | Validity | Storage |
|---|---|---|---|
| Access Token | JWT (Bearer) | 15 minutes | Client memory / Authorization header |
| Refresh Token | Opaque (SHA-256 hashed in DB) | 7 days | HttpOnly cookie |

**How to use:**
1. Call `POST /api/auth/login` to receive an `accessToken` in the response body.
2. Include it in every protected request as: `Authorization: Bearer <accessToken>`
3. When the access token expires, call `POST /api/auth/refresh` — the refresh token is sent automatically via cookie.
4. Refresh tokens are rotated on every use (old token is revoked, new one is issued).

---

## Roles and Permissions

Financial records belong to the **organization**, not to individual users. Every authenticated user sees the same company-wide records and dashboard. The `createdBy` field on a record is an audit trail only — it tracks who logged the entry, not who "owns" it.

| Action | ADMIN | ANALYST | VIEWER |
|---|:---:|:---:|:---:|
| Login | ✅ | ✅ | ✅ |
| View own profile | ✅ | ✅ | ✅ |
| View all financial records | ✅ | ✅ | ✅ |
| Filter financial records | ✅ | ✅ | ✅ |
| View dashboard | ✅ | ✅ | ✅ |
| View monthly trends | ✅ | ✅ | ✅ |
| Create financial records | ✅ | ✅ | ❌ |
| Delete financial records | ✅ | ✅ (own only) | ❌ |
| Create users | ✅ | ❌ | ❌ |
| View all users | ✅ | ❌ | ❌ |
| Update user role/status | ✅ | ❌ | ❌ |
| Toggle user active/inactive | ✅ | ❌ | ❌ |
| Delete users | ✅ | ❌ | ❌ |

> **Note on delete:** Admin can delete any record. Analyst can only delete records they personally created.

---

## API Reference

### Base URL
```
http://localhost:8080
```

### Auth Endpoints

#### POST `/api/auth/login`
Authenticate and receive tokens. No authorization required.

**Request body:**
```json
{
  "email": "admin@gmail.com",
  "password": "admin123"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGci...",
  "user": {
    "id": "a487c50e-605e-4381-97dc-0bd04b462c3a",
    "email": "admin@gmail.com",
    "name": "Admin",
    "role": "ADMIN",
    "active": true
  }
}
```

The refresh token is set automatically as an HttpOnly cookie.

---

#### POST `/api/auth/refresh`
Get a new access token using the refresh token cookie. No body needed.

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGci...",
  "user": null
}
```

---

#### POST `/api/auth/logout`
Revoke the refresh token and clear the cookie. No body needed.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Logged out Successfully"
}
```

---

### Admin Endpoints *(Admin only)*

All routes under `/api/admin/**` require role `ADMIN`.

---

#### POST `/api/admin/users`
Create a new user with a specified role.

**Request body:**
```json
{
  "email": "analyst@gmail.com",
  "password": "analyst123",
  "name": "Test Analyst",
  "role": "ANALYST"
}
```
`role` accepts: `ADMIN`, `ANALYST`, `VIEWER`

**Response `200 OK`:**
```json
{
  "id": "uuid",
  "email": "analyst@gmail.com",
  "name": "Test Analyst",
  "role": "ANALYST",
  "active": true
}
```

---

#### GET `/api/admin/users`
Retrieve all users in the system.

**Response `200 OK`:**
```json
[
  {
    "id": "uuid",
    "email": "analyst@gmail.com",
    "name": "Test Analyst",
    "role": "ANALYST",
    "active": true
  }
]
```

---

#### PUT `/api/admin/users/{id}`
Update a user's role or active status.

**Request body** (all fields optional):
```json
{
  "role": "VIEWER",
  "active": false
}
```

---

#### PATCH `/api/admin/users/{id}/status`
Toggle a user's active/inactive status. No request body needed.

---

#### DELETE `/api/admin/users/{id}`
Delete a user by ID. An admin cannot delete their own account.

**Error response when deleting self `400 Bad Request`:**
```json
{
  "success": false,
  "message": "Admin Cannot delete himself"
}
```

---

### Financial Record Endpoints

Accessible by all authenticated roles for `GET`. Creating and deleting requires `ADMIN` or `ANALYST`.

---

#### POST `/api/records` *(Admin, Analyst)*
Create a new financial record for the organization.

**Request body:**
```json
{
  "amount": 50000.00,
  "type": "INCOME",
  "category": "salary",
  "date": "2025-04-01",
  "description": "April salary credited"
}
```

`type` accepts: `INCOME`, `EXPENSE`

**Response `200 OK`:**
```json
{
  "id": "uuid",
  "amount": 50000.0,
  "type": "INCOME",
  "category": "salary",
  "date": "2025-04-01",
  "description": "April salary credited",
  "createdByName": "Admin",
  "createdByEmail": "admin@gmail.com"
}
```

---

#### GET `/api/records` *(All roles)*
Get all company financial records.

---

#### GET `/api/records/filter` *(All roles)*
Filter records using optional query parameters. All parameters are optional and can be combined.

| Query Param | Type | Example |
|---|---|---|
| `type` | `INCOME` or `EXPENSE` | `?type=INCOME` |
| `category` | String | `?category=salary` |
| `from` | Date `yyyy-MM-dd` | `?from=2025-03-01` |
| `to` | Date `yyyy-MM-dd` | `?to=2025-03-31` |

**Example requests:**
```
GET /api/records/filter?type=EXPENSE
GET /api/records/filter?category=rent
GET /api/records/filter?from=2025-03-01&to=2025-03-31
GET /api/records/filter?type=INCOME&category=salary
```

---

#### DELETE `/api/records/{id}` *(Admin, Analyst)*
Delete a financial record by ID.

- Admin can delete any record.
- Analyst can only delete records they created.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Record deleted successfully"
}
```

**Error response when Analyst tries to delete another user's record `401`:**
```json
{
  "success": false,
  "message": "You are not authorized to delete this record"
}
```

---

### Dashboard Endpoints

Accessible by all authenticated roles.

---

#### GET `/api/dashboard`
Returns company-wide financial summary.

**Response `200 OK`:**
```json
{
  "totalIncome": 75000.0,
  "totalExpense": 11500.0,
  "netBalance": 63500.0,
  "categorySummary": [
    { "category": "salary",      "total": 50000.0 },
    { "category": "consulting",  "total": 25000.0 },
    { "category": "rent",        "total":  8000.0 },
    { "category": "utilities",   "total":  3500.0 }
  ],
  "recentActivity": [
    {
      "amount": 8000.0,
      "category": "rent",
      "type": "EXPENSE",
      "date": "2025-04-02"
    }
  ],
  "message": "Dashboard loaded successfully"
}
```

---

#### GET `/api/dashboard/trends`
Returns monthly income and expense breakdown.

**Response `200 OK`:**
```json
[
  { "month": "2025-03", "totalIncome": 25000.0, "totalExpense": 3500.0 },
  { "month": "2025-04", "totalIncome": 50000.0, "totalExpense": 8000.0 }
]
```

---

### User Profile Endpoint

#### GET `/api/users/me`
Returns the currently authenticated user's profile. Works for all roles.

**Response `200 OK`:**
```json
{
  "id": "uuid",
  "email": "analyst@gmail.com",
  "name": "Test Analyst",
  "role": "ANALYST",
  "active": true
}
```

---

## Data Models

### User

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `email` | String | Unique, not null |
| `passwordHash` | String | BCrypt hashed |
| `name` | String | Display name |
| `role` | Enum | `ADMIN`, `ANALYST`, `VIEWER` |
| `active` | Boolean | Default `true` |
| `createdAt` | OffsetDateTime | Auto-set |
| `updatedAt` | OffsetDateTime | Updated on change |

### FinancialRecord

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `amount` | Double | Must be positive |
| `type` | Enum | `INCOME` or `EXPENSE` |
| `category` | String | Stored lowercase |
| `date` | LocalDate | Format `yyyy-MM-dd` |
| `description` | String | Optional |
| `createdBy` | User (FK) | Audit trail — not ownership |

### RefreshToken

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `user` | User (FK) | Token owner |
| `tokenHash` | String | SHA-256 hash of plain token |
| `revoked` | Boolean | Default `false` |
| `issuedAt` | OffsetDateTime | |
| `expiresAt` | OffsetDateTime | Configurable via properties |

---

## Design Decisions and Assumptions

**Financial records are organization-wide, not user-owned.**
Records belong to the company. Any authenticated user can view all records. The `createdBy` field exists purely as an audit trail to track who entered each record — it is not used for data filtering.

**Categories are stored in lowercase.**
On record creation, category values are automatically lowercased and trimmed. This prevents "Rent" and "rent" from being treated as separate categories in dashboard summaries.

**Refresh token rotation on every use.**
Each time `/api/auth/refresh` is called, the current refresh token is revoked and a new one is issued. This reduces the risk of token reuse after theft.

**Refresh tokens are hashed in the database.**
Plain refresh tokens are never stored. Only a SHA-256 hash is persisted, so a database breach does not expose usable tokens.

**Admin user is seeded from configuration, not hardcoded.**
The default admin credentials are read from `application.properties` at startup. If the admin account already exists, seeding is skipped. This makes it easy to change credentials per environment.

**Viewer role cannot perform any write operations.**
This is enforced at two levels — Spring Security `authorizeHttpRequests` rules and `@PreAuthorize` method annotations — so the restriction cannot be bypassed by routing alone.

**An admin cannot delete their own account.**
This prevents accidental lockout where the system is left with no admin user.

**`ANALYST` can delete only their own records.**
This provides accountability — an analyst who logged a record can correct it, but cannot erase records entered by others. Only Admin has full delete authority.

---

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "success": false,
  "message": "Descriptive error message here"
}
```

| Scenario | HTTP Status |
|---|---|
| Invalid credentials or token | `401 Unauthorized` |
| Insufficient role permissions | `403 Forbidden` |
| Resource not found | `404 Not Found` |
| Validation failure (missing/invalid fields) | `400 Bad Request` |
| Internal server error | `500 Internal Server Error` |

**Validation error example** — `POST /api/records` with missing fields:
```json
{
  "success": false,
  "message": "amount: Amount is required, type: Type is required"
}
```