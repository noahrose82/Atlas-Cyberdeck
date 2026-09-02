<div align="center">

# Atlas Cyberdeck — Changelog

### **Your Cyberdeck. Anywhere.**

All notable changes to Atlas Cyberdeck are documented here.

</div>

---

## About This Changelog

Atlas Cyberdeck is currently in **alpha development**.

Features, internal APIs, package organization, runtime behavior, and implementation details may continue to evolve before version 1.0.

This changelog records user-visible changes, major architectural work, runtime milestones, safety improvements, testing improvements, and release-level engineering changes.

The project roadmap contains the detailed phase history:

[`docs/ROADMAP.md`](ROADMAP.md)

---

# v0.13.0-alpha — Linux Runtime & Safety Foundation

`v0.13.0-alpha` represents the largest technical expansion of Atlas Cyberdeck so far.

This release line moves Atlas beyond a Linux-inspired terminal foundation and establishes a **real rootless Ubuntu ARM64 environment on Android**, together with persistent shell access, package management, Android network integration, runtime diagnostics, fail-closed safety controls, controlled recovery, interactive PTY applications, an Atlas-native terminal keyboard, and expanded regression protection.

### Release Highlights

| Area | Status |
|---|:---:|
| Atlas shell platform | ✅ |
| Persistent virtual filesystem | ✅ |
| Ubuntu 24.04.4 ARM64 RootFS | ✅ |
| Native PRoot runtime | ✅ |
| Persistent Ubuntu shell | ✅ |
| Android DNS synchronization | ✅ |
| `apt` / `apt-get` / `dpkg` | ✅ |
| Streaming Linux command execution | ✅ |
| Package transaction hardening | ✅ |
| Runtime circuit breaker | ✅ |
| Safe Mode | ✅ |
| Controlled Recovery | ✅ |
| Safety-aware UI | ✅ |
| Interactive PTY terminal | ✅ |
| Nano / Vim | ✅ |
| Persistent PTY sessions | ✅ |
| Live PTY resize | ✅ |
| Atlas-native PTY keyboard | ✅ |
| Ctrl combinations / SYM layer | ✅ |
| Settings / About experience | ✅ |
| Linux command regression tests | ✅ |
| Safety state-machine tests | ✅ |
| Full device regression smoke test | ✅ |

---

## Added

### Rootless Ubuntu Linux Runtime

Added a real Ubuntu Linux userspace for supported ARM64 Android devices.

Current guest environment:

```text
Distribution : Ubuntu 24.04.4 LTS
Architecture : ARM64 / AArch64
Runtime      : PRoot
Guest UID    : 0
Home         : /root
Android Root : Not required
```

The Linux environment is persistent and remains separate from the Atlas shell environment.

---

### Native PRoot Runtime

Added the native runtime layer required to launch Ubuntu inside the Android application.

Major additions include:

- native ARM64 PRoot runtime
- native runtime loader support
- ABI detection
- runtime architecture descriptors
- native asset provisioning
- runtime path management
- runtime storage preparation
- runtime asset validation
- runtime integrity validation
- runtime provenance diagnostics
- native runtime packaging inside the APK

---

### Ubuntu RootFS Provisioning

Added the Ubuntu ARM64 RootFS provisioning pipeline.

The RootFS workflow now includes:

- Ubuntu source definition
- archive staging
- staging diagnostics
- RootFS extraction
- RootFS preparation
- persistent installation state
- RootFS provenance reporting
- installation readiness validation

---

### Linux Runtime Controller

Added a consolidated runtime-control layer.

The runtime controller coordinates:

- installation state
- device capability
- runtime safety
- backend startup
- backend shutdown
- runtime session state
- process-state reconciliation

The controller is now the primary application-side authority for Linux startup and shutdown.

---

### Runtime Backend Abstraction

Added an explicit backend boundary between application runtime control and PRoot-specific implementation details.

Current runtime flow:

```text
LinuxRuntimeController
        │
        ▼
LinuxRuntimeBackend
        │
        ▼
ProotLinuxRuntimeBackend
```

This reduces direct coupling between the Android UI and native process management.

---

### Persistent Ubuntu Shell

Added persistent Ubuntu shell mode.

Users can now start Linux:

```console
atlas@cyberdeck:~$ linux start
Linux runtime started.
```

Enter Ubuntu:

```console
atlas@cyberdeck:~$ linux shell
Ubuntu shell mode enabled.
Type 'exit' to return to Atlas.

root@atlas:~#
```

