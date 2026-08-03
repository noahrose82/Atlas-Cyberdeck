# ADR-001: Terminal Features Implemented as Independent Engines

**Status:** Accepted

**Date:** August 2026

## Context

As Atlas Cyberdeck continued to grow, the terminal began supporting increasingly advanced functionality including command history, aliases, wildcard expansion, variable expansion, command completion, pipes, and file operations.

Implementing all functionality directly inside `TerminalCommandProcessor` would have resulted in a large, difficult-to-maintain class with multiple responsibilities.

## Decision

Terminal functionality will be implemented as independent feature engines.

Examples include:

- CommandHistory
- CommandAliases
- VariableExpander
- WildcardExpander
- CommandCompletion

Each engine is responsible for a single aspect of terminal behavior.

`TerminalCommandProcessor` coordinates command execution but delegates specialized work to these engines.

## Rationale

This approach follows the Single Responsibility Principle.

Benefits include:

- Improved readability
- Easier testing
- Better maintainability
- Simplified debugging
- Easier future expansion

Future terminal capabilities can be added without significantly increasing the complexity of the command processor.

## Consequences

Positive:

- Modular architecture
- Clear separation of responsibilities
- Reduced coupling
- Easier onboarding for contributors

Negative:

- Slight increase in the number of classes
- Additional coordination between components

The benefits significantly outweigh the additional organizational overhead.