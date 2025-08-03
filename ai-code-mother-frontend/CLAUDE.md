# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Vue 3 frontend application built with Vite, TypeScript, and modern development tools. It serves as the frontend for the "ai-code-mother" project.

- **Framework**: Vue 3.5.17 with Composition API
- **Build Tool**: Vite 7.0.0
- **State Management**: Pinia 3.0.3
- **Routing**: Vue Router 4.5.1
- **Language**: TypeScript with strict type checking
- **Styling**: CSS with scoped styles and CSS variables

## Development Commands

### Core Development
```bash
# Install dependencies
npm install

# Start development server with hot reload
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
# Type checking
npm run type-check

# Lint and auto-fix
npm run lint

# Format code with Prettier
npm run format
```

## Architecture Overview

### Project Structure
```
src/
├── main.ts              # Application entry point
├── App.vue              # Root component with navigation
├── router/              # Vue Router configuration
├── stores/              # Pinia state management
├── views/               # Page-level components
├── components/          # Reusable components
└── assets/              # Static assets and global styles
```

### Key Architecture Patterns

**Application Bootstrap** (`src/main.ts:9-14`):
- Creates Vue app instance with Pinia and Router plugins
- Standard Vue 3 + Pinia + Vue Router setup

**Routing** (`src/router/index.ts`):
- Uses Vue Router with history mode
- Implements lazy loading for the About view
- Base URL configured via environment variables

**State Management** (`src/stores/counter.ts`):
- Pinia stores using Composition API syntax
- Example counter store with reactive state and computed properties

**Component Architecture**:
- Uses `<script setup>` syntax for composition API
- Scoped styles with CSS variables for theming
- TypeScript throughout for type safety

### Configuration Details

**Vite Configuration** (`vite.config.ts`):
- Path alias `@` points to `src` directory
- Vue plugin with devtools enabled
- Standard Vite development setup

**TypeScript Configuration**:
- Multi-project setup with separate configs for app and Node.js tooling
- Vue TSC for type checking `.vue` files
- Strict type checking enabled

**ESLint Configuration** (`eslint.config.ts`):
- Vue + TypeScript preset with essential rules
- Prettier integration for formatting
- Configured for `.vue`, `.ts`, `.tsx` files

## Development Notes

- Uses Vue 3 Composition API exclusively (no Options API patterns found)
- All components use `<script setup>` syntax for better TypeScript integration
- Pinia stores follow the recommended composition-style pattern
- CSS variables are used for consistent theming across components
- Route-level code splitting is implemented for performance optimization

## Important Implementation Details

### TypeScript Integration
- Vue TSC handles type checking for `.vue` files
- Strict TypeScript configuration for better type safety
- Proper typing throughout the codebase

### Build Process
- Development: `npm run dev` starts Vite dev server
- Production: `npm run build` runs type checking then builds
- Type checking can be run independently with `npm run type-check`