And return to Atlas without unnecessarily stopping Linux:

```console
root@atlas:~# exit
Welcome back to Atlas shell.

atlas@cyberdeck:~$
```

Current Linux commands:

```text
linux status
linux start
linux stop
linux shell
```

---

### Ubuntu Shell Identity

Added dedicated Ubuntu terminal presentation.

The shell now visually differentiates environments:

```text
Atlas Shell       → Atlas application identity
Ubuntu Shell      → Black / Matrix green
Safe Mode         → Black / yellow
Recovery Mode     → Black / amber
```

Safety identity takes precedence over normal shell identity.

---

### Guest Command Execution

Added real command execution inside the running Ubuntu guest.

The guest executor supports:

- command submission
- stdout capture
- stderr capture
- exit-code tracking
- command completion
- command-specific timeouts
- runtime-death detection
- package-command handling
- recovery restrictions
- safety escalation

---

### Streaming Linux Output

Added streamed guest command output.

Long-running Linux commands can now update the Atlas terminal while they execute instead of waiting for the entire command to finish before displaying output.

This significantly improves package-management and diagnostic workflows.

---

### Nonblocking Runtime Execution

Moved guest command work away from the Android UI thread.

Coroutine-based execution prevents Linux commands from unnecessarily blocking Compose rendering or creating avoidable application-not-responding behavior.

---

### Interactive Command Guard

Added protection for commands that require true terminal behavior.

The guard remains a safety net for interactive commands that Atlas does not explicitly support. Commands with a validated PTY path are routed into the interactive terminal stack instead of being treated as finite guest commands.

---

### Interactive PTY Terminal

Added a real interactive PTY path for full-screen Linux applications.

The current interactive stack includes:

- PTY allocation through `/dev/pts`;
- ConnectBot termlib rendering;
- process-level persistent session ownership;
- full-screen Nano and Vim support;
- interactive input independent of the finite guest-command executor;
- natural return to the Ubuntu shell when the interactive application exits.

---

### Persistent Interactive Sessions

Interactive PTY sessions are owned at the application-process level rather than by a screen-scoped ViewModel.

Validated behavior includes:

- surviving Terminal → Dashboard → Terminal navigation;
- surviving Android Home / app return;
- preserving unsaved Nano buffers across UI recreation;
- restoring the active terminal emulator when the UI reattaches.

---

### Live PTY Resize

Added live terminal geometry synchronization.

Atlas now measures the actual interactive terminal viewport and propagates row / column changes through:

```text
TerminalScreen
    ↓
TerminalViewModel
    ↓
Interactive Session Controller
    ↓
Interactive Terminal Bridge
    ↓
Linux PTY / stty
    ↓
termlib renderer
```

This keeps the guest PTY and Android renderer on the same geometry contract.

---

### Atlas-Native Terminal Keyboard

Added an Atlas-native on-screen keyboard for interactive PTY applications.

Current capabilities include:

- letters and numbers;
- Shift;
- Space;
- Enter;
- Backspace;
- Ctrl;
- Esc;
- Tab;
- arrow keys;
- dedicated `Ctrl+C`, `Ctrl+O`, and `Ctrl+X` actions;
- one-shot Ctrl combinations;
- `SYM` / `ABC` punctuation layer.

termlib remains configured to accept physical / Bluetooth keyboard input. This input path was not part of the current physical-device regression pass.

---

## Networking

### Android DNS Synchronization

Added synchronization between Android's active DNS configuration and the Ubuntu guest.

Atlas now updates guest resolver configuration from the Android network environment rather than relying on a hard-coded public DNS server.

The guest can use:

```text
/etc/resolv.conf
```

with current Android DNS information.

---

### Functional Linux Networking

Validated Linux name resolution and package-network access inside the Ubuntu guest.

Example:

```console
root@atlas:~# apt update
```

can operate through the device's active Android network connection.

---

## Linux Filesystem Compatibility

### PRoot Link-to-Symlink Support

Added PRoot link-to-symlink behavior required for improved Debian package compatibility.

Runtime support includes:

```text
--link2symlink
-L
```

---

### Dedicated `.l2s` State

Added dedicated link-to-symlink state storage through:

```text
PROOT_L2S_DIR
```

Atlas preserves this state because it can be required for Linux filesystem and package consistency.

---

### Runtime Temporary Storage

Separated host-side PRoot temporary storage from guest-side Linux `/tmp`.

Guest processes now use normal Linux temporary paths:

