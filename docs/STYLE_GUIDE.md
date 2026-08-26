<div align="center">

# Atlas Cyberdeck — Style Guide

### **Your Cyberdeck. Anywhere.**

**Documentation • Branding • Terminology • Visual Identity • Engineering Communication**

**Current release:** `v0.13.0-alpha`

</div>

---

# 1. Purpose

This style guide defines how **Atlas Cyberdeck** and **Atlas Labs** should be presented across:

- repository documentation
- source-code comments
- screenshots
- architecture diagrams
- terminal examples
- release notes
- website content
- product pages
- Kickstarter materials
- social media
- future application-store listings

The goal is consistency.

Atlas should feel like one product built from one engineering system, not a collection of unrelated documents and visual experiments.

---

# 2. Product Naming

Use the following names consistently.

| Name | Use |
|---|---|
| **Atlas Cyberdeck** | Product name |
| **Atlas Labs** | Organization / development identity |
| **Atlas shell** | Atlas application shell environment |
| **Ubuntu guest** | Ubuntu userspace running through PRoot |
| **Linux runtime** | Atlas-managed Linux execution subsystem |
| **PRoot** | Native rootless Linux userspace runtime |
| **Safe Mode** | User-facing name for `SAFE_MODE` |
| **Recovery Mode** | User-facing name for `RECOVERY_ARMED` |

Do not casually shorten the product name to:

```text
Atlas App
Cyberdeck App
PocketLab
Pocket Lab
```

unless discussing historical development.

---

# 3. Product Tagline

Primary tagline:

> **Your Cyberdeck. Anywhere.**

Use this consistently in high-visibility product material.

Recommended locations:

- README hero
- website hero
- launch materials
- Kickstarter
- social graphics
- release graphics
- product footer

Do not create multiple competing primary taglines.

---

# 4. Product Description

Preferred short description:

> **A rootless Ubuntu Linux workspace and extensible cyberdeck platform for Android.**

Preferred extended description:

> **Atlas Cyberdeck is an Android application that combines an independent Atlas shell, persistent virtual filesystem, and a rootless Ubuntu ARM64 environment managed through PRoot, runtime diagnostics, package safeguards, and fail-closed recovery controls.**

Use shorter versions for public-facing material and longer versions for technical documentation.

---

# 5. Atlas Labs Statement

Preferred organization description:

> **Atlas Cyberdeck is developed by Atlas Labs with an emphasis on clean architecture, security, portability, maintainability, and long-term extensibility.**

Do not imply Atlas Labs has employees, departments, offices, or funding unless that becomes factually true.

Professional presentation should come from the quality of the work, not from pretending to be a larger organization.

---

# 6. Founder Attribution

When founder attribution is appropriate, preferred wording is:

> **Built independently by Noah Rose under Atlas Labs.**

Use founder attribution in places where the solo-development story strengthens credibility, such as:

- project history
- founder story
- Kickstarter
- website About section
- selected launch materials

Do not overuse founder attribution in every technical document.

---

# 7. Voice and Tone

Atlas communication should sound:

- technically confident
- precise
- calm
- practical
- modern
- security-aware
- ambitious without exaggeration

Avoid:

- hype without evidence
- exaggerated cybersecurity language
- fear-based marketing
- fake enterprise language
- unsupported performance claims
- language that implies features exist when they are only planned

Preferred:

> Atlas blocks normal runtime startup when safety state is uncertain.

Avoid:

> Atlas is the most secure Linux runtime ever created for Android.

---

# 8. Engineering Tone

Engineering documentation should prioritize:

1. what the system does
2. why the design exists
3. who owns the responsibility
4. what assumptions are safe
5. what failure behavior is expected
6. how behavior is validated

Avoid decorative prose inside deep technical documents.

Use direct statements.

Preferred:

> The controller owns runtime orchestration.

Avoid:

> The controller serves as the beating heart of the Linux experience.

---

# 9. Product Claims

Every public product claim should be traceable to working behavior.

Good claims:

```text
Rootless Ubuntu ARM64
Android root not required
Persistent Ubuntu shell
apt / dpkg package management
Runtime safety states
Controlled recovery
Android DNS synchronization
```

Avoid claims such as:

```text
Full Linux VM
Complete desktop replacement
Hardware virtualization
Perfect Android isolation
Untraceable environment
Military-grade security
Enterprise-ready
Production-ready
```

unless future implementation and validation genuinely support them.

---

# 10. Alpha Status

Atlas Cyberdeck is currently alpha software.

Use:

> **Atlas Cyberdeck is currently alpha software.**

or:

> **Atlas Cyberdeck remains under active alpha development.**

Do not describe the current product as:

```text
production-ready
stable release
enterprise-ready
finished
complete
```

before the project reaches those thresholds.

---

# 11. Release Naming

Current release format:

```text
v0.13.0-alpha
```

Preferred semantic structure:

```text
vMAJOR.MINOR.PATCH-stage
```

Examples:

```text
v0.13.0-alpha
v0.14.0-alpha
v0.15.0-beta
v1.0.0
```

Keep release labels consistent across:

- README
- CHANGELOG
- ROADMAP
- application version output
- release notes
- screenshots when visible
- website

---

# 12. Phase Terminology

Use **phase** terminology for roadmap and engineering progress.

Preferred:

```text
Phase 7 — Documentation & Product Readiness
```

Internal engineering tracks may retain codes such as:

```text
F3P-H5B
```

but those codes should not replace clear public phase names.

Do not use **milestone** or **sprint** as the primary roadmap structure unless referring to historical work.

---

# 13. Status Terminology

Standard project statuses:

| Status | Meaning |
|---|---|
| ✅ **LOCKED** | Implemented, validated, accepted |
| 🟢 **ACTIVE** | Current work |
| 🟡 **PLANNED** | Defined future work |
| 🔵 **EXPLORATORY** | Long-term research direction |
| ⏸️ **DEFERRED** | Intentionally postponed |

Use these consistently in roadmap and engineering tracking.

Do not invent new status words unless a real need appears.

---

# 14. Validation Language

Preferred validation terms:

```text
GREEN
LOCKED
device-validated
unit-tested
build-validated
regression-protected
```

Use **GREEN** only when validation actually succeeded.

Use **LOCKED** only when a phase has been explicitly accepted as complete.

Do not mark work locked because code merely compiles.

---

# 15. Runtime Terminology

Use these terms consistently.

| Preferred | Meaning |
|---|---|
| **Linux runtime** | Full Atlas-managed Linux execution subsystem |
| **Ubuntu guest** | Ubuntu userspace |
| **Ubuntu shell** | Interactive guest shell mode |
| **Atlas shell** | Atlas application shell |
| **RootFS** | Ubuntu root filesystem |
| **runtime backend** | Runtime implementation layer |
| **runtime controller** | Application orchestration layer |
| **guest executor** | Ubuntu command execution bridge |

Avoid vague terms such as:

```text
Linux thing
Ubuntu mode
virtual Linux
Linux emulator
```

unless explaining concepts informally.

---

# 16. Root Language

Atlas uses PRoot and does not require Android root.

Preferred:

> **Root inside the Ubuntu guest; Android root is not required.**

Do not say:

> Atlas roots your phone.

Do not imply guest UID `0` equals Android root privileges.

---

# 17. Safety Terminology

Internal enum names:

```text
NORMAL
SAFE_MODE
RECOVERY_ARMED
```

User-facing names:

```text
Normal
Safe Mode
Recovery Mode
```

Documentation may show both:

```text
Safe Mode (`SAFE_MODE`)
Recovery Mode (`RECOVERY_ARMED`)
```

---

# 18. Safety Messaging

Preferred Safe Mode message style:

> **Linux runtime blocked by Atlas Safe Mode.**

Preferred recovery message style:

> **Recovery Mode restricts the Ubuntu guest to approved diagnostic and repair operations.**

Safety communication should be:

- clear
- calm
- actionable
- specific

Avoid dramatic language such as:

```text
CRITICAL SYSTEM FAILURE!!!
DANGER!!!
YOUR LINUX SYSTEM IS BROKEN!!!
```

---

# 19. Fail-Closed Language

Preferred principle:

> **Unknown integrity fails closed.**

Other acceptable wording:

> Atlas blocks normal runtime access when safety state cannot be trusted.

Do not describe fail-closed behavior as:

