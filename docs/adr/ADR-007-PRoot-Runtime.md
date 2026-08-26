# ADR-007 — PRoot Runtime

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck needs to provide a real Linux userspace on Android without requiring the phone to be rooted.

The target environment is an ordinary Android application sandbox on supported ARM64 devices.

The runtime must support:

- Ubuntu ARM64;
- Linux filesystem semantics sufficient for package management;
- `/dev`, `/proc`, and `/sys` bindings;
- persistent RootFS storage;
- root-like UID behavior inside the guest;
- package tools such as `apt` and `dpkg`;
- Android network access;
- process startup and shutdown controlled by Atlas.

A full hardware virtual machine would introduce greater resource cost and platform constraints, while Android root would violate the core product requirement.

---

## Decision

Atlas Cyberdeck will use **PRoot** as the rootless Linux userspace runtime for the current Android implementation.

Current runtime profile:

```text
Runtime      : PRoot 5.1.107.92
Architecture : ARM64
Guest        : Ubuntu 24.04.4 LTS
Android Root : Not required
```

Atlas packages the native ARM64 runtime components inside the APK and provisions them into the application runtime environment.

---

## Native Runtime Assets

Current native runtime assets include:

```text
libproot_atlas.so
libproot_loader_atlas.so
```

The application validates:

- ABI compatibility;
- asset availability;
- native binary readiness;
- runtime provenance;
- launch paths.

---

## Launch Model

The PRoot launch specification includes concepts such as:

```text
--kill-on-exit
--link2symlink
-L
-0
-r <rootfs>
-b /dev
-b /proc
-b /sys
-w /root
/bin/sh
-l
```

The exact argument order and implementation may evolve, but the architectural requirements remain:

- launch the persistent Ubuntu RootFS;
- expose required pseudo-filesystems;
- preserve rootless Android operation;
- support Debian package workflows;
- terminate guest processes with the runtime when appropriate.

---

## Environment Model

Atlas distinguishes host runtime environment from guest Linux environment.

Important environment concepts include:

```text
PROOT_L2S_DIR
PROOT_TMP_DIR
HOME
USER
LOGNAME
SHELL
TERM
LANG
LC_ALL
PATH
TMPDIR
TMP
TEMP
```

Guest temporary variables should resolve to:

```text
/tmp
```

while PRoot host-side temporary storage remains separate.

---

## Link-to-Symlink Compatibility

Debian package operations require filesystem behavior that Android application storage does not provide directly.

Atlas therefore uses PRoot link-to-symlink support and dedicated `.l2s` state.

```text
--link2symlink
PROOT_L2S_DIR=<rootfs>/.l2s
```

This state is persistent and must not be treated as disposable runtime cache.

---

## Guest UID

PRoot may present the guest as:

```text
uid=0
```

inside Ubuntu.

This does **not** mean the Android device is rooted.

The application remains constrained by Android application permissions and sandbox behavior.

---

## Alternatives Considered

### Android root

Rejected because Atlas is explicitly designed not to require rooting the user's device.

### Full virtual machine

Not selected for the current Android implementation because of resource cost, platform complexity, and device support constraints.

### Chroot

Rejected because normal application-level Android execution does not provide the privileges required for a conventional chroot environment.

### Toy shell emulation

Rejected because Atlas requires real Linux package tools and real Ubuntu userspace behavior.

---

## Consequences

### Positive

- Real Ubuntu userspace can run without Android root.
- Standard Linux tools are available.
- Package management works inside the persistent guest.
- Atlas can control the runtime entirely from the application layer.
- The runtime remains compatible with the product's mobile-first goal.

### Tradeoffs

- PRoot is not hardware virtualization.
- Some Linux filesystem semantics require compatibility handling.
- Interactive PTY support remains a separate concern.
- Performance differs from native Linux.
- Some low-level kernel-dependent tools may not work as they would on a native Linux host.

---

## Validation

Validated behaviors include:

- Ubuntu ARM64 startup;
- guest shell entry;
- Python execution;
- `uname -m` reporting `aarch64`;
- DNS synchronization;
- `apt update`;
- package installation;
- `dpkg` operations;
- persistent RootFS behavior;
- safe runtime shutdown.

---

## Related Components

- PRoot native assets
- `LinuxRuntimeProcessLauncher`
- `ProotLinuxRuntimeBackend`
- `LinuxRuntimePathManager`
- Ubuntu RootFS provisioning
- runtime asset validator
- ABI detector

---

## Related Decisions

- ADR-006 — Linux Runtime Architecture
- ADR-009 — Controlled Recovery
- ADR-010 — Atlas Shell vs Ubuntu Separation