```text
TMPDIR=/tmp
TMP=/tmp
TEMP=/tmp
```

while PRoot implementation storage remains host-managed.

---

### Linux Pseudo-Filesystem Bindings

Added required runtime bindings for:

```text
/dev
/proc
/sys
```

---

## Package Management

### Debian Package Tools

Added functional support for:

```text
apt
apt-get
dpkg
```

Example:

```console
root@atlas:~# apt install -y python3
...
Atlas package health: CLEAN

root@atlas:~# python3 --version
Python 3.12.3
```

---

### Explicit Package Confirmation Policy

Added package-command policy requiring explicit user intent for mutating package operations.

Atlas does not silently append confirmation flags to package commands.

---

### Package Preflight Health Gate

Added a pre-transaction health check before normal package mutation.

Atlas can block risky package operations when Debian package state is already unhealthy.

Approved recovery commands remain available through the controlled recovery workflow.

---

### Post-Transaction Package Audit

Added package-state auditing after package mutation.

The runtime can report:

```text
Atlas package health: CLEAN
```

or warn when package state remains inconsistent.

---

### Original Exit-Code Preservation

Changed package execution so the **original package command result remains authoritative**.

A clean post-command audit does not convert a failed package command into a successful command.

This is especially important during recovery.

---

### Package Integrity Detection

Added recognition of package-state failures that should escalate runtime safety.

Package-integrity failures can now trip the Linux runtime circuit breaker instead of being treated as ordinary command errors.

---

# Runtime Safety

## Runtime Circuit Breaker

Added a fail-closed runtime safety subsystem.

The circuit breaker can activate after serious conditions such as:

- runtime process loss
- runtime integrity failure
- guest health failure
- package-state failure
- filesystem failure
- explicit developer safety testing

Current safety states:

```text
NORMAL
SAFE_MODE
RECOVERY_ARMED
```

---

### Safe Mode

Added `SAFE_MODE`.

When Safe Mode is active:

- normal Linux startup is blocked
- ordinary Ubuntu shell entry is blocked
- safety state is persisted
- the Linux Manager reflects the restriction
- terminal messaging explains the next recovery step
- safety status is visible across the application

---

### Recovery-Armed Mode

Added `RECOVERY_ARMED`.

Recovery mode allows Linux to start under restricted conditions so approved repair and diagnostic commands can run.

Normal unrestricted guest operation does not resume until Atlas verifies recovery.

---

### Safety Commands

Added:

```text
safety status
safety recover
safety trip-test
safety reset --force
```

`trip-test` exists for developer validation.

`reset --force` is a developer escape hatch and does not replace verified recovery.

---

### Controlled Recovery Policy

Added recovery-specific guest command restrictions.

Recovery operations can include approved commands such as:

```text
dpkg --configure -a
dpkg --configure --pending
apt --fix-broken install -y
apt-get -f install -y
```

Diagnostic commands may also be permitted where appropriate.

---

### Verified Recovery

Added verified recovery semantics.

Recovery is cleared only when:

1. an approved repair operation is executed;
2. the repair command itself succeeds;
3. post-repair package health is verified as clean.

A diagnostic-only audit is not enough to clear the safety latch.

A failed repair followed by a clean audit is also not enough to clear recovery.

---

### Persistent Safety State

Added local persistence for runtime safety state.

Safety state survives ordinary application recreation and is restored before the UI begins observing runtime safety.

---

### Fail-Closed Safety Record Handling

Changed corrupted or unreadable safety state behavior.

If Atlas cannot reliably read the runtime safety record, the runtime now fails closed into:

```text
SAFE_MODE
```

with a runtime-integrity reason instead of assuming the system is safe.

---

### Reactive Safety State

Added `StateFlow`-based runtime safety observation.

Safety changes can propagate to:

- Terminal
- Linux Manager
- app-wide safety banner
- diagnostics
- status
- neofetch

without each surface independently reconstructing runtime state.

---

### App-Wide Safety Banner

Added an application-level safety banner.

Safe Mode and Recovery Mode can now remain visible outside the terminal screen.

---

### Safety-Aware Linux Manager

Updated Linux controls to reflect runtime safety.

Examples include:

- Linux start disabled in Safe Mode
- recovery-specific start behavior
- install/remove operations safety-locked when required
- safety reason surfaced to the user
- stop behavior retained when appropriate

---

## Diagnostics

### Runtime Diagnostics Expansion

Expanded `diagnostics` to include Linux runtime and safety information.