```text
panic mode
lockdown apocalypse
emergency kill switch
```

in technical documentation.

---

# 20. Recovery Language

Recovery should be described as controlled and verifiable.

Preferred:

> Recovery clears only after an approved repair command succeeds and Atlas verifies package health.

Avoid:

> Atlas automatically fixes everything.

Atlas should never claim recovery succeeded unless the validation criteria were satisfied.

---

# 21. Destructive Operations

Use explicit language for destructive actions.

Preferred:

```text
remove
delete
reset
wipe disposable workspace
```

Do not obscure destructive behavior behind vague labels such as:

```text
clean
optimize
refresh
repair
```

when data loss may occur.

---

# 22. Burner Mode Terminology

Burner Mode is deferred.

If documented, describe it as:

> **A future disposable-workspace mode that operates only on explicitly Burner-owned data.**

Do not claim it:

- erases Android logs
- erases network-provider logs
- removes cloud evidence
- hides activity
- self-destructs the application

Visual identity remains reserved as:

```text
Burner Mode → Black + red
```

---

# 23. Brand Visual Identity

Core visual identities:

| Context | Visual Direction |
|---|---|
| Atlas | Dark neutral + Atlas accent |
| Ubuntu | Black + Matrix green |
| Safe Mode | Black + yellow |
| Recovery Mode | Black + amber |
| Burner Mode, future | Black + red |

The exact application theme may evolve, but meaning should remain consistent.

---

# 24. Color Meaning

Colors should communicate state, not decoration.

### Matrix Green

Use for:

- Ubuntu shell
- successful Linux runtime identity
- Linux-focused product graphics

Suggested reference:

```text
#00FF41
```

### Safe Mode Yellow

Use for:

- blocked runtime state
- caution
- safety banners

Suggested reference:

```text
#FFD600
```

### Recovery Amber

Use for:

- restricted recovery
- repair-in-progress state

Suggested reference:

```text
#FFA000
```

### Red

Reserve for:

- destructive action warnings
- critical failure states
- future Burner Mode identity

Do not use red as a generic accent.

---

# 25. Backgrounds

Preferred technical visual backgrounds:

```text
#000000
near-black
charcoal
```

Avoid:

- bright gradients behind terminal text
- noisy textures
- excessive glow
- heavy blur
- neon effects that reduce readability

Cyberdeck aesthetics should remain functional.

---

# 26. Typography

For documentation:

- default GitHub typography is acceptable
- use monospace for terminal output, commands, paths, and identifiers

For designed graphics:

- use a clean sans-serif for headings
- use a professional monospace for terminal text

Avoid novelty hacker fonts for long text.

---

# 27. Terminal Visuals

Terminal examples should look authentic.

Preferred structure:

```console
atlas@cyberdeck:~$ linux start
Linux runtime started.

atlas@cyberdeck:~$ linux shell
Ubuntu shell mode enabled.

root@atlas:~#
```

Use actual Atlas output whenever possible.

Do not fabricate capabilities for prettier screenshots.

---

# 28. Prompt Standards

Atlas prompt:

```text
atlas@cyberdeck:~$
```

Ubuntu prompt:

```text
root@atlas:~#
```

Keep prompt examples consistent unless a real runtime condition changes them.

---

# 29. Terminal Window Graphics

Professional terminal cards may include:

- dark title bar
- subtle border
- three window controls
- clean monospace typography
- accurate prompt colors
- generous padding
- no unnecessary fake chrome

Recommended labels:

```text
Atlas Terminal
Ubuntu 24.04 LTS — Atlas Cyberdeck
Atlas Recovery Shell
```

---

# 30. Screenshots

Screenshots should show:

- real application state
- current UI
- readable text
- no personal notifications
- no irrelevant desktop clutter
- no development overlays unless intentional
- consistent device framing when possible

Preferred screenshot set:

```text
Dashboard
Atlas Terminal
Ubuntu Shell
Linux Manager
Safe Mode
Recovery Mode
```

---

# 31. Screenshot File Naming

Use descriptive lowercase names with hyphens.

Preferred:

```text
dashboard.png
terminal-atlas.png
terminal-ubuntu.png
linux-manager.png
safe-mode.png
recovery-mode.png
```

Avoid:

