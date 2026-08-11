# Changelog

All notable changes to Atlas Cyberdeck are documented in this file.

Atlas Cyberdeck is currently in alpha development. Features, architecture,
APIs, and internal package organization may change as the project evolves.

---

## v0.13.0-alpha — Foundation Milestone

Sprint 050 establishes the current Atlas Cyberdeck foundation milestone,
bringing together the terminal, virtual filesystem, command architecture,
scripting framework, plugin foundation, testing, diagnostics, and continuous
integration infrastructure.

### Added

#### Virtual File System

- Persistent virtual filesystem
- Directory navigation
- File creation
- File deletion
- File reading
- File writing
- Directory creation
- Directory deletion
- File copying
- File moving and renaming
- Filesystem searching
- Directory tree visualization
- Working-directory tracking

#### Terminal

- Linux-inspired terminal environment
- Command history
- Command history recall
- Command aliases
- Environment variable expansion
- Wildcard expansion
- Hardware keyboard Tab completion
- Multi-stage command pipelines
- Text-processing commands
- Filesystem commands
- Utility commands
- System commands

#### Command Architecture

- Centralized `CommandRegistry`
- Command metadata
- Command descriptions
- Command usage information
- Command categories
- Registry-driven help system
- Registry-driven command completion
- Centralized `CommandDispatcher`
- Common `CommandHandler` interface
- `HandlerRegistry`
- Modular command-handler architecture

Current command-handler groups include:

- Utility
- File
- Directory
- Text

#### Shell Scripting

- Atlas shell `ScriptEngine`
- Multi-command script execution
- Virtual filesystem script loading
- `.ash` script support
- `runscript` command
- Blank-line handling
- Script comment handling
- Sequential command execution through the existing terminal architecture
- Filesystem state maintained between script commands

Example:

```text
mkdir Project
cd Project
touch hello.txt
echo Atlas Cyberdeck > hello.txt
cat hello.txt
pwd
ls
```

Scripts can be executed using:

```text
runscript example.ash
```

#### Plugin Framework

- `TerminalPlugin` interface
- `PluginInfo` metadata model
- `PluginRegistry`
- Plugin registration
- Plugin initialization
- Core plugin
- Installed-plugin discovery
- `plugins` terminal command

The current plugin system establishes the architectural foundation for
future extensibility. Dynamic external plugin loading is not yet implemented.

#### System Information

- Centralized `VersionInfo`
- Atlas Cyberdeck name
- Version information
- Build information
- Release codename
- Atlas Labs attribution
- `version` command
- Updated `neofetch` information

#### Diagnostics

- Built-in `diagnostics` command
- Filesystem status reporting
- Command registry status reporting
- Handler registry status reporting
- Plugin registry status reporting
- Registered command count
- Registered handler count
- Registered plugin count
- Overall system health reporting

#### Testing

- Virtual filesystem unit tests
- Terminal architecture testing
- Script execution validation
- Command dispatcher validation
- Registry validation
- Release smoke testing

#### Continuous Integration

- GitHub Actions CI workflow
- Automated project validation
- Automated unit-test execution
- Build verification on repository changes

#### Documentation

- Expanded project README
- Architecture documentation
- Roadmap documentation
- Changelog
- Architecture Decision Records
- Engineering documentation
- Security policy
- Contributing guide
- Code of conduct
- Style documentation

---

### Changed

#### Terminal Architecture

Refactored terminal command processing into a modular architecture.

Previous architecture:

```text
TerminalCommandProcessor
        │
        ├── UtilityCommands
        ├── FileCommands
        ├── DirectoryCommands
        └── TextCommands
```

Current architecture:

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

This reduces coupling between the terminal processor and individual
command implementations.

#### Command Dispatch

- Moved command-routing responsibility into `CommandDispatcher`
- Replaced direct command-group dispatch with handler-based dispatch
- Introduced a common `CommandHandler` contract
- Moved handler discovery into `HandlerRegistry`
- Reduced responsibilities inside `TerminalCommandProcessor`

#### Command Registry

- Centralized terminal command metadata
- Added command descriptions
- Added usage information
- Added command categories
- Connected help functionality to registry metadata
- Connected command completion to centralized command information

#### Aliases

Improved command alias handling.

Current aliases include:

```text
ll  -> ls
dir -> ls
cls -> clear
md  -> mkdir
rd  -> rmdir
```

#### Script Execution

Moved script execution from a hardcoded demonstration into the virtual
filesystem.

Atlas Cyberdeck can now create, store, read, and execute `.ash` scripts
inside its virtual environment.

#### Development Workflow

Standardized the development cycle around:

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

### Current Terminal Commands

The command registry currently includes commands such as:

```text
cat
cd
clear
cp
diagnostics
echo
find
grep
head
help
history
ls
mkdir
mv
neofetch
plugins
pwd
rm
rmdir
runscript
sort
status
tail
touch
tree
uniq
version
wc
whoami
```

---

### Architecture

Major Atlas Cyberdeck terminal services now include:

```text
Terminal Services
│
├── Command Registry
├── Command Dispatcher
├── Handler Registry
├── Command History
├── Alias Resolution
├── Variable Expansion
├── Wildcard Expansion
├── Command Completion
├── Pipe Engine
├── Script Engine
└── Plugin Registry
```

The architecture is designed around:

- Separation of concerns
- Single responsibility
- Delegation
- Unidirectional data flow
- Testability
- Maintainability
- Extensibility
- Portability

---

### Validation

The Foundation milestone was validated using the automated unit-test suite:

```bash
./gradlew testDebugUnitTest
```

Result:

```text
BUILD SUCCESSFUL
```

The debug application build was also validated using:

```bash
./gradlew assembleDebug
```

Result:

```text
BUILD SUCCESSFUL
```

Terminal smoke testing verified:

```text
version
diagnostics
plugins
help
pwd
ls
neofetch
runscript demo.ash
```

All milestone validation tests passed before release preparation.

---

## Earlier Development

Atlas Cyberdeck evolved incrementally through a series of early alpha
releases and engineering sprints.

The following entries document releases that were explicitly recorded in
the project's earlier changelog history.

---

## v0.5.0

### Added

- `LinuxDistribution` model
- `LinuxInstallation` model
- `LinuxRepository`
- Linux domain layer

---

## v0.4.1

### Fixed

- Dashboard state management
- Stable dashboard build

---

## v0.3.0

### Added

- Boot sequence
- Boot navigation

---

## v0.2.0

### Added

- Navigation architecture

---

## v0.1.0

### Added

- Initial Atlas Cyberdeck dashboard

---

## Development Status

Atlas Cyberdeck remains under active alpha development.

The `v0.13.0-alpha` Foundation milestone establishes the architectural
base for continued work on:

- Expanded shell scripting
- Plugin architecture
- Networking tools
- SSH
- Git integration
- Rootless Linux runtime
- Package management
- Additional filesystem capabilities
- Expanded automated testing
- Desktop support
- Dedicated cyberdeck hardware

---

## Atlas Labs

Atlas Cyberdeck is developed by Atlas Labs with a focus on clean
architecture, maintainable software, continuous improvement, and
long-term extensibility.

> *"Maybe not breaking free from the Matrix—but we are writing our own code instead of living inside someone else's. That is a pretty good way to spend our time."*