Current diagnostic areas include:

```text
Version
Filesystem
Runtime Storage
Command Registry
Handlers
Plugins
Linux Runtime
Runtime Assets
Runtime Safety
Safety Tripped
Runtime Access
Safety Reason
Safety Cleanup
Runtime ABI
Device Profile
Overall Status
```

---

### Safety-Aware Status

Updated:

```text
status
```

to report safety and runtime-access state.

---

### Safety-Aware Neofetch

Updated:

```text
neofetch
```

to report safety mode, access state, and safety reason when appropriate.

---

## Product Experience

### Settings / About

Added a product-facing About experience to Settings.

Current content includes:

- Atlas Cyberdeck emblem;
- product name and tagline;
- approachable product description;
- Atlas Labs attribution;
- creator credit;
- current version;
- Credits access;
- Licenses access;
- a small Settings-only Atlas cat personality detail.

The design intentionally keeps Atlas technically credible while making the product feel approachable to users who may not identify as Linux or cybersecurity experts.

---

## Fixed

### `linux shell` Regression

Fixed a regression where the Linux runtime could start successfully but the `linux shell` command surface was accidentally lost.

The expected command contract is restored:

```text
linux status
linux start
linux stop
linux shell
```

---

### Safe Mode Startup Bypass

Fixed a safety flaw where Linux startup could bypass the circuit breaker through a higher-level runtime-control path.

Runtime startup now checks the safety gate before allowing a normal start.

A secondary launch-specification safety gate remains in place as additional protection.

---

### Recovery False-Positive Clearing

Fixed a recovery flaw where a failed package-repair command could incorrectly return Atlas to `NORMAL` when a subsequent audit happened to report clean package state.

Recovery now requires the repair command itself to succeed.

---

### JVM Test Runtime Coupling

Fixed an early regression-test design that attempted to directly execute Android/runtime-dependent singleton behavior in local JVM tests.

Runtime command-contract tests now validate the command surface without requiring Android path initialization or a running PRoot environment.

---

### Runtime State Reconciliation

Improved handling of stale application runtime state when the underlying PRoot process is no longer alive.

Runtime session state and repository state are reconciled against actual process state rather than being trusted blindly.

---

### Interactive Session Lifecycle Detection

Hardened interactive-session lifecycle detection.

A lightweight application-process lifecycle monitor now observes the interactive process and clears the active-session state when the PTY command naturally completes.

Natural Nano and Vim exit paths were validated on-device after this change.

---

### Atlas Keyboard Ctrl Combinations

Fixed Atlas-native keyboard Ctrl behavior so an armed Ctrl modifier followed by a letter sends the expected control character.

Dedicated control actions continue to work independently.

---

## Changed

### Runtime Ownership

Refined runtime responsibilities so application layers no longer share Linux process state implicitly.

Conceptual ownership now follows:

```text
Repository          → installation/runtime data model
Controller          → runtime orchestration
Backend             → runtime implementation
Process Launcher    → native process creation
Guest Executor      → finite Linux command execution
PTY Controller       → persistent interactive terminal sessions
PTY Bridge           → renderer / PTY input and resize coordination
State Machine       → pure safety transitions
Circuit Breaker     → safety persistence and side effects
UI                  → authoritative-state rendering
```

---

### Linux Shell Routing

Changed terminal processing so active Ubuntu shell commands are routed before ordinary Atlas command parsing.

This preserves the distinction between:

```text
atlas@cyberdeck:~$
```

and:

```text
root@atlas:~#
```

---

### Interactive Terminal Routing

Changed terminal command processing so validated interactive commands are routed into the PTY path before the normal finite guest-command path.

The finite guest executor remains responsible for ordinary commands and package workflows; it is not used as an interactive shell transport.

---

### Recovery Command Authority

Changed recovery semantics so the repair command exit code is authoritative.

Post-command audit status supplements the result but does not overwrite it.

---

### Development Model

Updated project terminology and documentation around **engineering phases**.

Current workflow:

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
```

---

# Testing

## Linux Command Contract Tests

Added regression protection for the Linux command surface.

Coverage includes:

- `linux shell`
- normal shell entry
- Safe Mode shell blocking
- Recovery Mode shell entry
- runtime-start Safe Mode blocking
- expected Linux command usage text

---

## Runtime Safety State-Machine Tests

Added pure JVM tests for safety transitions.

Coverage includes:

```text
NORMAL         → runtime allowed
SAFE_MODE      → runtime blocked
RECOVERY_ARMED → runtime allowed for recovery