```text
Screenshot_2026-08-26_123445.png
finalfinal2.png
newpic.png
```

---

# 32. Diagram Style

Use Mermaid for maintainable repository diagrams when possible.

Preferred diagram types:

```text
flowchart
stateDiagram-v2
sequenceDiagram
```

Diagrams should answer a specific architectural question.

Avoid creating diagrams only because diagrams look impressive.

---

# 33. Architecture Diagram Rules

Every architecture diagram should make ownership clear.

Preferred:

```text
UI
 ↓
Controller
 ↓
Backend
 ↓
PRoot
 ↓
Ubuntu RootFS
```

For safety:

```text
Failure
 ↓
Circuit Breaker
 ↓
Safe Mode
 ↓
Controlled Recovery
 ↓
Verified Repair
 ↓
Normal
```

Avoid crossing lines and dozens of nodes when a smaller diagram explains the concept better.

---

# 34. Markdown Headings

Use heading levels hierarchically.

Preferred:

```markdown
# Document Title
## Major Section
### Subsection
#### Detail
```

Do not skip heading levels for visual size.

Avoid excessive level-one headings inside small documents.

---

# 35. Document Hero Sections

High-value public documents may use centered hero markup.

Recommended for:

```text
README.md
ROADMAP.md
ARCHITECTURE.md
CHANGELOG.md
STYLE_GUIDE.md
ATLAS_LABS.md
```

Do not use elaborate hero sections for tiny engineering notes.

---

# 36. Horizontal Rules

Use:

```markdown
---
```

to divide major document sections.

Do not place separators between every paragraph.

---

# 37. Tables

Use tables for:

- status comparisons
- architectural ownership
- feature summaries
- terminology
- state definitions

Avoid tables for long prose.

---

# 38. Lists

Use bullets for collections of related items.

Preferred:

```markdown
- runtime controller
- runtime backend
- guest executor
- safety state machine
```

Keep parallel grammatical structure.

---

# 39. Code Blocks

Always specify a language when helpful.

Examples:

````markdown
```bash
./gradlew assembleDebug
```

```kotlin
val runtimeStatus = ...
```

```text
SAFE_MODE
RECOVERY_ARMED
```

```console
atlas@cyberdeck:~$ linux start
```
````

Use `console` for prompt/output examples.

Use `text` for plain state lists.

---

# 40. Command Formatting

Inline commands use backticks:

```markdown
Run `linux start`.
```

Multi-line command examples use code blocks.

Do not use quotation marks around commands unless quoting literal text.

---

# 41. Paths

Paths always use backticks.

Examples:

```text
docs/ARCHITECTURE.md
app/src/main/java/
com.noahrose.pocketlab
/root
/etc/resolv.conf
```

---

# 42. Class and Symbol Names

Use backticks for code symbols.

Examples:

```text
LinuxRuntimeController
LinuxRuntimeCircuitBreaker
LinuxRuntimeSafetyStateMachine
LinuxShellMode
```

Preferred in prose:

> `LinuxRuntimeController` owns runtime orchestration.

---

# 43. Android Namespace

Current namespace:

```text
com.noahrose.pocketlab
```

Describe it as:

> **The legacy Android namespace remains in use while Atlas Cyberdeck continues its architectural evolution.**

Do not imply the package name is already migrated.

---

# 44. File Naming

Documentation files use descriptive names.

Preferred:

```text
Linux-Runtime.md
Runtime-Safety.md
Package-Management.md
Runtime-Recovery.md
```

ADRs use:

```text
ADR-001-Filesystem-Architecture.md
ADR-006-Linux-Runtime-Architecture.md
```

Keep numbering sequential.

---

# 45. Engineering Document Structure

Recommended structure:

```text
Title
Purpose
Scope
Architecture
Responsibilities
Data / State
Execution Flow
Failure Behavior
Testing
Known Limitations
Future Work
```

Not every document needs every section, but this is the preferred baseline.

---

# 46. ADR Structure

Architecture Decision Records should use:

```text
Title
Status
Context
Decision
Alternatives Considered
Consequences
Validation
Related Components
```

Status examples:

```text
Accepted
Superseded
Deprecated
Proposed
```

---

# 47. Changelog Style

The changelog should record notable changes, not every commit.

