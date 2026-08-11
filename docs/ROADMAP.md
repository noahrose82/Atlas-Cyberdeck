# Atlas Cyberdeck Roadmap

> **Portable Linux-Inspired Workspace • Cybersecurity Toolkit • Android • Kotlin**

This roadmap outlines the planned evolution of Atlas Cyberdeck from its current alpha foundation into a modular portable computing, development, and cybersecurity platform.

Atlas Cyberdeck is developed incrementally through focused engineering sprints. Features and priorities may evolve as the architecture matures.

---

# Current Milestone

## v0.13.0-alpha — Foundation

**Sprint 050**

The Foundation milestone establishes the core architecture required for future Atlas Cyberdeck development.

### Core Platform

- [x] Android application foundation
- [x] Kotlin architecture
- [x] Jetpack Compose interface
- [x] Material 3
- [x] Multi-screen navigation
- [x] Boot experience
- [x] Dashboard
- [x] Terminal interface
- [x] Filesystem interface
- [x] Linux workspace foundation

---

## Terminal Foundation

- [x] Linux-inspired command interface
- [x] Command parsing
- [x] Command history
- [x] History recall
- [x] Command aliases
- [x] Environment variable expansion
- [x] Wildcard expansion
- [x] Hardware keyboard Tab completion
- [x] Command pipelines
- [x] Multi-stage pipelines
- [x] Text-processing commands
- [x] Filesystem commands
- [x] Utility commands
- [x] System commands

---

## Command Architecture

- [x] Centralized `CommandRegistry`
- [x] Command metadata
- [x] Command descriptions
- [x] Command usage information
- [x] Command categories
- [x] Registry-driven help
- [x] Registry-driven command completion
- [x] `CommandHandler` interface
- [x] `HandlerRegistry`
- [x] Centralized `CommandDispatcher`
- [x] Modular command handlers
- [x] Reduced command-processor coupling

Current handler groups:

```text
Utility
File
Directory
Text
```

Current command-processing architecture:

```text
User Input
    │
    ▼
Alias Resolution
    │
    ▼
Variable Expansion
    │
    ▼
Wildcard Expansion
    │
    ▼
Pipe Engine
    │
    ▼
Command Dispatcher
    │
    ▼
Handler Registry
    │
    ▼
Command Handlers
```

---

## Virtual File System

- [x] Virtual filesystem foundation
- [x] Persistent filesystem state
- [x] Working-directory tracking
- [x] Directory navigation
- [x] Directory creation
- [x] Directory deletion
- [x] File creation
- [x] File deletion
- [x] File reading
- [x] File writing
- [x] File copying
- [x] File moving
- [x] File renaming
- [x] Filesystem searching
- [x] Directory tree visualization
- [x] Relative path operations
- [x] Filesystem unit tests

---

## Shell Scripting

- [x] `ScriptEngine`
- [x] Multi-command execution
- [x] Virtual filesystem script loading
- [x] `.ash` script format
- [x] `runscript` command
- [x] Blank-line handling
- [x] Script comments
- [x] Sequential command execution
- [x] Filesystem state preservation during script execution

Example:

```text
runscript example.ash
```

Future scripting work will expand this foundation with additional shell-language capabilities.

---

## Plugin Architecture

- [x] `TerminalPlugin` contract
- [x] `PluginInfo`
- [x] `PluginRegistry`
- [x] Plugin registration
- [x] Plugin initialization
- [x] Core plugin
- [x] `plugins` command
- [x] Installed-plugin discovery

Current built-in plugin:

```text
Core
```

Dynamic external plugin loading is planned for a future milestone.

---

## System Services

- [x] Centralized `VersionInfo`
- [x] Version command
- [x] Build information
- [x] Release codename
- [x] System information reporting
- [x] `neofetch`
- [x] `status`
- [x] Built-in diagnostics
- [x] Registry diagnostics
- [x] Plugin diagnostics
- [x] Handler diagnostics
- [x] Overall system health reporting

---

## Testing and Quality

- [x] Unit-test infrastructure
- [x] Filesystem tests
- [x] Terminal component testing
- [x] Script execution validation
- [x] Dispatcher validation
- [x] Registry validation
- [x] Debug build validation
- [x] Terminal smoke testing

Release validation commands:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

---

## Continuous Integration

- [x] GitHub Actions workflow
- [x] Automated unit-test execution
- [x] Automated build validation
- [x] CI validation on repository changes
- [x] Incremental commit workflow

Development workflow:

```text
Design
  ↓
Implement
  ↓
Build
  ↓
Test
  ↓
Commit
  ↓
Continuous Integration
```

---

# Next Milestone

## Terminal and Scripting Expansion

The next stage will build on the Sprint 050 architecture instead of expanding `TerminalCommandProcessor` directly.

### Shell Improvements

