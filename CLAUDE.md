# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack AI-powered code generation web application with a Spring Boot 3.5.4 backend and Vue 3 frontend. The project provides AI-driven code generation capabilities using LangChain4j with DeepSeek integration for generating HTML, multi-file projects, and Vue applications.

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.4 with Spring Web and Java 21
- **AI Integration**: LangChain4j 1.1.0 with DeepSeek integration and streaming support via Reactor
- **Code Generation**: AI-powered HTML, multi-file, and Vue project generation with custom prompts
- **Database Layer**: MyBatis-Flex 1.11.0 for ORM with code generation support
- **Database**: MySQL connected to `ai_code_mother` database
- **Connection Pool**: HikariCP for database connection management
- **API Documentation**: Integrated with Knife4j (Swagger UI) accessible at `/doc.html`
- **Base URL**: Application runs on port 8123 with context path `/api`
- **Streaming**: Server-Sent Events (SSE) support for real-time AI code generation
- **Utilities**: Hutool library for common Java utilities and Lombok for boilerplate reduction
- **Build Tool**: Maven with wrapper scripts (`mvnw` and `mvnw.cmd`)
- **Screenshot**: Selenium integration for webpage screenshots
- **Cloud Storage**: Tencent Cloud COS integration for file storage
- **Monitoring**: Prometheus metrics integration via Spring Boot Actuator
- **Local Cache**: Caffeine for high-performance caching
- **AI Tools**: LangGraph4j integration for advanced workflow management
- **Alternative Models**: Alibaba DashScope SDK for additional AI model support

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

# Run specific test class
mvnw.cmd test -Dtest=ClassName

# Run specific test method
mvnw.cmd test -Dtest=ClassName#methodName

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

# Fix TypeScript errors (skip type checking)
npm run build-only
```

### Full-Stack Development Workflow
1. **Prerequisites**: Ensure MySQL and Redis servers are running
2. Start the backend: `mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (Linux/Mac)
3. In another terminal, start the frontend: `cd ai-code-mother-frontend && npm run dev`
4. Backend runs on `http://localhost:8123/api`
5. Frontend runs on `http://localhost:5173` (or next available port)
6. After backend changes, regenerate frontend API client: `npm run openapi2ts`

### Prerequisites
- **MySQL**: Database `ai_code_mother` on localhost:3306
- **Redis**: Server running on localhost:6379 (required for session management and tests)
- **Java 21**: Required for Spring Boot 3.5.4
- **Node.js**: Required for frontend development
- **Chrome/Chromium**: Required for Selenium screenshot functionality

### Testing
- All tests: `mvnw.cmd test` (Windows) or `./mvnw test` (Linux/Mac)
- Single test: `mvnw.cmd test -Dtest=ClassName#methodName`
- Test class: `mvnw.cmd test -Dtest=ClassName`
- **Note**: Tests require Redis server running on localhost:6379

### API Testing and Documentation
- Health check: `curl -X GET "http://localhost:8123/api/health/" -H "accept: application/json"`
- Swagger UI: `http://localhost:8123/api/doc.html` (Chinese language)
- OpenAPI JSON: `http://localhost:8123/api/v3/api-docs`
- AI Code Generation (non-streaming): `curl -X POST "http://localhost:8123/api/app/generate/{id}" -H "accept: application/json"`
- AI Code Generation (streaming): Connect to `http://localhost:8123/api/app/generate/sse/{id}` for Server-Sent Events
- App Management: Full CRUD operations available at `http://localhost:8123/api/app/`
- Prometheus Metrics: `http://localhost:8123/api/actuator/prometheus`

## Architecture Overview

### Monorepo Structure
```
ai-code-mother/
├── src/                        # Spring Boot backend source
├── pom.xml                     # Backend dependencies and build config
├── ai-code-mother-frontend/    # Vue 3 frontend application
├── sql/                        # Database initialization scripts
├── tmp/                        # Generated code and screenshots
├── prometheus.yml              # Prometheus monitoring configuration
└── CLAUDE.md                   # This documentation file
```

