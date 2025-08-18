# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack AI-powered code generation web application with a Spring Boot 3.5.4 backend and Vue 3 frontend. The project is structured as a monorepo containing both components and provides AI-driven code generation capabilities using LangChain4j:

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.4 with Spring Web and Java 21
- **AI Integration**: LangChain4j 1.1.0 with OpenAI integration and streaming support via Reactor
- **Code Generation**: AI-powered HTML and multi-file code generation with custom prompts
- **Database Layer**: MyBatis-Flex 1.11.0 for ORM with code generation support
- **Database**: MySQL 连接到 `ai_code_mother` 数据库
- **Connection Pool**: HikariCP for database connection management
- **API Documentation**: Integrated with Knife4j (Swagger UI) accessible at `/doc.html`
- **Base URL**: Application runs on port 8123 with context path `/api`
- **Streaming**: Server-Sent Events (SSE) support for real-time AI code generation
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
- AI Code Generation (non-streaming): `curl -X POST "http://localhost:8123/api/app/generate/{id}" -H "accept: application/json"`
- AI Code Generation (streaming): Connect to `http://localhost:8123/api/app/generate/sse/{id}` for Server-Sent Events
- App Management: Full CRUD operations available at `http://localhost:8123/api/app/`

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
  - `ai/` - AI code generation services and models
    - `AiCodeGeneratorService` - LangChain4j service interface for AI code generation
    - `AiCodeGeneratorServiceFactory` - Factory for creating AI service instances
    - `model/` - AI-specific models
      - `HtmlCodeResult` - Result model for HTML code generation
      - `MultiFileCodeResult` - Result model for multi-file code generation
  - `core/` - Core business logic for code generation
    - `AiCodeGeneratorFacade` - Main facade for AI code generation workflow
    - `CodeParser` - Interface for parsing generated code
    - `CodeFileSaver` - Interface for saving generated files
    - `parser/` - Code parsing implementations
      - `CodeParserExecutor` - Executor for code parsing
      - `HtmlCodeParser` - Parser for HTML code
      - `MultiFileCodeParser` - Parser for multi-file code
    - `saver/` - File saving implementations
      - `CodeFileSaverExecutor` - Executor for file saving
      - `CodeFileSaverTemplate` - Template for file saving
      - `HtmlCodeFileSaverTemplate` - Template for saving HTML files
      - `MultiFileCodeSaverTemplate` - Template for saving multi-file projects
  - `common/` - Shared utilities and response wrappers
    - `BaseResponse<T>` - Standardized API response wrapper with Lombok @Data
    - `ResultUtils` - Utility for creating consistent responses
    - `PageRequest`, `DeleteRequest` - Common request DTOs
  - `controller/` - REST API endpoints
    - `HealthController` - Basic health check endpoint
    - `UserController` - Complete user CRUD operations with registration, login, and admin features
    - `AppController` - App management and AI code generation endpoints with SSE support
  - `service/` - Business logic layer
    - `UserService` - User service interface
    - `AppService` - App service interface for application management
    - `impl/UserServiceImpl` - User service implementation with MD5 password encryption and session management
    - `impl/AppServiceImpl` - App service implementation for CRUD operations
  - `mapper/` - MyBatis-Flex data access layer
    - `UserMapper` - User database operations interface (auto-generated)
    - `AppMapper` - App database operations interface (auto-generated)
  - `model/` - Data models and DTOs
    - `entity/` - Database entities
      - `User` - User entity with MyBatis-Flex annotations and Snowflake ID generation
      - `App` - App entity for storing application information and generation types
    - `dto/` - Request/response DTOs
      - `user/` - User-related DTOs (UserRegisterRequest, UserLoginRequest, UserQueryRequest, UserAddRequest, UserUpdateRequest)
      - `app/` - App-related DTOs (AppAddRequest, AppUpdateRequest, AppAdminUpdateRequest, AppQueryRequest)
    - `vo/` - View objects
      - `LoginUserVO` - Login user view object for session data
      - `UserVO` - User view object
      - `AppVO` - App view object
    - `enums/` - Enumerations
      - `UserRoleEnum` - User role enumeration (USER/ADMIN)
      - `CodeGenTypeEnum` - Code generation type enumeration (HTML/MULTI_FILE)
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
    - `AppConstant` - App-related constants
  - `config/` - Configuration classes
    - `CorsConfig` - CORS configuration for cross-origin requests

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
This is a full-stack AI-powered code generation application with both backend and frontend infrastructure set up, including advanced AI code generation capabilities and user/app management functionality. The project includes:

**Backend:**
- **AI Code Generation System**: Complete LangChain4j integration with OpenAI for generating HTML and multi-file code projects
- **Streaming Support**: Real-time code generation with Server-Sent Events (SSE) for live updates
- **Code Processing Pipeline**: Sophisticated parsing and file saving system with template-based approach
- **App Management**: Complete CRUD operations for applications with code generation type support
- Complete user management system with registration, CRUD operations, and pagination
- User and App entities with MyBatis-Flex annotations, Snowflake ID generation, and logical delete support
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

## AI Code Generation Features

### LangChain4j Integration
- **AI Service Interface**: `AiCodeGeneratorService` with structured output support
- **Code Generation Types**: 
  - `HTML` - Single HTML file generation with embedded CSS and JavaScript
  - `MULTI_FILE` - Multi-file project generation with proper directory structure
- **Streaming Support**: Real-time code generation using Reactor Flux for progressive updates
- **Custom Prompts**: System prompts stored in `src/main/resources/prompt/` directory
  - `codegen-html-system-prompt.txt` - Prompt for HTML generation
  - `codegen-multi-file-system-prompt.txt` - Prompt for multi-file generation