- [ ] Command chaining
- [ ] Conditional execution
- [ ] Improved redirection
- [ ] Append redirection
- [ ] Additional environment variables
- [ ] Improved path expansion
- [ ] Improved quoting
- [ ] Escaped characters
- [ ] Command arguments and option parsing
- [ ] Improved shell error reporting

### Atlas Shell

Expand `.ash` scripting with:

- [ ] Script arguments
- [ ] Script variables
- [ ] Conditional statements
- [ ] Basic loops
- [ ] Exit codes
- [ ] Script error handling
- [ ] Nested script execution
- [ ] Script permissions
- [ ] Additional comments/documentation support

The long-term goal is for Atlas shell scripts to provide lightweight automation inside the Atlas Cyberdeck environment.

---

# Plugin Expansion

The current plugin framework establishes the contract and registry foundation.

Future work includes:

- [ ] Plugin lifecycle management
- [ ] Plugin enable/disable state
- [ ] Plugin command registration
- [ ] Plugin-provided handlers
- [ ] Plugin-provided services
- [ ] Plugin dependency metadata
- [ ] Plugin compatibility checks
- [ ] Plugin version validation
- [ ] Plugin error isolation
- [ ] Dynamic plugin discovery
- [ ] External plugin loading

Potential future modules include:

```text
Core
Scripting
Networking
SSH
Git
Package Manager
Development Tools
Cybersecurity Tools
```

---

# Linux Runtime

Atlas Cyberdeck is intended to evolve beyond a simulated Linux-inspired workspace.

Planned Linux capabilities include:

- [ ] Rootless Linux runtime research
- [ ] Linux distribution management
- [ ] Distribution installation
- [ ] Distribution lifecycle management
- [ ] Linux process execution
- [ ] Linux command integration
- [ ] Linux filesystem integration
- [ ] Terminal/runtime bridging
- [ ] Package installation
- [ ] Multiple Linux distributions

Potential distributions may include:

- Debian
- Ubuntu
- Kali Linux

The final implementation will depend on Android platform limitations, security requirements, and runtime architecture.

---

# SSH and Remote Administration

Planned remote-management capabilities include:

- [ ] SSH client foundation
- [ ] SSH connection profiles
- [ ] Password authentication
- [ ] SSH key authentication
- [ ] Known-host management
- [ ] Remote shell sessions
- [ ] Session persistence
- [ ] Secure credential storage
- [ ] SFTP integration
- [ ] Remote file management

SSH functionality will be implemented with security and credential isolation as primary design requirements.

---

# Git Integration

Planned Git functionality includes:

- [ ] Repository initialization
- [ ] Repository cloning
- [ ] Repository status
- [ ] Staging
- [ ] Commits
- [ ] Branch management
- [ ] Remote repositories
- [ ] Pull
- [ ] Push
- [ ] Repository history
- [ ] Authentication management
- [ ] Git terminal commands
- [ ] Git user interface

---

# Networking Tools

Future networking capabilities may include:

- [ ] Network interface information
- [ ] IP configuration
- [ ] DNS tools
- [ ] Ping
- [ ] Traceroute
- [ ] Connection testing
- [ ] Port information
- [ ] Network diagnostics
- [ ] HTTP utilities
- [ ] Secure network tooling

Cybersecurity-related functionality will be designed for legitimate administration, education, testing, and authorized security work.

---

# Package Management

A future Atlas package-management layer may provide:

- [ ] Package metadata
- [ ] Package registry
- [ ] Package search
- [ ] Package installation
- [ ] Package removal
- [ ] Package updates
- [ ] Dependency resolution
- [ ] Version management
- [ ] Plugin/package integration

---

# Filesystem Expansion

Future virtual filesystem work includes:

- [ ] File metadata
- [ ] Permissions model
- [ ] Ownership model
- [ ] Hidden files
- [ ] Symbolic-link research
- [ ] Improved path normalization
- [ ] Import from Android storage
- [ ] Export to Android storage
- [ ] Archive support
- [ ] Filesystem backup
- [ ] Filesystem restore
- [ ] Storage quotas
- [ ] Filesystem integrity checks

---

# User Interface Expansion

Future interface development includes:

- [ ] Improved terminal customization
- [ ] Terminal themes
- [ ] Configurable fonts
- [ ] Command palette
- [ ] Improved file manager
- [ ] File editor
- [ ] Script editor
- [ ] Plugin manager
- [ ] SSH connection manager
- [ ] Git interface
- [ ] Linux distribution manager
- [ ] System diagnostics dashboard
- [ ] Settings expansion
- [ ] Tablet optimization
- [ ] Chromebook optimization
- [ ] Landscape workspace improvements
- [ ] Keyboard-first workflows

---

# Security

Security will remain a core architectural requirement.

Planned work includes:

- [ ] Secure credential storage
- [ ] Secrets management
- [ ] SSH key protection
- [ ] Plugin isolation
- [ ] Script execution controls
- [ ] Input validation improvements
- [ ] Filesystem permission model
- [ ] Security-focused testing
- [ ] Dependency scanning
- [ ] Static analysis
- [ ] Security review process
- [ ] Threat modeling
- [ ] Release security checks

