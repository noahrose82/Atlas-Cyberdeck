# ADR-002: VirtualFileSystem Acts as an Orchestrator

**Status:** Accepted

**Date:** August 2026

## Context

Initially, `VirtualFileSystem` contained responsibility for nearly every filesystem operation including:

- File creation
- File deletion
- File reading
- File writing
- Directory management
- Search
- Tree generation
- Copy operations
- Move operations

As functionality increased, the class became larger and more difficult to extend.

## Decision

`VirtualFileSystem` will function primarily as a coordinator rather than implementing every filesystem operation directly.

Specialized responsibilities are delegated to dedicated operation classes.

Examples include:

- FileOperations
- DirectoryOperations
- SearchOperations
- TreeOperations
- CopyMoveOperations
- PersistenceManager

`VirtualFileSystem` remains responsible for:

- Root directory
- Current directory
- Current path
- Observable filesystem state
- Coordination of filesystem operations

## Rationale

Delegating responsibilities keeps the coordinator small while allowing each subsystem to evolve independently.

This architecture supports future features such as filesystem persistence, scripting, and advanced search without increasing the complexity of the coordinator.

## Consequences

Positive:

- Better separation of concerns
- Improved maintainability
- Easier testing
- Cleaner architecture
- Simplified future development

Negative:

- Additional files within the project
- Slightly more navigation during development

The improved organization provides long-term benefits that outweigh the additional project structure.