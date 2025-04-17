# Kotlin Ktor JWT Authentication System

A RESTful API built with Kotlin and Ktor that implements JWT-based authentication, password hashing with BCrypt, and PostgreSQL database integration.

## Features

- JWT-based authentication
- Password hashing with BCrypt
- PostgreSQL database connection
- Automatic table creation
- RESTful API endpoints for user management
- Role-based access control (user/admin)

## Tech Stack

- **Framework**: Ktor
- **Database**: PostgreSQL
- **Authentication**: JWT
- **Password Hashing**: BCrypt
- **Database ORM**: Exposed
- **Testing**: JUnit, Ktor Test

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── org/
│   │       └── example/
│   │           ├── config/
│   │           │   ├── DatabaseFactory.kt
│   │           │   └── JWTConfig.kt
│   │           ├── models/
│   │           │   ├── DTOs.kt
│   │           │   └── Tables.kt
│   │           ├── plugins/
│   │           │   └── Routing.kt
│   │           ├── repositories/
│   │           │   ├── TokenRepository.kt
│   │           │   └── UserRepository.kt
│   │           ├── routes/
│   │           │   ├── AuthRoutes.kt
│   │           │   └── UserRoutes.kt
│   │           ├── services/
│   │           │   ├── AuthService.kt
│   │           │   └── UserService.kt
│   │           └── Application.kt
│   └── resources/
│       ├── application.conf
│       └── logback.xml
└── test/
    └── kotlin/
        └── org/
            └── example/
                ├── AuthTest.kt
                └── UserTest.kt
```

## API Endpoints

### Authentication Endpoints

- **POST /api/auth/register** - User registration
  - Request: `{ username, email, password }`
  - Response: `{ message, userId }`

- **POST /api/auth/login** - User login
  - Request: `{ email, password }`
  - Response: `{ token, userId, username }`

- **GET /api/auth/me** - Get current user info (protected)
  - Headers: `Authorization: Bearer <token>`
  - Response: `{ userId, username, email, role }`

- **POST /api/auth/logout** - Logout (protected)
  - Headers: `Authorization: Bearer <token>`
  - Response: `{ message }`

### User Management Endpoints

- **GET /api/users** - List all users (admin only)
  - Headers: `Authorization: Bearer <token>`
  - Response: `[{ userId, username, email, role }]`

- **GET /api/users/{id}** - Get specific user
  - Headers: `Authorization: Bearer <token>`
  - Response: `{ userId, username, email, role }`

- **PUT /api/users/{id}** - Update user (owner or admin)
  - Headers: `Authorization: Bearer <token>`
  - Request: `{ username?, email? }`
  - Response: `{ userId, username, email, role }`

- **DELETE /api/users/{id}** - Delete user (owner or admin)
  - Headers: `Authorization: Bearer <token>`
  - Response: `{ message }`

## Database Schema

### Users Table

- `id` (PK, auto-increment)
- `username` (unique)
- `email` (unique)
- `password_hash`
- `role` (default: 'user')
- `created_at`
- `updated_at`

### Tokens Table

- `id` (PK)
- `user_id` (FK to users)
- `token`
- `expires_at`
- `created_at`

## Setup and Running

### Prerequisites

- JDK 11 or higher
- PostgreSQL database

### Configuration

Edit the `src/main/resources/application.conf` file to configure:

- Server port
- Database connection details
- JWT settings

### Running the Application

```bash
./gradlew run
```

### Running Tests

```bash
./gradlew test
```

## Security Features

- JWT token expiration (24 hours by default)
- Password hashing with BCrypt
- Role-based access control
- Token invalidation on logout
- Input validation
- Proper error handling

## License

MIT