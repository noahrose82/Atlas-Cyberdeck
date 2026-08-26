# Atlas Cyberdeck — Runtime Networking

**Document type:** Engineering  
**Status:** Current  
**Release baseline:** `v0.13.0-alpha`

---

## Purpose

This document describes how Atlas Cyberdeck gives the Ubuntu guest functional network name resolution using the Android device's active network configuration.

---

## Networking Model

PRoot does not create a separate virtual network adapter.

The Ubuntu guest operates through the Android application's available networking context.

Atlas is responsible for ensuring the guest resolver configuration matches the active device environment.

---

## DNS Synchronization Flow

```mermaid
flowchart LR
    ANDROID["Android Connectivity"] --> DNS["Active DNS Servers"]
    DNS --> SYNC["Atlas DNS Sync"]
    SYNC --> RESOLV["Ubuntu /etc/resolv.conf"]
    RESOLV --> GUEST["Ubuntu Networking"]
```

---

## Why Synchronize DNS

Hard-coded public DNS servers are undesirable because they may:

- fail on captive or managed networks;
- ignore VPN DNS;
- ignore enterprise/local DNS;
- break split-horizon name resolution;
- diverge from the device's current network.

Atlas therefore reads active Android DNS information and writes the relevant resolver configuration into the Ubuntu guest.

---

## Guest Resolver

Target:

```text
/etc/resolv.conf
```

The generated file contains resolver entries based on current Android network information.

---

## Runtime Timing

DNS synchronization should occur when the runtime is prepared or started so the guest does not rely on stale network settings from an earlier connection.

---

## Network Changes

A future enhancement may react more dynamically to network transitions while Linux remains running.

Current architecture should keep DNS synchronization isolated enough that refresh behavior can evolve without changing the rest of the runtime stack.

---

## Package Management Dependency

Functional DNS is required for workflows such as:

```text
apt update
apt install
```

Therefore runtime networking is part of Linux readiness, not merely a convenience feature.

---

## Failure Behavior

DNS synchronization failure should be surfaced diagnostically.

Atlas should avoid silently claiming full network health if the guest resolver could not be prepared.

---

## Security Considerations

Atlas should follow the device's active DNS environment rather than bypassing it with arbitrary external resolvers.

The guest remains subject to Android application networking permissions and the device's current network path.

---

## Validation

Validated workflows include:

```text
guest DNS resolution
apt update
package download
Android DNS → guest resolv.conf
```

---

## Related Documents

- `Linux-Runtime.md`
- `Package-Management.md`
- `Ubuntu-RootFS.md`
