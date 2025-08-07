# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack web application with a Spring Boot 3.5.4 backend and Vue 3 frontend. The project is structured as a monorepo containing both components:

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.4 with Spring Web and Java 21
- **Database Layer**: MyBatis-Flex 1.11.0 for ORM with code generation support
- **Database**: MySQL 连接到 `ai_code_mother` 数据库
- **Connection Pool**: HikariCP for database connection management
- **API Documentation**: Integrated with Knife4j (Swagger UI) accessible at `/doc.html`
- **Base URL**: Application runs on port 8123 with context path `/api`
- **Utilities**: Hutool library for common Java utilities and Lombok for boilerplate reduction
- **Build Tool**: Maven with wrapper scripts (`mvnw` and `mvnw.cmd`)

### Frontend (Vue 3)
- **Framework**: Vue 3 with TypeScript and Vite
- **UI Library**: Ant Design Vue for components
- **State Management**: Pinia for state management
- **HTTP Client**: Axios for API requests
- **API Integration**: Automated OpenAPI client generation from backend
- **Development**: Hot-reload development server with TypeScript support

## Development Commands

### Backend Development
```bash
# Build the backend
mvnw.cmd clean compile    # Windows
./mvnw clean compile      # Linux/Mac

# Run tests
mvnw.cmd test            # Windows
./mvnw test              # Linux/Mac

# Run the backend application
mvnw.cmd spring-boot:run  # Windows
./mvnw spring-boot:run    # Linux/Mac

# Package as JAR
mvnw.cmd clean package    # Windows
./mvnw clean package      # Linux/Mac

# Run code generator (for new database tables)
mvnw.cmd compile exec:java -Dexec.mainClass="com.spring.aicodemother.generator.MyBatisCodeGenerator"
```

### Frontend Development
```bash
# Navigate to frontend directory
cd ai-code-mother-frontend

# Install dependencies
npm install

# Start development server with hot-reload
npm run dev

# Build for production (includes type-check)
npm run build

# Build only (skip type checking)
npm run build-only

# Type checking only
npm run type-check

# Lint and auto-fix code
npm run lint

# Format code with Prettier
npm run format

# Generate API client from backend OpenAPI spec
npm run openapi2ts
```

### Full-Stack Development Workflow
1. Start the backend: `mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (Linux/Mac)
2. In another terminal, start the frontend: `cd ai-code-mother-frontend && npm run dev`
3. Backend runs on `http://localhost:8123/api`
4. Frontend runs on `http://localhost:5173` (or next available port)
5. After backend changes, regenerate frontend API client: `npm run openapi2ts`

### Testing
- All tests: `mvnw.cmd test` (Windows) or `./mvnw test` (Linux/Mac)
- Single test: `mvnw.cmd test -Dtest=ClassName#methodName`
- Test class: `mvnw.cmd test -Dtest=ClassName`

### API Testing and Documentation
- Health check: `curl -X GET "http://localhost:8123/api/health/" -H "accept: application/json"`
- Swagger UI: `http://localhost:8123/api/doc.html` (Chinese language)
- OpenAPI JSON: `http://localhost:8123/api/v3/api-docs`

## Architecture Overview

### Monorepo Structure
```
ai-code-mother/
├── src/                        # Spring Boot backend source
├── pom.xml                     # Backend dependencies and build config
├── ai-code-mother-frontend/    # Vue 3 frontend application
└── CLAUDE.md                   # This documentation file
```

