# Atlas Cyberdeck — Runtime Recovery

**Document type:** Engineering  
**Status:** Current  
**Release baseline:** `v0.13.0-alpha`

---

## Purpose

This document describes controlled recovery from Atlas Safe Mode.

The recovery system is designed to repair the existing Ubuntu environment while preserving user data and preventing unrestricted runtime access during an integrity incident.

---

## Recovery Entry

From Safe Mode:

```console
atlas@cyberdeck:~$ safety recover
Atlas recovery mode armed.
```

Atlas transitions:

```text
SAFE_MODE
    ↓
RECOVERY_ARMED
```

---

## Recovery Runtime

Recovery Mode permits the runtime to start.

```console
atlas@cyberdeck:~$ linux start
Linux runtime started.
```

Ubuntu shell entry is allowed with restrictions.

---

## Recovery Policy

`LinuxRuntimeRecoveryPolicy` classifies commands into:

```text
allowed diagnostics
repair operations
audit-only operations
disallowed commands
```

---

## Approved Repair Operations

Current examples include:

```text
dpkg --configure -a
dpkg --configure --pending
apt --fix-broken install -y
apt --fix-broken install --assume-yes
apt-get -f install -y
apt-get --fix-broken install -y
apt-get --fix-broken install --assume-yes
```

---

## Approved Diagnostics

Examples may include:

```text
pwd
whoami
id
uname
cat /etc/os-release
dpkg --audit
```

Diagnostic commands are useful for inspection but do not necessarily count as repairs.

---

## Verification Rule

Recovery clears only when:

```text
approved repair
AND
exit code == 0
AND
Atlas package health == CLEAN
```

---

## Failed Repair

If the repair exits nonzero:

```text
recovery remains armed
```

even if a later package audit is clean.

This prevents false-positive recovery.

---

## Audit-Only Rule

`dpkg --audit` can be allowed and can return successfully.

It does not clear Recovery Mode by itself.

---

## Successful Recovery

Expected flow:

```console
root@atlas:~# dpkg --configure -a

Atlas package health: CLEAN
Atlas safety: recovery verified; safe mode cleared.
```

Final state:

```text
NORMAL
```

---

## Data Preservation

Recovery must preserve:

- RootFS;
- `/root`;
- package database;
- package metadata;
- `.l2s`;
- Atlas VFS;
- installation state.

Recovery is not reinstall.

---

## Force Reset

Developer escape hatch:

```text
safety reset --force
```

Use only when intentionally bypassing verified repair for development/testing.

It should not be presented as the normal recovery workflow.

---

## Recovery Failure Modes

Potential failure cases:

```text
repair command blocked
repair exits nonzero
package audit remains dirty
PRoot dies during repair
filesystem integrity fails
new safety trip occurs
```

Any of these must prevent automatic return to `NORMAL`.

---

## Testing

Validated recovery scenarios include:

- Safe Mode → Recovery Mode;
- recovery runtime start;
- restricted shell;
- allowed diagnostics;
- blocked unrelated commands;
- successful repair;
- failed repair;
- audit-only command;
- verified return to Normal.

---

## Related Documents

- `Runtime-Safety.md`
- `Package-Management.md`
- `Guest-Command-Execution.md`
- `../adr/ADR-009-Controlled-Recovery.md`
