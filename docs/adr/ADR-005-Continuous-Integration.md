# ADR-005 — Continuous Integration

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck is developed incrementally through small engineering phases.

Each locked phase should leave the repository in a buildable and testable condition.

Local development alone is insufficient because a clean local environment may hide:

- uncommitted dependencies;
- machine-specific state;
- missing files;
- build regressions;
- unit-test failures.

However, CI cannot fully validate native PRoot behavior or physical Android device behavior.

---

## Decision

Atlas will use automated continuous integration as a **repository validation layer**, while retaining physical-device validation for runtime-critical work.

The engineering flow is:

```text
Design
  ↓
Implement
  ↓
Build
  ↓
Test
  ↓
Device Validation
  ↓
Lock
  ↓
Commit / Push
  ↓
Continuous Integration
```

CI is expected to validate deterministic repository behavior such as:

- project configuration;
- compilation;
- unit tests;
- regression tests;
- reproducible build steps.

---

## Current CI Platform

Atlas currently uses:

```text
GitHub Actions
```

Source is maintained with Git and mirrored across:

```text
GitHub
GitLab
```

---

## CI Is Not Device Proof

A green CI run does not prove:

- PRoot starts correctly on a physical device;
- Android DNS behavior is correct;
- package installation succeeds under real filesystem semantics;
- Ubuntu process lifecycle is correct;
- runtime safety behaves correctly across real application/process transitions.

Those behaviors require device validation.

---

## Alternatives Considered

### No CI

Rejected because repository regressions should be caught independently of the development workstation.

### CI as the sole release gate

Rejected because native Android runtime behavior cannot be completely validated in the current CI environment.

---

## Consequences

### Positive

- Broken builds are detected early.
- Unit regressions are caught after pushes.
- Repository health does not depend on one workstation.
- Incremental phase locking becomes safer.

### Tradeoffs

- Device validation remains part of the release process.
- CI configuration must evolve as the project adds new modules and platforms.
- Native runtime checks may require future specialized CI infrastructure.

---

## Validation Standard

Before a runtime-critical phase is considered locked:

```text
Unit tests          → GREEN
Debug build         → GREEN
Physical device     → validated
Phase status        → LOCKED
Repository push     → complete
CI                  → expected to remain GREEN
```

---

## Related Decisions

- ADR-004 — Testing Strategy
- ADR-006 — Linux Runtime Architecture
- ADR-007 — PRoot Runtime
