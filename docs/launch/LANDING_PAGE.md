# Atlas Cyberdeck — Landing Page Copy

This is the recommended first public landing-page structure for audience validation.

---

# HERO

## Atlas Cyberdeck

### **A real Linux workstation in your pocket. No root required.**

Run a real Ubuntu ARM64 workspace on Android inside a native cyberdeck-style application built around portability, visibility, and deliberate runtime safety.

**Primary CTA:** `Join the Launch List`

**Secondary CTA:** `See It Running`

Supporting line:

```text
Ubuntu 24.04.4 LTS • ARM64 • Android • No Android Root
```

Use the real Atlas hero graphic and real-device screenshots.

---

# PROOF STRIP

```text
REAL UBUNTU
ARM64
APT / DPKG
PYTHON
PERSISTENT ROOTFS
SAFE MODE
CONTROLLED RECOVERY
```

Do not lead with a long feature list.

Lead with the proof that this is a real working environment.

---

# SECTION — THIS IS RUNNING TODAY

## Not a concept render.

Atlas Cyberdeck already runs Ubuntu 24.04.4 LTS on real Android hardware.

Show:

- `dashboard-device.png`
- `terminal-ubuntu-device.png`

Supporting proof:

```console
root@atlas:~# uname -m
aarch64

root@atlas:~# python3 --version
Python 3.12.3
```

Copy:

> The current alpha can provision an Ubuntu ARM64 RootFS, launch it through PRoot, enter a persistent Ubuntu shell, resolve DNS through Android, use Debian package tools, and run Linux software — without rooting the Android device.

---

# SECTION — MORE THAN A TERMINAL

## Atlas Shell + Ubuntu

Atlas Cyberdeck has two distinct environments.

### Atlas Shell

The native application shell handles:

- application-level commands;
- diagnostics;
- virtual filesystem workflows;
- scripts;
- Linux lifecycle controls;
- runtime safety.

### Ubuntu

The Linux guest provides:

- Ubuntu ARM64 userspace;
- package management;
- Python and Linux tooling;
- persistent guest files;
- Linux command workflows.

CTA:

`See the Architecture`

---

# SECTION — BUILT TO FAIL SAFELY

## Linux failures should be visible.

Show the safety/recovery visual.

Copy:

> Atlas includes a fail-closed runtime circuit breaker. Critical runtime, package-state, filesystem, or integrity failures can block normal Linux startup while preserving the Ubuntu workspace and guiding the user into controlled recovery.

State sequence:

```text
NORMAL
  ↓
SAFE_MODE
  ↓
RECOVERY_ARMED
  ↓
VERIFIED REPAIR
  ↓
NORMAL
```

---

# SECTION — WHO IT IS FOR

### Developers

A portable environment for Linux tools, Python, scripting, and future development workflows.

### Technical Students

A Linux workspace for learning and coursework that can travel on the Android device already in your pocket.

### IT Professionals

A portable technical workspace for diagnostics, scripting, and future remote-administration workflows.

### Linux / Homelab / Cyberdeck Enthusiasts

A transparent ARM64 Linux environment built around mobile computing rather than pretending a phone is merely a consumption device.

---

# SECTION — WHAT KICKSTARTER WOULD FUND

## The runtime works. Now build the product.

Copy:

> Atlas Cyberdeck is currently `v0.13.0-alpha`. Kickstarter would support the transition from a proven engineering foundation to a polished Atlas Cyberdeck 1.0 release.

Funding toward 1.0 includes:

- wider Android device validation;
- onboarding;
- terminal UX refinement;
- accessibility;
- performance profiling;
- battery / storage optimization;
- regression hardening;
- public documentation;
- privacy and security review;
- release signing and distribution readiness.

CTA:

`Follow the Build`

---

# SECTION — NOT THE PROMISE

Use a concise transparency block:

```text
Atlas 1.0 does not promise:
• every Android device
• every Linux package
• desktop-Linux equivalence
• dedicated hardware
• future SSH/Git suites
• Burner Mode
```

Copy:

> Future roadmap ideas remain future roadmap ideas. The campaign will be built around a defined 1.0 delivery scope.

---

# SECTION — FOUNDER

## Built independently under Atlas Labs.

> Atlas Cyberdeck is being built independently by Noah Rose under Atlas Labs, with a focus on owning and understanding the systems we rely on rather than treating mobile computing as a closed box.

Optional founder line:

> "Maybe not breaking free from the Matrix—but we are writing our own code instead of living inside someone else's."

---

# FINAL CTA

## Your Cyberdeck. Anywhere.

### Be there when Atlas launches.

**Primary CTA:** `Join the Launch List`

Once the Kickstarter pre-launch page is active, add:

**Secondary CTA:** `Notify Me on Kickstarter`

Supporting text:

> Early followers will get real-device demos, engineering updates, and the Kickstarter launch announcement.

Do not promise spam-free frequency that cannot be maintained. State the actual update cadence once it is chosen.