### Backend Package Structure (Spring Boot)
- `com.spring.aicodemother` - Root package
  - `common/` - Shared utilities and response wrappers
    - `BaseResponse<T>` - Standardized API response wrapper with Lombok @Data
    - `ResultUtils` - Utility for creating consistent responses
    - `PageRequest`, `DeleteRequest` - Common request DTOs
  - `controller/` - REST API endpoints
    - `HealthController` - Basic health check endpoint
    - `UserController` - Complete user CRUD operations with registration, login, and admin features
  - `service/` - Business logic layer
    - `UserService` - User service interface
    - `impl/UserServiceImpl` - User service implementation with MD5 password encryption and session management
  - `mapper/` - MyBatis-Flex data access layer
    - `UserMapper` - User database operations interface (auto-generated)
  - `model/` - Data models and DTOs
    - `entity/User` - User entity with MyBatis-Flex annotations and Snowflake ID generation
    - `dto/` - Request/response DTOs (UserRegisterRequest, UserLoginRequest, UserQueryRequest, UserAddRequest, UserUpdateRequest, UserVO)
    - `vo/LoginUserVO` - Login user view object for session data
    - `enums/UserRoleEnum` - User role enumeration (USER/ADMIN)
  - `exception/` - Centralized error handling
    - `ErrorCode` - Enum with standardized error codes
    - `BusinessException` - Custom business exception
    - `GlobalExceptionHandler` - Global exception handling with proper error responses
    - `ThrowUtils` - Utility for throwing exceptions
  - `generator/` - Code generation utilities
    - `MyBatisCodeGenerator` - MyBatis-Flex code generator for entities, mappers, and services
  - `annotation/` - Custom annotations
    - `AuthCheck` - Role-based authorization annotation
  - `aop/` - Aspect-oriented programming
    - `AuthInterceptor` - Authorization interceptor for role checking
  - `constant/` - Application constants
    - `UserConstant` - User-related constants (salt, session keys, etc.)

### Frontend Architecture (Vue 3)
- **Components**: Reusable Vue components with TypeScript
- **Pages**: Route-based page components
- **API Layer**: Auto-generated TypeScript clients from OpenAPI spec
- **State Management**: Pinia stores for application state
- **Routing**: Vue Router for SPA navigation
- **Styling**: CSS modules with Ant Design Vue components

### API Response Pattern
All API endpoints follow a consistent response pattern using `BaseResponse<T>`:
```java
{
  "code": 0,           // 0 = success, other codes indicate errors
  "data": <T>,         // Response payload
  "message": "ok"      // Status message
}
```

### Error Code System
Standardized error codes defined in `ErrorCode` enum:
- `0` - SUCCESS
- `40000` - PARAMS_ERROR (Request parameter error)
- `40100` - NOT_LOGIN_ERROR
- `40101` - NO_AUTH_ERROR
- `40400` - NOT_FOUND_ERROR
- `40300` - FORBIDDEN_ERROR
- `50000` - SYSTEM_ERROR
- `50001` - OPERATION_ERROR

### Current State
This is a full-stack application with both backend and frontend infrastructure set up and basic user management functionality implemented. The project includes:

**Backend:**
- Complete user management system with registration, CRUD operations, and pagination
- User entity with MyBatis-Flex annotations, Snowflake ID generation, and logical delete support
- MD5 password encryption with salt ("Join2049") for security
- MyBatis-Flex code generator for rapid development of entities, mappers, and services
- Health check endpoint at `/api/health`
- Complete error handling framework with custom exception hierarchy
- API documentation setup with Knife4j (Chinese language)
- Database configured for MySQL connection to `ai_code_mother` database

**Frontend:**
- Vue 3 application with TypeScript and comprehensive UI framework setup  
- Ant Design Vue integration for consistent component library
- HTTP client with authentication handling (40100 error code triggers login redirect)
- Automated API client generation from backend OpenAPI spec
- Development environment with hot-reload and full TypeScript support

## Development Notes

### Backend Development
- The application uses Chinese language configuration for Knife4j documentation
- Lombok annotations are configured for compile-time processing
- Global exception handling is already implemented for consistent error responses
- The project structure follows standard Spring Boot conventions

### Frontend Development
- Uses Vue 3 Composition API with TypeScript
- Ant Design Vue provides pre-built components
- Vite for fast development and building
- ESLint and Prettier configured for code quality
- OpenAPI integration automatically generates TypeScript API clients

### Full-Stack Integration
- Frontend communicates with backend through auto-generated API clients
- CORS configuration may need adjustment for production deployment
- Frontend proxy configuration handles API routing during development

## Important Implementation Details

