# Atlas Cyberdeck Native Runtime Provenance

This directory preserves the build provenance for the native ARM64 PRoot runtime distributed with Atlas Cyberdeck.

## Termux build framework

Repository:

`https://github.com/termux/termux-packages.git`

Historical build-framework commit:

`8c10f0160ae206c29b0faade325c99ab5ed1bc99`

## Atlas build recipes

### PRoot

File:

`build-recipes/proot-atlas-build.sh`

SHA-256:

`3677AE50F60B3CDAD3FD8D1D1146953162DB10F1815660B8870049B471C68EB3`

Upstream:

- Project: PRoot
- Version: `5.1.107.92`
- License: GPL-2.0-or-later
- Source SHA-256:
  `29385d1ddb619a9c4449ab512bfd55032034b22f724ddf98fc95ff300ea32135`

### libandroid-shmem

File:

`build-recipes/libandroid-shmem-atlas-build.sh`

SHA-256:

`B3D57659278928ACA09451EA8AA8872F00B4E922846627EE526DCC4971E3E68A`

Upstream:

- Project: termux/libandroid-shmem
- Version: `0.7`
- License: BSD-3-Clause
- Source SHA-256:
  `1e5ff8459bc0a8c229dd8a94b27d119987e09ef3414331c2b5ebfff20b98e867`

### talloc

The Atlas PRoot build statically links talloc.

- Version: `2.4.3`
- Library license: LGPL-3.0-or-later
- Source SHA-256:
  `dc46c40b9f46bb34dd97fe41f548b0e8b247b77a918576733c528e83abd854dd`

## Shipped ARM64 artifacts

### PRoot

File:

`app/src/main/jniLibs/arm64-v8a/libproot_atlas.so`

SHA-256:

`bf562a87debdf108c9e6373dc93c3e59bb08ec0efaf3da0313c7420631454c67`

### PRoot loader

File:

`app/src/main/jniLibs/arm64-v8a/libproot_loader_atlas.so`

SHA-256:

`44ef39c1e1a18c09f6e4c4b5d6f8bba82d30596598bd155ec162d05c5122ff04`

These hashes match the historical Atlas build artifacts preserved in the original Termux build workspace.

## Build modifications

The Atlas build recipes modify upstream build behavior and source code for Android compatibility and Atlas runtime requirements, including:

- static linking of talloc;
- static linking of Atlas-built libandroid-shmem;
- removal of the Termux runtime rpath;
- PRoot temporary-directory handling through `PROOT_TMP_DIR`, `TMPDIR`, and `/tmp`;
- removal of Termux-specific diagnostic wording;
- libandroid-shmem temporary-path handling through `TMPDIR`.

The preserved build recipes are the authoritative record of these modifications.