trip           → SAFE_MODE
armRecovery    → RECOVERY_ARMED
reset          → NORMAL
fail closed    → SAFE_MODE
```

These tests do not require:

- Android
- PRoot
- runtime paths
- disk persistence
- native binaries

---

## Package Policy Validation

Expanded package-management validation around:

- explicit confirmation
- preflight health checks
- post-transaction auditing
- repair-command handling
- original exit-code preservation
- recovery verification

---

## Physical Device Validation

Runtime-critical changes were validated on a real ARM64 Android device.

Validated workflows include:

```text
Linux start
Linux stop
Ubuntu shell entry
Ubuntu shell exit
Python execution
DNS synchronization
apt update
package installation
package auditing
Safe Mode
Recovery Mode
verified recovery
runtime diagnostics
Nano full-screen editing
Vim insert / save / exit
interactive session persistence
live PTY resize
Atlas-native keyboard input
Ctrl combinations
SYM / ABC punctuation layer
natural PTY exit
runtime stop / start regression
app-wide screen smoke test
```

---

# Documentation

## README

Rebuilt the project README around the current product rather than the earlier terminal-only foundation.

The public description now reflects:

- real Ubuntu ARM64
- rootless PRoot execution
- package management
- runtime safety
- recovery
- Linux diagnostics
- current testing strategy
- current architecture
- interactive PTY terminal support
- Nano and Vim validation
- Atlas-native PTY keyboard
- live PTY resize
- current device-regression coverage

---

## Roadmap

Rebuilt the roadmap around public product phases while retaining the complete internal engineering completion record.

Internal milestones through:

```text
F3P-H7B
```

remain documented for traceability.

The roadmap now records the interactive PTY terminal work as H6 and current device-regression / product-UX work as H7.

---

## Architecture

Expanded architecture documentation to cover:

- Android application layer
- Atlas shell
- command architecture
- virtual filesystem
- script engine
- plugin foundation
- runtime controller
- backend abstraction
- PRoot
- Ubuntu RootFS
- guest executor
- DNS synchronization
- package-management policy
- runtime safety
- recovery
- testing
- CI
- SOLID boundaries

---

# Validation

Standard automated validation:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Physical-device installation validation:

```bash
./gradlew installDebug
```

Runtime-critical phases are not considered complete based on compilation alone.

---

# Engineering Traceability

The current Linux-runtime engineering track is complete through:

```text
F3P-H7B
```

Detailed phase history is maintained in:

[`ROADMAP.md`](ROADMAP.md)

Architecture details are maintained in:

[`ARCHITECTURE.md`](ARCHITECTURE.md)

---

# Earlier Releases

The following entries preserve earlier explicitly recorded Atlas Cyberdeck release history.

---

## v0.10.0-alpha — Foundation

### Added

- modular terminal architecture
- persistent virtual filesystem
- command registry
- command dispatcher
- command-handler registry
- command history
- aliases
- environment variables
- wildcard expansion
- command pipelines
- command completion
- Atlas `.ash` script engine
- plugin framework foundation
- version service
- diagnostics
- automated unit testing
- GitHub Actions CI
- architecture and engineering documentation

### Architecture

The v0.10.0-alpha line established the application architecture that later supported the real Linux runtime.

---

## v0.5.0

### Added

- `LinuxDistribution` model
- `LinuxInstallation` model
- `LinuxRepository`
- Linux domain foundation

---

## v0.4.1

### Fixed

- dashboard state management
- stable dashboard build

---

## v0.3.0

### Added

- boot sequence
- boot navigation

---

## v0.2.0

### Added

- navigation architecture

---

## v0.1.0

### Added

- initial Atlas Cyberdeck dashboard

---

# Current Development Status

Atlas Cyberdeck remains under active alpha development.

The current focus is **documentation and product readiness** following the Linux runtime, safety hardening, interactive PTY terminal, device-regression, and Settings / About work completed through `F3P-H7B`.

Upcoming product work is tracked in:

[`ROADMAP.md`](ROADMAP.md)

---

<div align="center">

## Atlas Labs

### **Build the platform. Prove the runtime. Earn the trust.**

<br>

> *"Maybe not breaking free from the Matrix—but we are writing our own code instead of living inside someone else's. That is a pretty good way to spend our time."*

<br>

**Atlas Cyberdeck — v0.13.0-alpha**

### **Your Cyberdeck. Anywhere.**

</div>
