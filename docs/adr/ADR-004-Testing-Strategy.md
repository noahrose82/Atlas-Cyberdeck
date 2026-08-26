# ADR-004 — Testing Strategy

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck contains both ordinary Kotlin logic and behavior that depends on:

- Android framework initialization;
- native ARM64 binaries;
- PRoot;
- Ubuntu RootFS;
- Android filesystem semantics;
- network state;
- physical-device process behavior.

Attempting to force all runtime behavior into local JVM tests can produce false failures or encourage test-only architecture.

Conversely, relying only on manual device testing would leave pure policy and command contracts vulnerable to regression.

---

## Decision

Atlas will use a **layered testing strategy**.

```text
Pure Unit Tests
      ↓
Policy / Contract Tests
      ↓
Gradle Build Validation
      ↓
Physical Device Validation
      ↓
Phase Lock
```

---

## Pure JVM Tests

Use for logic that does not require Android or native runtime state.

Examples:

- safety state transitions;
- command parsing;
- registries;
- package policy;
- recovery policy;
- deterministic helpers.

---

## Contract Tests

Use when the purpose is to protect an important command or source-level behavior that cannot safely execute in a local JVM environment.

Examples:

- preserving `linux shell`;
- preserving Safe Mode branches;
- preserving Recovery Mode messages;
- protecting Linux command usage strings.

Contract tests should protect meaningful behavior, not implementation trivia.

---

## Android / Device Validation

Required for:

- PRoot launch;
- native runtime assets;
- RootFS provisioning;
- Linux networking;
- package management;
- filesystem compatibility;
- process lifecycle;
- Compose/runtime integration;
- end-to-end Safe Mode and Recovery Mode.

---

## Standard Validation

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Physical-device installation:

```bash
./gradlew installDebug
```

Compilation alone does not prove runtime correctness.

---

## Alternatives Considered

### JVM tests only

Rejected because native runtime behavior cannot be faithfully reproduced by ordinary local unit tests.

### Manual testing only

Rejected because deterministic policy and command regressions should be caught automatically.

### Full instrumentation coverage for everything

Rejected as the only strategy because many critical rules are faster and clearer as pure JVM tests.

---

## Consequences

### Positive

- Pure logic remains fast to test.
- Native behavior is validated where it actually runs.
- Critical command contracts are regression-protected.
- Tests are less likely to depend on accidental Android initialization.

### Tradeoffs

- Some validation remains manual/device-dependent.
- The project must clearly document which layer validates which behavior.
- Contract tests must be reviewed so they do not become brittle text snapshots.

---

## Current Regression Coverage

Includes:

- Linux command contract tests;
- safety state-machine tests;
- package policy tests;
- recovery behavior tests;
- command/registry validation;
- runtime access-state validation.

---

## Related Decisions

- ADR-002 — Terminal Architecture
- ADR-005 — Continuous Integration
- ADR-008 — Runtime Safety Model
