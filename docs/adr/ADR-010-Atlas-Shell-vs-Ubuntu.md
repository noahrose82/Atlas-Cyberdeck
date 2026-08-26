# ADR-010 — Atlas Shell vs Ubuntu Separation

**Status:** Accepted  
**Product:** Atlas Cyberdeck  
**Current release:** v0.13.0-alpha

---

## Context

Atlas Cyberdeck contains two command environments:

1. the Atlas application shell;
2. the Ubuntu guest shell.

Both environments are terminal-based, but they serve different architectural purposes.

Without a clear boundary, commands such as:

```text
pwd
whoami
ls
```

could behave unpredictably depending on runtime state.

The application also needs to remain useful when Ubuntu is:

- not installed;
- stopped;
- blocked by Safe Mode;
- unavailable on the device.

Therefore, the Atlas shell cannot simply become a thin alias for Ubuntu.

---

## Decision

Atlas Cyberdeck will maintain **two intentionally separate shell environments**.

### Atlas Shell

Prompt:

```text
atlas@cyberdeck:~$
```

Owns:

- Atlas commands;
- Atlas virtual filesystem;
- application diagnostics;
- Linux runtime controls;
- scripting;
- plugins;
- application-level status.

### Ubuntu Shell

Prompt:

```text
root@atlas:~#
```

Owns:

- Ubuntu guest commands;
- Linux filesystem;
- `apt`;
- `dpkg`;
- Python;
- Linux userspace tools.

---

## Entry and Exit

The user enters Ubuntu explicitly:

```console
atlas@cyberdeck:~$ linux shell
Ubuntu shell mode enabled.
Type 'exit' to return to Atlas.

root@atlas:~#
```

The user returns to Atlas with:

```console
root@atlas:~# exit
Welcome back to Atlas shell.

atlas@cyberdeck:~$
```

Leaving shell mode does not necessarily stop the Linux runtime.

---

## Routing Rule

When Ubuntu shell mode is active, guest command routing occurs before ordinary Atlas command parsing.

Conceptually:

```text
User Input
    ↓
Is Ubuntu shell active?
    ├── yes → Guest Command Executor
    └── no  → Atlas Command Pipeline
```

`exit` is intercepted to leave Ubuntu shell mode.

---

## Atlas Command Identity

Generic Atlas commands remain Atlas commands unless Ubuntu shell mode is active.

Examples:

```text
whoami
pwd
ls
status
diagnostics
```

This prevents runtime presence from silently changing the meaning of the Atlas shell.

---

## Filesystem Separation

Atlas virtual filesystem:

```text
Atlas shell storage
```

Ubuntu RootFS:

```text
Linux guest storage
```

These are separate persistence domains.

Future file-transfer or workspace-bridge features should be explicit rather than silently merging the two filesystems.

---

## Visual Identity

The terminal uses visual state to communicate the active environment.

| Environment | Visual Identity |
|---|---|
| Atlas shell | Atlas theme |
| Ubuntu shell | Black + Matrix green |
| Safe Mode | Black + yellow |
| Recovery Mode | Black + amber |

Safety state takes priority over ordinary shell identity.

---

## Safety Interaction

### Safe Mode

Normal Ubuntu shell entry is blocked.

Atlas shell remains available so the user can:

```text
safety status
safety recover
diagnostics
status
```

### Recovery Mode

Ubuntu shell entry may be allowed, but guest commands are restricted by recovery policy.

---

## Alternatives Considered

### Always run commands in Ubuntu when Linux is installed

Rejected because Atlas behavior would depend on runtime availability and would lose the independent application shell.

### Replace the Atlas shell with Ubuntu

Rejected because application controls, diagnostics, safety commands, scripting, and VFS behavior would become coupled to the guest.

### Automatically switch environments based on command name

Rejected because commands such as `pwd`, `ls`, and `whoami` exist in both environments and would become ambiguous.

---

## Consequences

### Positive

- Shell behavior is predictable.
- Atlas remains useful without Linux.
- Safety Mode can block Ubuntu while preserving application control.
- The VFS remains independent.
- Ubuntu behaves like a real guest environment when entered explicitly.
- Future SSH or additional guest environments can follow the same explicit-session model.

### Tradeoffs

- Users must understand which shell is active.
- Some commands exist in both environments with different state.
- File movement between environments requires explicit tooling.
- UI must clearly communicate active shell identity.

---

## Validation

The separation has been validated through:

- Atlas prompt behavior;
- Ubuntu prompt behavior;
- `linux shell`;
- `exit`;
- persistent Linux runtime after shell exit;
- Atlas `whoami` / `pwd` behavior;
- Ubuntu guest command execution;
- Safe Mode shell blocking;
- Recovery Mode restricted shell access.

---

## Related Components

- `TerminalCommandProcessor`
- `LinuxShellMode`
- Atlas virtual filesystem
- guest command executor
- `UtilityCommands`
- runtime safety subsystem

---

## Related Decisions

- ADR-001 — Filesystem Architecture
- ADR-002 — Terminal Architecture
- ADR-006 — Linux Runtime Architecture
- ADR-008 — Runtime Safety Model
- ADR-009 — Controlled Recovery