### Backend Package Structure (Spring Boot)
- `com.spring.aicodemother` - Root package
  - `ai/` - AI code generation services and models with DeepSeek integration
  - `core/` - Core business logic for code generation with parsers and savers
  - `controller/` - REST API endpoints with SSE streaming support
  - `service/` - Business logic layer with user and app management
  - `mapper/` - MyBatis-Flex data access layer
  - `model/` - Data models, DTOs, and view objects
  - `exception/` - Centralized error handling with custom exceptions
  - `config/` - Configuration classes including CORS and COS client
  - `utils/` - Utility classes including web screenshot functionality
  - `manager/` - Cloud storage manager for Tencent COS

### Frontend Architecture (Vue 3)
- **Components**: Reusable Vue components with TypeScript
- **Pages**: Route-based page components for app management and chat
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

## Key Features

### AI Code Generation System
- **DeepSeek Integration**: Uses DeepSeek API for AI code generation
- **Multiple Generation Types**: HTML, multi-file projects, and Vue applications
- **Real-time Streaming**: Server-Sent Events for live code generation updates
- **Custom Prompts**: System prompts stored in `src/main/resources/prompt/`
- **File Processing**: Sophisticated parsing and file saving system

### Database Management
- **MyBatis-Flex ORM**: Advanced ORM with annotation-based mapping
- **Snowflake ID Generation**: Distributed unique ID generation
- **Logical Delete**: Soft delete support with `isDelete` flag
- **User Management**: Complete user system with role-based access
- **App Management**: Application CRUD operations with generation type support

### Cloud Integration
- **Tencent Cloud COS**: Object storage for generated files and screenshots
- **Selenium Screenshots**: Automated webpage screenshot functionality
- **Redis Session Management**: Distributed session storage

## Important Implementation Details

### AI Configuration
- **Primary Model**: DeepSeek Reasoner (`deepseek-reasoner`) for complex reasoning tasks
- **Routing Model**: Qwen Turbo (`qwen-turbo`) for simple classification tasks
- **DeepSeek API**: Base URL `https://api.deepseek.com` with max 32,768 tokens
- **DashScope API**: Alibaba Cloud alternative with base URL `https://dashscope.aliyuncs.com/compatible-mode/v1`
- **Structured Output**: JSON response format with strict schema validation

### Security Configuration
- **Password Encryption**: MD5 with salt "Join2049" for user passwords
- **Session Management**: 30-day session timeout with Redis storage
- **Role-based Access**: `@AuthCheck` annotation for method-level security

### File Generation
- **Output Directory**: Generated code saved to `tmp/code_output/` with timestamped subdirectories
- **Screenshot Storage**: Screenshots saved to `tmp/screenshots/` with compression
- **Deployment Support**: Generated projects can be deployed to cloud storage

### Development Notes
- **Lombok Usage**: Critical for JSON serialization - ensure `@Data` annotations are present
- **API Client Sync**: Always regenerate frontend API clients after backend changes
- **CORS Configuration**: Properly configured for frontend-backend communication
- **Test Requirements**: Tests require Redis server running on localhost:6379

## Common Issues & Solutions

- **HTTP 406 "Not Acceptable"**: Ensure response DTOs have proper Lombok annotations
- **JSON Serialization**: All response objects must have accessible fields
- **AI Generation Timeout**: Use streaming endpoints for long-running operations
- **File System Permissions**: Ensure `tmp/` directory has write permissions
- **OpenAPI Generation**: Clear proxy settings before running `npm run openapi2ts`

## Performance Considerations
- Use SSE endpoints for real-time feedback during AI generation
- Implement cleanup mechanism for generated files in `tmp/` directory
- Consider rate limiting for AI generation endpoints
- Ensure proper database indexing on frequently queried fields
- Caffeine local cache is enabled for improved performance
- Prometheus metrics available for monitoring application performance

## Monitoring and Observability
- **Prometheus Integration**: Metrics exposed at `/api/actuator/prometheus`
- **Health Checks**: Available at `/api/actuator/health` with detailed status
- **Grafana Support**: Custom dashboard configuration available in `ai_model_grafana_config.json`
- **Application Metrics**: Performance monitoring for AI model usage and database operations