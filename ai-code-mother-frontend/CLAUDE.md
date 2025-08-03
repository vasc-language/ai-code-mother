# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Vue 3 frontend application built with Vite, TypeScript, and Ant Design Vue. It serves as the frontend for the "ai-code-mother" project, providing a web interface for AI application generation.

- **Framework**: Vue 3.5.17 with Composition API and `<script setup>` syntax
- **UI Library**: Ant Design Vue 4.2.6 with complete component system
- **Build Tool**: Vite 7.0.0 with Vue DevTools integration
- **State Management**: Pinia 3.0.3 for reactive state management
- **Routing**: Vue Router 4.5.1 with history mode
- **HTTP Client**: Axios 1.11.0 with request/response interceptors
- **Language**: TypeScript with strict type checking
- **Code Generation**: OpenAPI-to-TypeScript integration for backend API types

## Development Commands

### Core Development
```bash
# Install dependencies
npm install

# Start development server with hot reload (port typically 5173)
npm run dev

# Build for production (includes type checking)
npm run build

# Build only (without type checking)
npm run build-only

# Preview production build locally
npm run preview
```

### Code Quality
```bash
# Type checking with Vue TSC
npm run type-check

# Lint and auto-fix with ESLint
npm run lint

# Format code with Prettier
npm run format
```

### API Integration
```bash
# Generate TypeScript types from backend OpenAPI spec (recommended)
npm run openapi2ts

# Alternative command
npx @umijs/openapi
```

## Architecture Overview

### Project Structure
```
src/
├── main.ts              # App entry point with Ant Design Vue integration
├── App.vue              # Root component with BasicLayout
├── request.ts           # Axios instance with interceptors and auth handling
├── layouts/             # Layout components
│   └── BasicLayout.vue  # Main layout with header/footer/content
├── components/          # Reusable UI components
│   ├── GlobalHeader.vue # Navigation header with menu and user auth
│   └── GlobalFooter.vue # Site footer
├── page/                # Page-level components (note: uses 'page' not 'views')
│   └── HomeView.vue     # Home page component
├── router/              # Vue Router configuration
├── stores/              # Pinia state stores
├── assets/              # Static assets (logo, styles)
└── api/                 # Generated API client from OpenAPI (when present)
```

### Key Architecture Patterns

**Application Bootstrap** (`src/main.ts:10-16`):
- Integrates Vue 3 + Pinia + Vue Router + Ant Design Vue
- Imports Ant Design reset CSS for consistent styling
- Full Ant Design component library available globally

**HTTP Client Setup** (`src/request.ts`):
- Axios instance configured for backend at `http://localhost:8123/api`
- Request/response interceptors for authentication and error handling
- Automatic redirect to login page for 401 unauthorized responses
- 60-second timeout with credentials support

**Layout System** (`src/layouts/BasicLayout.vue`):
- Three-tier layout: GlobalHeader + main content + GlobalFooter
- Responsive design with max-width container (1200px)
- Router view integration for page content

**Navigation** (`src/components/GlobalHeader.vue:45-62`):
- Ant Design Menu component with horizontal layout
- Route-based menu selection with Vue Router integration
- External link support (编程导航)
- User authentication area (login button placeholder)

**Routing** (`src/router/index.ts`):
- Simple router setup with home route only
- Uses environment-based BASE_URL configuration
- Designed for expansion with additional routes

### Backend Integration

**API Configuration** (`openapi2ts.config.ts`):
- Connects to Spring Boot backend at `http://localhost:8123/api/v3/api-docs`
- Generates TypeScript types and request functions from OpenAPI spec
- Imports custom request instance from `@/request`

**Authentication Flow** (`src/request.ts:28-36`):
- Intercepts 40100 error code (NOT_LOGIN_ERROR from backend)
- Automatically redirects to `/user/login` with return URL
- Handles login state across the application

## Development Notes

### UI Framework Integration
- Ant Design Vue provides complete component library
- Components use `a-` prefix (e.g., `a-layout`, `a-menu`, `a-button`)
- Reset CSS applied globally for consistent styling
- Message component available for notifications

### TypeScript Patterns
- Uses `<script setup>` syntax throughout for better TypeScript integration
- Strict type checking enabled across the project
- API types generated automatically from backend OpenAPI specification

### State Management
- Pinia stores use Composition API pattern
- Counter store example provided as template
- Ready for expansion with user auth and application state

### Build Process
- Development server runs with Vite hot reload
- Production builds include automatic type checking
- Vue DevTools enabled for development debugging

## Important Implementation Details

### Backend Communication
- Backend expects to run on port 8123 with `/api` context path
- Frontend development server typically runs on port 5173
- CORS and authentication handled through Axios interceptors

### Authentication System
- Login redirects preserve return URLs for better UX  
- Error code 40100 triggers automatic login flow
- Credentials included in requests for session management

### Code Generation Workflow
- OpenAPI types generated from `http://localhost:8123/api/v3/api-docs`
- Generated files placed in `./src` directory  
- Custom request instance used for all API calls

## Common Issues and Solutions

### OpenAPI Code Generation Failures
**Problem**: `npm run openapi2ts` fails with `ECONNREFUSED` or proxy errors
**Solution**: Clear proxy environment variables before running:
```bash
set HTTP_PROXY= && set HTTPS_PROXY= && npm run openapi2ts
```

**Problem**: Missing API files causing import errors like `Cannot find module '@/api/healthController.ts'`
**Solution**: 
1. Ensure backend server is running on port 8123
2. Generate API client: `npm run openapi2ts`
3. Verify files are created in `src/api/` directory

### Path Resolution Issues
**Problem**: Import errors for components like `Cannot find module '@/pages/HomeView.vue'`
**Solution**: Check actual directory structure - this project uses `src/page/` (singular) not `src/pages/` (plural)