### MyBatis-Flex Database Layer
- **ORM Framework**: MyBatis-Flex 1.11.0 provides advanced ORM capabilities with annotation-based mapping
- **Entity Annotations**: Use `@Table`, `@Id`, `@Column` annotations for database mapping
- **ID Generation**: Snowflake algorithm for distributed unique IDs (`@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)`)
- **Logical Delete**: Built-in soft delete support using `@Column(value = "isDelete", isLogicDelete = true)`
- **Mapper Scanning**: `@MapperScan("com.spring.aicodemother.mapper")` in main application class enables automatic mapper discovery

### Code Generation Workflow
- **Generator Location**: `MyBatisCodeGenerator.java` in `generator/` package
- **Database Connection**: Reads from `application.yml` datasource configuration
- **Generated Components**: Entities, Mappers, Services, and Controllers with complete CRUD operations
- **Target Package**: Currently configured to generate to `com.yupi.yuaicodemother.genresult` (should be updated for this project)
- **Run Generator**: Execute `MyBatisCodeGenerator.main()` to generate code for specified tables

### User Management System
- **Password Security**: MD5 encryption with salt "Join2049" in `UserServiceImpl.getEncryptPassword()`
- **User Registration**: Complete validation including account length, password strength, and duplicate checking
- **User Roles**: Enumerated roles (USER/ADMIN) defined in `UserRoleEnum`
- **API Endpoints**: Full REST API with registration (`/user/register`), CRUD operations, and pagination

### Backend (Lombok Usage)
- `BaseResponse<T>` uses `@Data` annotation to automatically generate getters/setters
- This is crucial for JSON serialization - without it, endpoints return HTTP 406 errors
- All data classes should use appropriate Lombok annotations for consistency

### Frontend (API Integration)
- Run `npm run openapi2ts` after backend API changes to regenerate TypeScript clients
- API clients are generated in `src/api/` directory with proper TypeScript types
- Use generated clients instead of manual HTTP requests for type safety

### Database Configuration
- **Connection**: MySQL database `ai_code_mother` on localhost:3306
- **Credentials**: Username `root`, password configured in `application.yml`
- **Pool**: HikariCP for connection pooling and performance optimization
- **Schema**: User table with logical delete support and complete user management fields
- **Test Data**: Pre-populated with admin and user accounts (check `sql/create_table.sql`)

### Authentication & Authorization System
- **Password Security**: MD5 encryption with salt "Join2049" in `UserServiceImpl.getEncryptPassword()`
- **Session Management**: Spring session-based authentication
- **Role-based Access**: `@AuthCheck` annotation with AOP interceptor for method-level security
- **User Roles**: Enumerated roles (USER/ADMIN) with different access levels
- **Frontend Integration**: Automatic login redirect on 40100 error code

### Code Generation & Development Tools
- **MyBatis-Flex Generator**: Located in `MyBatisCodeGenerator.java` - run via Maven exec plugin
- **Target Package**: Currently configured to generate to `com.yupi.yuaicodemother.genresult` (update if needed)
- **Generated Components**: Entities, Mappers, Services, and Controllers with complete CRUD operations
- **Database Connection**: Reads configuration from `application.yml` datasource settings

### Key Development Patterns
- **API Response Pattern**: All endpoints use `BaseResponse<T>` wrapper with standardized error codes
- **Exception Handling**: Global exception handler converts all exceptions to consistent API responses
- **Validation**: Request DTOs with validation annotations for parameter checking
- **Pagination**: Built-in pagination support using `PageRequest` and MyBatis-Flex page queries
- **Soft Delete**: Logical delete pattern using `isDelete` flag with MyBatis-Flex support

### Common Issues & Solutions
- **HTTP 406 "Not Acceptable"**: Usually caused by missing getters in response objects. Ensure `@Data` or equivalent annotations are used
- **JSON Serialization**: All response DTOs must have accessible fields for Jackson serialization
- **CORS Issues**: Configure CORS in Spring Boot if frontend and backend run on different ports
- **API Client Sync**: Always regenerate frontend API clients after backend OpenAPI spec changes