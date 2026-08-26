# Atlas Cyberdeck Security Policy

## Security Philosophy

Security is a primary design objective of Atlas Cyberdeck.

The project includes a persistent Linux environment, native runtime components, package management, runtime safety, and recovery logic. Security issues affecting these systems should be handled carefully and, when appropriate, reported privately before public disclosure.

---

## Supported Versions

Atlas Cyberdeck is currently in alpha development.

| Version | Security Support |
|---|---|
| `v0.13.x-alpha` | ✅ Current development line |
| Earlier alpha releases | ⚠️ Best effort only |

Security fixes are generally targeted at the current development line.

---

## Reporting a Vulnerability

Please **do not open a public issue** for a vulnerability that could put users at risk.

Report potential vulnerabilities privately to the project owner using an available private repository or project communication channel.

If the repository enables a dedicated private vulnerability-reporting feature, prefer that mechanism.

Include as much of the following as practical:

- affected version or commit;
- affected component;
- description of the issue;
- reproduction steps;
- expected behavior;
- observed behavior;
- potential impact;
- logs or screenshots where useful;
- suggested mitigation, if known.

Do not include passwords, private keys, tokens, or unrelated personal data.

---

## Security-Sensitive Areas

Reports involving the following areas are especially relevant:

- runtime safety bypass;
- Safe Mode bypass;
- Recovery Mode bypass;
- package-integrity handling;
- unauthorized destructive behavior;
- native PRoot execution;
- runtime asset integrity;
- RootFS integrity;
- path traversal;
- filesystem boundary errors;
- arbitrary file overwrite;
- command injection;
- unsafe command construction;
- privilege-boundary confusion;
- exposed secrets;
- insecure key storage;
- unsafe future SSH behavior;
- unintended data disclosure.

---

## Atlas Security Boundaries

Atlas Cyberdeck uses PRoot to provide a rootless Ubuntu userspace.

Important boundary:

> **Guest root is not Android root.**

A report should not treat ordinary guest UID `0` behavior as an Android privilege escalation unless the behavior actually crosses the Android application boundary.

---

## Runtime Safety

Atlas currently uses:

```text
NORMAL
SAFE_MODE
RECOVERY_ARMED
```

Security reports involving runtime safety should identify whether the issue can:

- start Linux while `SAFE_MODE` is active;
- bypass recovery command restrictions;
- clear recovery without verified repair;
- suppress a required safety trip;
- corrupt or replace safety state;
- cause Atlas to fail open when safety state is unreadable.

---

## Data Preservation

Atlas is designed to preserve persistent user data during recoverable failures.

Security-relevant destructive behavior includes unexpected modification or deletion of:

```text
Ubuntu RootFS
/root user data
package database
package metadata
.l2s state
Atlas virtual filesystem
installation state
safety state
```

---

## Coordinated Disclosure

Please allow reasonable time for the issue to be:

1. reproduced;
2. assessed;
3. fixed;
4. tested;
5. released or otherwise mitigated.

Once a fix or mitigation is available, coordinated public disclosure may be appropriate.

Please avoid publishing exploit details that would unnecessarily expose users before a fix is available.

---

## What to Expect

After a private report is received, the project will make a reasonable effort to:

- acknowledge the report;
- determine whether the issue is reproducible;
- evaluate severity and affected components;
- develop a fix or mitigation where appropriate;
- add regression protection where practical;
- update documentation when necessary.

Because Atlas Cyberdeck is independently developed and currently in alpha, response time may vary depending on issue complexity and availability.

---

## Security Fix Standards

A security fix should, where practical:

- address the root cause;
- avoid weakening existing safety controls;
- preserve user data;
- include regression coverage;
- update relevant documentation;
- update an ADR if architecture or policy changes;
- pass standard build validation;
- receive physical-device validation when native runtime behavior is involved.

Standard validation includes:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Runtime-sensitive fixes may additionally require:

```bash
./gradlew installDebug
```

and physical-device testing.

---

## Public Issues That Are Appropriate

Public issue reporting is generally appropriate for non-sensitive problems such as:

- UI bugs;
- documentation mistakes;
- ordinary crashes without security impact;
- feature requests;
- compatibility reports;
- non-sensitive command behavior;
- performance issues.

If you are unsure whether an issue is security-sensitive, prefer private reporting first.

---

## Out of Scope

The following are generally not security vulnerabilities by themselves:

- the Ubuntu guest presenting UID `0`;
- behavior inherent to PRoot that does not cross Atlas or Android security boundaries;
- unsupported interactive terminal applications;
- features explicitly documented as unavailable;
- vulnerabilities in obsolete versions that do not affect the current development line, unless they reveal a current architectural issue;
- social-engineering reports without a product vulnerability.

---

## No Security Theater

Atlas Labs does not use unsupported security claims such as:

```text
military-grade
unhackable
perfectly isolated
anonymous
untraceable
```

Security documentation should describe actual controls and actual boundaries.

---

## Security Documentation

Relevant technical documentation includes:

```text
docs/ARCHITECTURE.md
docs/engineering/Runtime-Safety.md
docs/engineering/Runtime-Recovery.md
docs/engineering/Package-Management.md
docs/engineering/Linux-Runtime.md
docs/adr/ADR-008-Runtime-Safety-Model.md
docs/adr/ADR-009-Controlled-Recovery.md
```

---

## Thank You

Responsible vulnerability reporting helps Atlas Cyberdeck become safer for everyone who uses or contributes to the project.

---

<div align="center">

## Atlas Labs

### **Build the platform. Prove the runtime. Earn the trust.**

**Atlas Cyberdeck — Your Cyberdeck. Anywhere.**

</div>
