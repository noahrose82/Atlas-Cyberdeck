# Atlas Cyberdeck Architecture

## Overview

Atlas Cyberdeck follows a layered, modular architecture designed to keep the application maintainable, testable, and portable across future platforms.

The project emphasizes separation of concerns, single responsibility, and extensibility. As the application grows, new functionality should be added through dedicated components rather than increasing the complexity of existing classes.

---

# High-Level Architecture

```text
User Interface
│
├── Dashboard
├── Linux
├── Terminal
├── Files
└── SSH
│
▼
ViewModels
│
▼
Feature Engines
│
├── Terminal Engine
├── Virtual File System
├── Command History
├── Alias Resolution
├── Variable Expansion
├── Wildcard Expansion
├── Command Completion
├── File Operations
└── Directory Operations
│
▼
Domain Models
│
├── FileNode
├── TerminalUiState
└── LinuxUiState
│
▼
System Services
│
├── Persistence
├── SSH
├── Linux Runtime
└── Device Services
```

---

# Architectural Principles

## Separation of Concerns

Business logic is separated from the user interface whenever possible.

Composable screens are responsible for displaying information and forwarding user actions.

ViewModels manage UI state.

Feature engines perform application logic.

Domain models represent application data.

---

## Single Responsibility Principle

Every class should have one clearly defined purpose.

### Examples

- **TerminalScreen** — Renders the terminal interface.
- **TerminalViewModel** — Manages terminal UI state.
- **TerminalCommandProcessor** — Parses and executes commands.
- **CommandHistory** — Stores command history.
- **CommandCompletion** — Handles command completion.
- **WildcardExpander** — Expands wildcard expressions.
- **VariableExpander** — Expands shell variables.
- **FileOperations** — Manages file creation, reading, writing, and deletion.
- **DirectoryOperations** — Manages directory creation, deletion, and navigation.
- **VirtualFileSystem** — Coordinates filesystem state.

---

## Delegation

Large coordinator classes should delegate specialized work to focused components.

VirtualFileSystem coordinates filesystem state while delegating implementation to operation classes.

Example:

```text
VirtualFileSystem
        │
        ├── FileOperations
        ├── DirectoryOperations
        ├── SearchOperations
        ├── TreeOperations
        ├── CopyMoveOperations
        └── PersistenceManager
```

This approach keeps responsibilities isolated and simplifies future expansion.

---

## Unidirectional Data Flow

Atlas Cyberdeck follows a predictable state flow.

```text
User Action
        │
        ▼
ViewModel
        │
        ▼
Feature Engine
        │
        ▼
Updated State
        │
        ▼
Compose UI
```

This architecture keeps state predictable and reduces coupling between the interface and application logic.

---

## Extensibility

Terminal functionality is implemented using dedicated feature engines.

Current engines include:

- CommandHistory
- CommandAliases
- CommandCompletion
- VariableExpander
- WildcardExpander

Future engines may include:

- SearchOperations
- TreeOperations
- PersistenceManager
- ScriptEngine
- PluginManager

This allows new functionality to be introduced without increasing the complexity of existing components.

---

## Portability

The application is designed to minimize platform-specific dependencies.

Long-term goals include support for:

- Android phones
- Android tablets
- Chromebooks
- Cyberdeck hardware
- Desktop platforms
- Additional Kotlin-supported platforms

---

# Package Organization

```text
com.noahrose.pocketlab
│
├── feature
│   │
│   ├── filesystem
│   │   │
│   │   ├── FileNode
│   │   ├── VirtualFileSystem
│   │   │
│   │   ├── operations
│   │   │   ├── FileOperations
│   │   │   └── DirectoryOperations
│   │   │
│   │   └── persistence
│   │
│   ├── terminal
│   │   │
│   │   ├── TerminalCommandProcessor
│   │   ├── TerminalViewModel
│   │   │
│   │   ├── alias
│   │   ├── completion
│   │   ├── environment
│   │   ├── history
│   │   └── wildcard
│   │
│   └── linux
│
└── ui
    └── screens
```

---

# Design Goals

Atlas Cyberdeck is developed with the following priorities:

- Readability
- Maintainability
- Testability
- Extensibility
- Scalability
- Portability

New features should improve the architecture rather than simply increase the amount of code.

---

# Development Philosophy

Atlas Cyberdeck is developed using small, incremental engineering sprints.

Each sprint follows the same process:

1. Design
2. Implement
3. Build
4. Test
5. Commit

Large features are divided into smaller components whenever possible.

Architecture refactoring is treated as an important milestone rather than deferred until later development.

---

# Long-Term Vision

Atlas Cyberdeck is intended to evolve beyond a terminal emulator into a modular cybersecurity platform capable of supporting advanced Linux tooling, scripting, automation, remote administration, and future cyberdeck hardware.

Every architectural decision should support that long-term vision.

---

> "Maybe not breaking free from the Matrix—but we are writing our own code instead of living inside someone else's. That is a pretty good way to spend our time."
> 
> ---

**Document Version:** 1.0  
**Last Updated:** August 2026