# ADR-001 — Filesystem Architecture

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck requires a filesystem that remains available independently of the Ubuntu guest runtime.

The Atlas shell must continue to support file and directory workflows when:

- Ubuntu is not installed;
- the Linux runtime is stopped;
- the device does not support the Linux runtime;
- Atlas is in Safe Mode;
- the Ubuntu RootFS is unavailable.

Using the Ubuntu RootFS as the only filesystem would tightly couple the Atlas shell to the Linux runtime and make application-level commands dependent on PRoot process state.

---

## Decision

Atlas Cyberdeck will maintain a **persistent Atlas virtual filesystem** that is architecturally separate from the Ubuntu RootFS.

The Atlas virtual filesystem owns:

- working-directory state;
- relative-path behavior;
- file creation;
- file reading and writing;
- file deletion;
- file copying;
- file moving and renaming;
- directory creation and deletion;
- filesystem search;
- tree visualization;
- persistent Atlas shell storage.

The Ubuntu RootFS remains a separate Linux filesystem managed by the Linux runtime subsystem.

---

## Alternatives Considered

### Use the Ubuntu RootFS for all shell operations

Rejected because it would make the Atlas shell unavailable whenever the Linux runtime was absent, stopped, blocked, or unhealthy.

### Map the Atlas shell directly to Android storage

Rejected because it would expose host-storage semantics directly to shell behavior and make portability, sandboxing, testing, and future platform support more difficult.

---

## Consequences

### Positive

- Atlas shell remains functional without Ubuntu.
- Filesystem behavior can be tested independently of PRoot.
- Atlas application state is not tied to Linux process state.
- Future desktop or alternate-platform implementations can preserve the same Atlas filesystem model.
- Runtime safety can block Linux without disabling the Atlas shell.

### Tradeoffs

- Atlas and Ubuntu maintain separate filesystems.
- Explicit import/export or bridging features are required if users need to move data between environments.
- Documentation must clearly explain which filesystem a command is operating on.

---

## Validation

The architecture is validated by current Atlas behavior:

- Atlas filesystem commands operate independently of Ubuntu.
- Atlas working-directory state persists.
- Ubuntu may be stopped while Atlas filesystem operations remain available.
- Runtime Safe Mode does not require destruction of Atlas filesystem state.

---

## Related Components

- `VirtualFileSystem`
- Atlas command handlers
- `TerminalCommandProcessor`
- Files UI
- Linux runtime subsystem

---

## Related Decisions

- ADR-002 — Terminal Architecture
- ADR-003 — Persistence
- ADR-010 — Atlas Shell vs Ubuntu Separation
