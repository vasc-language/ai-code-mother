# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.4 backend project called "ai-code-mother" using Java 21. It's a RESTful API application with the following key characteristics:

- **Framework**: Spring Boot 3.5.4 with Spring Web
- **API Documentation**: Integrated with Knife4j (Swagger UI) accessible at `/doc.html`
- **Base URL**: Application runs on port 8123 with context path `/api`
- **Database**: MySQL connector included (database configuration not yet set up)
- **Utilities**: Hutool library for common Java utilities
- **Build Tool**: Maven with wrapper scripts (`mvnw` and `mvnw.cmd`)

## Development Commands

### Building and Running
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Package as JAR
./mvnw clean package
```

**Note for Windows**: Use `mvnw.cmd` instead of `./mvnw` on Windows systems, or use `mvn` if Maven is installed globally.

### Testing
- Single test: `./mvnw test -Dtest=ClassName#methodName`
- Test class: `./mvnw test -Dtest=ClassName`

### API Testing
- Health check: `curl -X GET "http://localhost:8123/api/health/" -H "accept: application/json"`
- Swagger UI: `http://localhost:8123/api/doc.html`

## Architecture Overview

### Package Structure
- `com.spring.aicodemother` - Root package
  - `common/` - Shared utilities and response wrappers
    - `BaseResponse<T>` - Standardized API response wrapper
    - `ResultUtils` - Utility for creating consistent responses
    - `PageRequest`, `DeleteRequest` - Common request DTOs
  - `controller/` - REST API endpoints
    - `HealthController` - Basic health check endpoint
  - `exception/` - Centralized error handling
    - `ErrorCode` - Enum with standardized error codes
    - `BusinessException` - Custom business exception
    - `GlobalExceptionHandler` - Global exception handling
    - `ThrowUtils` - Utility for throwing exceptions
  - `config/` - Configuration classes (currently empty)

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
This appears to be a project template or starter with basic infrastructure set up but minimal business logic implemented. The project includes:
- Health check endpoint at `/api/health`
- Complete error handling framework
- API documentation setup with Knife4j
- Lombok for reducing boilerplate code

## Development Notes
- The application uses Chinese language configuration for Knife4j documentation
- Lombok annotations are configured for compile-time processing
- Global exception handling is already implemented for consistent error responses
- The project structure follows standard Spring Boot conventions

## Important Implementation Details

### Lombok Usage
- `BaseResponse<T>` uses `@Data` annotation to automatically generate getters/setters
- This is crucial for JSON serialization - without it, endpoints return HTTP 406 errors
- All data classes should use appropriate Lombok annotations for consistency

### Common Issues & Solutions
- **HTTP 406 "Not Acceptable"**: Usually caused by missing getters in response objects. Ensure `@Data` or equivalent annotations are used
- **JSON Serialization**: All response DTOs must have accessible fields for Jackson serialization