Preferred categories:

```text
Added
Changed
Fixed
Security
Testing
Documentation
Deprecated
Removed
```

Group related work by subsystem.

Do not paste raw Git history into the changelog.

---

# 48. Roadmap Style

The public roadmap should explain product direction.

Detailed engineering codes should remain available for traceability but should not dominate the public view.

Preferred:

```text
Phase 5 — Runtime Safety & Recovery
```

Internal traceability:

```text
H4A → H5B
```

---

# 49. README Style

README priority:

```text
What is Atlas?
Why is it different?
What can it do?
What does it look like?
How is it built?
Where is it going?
```

Do not turn README into a complete internal design specification.

Link deeper topics to documentation.

---

# 50. Architecture Style

`ARCHITECTURE.md` explains:

> **How major systems fit together.**

It should not duplicate every implementation detail from engineering documents.

---

# 51. Engineering Docs Style

`docs/engineering/` explains:

> **How specific systems work internally.**

Examples:

```text
Linux-Runtime.md
Guest-Command-Execution.md
Runtime-Networking.md
Package-Management.md
Runtime-Safety.md
Runtime-Recovery.md
Runtime-Testing.md
```

---

# 52. Documentation Linking

Use relative repository links.

Preferred:

```markdown
[Architecture](ARCHITECTURE.md)
```

Inside `docs/`:

```markdown
[Roadmap](ROADMAP.md)
```

Avoid absolute GitHub URLs for files inside the same repository unless there is a strong reason.

---

# 53. Terminology Consistency

Preferred capitalization:

```text
Atlas Cyberdeck
Atlas Labs
Atlas shell
Ubuntu shell
Ubuntu guest
Linux runtime
RootFS
PRoot
Safe Mode
Recovery Mode
Android
Kotlin
Jetpack Compose
Material 3
StateFlow
```

Avoid inconsistent forms such as:

```text
safe mode
Safe mode
SAFE Mode
rootfs
Rootfs
Proot
proot
```

unless matching an exact code identifier.

---

# 54. Source-Code Comments

Comments should explain **why**, not restate obvious code.

Preferred:

```kotlin
// Persist SAFE_MODE before stopping PRoot so a process failure
// cannot leave the runtime appearing safe after restart.
```

Avoid:

```kotlin
// Set mode
mode = SAFE_MODE
```

---

# 55. TODO Comments

Preferred:

```kotlin
// TODO(PTY): Replace interactive-command guard when PTY support is implemented.
```

Make TODOs specific enough to understand later.

Avoid:

```kotlin
// TODO fix this
```

---

# 56. Logging

Logs should identify the subsystem and event.

Preferred conceptually:

```text
AtlasRuntime: PRoot process started
AtlasSafety: circuit breaker tripped
AtlasDNS: guest resolver synchronized
```

Do not log:

- passwords
- private keys
- authentication tokens
- sensitive file contents

---

# 57. Error Messages

Errors should answer:

1. what failed?
2. what state is Atlas in?
3. what can the user do?

Preferred:

```text
linux: runtime startup blocked by Atlas Safe Mode.
Run 'safety recover' to begin controlled recovery.
```

Avoid:

```text
Error 42
Operation failed
Something went wrong
```

when more specific information exists.

---

# 58. Success Messages

Success messages should be brief and factual.

Preferred:

```text
Linux runtime started.
Ubuntu shell mode enabled.
Atlas package health: CLEAN
Atlas safety: recovery verified; safe mode cleared.
```

Avoid celebratory noise for routine operations.

---

# 59. Diagnostic Output

Diagnostic fields should align visually when practical.

Preferred:

```text
Runtime Safety   : NORMAL
Safety Tripped   : NO
Runtime Access   : ENABLED
Safety Reason    : NONE
```

Keep labels stable so regression tests and users can recognize them.

---

# 60. Safety Priority

When multiple visual states could apply:

```text
Safety state > shell identity
```

Example:

A running Ubuntu shell in Safe Mode should use Safe Mode visual identity rather than ordinary Ubuntu green.

---

# 61. Accessibility

Visual design should not rely on color alone.

Use:

- text labels
- icons
- clear state names
- contrast

Example:

```text
ATLAS SAFE MODE
Runtime blocked
```

