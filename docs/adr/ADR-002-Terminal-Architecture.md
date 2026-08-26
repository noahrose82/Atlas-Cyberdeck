# ADR-002 — Terminal Architecture

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Early terminal implementations tend to accumulate parsing, routing, command behavior, history, scripting, completion, and filesystem logic in one processor.

Atlas Cyberdeck requires a terminal architecture that can support:

- Atlas shell commands;
- command history;
- aliases;
- variable expansion;
- wildcard expansion;
- pipelines;
- command completion;
- scripts;
- plugins;
- Linux runtime commands;
- Ubuntu shell routing;
- future SSH and developer workflows.

A monolithic terminal processor would become difficult to test, extend, and maintain.

---

## Decision

Atlas will use a **modular command-processing architecture**.

Primary responsibilities are separated into:

```text
User Input
    ↓
History / Expansion
    ↓
Alias Resolution
    ↓
Variable Expansion
    ↓
Wildcard Expansion
    ↓
Pipe Engine
    ↓
Command Dispatcher
    ↓
Handler Registry
    ↓
Command Handlers
```

Supporting services include:

- `CommandRegistry`
- `CommandDispatcher`
- `HandlerRegistry`
- `CommandHistory`
- alias resolution
- variable expansion
- wildcard expansion
- completion
- pipe engine
- script engine
- plugin registry

When Ubuntu shell mode is active, guest command routing occurs before ordinary Atlas command parsing.

---

## Alternatives Considered

### Single command processor

Rejected because it centralizes too many responsibilities and makes regression risk increase as commands are added.

### One class per command without registries

Rejected because help, completion, discovery, diagnostics, and metadata would become duplicated or difficult to keep synchronized.

---

## Consequences

### Positive

- Commands can be extended without rewriting the central processor.
- Help and completion can share registry metadata.
- Handler responsibilities remain narrow.
- Atlas shell and Ubuntu shell routing can remain distinct.
- Contract tests can protect critical command surfaces.

### Tradeoffs

- More classes and registries are required.
- Command ownership must remain disciplined.
- Registry metadata must stay synchronized with implementations.

---

## Validation

Current validation includes:

- command dispatcher tests;
- registry tests;
- script execution validation;
- Linux command contract tests;
- physical-device validation of Atlas/Ubuntu shell transitions.

---

## Related Components

- `TerminalCommandProcessor`
- `CommandRegistry`
- `CommandDispatcher`
- `HandlerRegistry`
- `LinuxShellMode`
- `ScriptEngine`
- `PluginRegistry`

---

## Related Decisions

- ADR-001 — Filesystem Architecture
- ADR-004 — Testing Strategy
- ADR-010 — Atlas Shell vs Ubuntu Separation
