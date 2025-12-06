# HMCTS Task Management System

A full-stack application for caseworkers to create and track tasks, built as part of the DTS Developer Technical Test.

## 🚀 Features

- **Task Creation**: Create new tasks with title, description (optional), status, and due date/time
- **Validation**: Comprehensive client-side and server-side validation
- **Error Handling**: Graceful error handling with user-friendly messages
- **GOV.UK Design System**: Frontend styled using the official GOV.UK Design System components
- **RESTful API**: Clean, documented API endpoints with OpenAPI/Swagger documentation
- **Database Storage**: Tasks persisted in H2 in-memory database

## 📋 Task Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| Title | String | Yes | Task title (1-255 characters) |
| Description | String | No | Optional task description (max 1000 characters) |
| Status | Enum | Yes | PENDING, IN_PROGRESS, COMPLETED, CANCELLED |
| Due Date | DateTime | Yes | Must be in the future |

## 🏗️ Architecture

```
┌─────────────┐      ┌─────────────┐      ┌──────────────┐
│   Frontend  │ ───► │   Backend   │ ───► │   Database   │
│   (Node.js) │      │  (Spring)   │      │     (H2)     │
│   Port 3100 │      │  Port 4000  │      │  In-Memory   │
└─────────────┘      └─────────────┘      └──────────────┘
```

## 🛠️ Tech Stack

### Backend
- Java 21
- Spring Boot 3.5
- Spring Data JPA
- H2 Database
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 & Mockito

### Frontend
- Node.js 18+
- Express.js
- Nunjucks templating
- GOV.UK Frontend
- TypeScript
- Jest & Chai

## 🚦 Getting Started

### Prerequisites
- Java 21
- Node.js 18+
- Yarn (npm alternative)

### Running the Backend

```bash
cd backend

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Build the JAR
./gradlew build
```

The backend will start on http://localhost:4000

### Running the Frontend

```bash
cd frontend

# Install dependencies
yarn install

# Build assets
yarn build

# Run in development mode
yarn start:dev

# Run tests
yarn test:routes
```

The frontend will start on http://localhost:3100

## 📚 API Documentation

### Swagger UI
Once the backend is running, access the interactive API documentation at:
- http://localhost:4000/swagger-ui.html

### API Endpoints

#### Create Task
```
POST /api/tasks
Content-Type: application/json

{
  "title": "Review case documents",
  "description": "Review all submitted documents for case ABC123",
  "status": "PENDING",
  "dueDate": "2025-12-31T17:00:00"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Review case documents",
  "description": "Review all submitted documents for case ABC123",
  "status": "PENDING",
  "dueDate": "2025-12-31T17:00:00",
  "createdAt": "2025-12-06T10:30:00",
  "updatedAt": "2025-12-06T10:30:00"
}
```

**Validation Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    "title: Title is required",
    "dueDate: Due date must be in the future"
  ],
  "timestamp": "2025-12-06T10:30:00"
}
```

### Status Values
| Value | Display Name |
|-------|--------------|
| PENDING | Pending |
| IN_PROGRESS | In Progress |
| COMPLETED | Completed |
| CANCELLED | Cancelled |

## 🧪 Testing

### Backend Tests
```bash
cd backend
./gradlew test
```

Test coverage includes:
- `TaskControllerTest`: Controller layer tests with validation scenarios
- `TaskServiceTest`: Service layer business logic tests
- `TaskStatusTest`: Enum tests
- `TaskResponseTest`: DTO mapping tests

### Frontend Tests
```bash
cd frontend
yarn test:routes
```

Test coverage includes:
- Route rendering tests
- Form validation tests
- API integration tests (mocked)
- Error handling tests

## 📁 Project Structure

```
├── backend/
│   └── src/
│       ├── main/java/uk/gov/hmcts/reform/dev/
│       │   ├── controllers/      # REST controllers
│       │   ├── dto/              # Data Transfer Objects
│       │   ├── exception/        # Global exception handling
│       │   ├── models/           # JPA entities
│       │   ├── repository/       # Data repositories
│       │   └── service/          # Business logic
│       └── test/java/            # Unit tests
│
└── frontend/
    └── src/
        ├── main/
        │   ├── routes/           # Express routes
        │   └── views/            # Nunjucks templates
        └── test/
            └── routes/           # Route tests
```

## 🔒 Validation Rules

### Title
- Required field
- Must be between 1 and 255 characters

### Description
- Optional field
- Maximum 1000 characters

### Status
- Required field
- Must be one of: PENDING, IN_PROGRESS, COMPLETED, CANCELLED

### Due Date
- Required field
- Must be a valid future date and time

## 📝 Design Decisions

1. **H2 In-Memory Database**: Chosen for simplicity and zero-configuration setup. In production, this would be replaced with PostgreSQL.

2. **GOV.UK Design System**: Used the official GOV.UK components to ensure accessibility and consistency with government service standards.

3. **Server-Side Rendering**: Frontend renders HTML server-side using Nunjucks templates, following the GOV.UK approach for better accessibility and progressive enhancement.

4. **DTO Pattern**: Separate DTOs for request/response to decouple API contract from entity structure.

5. **Global Exception Handler**: Centralized error handling for consistent API responses.

## 🔗 Useful Links

- [Backend README](./backend/README.md)
- [Frontend README](./frontend/README.md)
- [GOV.UK Design System](https://design-system.service.gov.uk/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
