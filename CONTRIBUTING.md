<div align="center">

# Contributing to Atlas Cyberdeck

### **Build carefully. Validate thoroughly. Improve intentionally.**

</div>

---

## Welcome

Thank you for your interest in contributing to **Atlas Cyberdeck**.

Atlas Cyberdeck is under active alpha development and is evolving quickly. Contributions are welcome when they improve the project without weakening its architecture, runtime safety, maintainability, or reliability.

Before contributing, please review:

- [`README.md`](README.md)
- [`ATLAS_LABS.md`](ATLAS_LABS.md)
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/ROADMAP.md`](docs/ROADMAP.md)
- [`docs/STYLE_GUIDE.md`](docs/STYLE_GUIDE.md)
- [`docs/adr/`](docs/adr/)
- [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)

---

# Development Principles

Atlas Cyberdeck follows several core engineering principles:

- stability before unnecessary feature expansion;
- small, reviewable changes;
- every locked phase must build successfully;
- runtime-critical work must be validated appropriately;
- commit working code;
- preserve established architectural boundaries;
- protect user data;
- fail closed when runtime integrity is uncertain;
- document meaningful architectural decisions;
- add regression protection when fixing important failures.

---

# Current Project Status

Atlas Cyberdeck is currently in:

```text
v0.13.0-alpha
```

The current public phase is:

```text
Documentation & Product Readiness
```

The Linux runtime and safety engineering track is currently locked through:

```text
F3P-H5B
```

Please consult the roadmap before proposing large new subsystems.

---

# Contribution Scope

Useful contributions may include:

- bug fixes;
- documentation corrections;
- unit tests;
- regression tests;
- accessibility improvements;
- UI polish;
- performance improvements;
- diagnostics;
- command improvements;
- Linux runtime reliability;
- package-management hardening;
- developer tooling;
- documentation diagrams;
- issue reproduction;
- device compatibility reports.

Large new features should align with the roadmap or be discussed before implementation.

---

# Before You Start

For small fixes, a pull request may be sufficient.

For larger architectural changes, open a discussion or issue first so the intended direction can be evaluated before significant implementation work begins.

Examples that should be discussed first:

- new runtime backends;
- changes to persistence;
- safety-model changes;
- destructive operations;
- new plugin APIs;
- new Linux installation strategies;
- PTY architecture;
- SSH subsystem architecture;
- large navigation changes;
- package namespace migration;
- new supported platforms.

---

# Architecture Matters

Atlas Cyberdeck is intentionally modular.

Do not bypass established ownership boundaries simply because a shortcut is easier.

Examples:

```text
UI
 ↓
ViewModel / State
 ↓
Controller
 ↓
Backend
 ↓
