# ADR-006 — Linux Runtime Architecture

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck requires a Linux execution environment that can run on supported Android devices without requiring Android root access.

The Linux subsystem must support:

- persistent installation state;
- native runtime launch;
- Ubuntu ARM64 RootFS management;
- runtime start and stop;
- persistent shell sessions;
- guest command execution;
- package management;
- network access;
- diagnostics;
- runtime safety;
- controlled recovery.

Directly coupling the Android UI, terminal, repository, and native PRoot process would make runtime state difficult to reason about and would increase the chance of unsafe bypasses.

The architecture therefore needs explicit boundaries between application state, runtime orchestration, backend implementation, native process state, and guest command execution.

---

## Decision

Atlas Cyberdeck will use a layered Linux runtime architecture.

```text
Android UI / Atlas Terminal
        │
        ▼
Linux Runtime Controller
        │
        ▼
Linux Runtime Backend
        │
        ▼
PRoot Runtime Backend
        │
        ▼
Native PRoot Process
        │
        ▼
Ubuntu ARM64 RootFS
```

Additional services operate alongside this stack:

```text
Linux Repository
Runtime Path Manager
Runtime Asset Validation
ABI Detection
Guest Command Executor
Linux Shell Mode
Runtime Diagnostics
Runtime Safety
```

The `LinuxRuntimeController` is the application-side authority for runtime orchestration.

The backend owns runtime-specific implementation details.

The native PRoot layer owns the actual Linux userspace process.

The guest executor owns command execution inside the running Ubuntu environment.

---

## Responsibility Boundaries

### Linux Repository

Owns installation/runtime data models and persistent installation state.

It does not prove that a native process is alive.

### Linux Runtime Controller

Owns:

- runtime start requests;
- runtime stop requests;
- runtime session coordination;
- safety-gate checks;
- stale-state reconciliation;
- backend interaction.

### Runtime Backend

Owns:

- runtime implementation contract;
- backend-specific start/stop behavior;
- session creation;
- process-liveness integration.

### PRoot Runtime Backend

Owns:

- PRoot-specific launch behavior;
- active process lifecycle;
- native runtime integration.

### Guest Command Executor

Owns:

- command execution in Ubuntu;
- streaming output;
- exit-code capture;
- package command handling;
- recovery restrictions;
- runtime-death escalation.

### Linux Shell Mode

Owns the persistent user-facing Ubuntu shell session state.

---

## Safety Gate

Normal runtime startup must pass through the Atlas safety system before backend startup.

Conceptually:

```text
Start Request
    ↓
Runtime Safety Gate
    ↓
Installation / Capability Checks
    ↓
Backend Start
    ↓
Native Process
```

If Atlas is in `SAFE_MODE`, normal runtime startup is denied.

A secondary safety gate may exist closer to process launch as defense in depth.

---

## Process State vs Repository State

A saved state such as:

```text
RUNNING
```

does not prove the PRoot process is alive.

Runtime state must be reconciled against actual backend/process state.

If the native process is gone:

- active session state must be cleared;
- repository/runtime state must be corrected;
- the runtime may escalate safety when process loss is unexpected.

---

## Alternatives Considered

### UI directly manages PRoot

Rejected because UI code should not own process lifecycle or safety policy.

### Repository directly manages PRoot

Rejected because persistence and native process orchestration are separate concerns.

### One Linux singleton owns everything

Rejected because it would combine installation, process control, command execution, persistence, safety, and diagnostics into a single high-coupling component.

---

## Consequences

### Positive

- Runtime responsibilities are explicit.
- Safety gates have clear insertion points.
- UI code remains state-driven.
- Backend implementation can evolve independently.
- Guest command execution is isolated from startup orchestration.
- Native process state can be reconciled separately from repository state.
- Unit testing is possible for policy layers that do not require Android or PRoot.

### Tradeoffs

- More components must coordinate correctly.
- Session ownership must remain disciplined.
- Cross-layer shortcuts must be resisted.
- Documentation must stay synchronized with runtime ownership.

---

## Validation

The current architecture has been validated through:

- successful native PRoot startup;
- Ubuntu ARM64 guest launch;
- persistent `linux shell`;
- runtime stop/start;
- package management;
- DNS synchronization;
- runtime diagnostics;
- Safe Mode startup blocking;
- controlled recovery;
- runtime regression tests.

---

## Related Components

- `LinuxRepository`
- `LinuxRuntimeController`
- `LinuxRuntimeBackend`
- `ProotLinuxRuntimeBackend`
- `LinuxRuntimeProcessLauncher`
- `LinuxShellMode`
- guest command executor
- runtime safety subsystem

---

## Related Decisions

- ADR-003 — Persistence Strategy
- ADR-004 — Testing Strategy
- ADR-007 — PRoot Runtime
- ADR-008 — Runtime Safety Model
- ADR-010 — Atlas Shell vs Ubuntu Separation
