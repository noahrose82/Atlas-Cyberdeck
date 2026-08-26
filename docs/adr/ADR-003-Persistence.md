# ADR-003 — Persistence Strategy

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck manages several categories of state with different durability requirements.

Examples include:

- Atlas virtual filesystem state;
- Linux installation state;
- Ubuntu RootFS data;
- package metadata;
- runtime safety state;
- shell/runtime session state;
- transient PRoot process state.

Treating all state as either permanent or disposable would create data-loss risk and stale-runtime behavior.

---

## Decision

Atlas will distinguish **persistent state** from **transient runtime state** and assign ownership explicitly.

### Persistent state

Examples:

```text
Atlas virtual filesystem
Ubuntu RootFS
user files
package database
package metadata
.l2s state
Linux installation state
runtime safety record
```

### Transient state

Examples:

```text
active native process state
ephemeral process markers
temporary launch state
disposable runtime scratch state
```

Persistence must not be used as proof that a native runtime process is alive.

Runtime state must be reconciled against actual backend/process state.

---

## Safety Requirement

Recoverable runtime cleanup must target **transient data only** unless the user explicitly requests a destructive operation.

Atlas must preserve:

- Ubuntu RootFS;
- user data;
- package state;
- `.l2s` state;
- primary Atlas workspace.

---

## Alternatives Considered

### Treat the Linux environment as disposable

Rejected because Atlas is designed as a persistent mobile Linux workspace.

### Persist every runtime signal

Rejected because process-liveness information becomes stale and can cause the application to report a runtime as active after the native process has died.

---

## Consequences

### Positive

- User data survives recoverable failures.
- Safety recovery can stop/restart Linux without reinstalling Ubuntu.
- Package state remains available for repair.
- Runtime process death can be reconciled separately from installation state.

### Tradeoffs

- Persistence ownership must be documented carefully.
- Corrupt persisted safety state requires fail-closed handling.
- Cleanup code must explicitly distinguish transient from persistent paths.

---

## Validation

Current behavior validates the decision through:

- persistent Ubuntu installation;
- persistent Atlas virtual filesystem;
- persistent safety state;
- preservation of RootFS during Safe Mode;
- runtime-state reconciliation against actual PRoot process state.

---

## Related Components

- `LinuxRepository`
- `LinuxRuntimePathManager`
- `LinuxRuntimeCircuitBreaker`
- `VirtualFileSystem`
- PRoot runtime storage
- Ubuntu RootFS

---

## Related Decisions

- ADR-001 — Filesystem Architecture
- ADR-008 — Runtime Safety Model
- ADR-009 — Controlled Recovery