Runtime
```

The UI should not directly own native PRoot process state.

Repository state should not be treated as proof that a native process is alive.

Runtime safety should not be bypassed by alternate startup paths.

The Atlas shell and Ubuntu guest should remain architecturally distinct.

---

# Code Quality

Contributed code should be:

- readable;
- maintainable;
- appropriately documented;
- testable;
- consistent with nearby code;
- narrow in responsibility;
- explicit about failure behavior.

Prefer clear code over clever code.

---

# Kotlin Style

Follow the existing Kotlin style in the repository.

General expectations:

- meaningful names;
- small focused functions;
- immutable state where practical;
- explicit state models;
- coroutines for blocking work;
- no long-running work on the UI thread;
- avoid unnecessary global mutable state;
- keep side effects outside pure policy where practical.

Use comments to explain **why**, not obvious syntax.

---

# UI Contributions

Atlas uses:

```text
Kotlin
Jetpack Compose
Material 3
ViewModel
StateFlow
```

UI contributions should:

- render authoritative state;
- avoid duplicating business logic;
- avoid inventing runtime state locally;
- respect Safe Mode and Recovery Mode;
- preserve accessibility;
- remain usable with hardware keyboards where relevant.

Safety identity takes priority over normal shell identity.

---

# Linux Runtime Contributions

Changes involving the Linux runtime require additional care.

Relevant areas include:

- PRoot;
- Ubuntu RootFS;
- native runtime assets;
- filesystem compatibility;
- DNS synchronization;
- package management;
- guest command execution;
- runtime safety;
- recovery.

Before modifying these areas, review:

```text
docs/engineering/Linux-Runtime.md
docs/engineering/Ubuntu-RootFS.md
docs/engineering/Guest-Command-Execution.md
docs/engineering/Package-Management.md
docs/engineering/Runtime-Networking.md
docs/engineering/Runtime-Safety.md
docs/engineering/Runtime-Recovery.md
docs/engineering/Runtime-Testing.md
```

---

# Runtime Safety Requirements

Changes must not silently weaken runtime safety.

Current states:

```text
NORMAL
SAFE_MODE
RECOVERY_ARMED
```

Important rules include:

- Safe Mode blocks normal runtime startup;
- recovery is restricted;
- recovery clears only after verified repair;
- corrupt safety state fails closed;
- persistent user data is preserved during recoverable failures;
- audit-only commands do not clear recovery;
- failed repair commands do not clear recovery.

Any change to these rules should include tests and an ADR update or new ADR where appropriate.

---

# Package Management

Atlas supports:

```text
apt
apt-get
dpkg
```

Package-related changes should preserve:

- explicit confirmation policy;
- preflight package-health checks;
- post-transaction auditing;
- original command exit-code authority;
- recovery restrictions;
- package-integrity escalation.

Do not silently append `-y` to user commands.

---

# Testing

At minimum, run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

For device-targeted changes, also run:

```bash
./gradlew installDebug
```

Native runtime changes may require physical-device validation.

---

# When Device Validation Is Required

Physical-device testing is expected for changes involving:

- native PRoot startup;
- Ubuntu RootFS;
- Android filesystem behavior;
- networking;
- package installation;
- process lifecycle;
- runtime safety side effects;
- Recovery Mode;
- Compose/native runtime integration.

A successful compile is not enough to prove these behaviors work.

---

# Regression Tests

When fixing an important bug, add regression protection where practical.

Good candidates include:

```text
lost command branches
incorrect safety transitions
startup bypasses
package-policy regressions
recovery false positives
state reconciliation failures
```

Tests should protect behavior, not merely mirror implementation syntax.

---

# Documentation

Documentation changes should follow:

[`docs/STYLE_GUIDE.md`](docs/STYLE_GUIDE.md)

Use project terminology consistently:

```text
Atlas Cyberdeck
Atlas Labs
Atlas shell
Ubuntu shell
Ubuntu guest
Linux runtime
PRoot
RootFS
Safe Mode
Recovery Mode
phase
GREEN
LOCKED
```

Do not reintroduce stale documentation that describes the Linux runtime or package management as future work.

---

# Architecture Decision Records

Major architectural changes should be documented in:

```text
docs/adr/
```

Use an ADR when a change:

- alters a major subsystem boundary;
- introduces a long-lived runtime dependency;
- changes persistence;
- changes safety policy;
- changes recovery behavior;
- changes Linux execution strategy;
- creates future compatibility obligations.

---

# Commit Guidelines

Keep commits focused and descriptive.

Good examples:

```text
fix(linux): preserve shell command contract
feat(safety): add fail-closed state transitions
docs(runtime): document controlled recovery
test(linux): protect safe-mode startup gate
```

Avoid vague messages such as:

```text
updates
stuff
fix
changes
```

---

# Pull Request Guidelines

A pull request should explain:

- what changed;
- why it changed;
- how it was validated;
- whether it affects runtime safety;
- whether it affects persistence;
- whether documentation changed;
- whether physical-device validation was performed.

Keep unrelated changes out of the same pull request when practical.

---

# Pull Request Checklist

Before submitting:

- [ ] The project builds successfully.
- [ ] Relevant unit tests pass.
- [ ] Runtime-critical behavior was device-tested where required.
- [ ] No known safety gate was bypassed.
- [ ] No persistent user data is deleted unintentionally.
- [ ] Documentation was updated when behavior changed.
- [ ] New architectural decisions were documented where appropriate.
- [ ] New warnings or errors are actionable.
- [ ] No secrets, credentials, or private keys were committed.
- [ ] The change follows the Code of Conduct.

---

# Security Issues

Do **not** open a public issue for a vulnerability that could put users at risk.

Follow the instructions in:

[`SECURITY.md`](SECURITY.md)

---

# What May Be Declined

A contribution may be declined if it:

- conflicts with current architecture;
- weakens runtime safety;
- introduces unnecessary coupling;
- duplicates existing functionality;
- relies on unsupported claims;
- is outside the current roadmap;
- lacks required validation;
- introduces destructive behavior without clear user control;
- significantly increases maintenance burden without sufficient benefit.

Declining a contribution does not mean the idea is bad; it may simply not fit the current product direction.

---

# Licensing

By contributing, you agree that your contribution may be distributed under the repository's existing license.

See:

[`LICENSE`](LICENSE)

---

# Questions

For design questions, implementation discussion, or contribution planning, use the collaboration channels available in the repository.

Please keep security vulnerability reports private as described in `SECURITY.md`.

---

<div align="center">

## Atlas Labs

### **Build the platform. Prove the runtime. Earn the trust.**

**Atlas Cyberdeck — Your Cyberdeck. Anywhere.**

</div>