### Code Generation Architecture
- **Facade Pattern**: `AiCodeGeneratorFacade` provides unified entry point for all generation operations
- **Strategy Pattern**: Different parsers and savers for different code generation types
- **Template Pattern**: `CodeFileSaverTemplate` provides base structure for file saving operations
- **Parser Pipeline**: 
  - `CodeParserExecutor` manages parsing workflow
  - `HtmlCodeParser` and `MultiFileCodeParser` handle type-specific parsing
- **Saver Pipeline**:
  - `CodeFileSaverExecutor` manages file saving workflow
  - Template-based savers handle different output formats

### API Endpoints
- **Non-streaming**: `/api/app/generate/{id}` - Generate code and return directory path
- **Streaming**: `/api/app/generate/sse/{id}` - Real-time code generation with SSE
- **App Management**: Full CRUD operations for managing generation apps

### File Output Structure
- Generated code saved to `tmp/` directory with timestamped subdirectories
- Automatic directory creation and file organization
- Support for complex project structures with multiple files and directories

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

### App Management System
- **App Entity**: Complete application model with code generation type support
- **Code Generation Types**: Enumerated types (HTML/MULTI_FILE) in `CodeGenTypeEnum`
- **CRUD Operations**: Full create, read, update, delete operations for apps
- **User Association**: Apps are associated with users for ownership and access control
- **Generation Integration**: Apps serve as templates/configurations for AI code generation
- **API Endpoints**: Full REST API at `/api/app/` with pagination and admin features

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
- **Schema**: 
  - User table with logical delete support and complete user management fields
  - App table for storing application information and generation configurations
  - Both tables use Snowflake ID generation for distributed uniqueness
- **Test Data**: Pre-populated with admin and user accounts (check `sql/create_table.sql`)

### LangChain4j Dependencies
- **Core Library**: `dev.langchain4j:langchain4j:1.1.0` - Main LangChain4j functionality
- **OpenAI Integration**: `dev.langchain4j:langchain4j-open-ai-spring-boot-starter:1.1.0-beta7` - OpenAI API integration with Spring Boot auto-configuration
- **Streaming Support**: `dev.langchain4j:langchain4j-reactor:1.1.0-beta7` - Reactive streams for real-time AI interactions
- **Structured Output**: Built-in support for structured AI responses using `@Description` annotations
- **Custom Prompts**: System prompts loaded from classpath resources

### Authentication & Authorization System
- **Password Security**: MD5 encryption with salt "Join2049" in `UserServiceImpl.getEncryptPassword()`
- **Session Management**: Spring session-based authentication
- **Role-based Access**: `@AuthCheck` annotation with AOP interceptor for method-level security
- **User Roles**: Enumerated roles (USER/ADMIN) with different access levels
- **Frontend Integration**: Automatic login redirect on 40100 error code

### Code Generation & Development Tools
- **AI Code Generator**: LangChain4j-powered service for generating HTML and multi-file projects
- **MyBatis-Flex Generator**: Located in `MyBatisCodeGenerator.java` - run via Maven exec plugin
- **Target Package**: Currently configured to generate to `com.yupi.yuaicodemother.genresult` (update if needed)
- **Generated Components**: Entities, Mappers, Services, and Controllers with complete CRUD operations
- **Database Connection**: Reads configuration from `application.yml` datasource settings
- **Custom Prompts**: AI generation prompts can be customized in `src/main/resources/prompt/`
- **File Output**: Generated files automatically saved to `tmp/` directory with organized structure

### Key Development Patterns
- **API Response Pattern**: All endpoints use `BaseResponse<T>` wrapper with standardized error codes
- **Exception Handling**: Global exception handler converts all exceptions to consistent API responses
- **Validation**: Request DTOs with validation annotations for parameter checking
- **Pagination**: Built-in pagination support using `PageRequest` and MyBatis-Flex page queries
- **Soft Delete**: Logical delete pattern using `isDelete` flag with MyBatis-Flex support
- **Facade Pattern**: `AiCodeGeneratorFacade` provides unified interface for complex AI operations
- **Strategy Pattern**: Pluggable parsers and savers for different code generation types
- **Template Pattern**: Base templates for consistent file saving operations
- **Streaming Pattern**: Server-Sent Events for real-time AI generation updates

### Common Issues & Solutions
- **HTTP 406 "Not Acceptable"**: Usually caused by missing getters in response objects. Ensure `@Data` or equivalent annotations are used
- **JSON Serialization**: All response DTOs must have accessible fields for Jackson serialization
- **CORS Issues**: Configure CORS in Spring Boot if frontend and backend run on different ports
- **API Client Sync**: Always regenerate frontend API clients after backend OpenAPI spec changes
- **AI Generation Timeout**: Long-running AI operations may timeout; use streaming endpoints for better user experience
- **File System Permissions**: Ensure `tmp/` directory has proper write permissions for generated files
- **LangChain4j Configuration**: Verify OpenAI API key is properly configured in `application.yml`
- **Structured Output Issues**: Ensure AI model classes use proper `@Description` annotations for reliable parsing

### Performance Considerations
- **Streaming vs Non-Streaming**: Use SSE endpoints for real-time feedback, non-streaming for simple operations
- **File Management**: Implement cleanup mechanism for generated files in `tmp/` directory
- **AI Rate Limiting**: Consider implementing rate limiting for AI generation endpoints
- **Database Indexing**: Ensure proper indexes on frequently queried fields (userId, createTime, etc.)