---

# Testing Expansion

Planned quality improvements include:

- [ ] Increased unit-test coverage
- [ ] Command-handler tests
- [ ] Command registry tests
- [ ] Handler registry tests
- [ ] Plugin registry tests
- [ ] Script-engine tests
- [ ] Pipeline tests
- [ ] Alias tests
- [ ] Variable-expansion tests
- [ ] Wildcard-expansion tests
- [ ] Integration tests
- [ ] UI tests
- [ ] Regression tests

---

# Continuous Integration and Delivery

Future automation includes:

- [ ] Static-analysis checks
- [ ] Code-style validation
- [ ] Dependency checks
- [ ] Automated release builds
- [ ] Release artifact generation
- [ ] Version consistency checks
- [ ] Automated changelog validation
- [ ] Release signing research

---

# Desktop Edition

Long-term plans include exploring an Atlas Cyberdeck desktop application.

Potential targets include:

- [ ] Windows
- [ ] Linux
- [ ] macOS

The desktop edition should share architecture and concepts with the Android application where practical.

Potential capabilities include:

- Terminal
- Filesystem
- Scripting
- Plugins
- SSH
- Git
- Development tools
- Cybersecurity tools
- Remote administration

---

# Dedicated Cyberdeck Hardware

A long-term objective is to explore dedicated hardware designed specifically for Atlas Cyberdeck.

Potential hardware concepts include:

- Compact cyberdeck form factor
- Integrated keyboard
- Touch display
- Portable battery
- USB expansion
- External storage
- Ethernet
- Wi-Fi
- Bluetooth
- Hardware status indicators
- Modular peripherals

This remains a long-term research and development objective.

---

# Path to v1.0.0

Atlas Cyberdeck will not reach `v1.0.0` simply because a predetermined number of sprints has been completed.

The stable release should represent a platform that is:

- Reliable
- Tested
- Documented
- Maintainable
- Secure
- Extensible
- Useful for real workflows

Major objectives before `v1.0.0` include:

- [ ] Stable terminal architecture
- [ ] Stable virtual filesystem
- [ ] Mature shell scripting
- [ ] Mature plugin architecture
- [ ] SSH support
- [ ] Git integration
- [ ] Linux runtime integration
- [ ] Expanded automated testing
- [ ] Security hardening
- [ ] Performance testing
- [ ] User documentation
- [ ] Developer documentation
- [ ] Release process
- [ ] Stable application upgrade path

---

# Long-Term Vision

Atlas Cyberdeck is intended to evolve into more than a terminal emulator.

The long-term vision is a portable computing platform that combines:

```text
Android
   │
   ├── Atlas Interface
   │
   ├── Terminal
   │
   ├── Virtual File System
   │
   ├── Atlas Shell
   │
   ├── Plugin Platform
   │
   ├── Linux Runtime
   │
   ├── SSH
   │
   ├── Git
   │
   ├── Networking
   │
   └── Cybersecurity Tools
   │
   ▼
Portable Cyberdeck Platform
```

Atlas Cyberdeck should remain modular enough that individual capabilities can evolve without requiring the entire application to be redesigned.

---

# Development Principles

Atlas Cyberdeck development follows several core principles:

1. Build foundations before features.
2. Prefer modular components to monolithic classes.
3. Keep responsibilities clearly separated.
4. Test important behavior.
5. Refactor when architecture begins creating unnecessary coupling.
6. Keep documentation synchronized with implementation.
7. Validate changes before release.
8. Design new functionality with long-term extensibility in mind.
9. Avoid claiming functionality that has not actually been implemented.
10. Treat security as an architectural requirement rather than an afterthought.

---

# Current Status

```text
Atlas Cyberdeck
Version  : v0.13.0-alpha
Sprint   : 050
Codename : Foundation
Status   : ACTIVE DEVELOPMENT
```

### Foundation Milestone

- [x] Terminal
- [x] Virtual File System
- [x] Command Registry
- [x] Command Dispatcher
- [x] Handler Registry
- [x] Command History
- [x] Alias Resolution
- [x] Variable Expansion
- [x] Wildcard Expansion
- [x] Command Completion
- [x] Pipe Engine
- [x] Script Engine
- [x] `.ash` Scripts
- [x] Plugin Framework Foundation
- [x] Version Service
- [x] Diagnostics
- [x] Unit Testing
- [x] Continuous Integration
- [x] Architecture Documentation

---

## Atlas Labs

Atlas Cyberdeck is developed by **Atlas Labs** with a focus on clean architecture, maintainable software, continuous improvement, security, and long-term extensibility.

> *"Maybe not breaking free from the Matrix—but we are writing our own code instead of living inside someone else's. That is a pretty good way to spend our time."*

---

**Atlas Cyberdeck — v0.13.0-alpha**

**Sprint 050 — Foundation Milestone**