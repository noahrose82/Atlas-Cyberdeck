# ADR-003: Command Completion Implemented as a Dedicated Engine

**Status:** Accepted

**Date:** August 2026

## Context

Atlas Cyberdeck introduced command completion to improve usability and provide a more realistic shell experience.

Command completion is fundamentally different from command execution.

Completion occurs while the user is typing, before any command is processed.

## Decision

Command completion is implemented as a dedicated engine (`CommandCompletion`) instead of being incorporated into `TerminalCommandProcessor`.

The terminal user interface invokes the completion engine in response to user interaction.

Current implementations include:

- Complete button
- Hardware keyboard Tab support

Future enhancements may include:

- Filename completion
- Directory completion
- Alias completion
- Environment variable completion
- Intelligent command suggestions

## Rationale

Separating command completion from command execution maintains a clean distinction between user interface behavior and command processing.

The completion engine remains reusable and independent of specific interface implementations.

## Consequences

Positive:

- Cleaner architecture
- Easier future enhancements
- Improved maintainability
- Reusable completion engine
- Support for multiple input methods

Negative:

- Additional coordination between the UI and completion engine

The separation improves long-term scalability and preserves the single responsibility of the command processor.