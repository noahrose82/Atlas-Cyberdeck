# Atlas Cyberdeck — Demo Video Storyboard

## Target Length

**Primary campaign cut:** 90–150 seconds

**Short cut:** 30–45 seconds

**Technical cut:** 3–5 minutes

---

# Scene 1 — The Phone

**Time:** 0:00–0:10

Visual:

- physical Android phone;
- screen off, then wake;
- Atlas Cyberdeck icon/app launch.

Narration:

> "This is an Android phone. And inside it is a real Ubuntu Linux workspace."

On-screen text:

```text
ATLAS CYBERDECK
Your Cyberdeck. Anywhere.
```

---

# Scene 2 — Boot

**Time:** 0:10–0:20

Visual:

- `boot-device.png`;
- live capability checks;
- progress moving through the startup sequence.

Narration:

> "Atlas checks the device, initializes its workspace, and prepares the Linux environment."

---

# Scene 3 — Dashboard

**Time:** 0:20–0:32

Visual:

- Dashboard with Linux RUNNING;
- package count;
- RootFS storage;
- ARM64 architecture.

Narration:

> "The app tracks the real runtime state, installed packages, storage usage, and architecture."

On-screen callouts:

```text
Linux: RUNNING
ARM64
Real RootFS metrics
```

---

# Scene 4 — Atlas Shell

**Time:** 0:32–0:45

Visual:

```console
clear
neofetch
```

Show:

```text
Atlas Cyberdeck
Safety : NORMAL
Access : ENABLED
Linux  : Ubuntu 24.04 LTS
Runtime: RUNNING
```

Narration:

> "Atlas has its own command environment, diagnostics, virtual filesystem, and runtime controls."

---

# Scene 5 — Enter Ubuntu

**Time:** 0:45–1:05

Visual:

```console
linux shell
cat /etc/os-release
uname -m
python3 --version
```

Show:

```text
Ubuntu 24.04.4 LTS
aarch64
Python 3.12.3
```

Narration:

> "Then Atlas can hand you into a real Ubuntu ARM64 userspace — without rooting Android."

Hold on the phone long enough for viewers to read the output.

---

# Scene 6 — Package Management

**Time:** 1:05–1:18

Visual:

Use a concise pre-recorded or accelerated package operation.

Possible commands:

```console
apt update
```

or a safe already-validated package demonstration.

Narration:

> "Networking and Debian package tools operate inside the guest, with Atlas adding package-state health checks around sensitive operations."

Do not spend 30 seconds showing download progress.

---

# Scene 7 — Safe Mode

**Time:** 1:18–1:32

Visual:

- Safe Mode banner;
- startup blocked.

Narration:

> "And when something critical goes wrong, Atlas is designed to fail closed instead of pretending everything is fine."

On-screen:

```text
ATLAS SAFE MODE
Runtime blocked
```

---

# Scene 8 — Controlled Recovery

**Time:** 1:32–1:48

Visual:

- Recovery Mode;
- `dpkg --configure -a`;
- package health clean;
- return to NORMAL.

Narration:

> "Recovery is deliberate. The workspace is preserved, repair commands are restricted, and normal access returns only after verified recovery."

---

# Scene 9 — Why It Exists

**Time:** 1:48–2:03

Visual:

Quick montage:

- phone;
- Dashboard;
- Atlas shell;
- Ubuntu shell.

Narration:

> "Atlas is for developers, technical students, IT professionals, and anyone who wants a serious Linux workspace that can travel in a pocket."

---

# Scene 10 — Kickstarter Ask

**Time:** 2:03–2:20

Visual:

Atlas Labs hero graphic.

Narration:

> "The hard part is no longer proving the idea can work. The next phase is compatibility testing, polish, onboarding, documentation, and getting Atlas ready for real users."

Final frame:

```text
ATLAS CYBERDECK

A real Linux workstation in your pocket.
No root required.

Your Cyberdeck. Anywhere.
```

---

# Recording Notes

- Capture the real phone whenever possible.
- Prefer direct screen capture only when terminal text must be perfectly readable.
- Use both views deliberately: device view proves mobility; clean capture proves technical output.
- Keep keyboard visibility when it helps demonstrate actual interaction.
- Hide Samsung Edge handles and irrelevant notifications.
- Use the current `v0.13.0-alpha` build identity.
- Do not fake terminal output.
- Avoid stock "hacker" footage.
- Avoid green-code rain, hooded figures, or threat imagery.
- Keep transitions simple.
- Let the product be the visual effect.

---

# Audio

Recommended:

- founder voiceover;
- clean microphone;
- very light background music if used;
- no aggressive cyberpunk soundtrack that competes with narration.

The tone should be confident, technical, and grounded.

---

# Short 30–45 Second Cut

```text
0–05  Phone + Atlas
05–12  Dashboard / Linux RUNNING
12–23  linux shell / Ubuntu / aarch64 / Python
23–31  Safe Mode
31–38  Recovery
38–45  Atlas logo + campaign message
```

Core script:

> "Atlas Cyberdeck puts a real Ubuntu ARM64 workspace on Android without requiring root. It has its own shell, Linux runtime management, real package tools, and a fail-closed safety and recovery system. The core runtime works today. The next phase is turning it into a polished public product. Atlas Cyberdeck. Your Cyberdeck. Anywhere."