not merely a yellow background.

---

# 62. Professional Repository Presentation

The repository should feel intentional.

Maintain:

```text
README.md
ATLAS_LABS.md
CODE_OF_CONDUCT.md
CONTRIBUTING.md
LICENSE
SECURITY.md

docs/
├── ARCHITECTURE.md
├── CHANGELOG.md
├── ROADMAP.md
├── STYLE_GUIDE.md
├── adr/
├── engineering/
├── images/
└── screenshots/
```

Do not leave abandoned duplicates such as:

```text
README-new.md
ROADMAP-final.md
architecture-old.md
```

in the main branch.

---

# 63. Documentation Quality Gate

Before a documentation phase is marked LOCKED:

- version references should match
- roadmap terminology should use phases
- Linux runtime should not be listed as future work
- package management should not be listed as future work
- links should resolve
- screenshots should exist
- filenames should match references
- code examples should reflect real behavior
- safety terminology should match the application
- current release should be consistent
- obsolete sprint language should be removed unless historical

---

# 64. Visual Asset Quality Gate

Before publishing a visual:

- no blur unless intentional
- text must be readable
- current branding must be used
- command output must be accurate
- no visible personal information
- no temporary debug labels
- state colors must be correct
- aspect ratio should match intended use
- source asset should be retained when practical

---

# 65. Marketing Style

Marketing should lead with capability.

Preferred themes:

```text
Real Ubuntu
No Android root required
Persistent Linux workspace
Designed for developers and cybersecurity learners
Runtime safety
Portable workflow
```

Avoid leading with:

```text
hacking
breaking systems
anonymous activity
stealth
bypassing controls
```

Atlas should be positioned as a **portable Linux and cybersecurity workspace**, not a malicious-tool bundle.

---

# 66. Kickstarter Style

Kickstarter communication should be:

- transparent
- demonstrable
- founder-driven
- technically grounded
- realistic about alpha status
- clear about what funding enables

Use real-device footage whenever possible.

Do not promise future features solely because they sound attractive.

---

# 67. Website Style

Recommended website hierarchy:

```text
Hero
Real Ubuntu / No Root
Product Demo
Core Capabilities
Safety Architecture
Screenshots
Architecture
Roadmap
Founder Story
FAQ
Call to Action
```

Keep deep engineering documentation linked rather than embedded in the entire landing page.

---

# 68. Social Media Style

Short-form posts should usually show one capability at a time.

Examples:

```text
Atlas → Ubuntu in seconds
Run Python on Android
Safe Mode in action
apt inside Atlas
Persistent Ubuntu shell
```

Prefer proof over promotional adjectives.

---

# 69. Founder Story Style

The founder story should focus on:

- why Atlas exists
- the engineering journey
- learning and persistence
- portable computing
- Linux access
- product-building discipline

Avoid turning the founder story into a résumé dump.

---

# 70. Project Philosophy

Preferred public philosophy:

> **Build the platform. Prove the runtime. Earn the trust.**

This may appear in:

- roadmap
- architecture
- changelog
- launch materials

Use it as a supporting line, not a replacement for the primary tagline.

---

# 71. Atlas Labs Quote

Preferred project quote:

> *"Maybe not breaking free from the Matrix—but we are writing our own code instead of living inside someone else's. That is a pretty good way to spend our time."*

Use sparingly in major documents and brand materials.

Do not place it in every engineering note.

---

# 72. Professional Standard

Every Atlas artifact should pass one simple test:

> **Does this look like it belongs to the same product, engineering system, and brand as everything else?**

If not, fix the inconsistency before publishing.

---

# 73. Current Documentation Standard

The public documentation set should now follow this hierarchy:

```text
README.md
    → product overview

docs/ROADMAP.md
    → product direction and engineering phases

docs/ARCHITECTURE.md
    → high-level system architecture

docs/CHANGELOG.md
    → notable release history

docs/STYLE_GUIDE.md
    → presentation and terminology standard

docs/adr/
    → major architectural decisions

docs/engineering/
    → deep subsystem implementation
```

---

# 74. Final Rule

Atlas Cyberdeck should look polished because the engineering is disciplined—not because the documentation exaggerates what exists.

The product should always be able to back up the presentation.

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
