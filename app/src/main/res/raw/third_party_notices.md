# Atlas Cyberdeck — Third-Party Notices

This document records third-party software used by the Atlas Cyberdeck Android release.

The Gradle inventory below was generated from the resolved `releaseRuntimeClasspath`.
Native/runtime components that are not represented in Gradle are documented separately.

Atlas Cyberdeck's own source code and assets remain governed by the project's top-level `LICENSE`.
Third-party software remains governed by its respective upstream license.

## Standard license texts distributed with this notice

- `licenses/APACHE-2.0.txt` — Apache License 2.0
- `licenses/GPL-2.0.txt` — GNU General Public License version 2
- `licenses/GPL-3.0.txt` — GNU General Public License version 3
- `licenses/LGPL-3.0.txt` — GNU Lesser General Public License version 3
- `licenses/LIBANDROID-SHMEM-BSD-3-CLAUSE.txt` — exact BSD-3-Clause license text from libandroid-shmem 0.7
- `licenses/SLF4J-MIT.txt` — exact MIT license text extracted from the resolved SLF4J 2.0.18 JAR
- `licenses/LIBVTERM-MIT.txt` — libvterm MIT license notice
- `licenses/COMMONS-CODEC-NOTICE.txt` — Apache Commons Codec NOTICE
- `licenses/COMMONS-COMPRESS-NOTICE.txt` — Apache Commons Compress NOTICE
- `licenses/COMMONS-IO-NOTICE.txt` — Apache Commons IO NOTICE
- `licenses/COMMONS-LANG3-NOTICE.txt` — Apache Commons Lang NOTICE

The exact MIT notices for SLF4J and libvterm are also reproduced later in this document so they remain visible in the in-app licenses screen.

---

## AndroidX / Jetpack / Jetpack Compose

**License:** Apache-2.0

Atlas Cyberdeck uses AndroidX libraries including Activity, Core, Lifecycle, Navigation,
SavedState, AppCompat, Fragment, Window, Compose UI, Compose Runtime, Compose Foundation,
Compose Animation, and Material 3.

Upstream:

- https://developer.android.com/jetpack/androidx
- https://android.googlesource.com/platform/frameworks/support/

The full Apache-2.0 text is included in `licenses/APACHE-2.0.txt`.

## Material Components for Android

**License:** Apache-2.0

Resolved dependency family:

- `com.google.android.material`

Upstream:

- https://github.com/material-components/material-components-android

## Kotlin and JetBrains runtime libraries

**License:** Apache-2.0

Resolved families include:

- Kotlin standard library
- Kotlin parcelize runtime
- JetBrains annotations
- kotlinx.coroutines
- kotlinx.serialization
- kotlinx-io

Upstream:

- https://github.com/JetBrains/kotlin
- https://github.com/JetBrains/java-annotations
- https://github.com/Kotlin/kotlinx.coroutines
- https://github.com/Kotlin/kotlinx.serialization
- https://github.com/Kotlin/kotlinx-io

## Ktor

**Version used:** 3.5.2
**License:** Apache-2.0

Atlas Cyberdeck uses Ktor client, OkHttp engine integration, content negotiation,
serialization, HTTP, I/O, networking, events, WebSocket serialization, and related modules.

Upstream:

- https://github.com/ktorio/ktor

## Square OkHttp / Okio

**Resolved versions:**

- OkHttp 5.3.2
- Okio 3.17.0

**License:** Apache-2.0

Upstream:

- https://github.com/square/okhttp
- https://github.com/square/okio

## Apache Commons

**License:** Apache-2.0

Resolved components:

- Apache Commons Compress 1.28.0
- Apache Commons Codec 1.19.0
- Apache Commons IO 2.20.0
- Apache Commons Lang 3 3.18.0

Upstream:

- https://commons.apache.org/proper/commons-compress/
- https://commons.apache.org/proper/commons-codec/
- https://commons.apache.org/proper/commons-io/
- https://commons.apache.org/proper/commons-lang/

The exact `NOTICE.txt` files from the resolved Apache Commons artifacts are preserved
under `licenses/` and reproduced in Appendix D below.

## Google Guava ListenableFuture / Error Prone annotations

**License:** Apache-2.0

Resolved families:

- `com.google.guava:listenablefuture`
- `com.google.errorprone`

Upstream:

- https://github.com/google/guava
- https://github.com/google/error-prone

## JSpecify

**Version used:** 1.0.0
**License:** Apache-2.0

Upstream:

- https://github.com/jspecify/jspecify

## SLF4J API

**Version used:** 2.0.18
**License:** MIT

Upstream license:

- https://github.com/qos-ch/slf4j/blob/master/LICENSE.txt

The exact SLF4J 2.0.18 MIT notice is included in `licenses/SLF4J-MIT.txt` and reproduced in Appendix B.

## ConnectBot termlib

**Version used:** 0.1.0
**License:** Apache-2.0

Atlas Cyberdeck uses ConnectBot's terminal emulator library for interactive Ubuntu
terminal rendering.

Upstream:

- https://github.com/connectbot/termlib

### Embedded libvterm

ConnectBot termlib documents that it uses **libvterm by Paul Evans** under the **MIT License**.

Upstream information:

- https://github.com/connectbot/termlib
- https://www.leonerd.org.uk/code/libvterm/

The libvterm MIT notice is included in `licenses/LIBVTERM-MIT.txt` and reproduced in Appendix C.

---

## PRoot runtime and external loader

**Upstream project:** `termux/proot`
**Version:** 5.1.107.92
**License:** GPL-2.0-or-later

Termux package metadata historically identifies the package as GPL-2.0. The upstream
PRoot source for this release permits redistribution under GNU GPL version 2 or, at the
recipient's option, any later version.

Termux package metadata:

- https://github.com/termux/termux-packages/blob/master/packages/proot/build.sh

Source archive SHA-256:

`29385d1ddb619a9c4449ab512bfd55032034b22f724ddf98fc95ff300ea32135`

Atlas-bundled ARM64 PRoot binary:

- File: `libproot_atlas.so`
- SHA-256: `bf562a87debdf108c9e6373dc93c3e59bb08ec0efaf3da0313c7420631454c67`

Atlas-bundled external loader:

- File: `libproot_loader_atlas.so`
- SHA-256: `44ef39c1e1a18c09f6e4c4b5d6f8bba82d30596598bd155ec162d05c5122ff04`

Atlas preserves both GNU GPL version 2 and GNU GPL version 3 license texts under:

- `licenses/GPL-2.0.txt`
- `licenses/GPL-3.0.txt`

The exact Atlas native-runtime build provenance is preserved under:

`docs/native-runtime/`

The Atlas PRoot build recipe is preserved at:

`docs/native-runtime/build-recipes/proot-atlas-build.sh`

Build-recipe SHA-256:

`3677AE50F60B3CDAD3FD8D1D1146953162DB10F1815660B8870049B471C68EB3`

The Atlas build modifies upstream PRoot source/build behavior for Android runtime
compatibility, including temporary-directory handling and removal of Termux-specific
runtime assumptions.

Corresponding source and the Atlas build modifications for the exact distributed native
runtime should be preserved and made available with the public release.

## libandroid-shmem

**Upstream project:** `termux/libandroid-shmem`
**Version:** 0.7
**License:** BSD-3-Clause

Upstream:

- https://github.com/termux/libandroid-shmem

Source archive SHA-256:

`1e5ff8459bc0a8c229dd8a94b27d119987e09ef3414331c2b5ebfff20b98e867`

Atlas statically links an Atlas-built copy of libandroid-shmem into the native PRoot
runtime.

The Atlas build modifies upstream `shmem.c` temporary-path behavior so the runtime
uses `TMPDIR` instead of relying on a fixed Android/Termux temporary path. The build
also performs path-length and error handling appropriate for the Atlas runtime.

The exact Atlas build recipe is preserved at:

`docs/native-runtime/build-recipes/libandroid-shmem-atlas-build.sh`

Build-recipe SHA-256:

`B3D57659278928ACA09451EA8AA8872F00B4E922846627EE526DCC4971E3E68A`

The exact BSD-3-Clause license text from libandroid-shmem 0.7 is preserved at:

`licenses/LIBANDROID-SHMEM-BSD-3-CLAUSE.txt`

Preserved license-file SHA-256:

`0dd43abe17196b0224cb0b159819af798cd508a82a21e5870126fff4e899a7eb`

## talloc

**Upstream project:** Samba talloc
**Version:** 2.4.3
**Library license:** LGPL-3.0-or-later

Upstream:

- https://talloc.samba.org/
- https://www.samba.org/ftp/talloc/

Source archive:

`talloc-2.4.3.tar.gz`

Source archive SHA-256:

`dc46c40b9f46bb34dd97fe41f548b0e8b247b77a918576733c528e83abd854dd`

The Atlas PRoot build statically links talloc 2.4.3.

The GNU Lesser General Public License version 3 text extracted from the exact
talloc 2.4.3 source archive is preserved at:

`licenses/LGPL-3.0.txt`

Because talloc is statically linked into the Atlas native PRoot runtime, Atlas preserves
the exact talloc source version/hash and the license text together with the native-runtime
build provenance.

---

## Ubuntu Base

Atlas Cyberdeck can provision:

- Ubuntu Base 24.04.4 LTS
- Noble Numbat
- ARM64
- Archive: `ubuntu-base-24.04.4-base-arm64.tar.gz`
- SHA-256: `04207713ece899c3740823d33690441ad3a7f0ded1101aca744e2b0f37ac7ff2`

Download source:

- https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/ubuntu-base-24.04.4-base-arm64.tar.gz

Ubuntu Base is downloaded separately rather than being embedded in the Atlas Cyberdeck APK.
Ubuntu is a collection of packages under multiple licenses; there is not one single
license covering the entire root filesystem. Package copyright/license information is
retained inside the installed Ubuntu filesystem, typically under
`/usr/share/doc/<package>/copyright`.

Ubuntu names and trademarks remain the property of their respective owners.

---

# Appendix A — Resolved release runtime coordinates

The following coordinates were extracted from the Gradle
`releaseRuntimeClasspath` report supplied for this release build.

- `androidx.activity:activity:1.8.2` — Apache-2.0
- `androidx.activity:activity-compose:1.8.2` — Apache-2.0
- `androidx.activity:activity-ktx:1.8.2` — Apache-2.0
- `androidx.annotation:annotation:1.9.1` — Apache-2.0
- `androidx.annotation:annotation-experimental:1.4.1` — Apache-2.0
- `androidx.annotation:annotation-jvm:1.9.1` — Apache-2.0
- `androidx.appcompat:appcompat:1.7.1` — Apache-2.0
- `androidx.appcompat:appcompat-resources:1.7.1` — Apache-2.0
- `androidx.arch.core:core-common:2.2.0` — Apache-2.0
- `androidx.arch.core:core-runtime:2.2.0` — Apache-2.0
- `androidx.autofill:autofill:1.0.0` — Apache-2.0
- `androidx.cardview:cardview:1.0.0` — Apache-2.0
- `androidx.collection:collection:1.5.0` — Apache-2.0
- `androidx.collection:collection-jvm:1.5.0` — Apache-2.0
- `androidx.collection:collection-ktx:1.5.0` — Apache-2.0
- `androidx.compose:compose-bom:2026.05.01` — Apache-2.0
- `androidx.compose.animation:animation:1.11.2` — Apache-2.0
- `androidx.compose.animation:animation-android:1.11.2` — Apache-2.0
- `androidx.compose.animation:animation-core:1.11.2` — Apache-2.0
- `androidx.compose.animation:animation-core-android:1.11.2` — Apache-2.0
- `androidx.compose.foundation:foundation:1.11.2` — Apache-2.0
- `androidx.compose.foundation:foundation-android:1.11.2` — Apache-2.0
- `androidx.compose.foundation:foundation-layout:1.11.2` — Apache-2.0
- `androidx.compose.foundation:foundation-layout-android:1.11.2` — Apache-2.0
- `androidx.compose.material:material-ripple:1.11.2` — Apache-2.0
- `androidx.compose.material:material-ripple-android:1.11.2` — Apache-2.0
- `androidx.compose.material3:material3:1.4.0` — Apache-2.0
- `androidx.compose.material3:material3-android:1.4.0` — Apache-2.0
- `androidx.compose.runtime:runtime:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-android:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-annotation:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-annotation-android:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-retain:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-retain-android:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-saveable:1.11.2` — Apache-2.0
- `androidx.compose.runtime:runtime-saveable-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-geometry:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-geometry-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-graphics:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-graphics-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-text:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-text-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-tooling-preview:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-tooling-preview-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-unit:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-unit-android:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-util:1.11.2` — Apache-2.0
- `androidx.compose.ui:ui-util-android:1.11.2` — Apache-2.0
- `androidx.concurrent:concurrent-futures:1.1.0` — Apache-2.0
- `androidx.constraintlayout:constraintlayout:2.2.1` — Apache-2.0
- `androidx.constraintlayout:constraintlayout-core:1.1.1` — Apache-2.0
- `androidx.coordinatorlayout:coordinatorlayout:1.1.0` — Apache-2.0
- `androidx.core:core:1.18.0` — Apache-2.0
- `androidx.core:core-ktx:1.18.0` — Apache-2.0
- `androidx.core:core-viewtree:1.0.0` — Apache-2.0
- `androidx.cursoradapter:cursoradapter:1.0.0` — Apache-2.0
- `androidx.customview:customview:1.2.0` — Apache-2.0
- `androidx.customview:customview-poolingcontainer:1.0.0` — Apache-2.0
- `androidx.drawerlayout:drawerlayout:1.1.1` — Apache-2.0
- `androidx.dynamicanimation:dynamicanimation:1.1.0` — Apache-2.0
- `androidx.emoji2:emoji2:1.4.0` — Apache-2.0
- `androidx.emoji2:emoji2-views-helper:1.4.0` — Apache-2.0
- `androidx.fragment:fragment:1.5.4` — Apache-2.0
- `androidx.graphics:graphics-path:1.0.1` — Apache-2.0
- `androidx.graphics:graphics-shapes:1.0.1` — Apache-2.0
- `androidx.graphics:graphics-shapes-android:1.0.1` — Apache-2.0
- `androidx.interpolator:interpolator:1.0.0` — Apache-2.0
- `androidx.lifecycle:lifecycle-common:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-common-java8:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-common-jvm:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-livedata:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-livedata-core:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-livedata-core-ktx:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-process:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-runtime:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-runtime-android:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-runtime-compose:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-runtime-compose-android:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-runtime-ktx:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-runtime-ktx-android:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel-android:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel-compose-android:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel-savedstate:2.9.4` — Apache-2.0
- `androidx.lifecycle:lifecycle-viewmodel-savedstate-android:2.9.4` — Apache-2.0
- `androidx.loader:loader:1.0.0` — Apache-2.0
- `androidx.navigation:navigation-common:2.9.4` — Apache-2.0
- `androidx.navigation:navigation-common-android:2.9.4` — Apache-2.0
- `androidx.navigation:navigation-compose:2.9.4` — Apache-2.0
- `androidx.navigation:navigation-compose-android:2.9.4` — Apache-2.0
- `androidx.navigation:navigation-runtime:2.9.4` — Apache-2.0
- `androidx.navigation:navigation-runtime-android:2.9.4` — Apache-2.0
- `androidx.profileinstaller:profileinstaller:1.4.0` — Apache-2.0
- `androidx.recyclerview:recyclerview:1.2.1` — Apache-2.0
- `androidx.resourceinspection:resourceinspection-annotation:1.0.1` — Apache-2.0
- `androidx.savedstate:savedstate:1.3.2` — Apache-2.0
- `androidx.savedstate:savedstate-android:1.3.2` — Apache-2.0
- `androidx.savedstate:savedstate-compose:1.3.2` — Apache-2.0
- `androidx.savedstate:savedstate-compose-android:1.3.2` — Apache-2.0
- `androidx.savedstate:savedstate-ktx:1.3.2` — Apache-2.0
- `androidx.startup:startup-runtime:1.2.0` — Apache-2.0
- `androidx.tracing:tracing:1.2.0` — Apache-2.0
- `androidx.transition:transition:1.6.0` — Apache-2.0
- `androidx.vectordrawable:vectordrawable:1.1.0` — Apache-2.0
- `androidx.vectordrawable:vectordrawable-animated:1.1.0` — Apache-2.0
- `androidx.versionedparcelable:versionedparcelable:1.1.1` — Apache-2.0
- `androidx.viewpager:viewpager:1.0.0` — Apache-2.0
- `androidx.viewpager2:viewpager2:1.0.0` — Apache-2.0
- `androidx.window:window:1.5.0` — Apache-2.0
- `androidx.window:window-core:1.5.0` — Apache-2.0
- `androidx.window:window-core-android:1.5.0` — Apache-2.0
- `com.google.android.material:material:1.14.0` — Apache-2.0
- `com.google.errorprone:error_prone_annotations:2.15.0` — Apache-2.0
- `com.google.guava:listenablefuture:1.0` — Apache-2.0
- `com.squareup.okhttp3:okhttp:5.3.2` — Apache-2.0
- `com.squareup.okhttp3:okhttp-android:5.3.2` — Apache-2.0
- `com.squareup.okio:okio:3.17.0` — Apache-2.0
- `com.squareup.okio:okio-jvm:3.17.0` — Apache-2.0
- `commons-codec:commons-codec:1.19.0` — Apache-2.0
- `commons-io:commons-io:2.20.0` — Apache-2.0
- `io.ktor:ktor-client-content-negotiation:3.5.2` — Apache-2.0
- `io.ktor:ktor-client-content-negotiation-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-client-core:3.5.2` — Apache-2.0
- `io.ktor:ktor-client-core-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-client-okhttp:3.5.2` — Apache-2.0
- `io.ktor:ktor-client-okhttp-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-events:3.5.2` — Apache-2.0
- `io.ktor:ktor-events-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-http:3.5.2` — Apache-2.0
- `io.ktor:ktor-http-cio:3.5.2` — Apache-2.0
- `io.ktor:ktor-http-cio-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-http-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-io:3.5.2` — Apache-2.0
- `io.ktor:ktor-io-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-network:3.5.2` — Apache-2.0
- `io.ktor:ktor-network-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-serialization:3.5.2` — Apache-2.0
- `io.ktor:ktor-serialization-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-serialization-kotlinx:3.5.2` — Apache-2.0
- `io.ktor:ktor-serialization-kotlinx-json:3.5.2` — Apache-2.0
- `io.ktor:ktor-serialization-kotlinx-json-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-serialization-kotlinx-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-sse:3.5.2` — Apache-2.0
- `io.ktor:ktor-sse-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-utils:3.5.2` — Apache-2.0
- `io.ktor:ktor-utils-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-websocket-serialization:3.5.2` — Apache-2.0
- `io.ktor:ktor-websocket-serialization-jvm:3.5.2` — Apache-2.0
- `io.ktor:ktor-websockets:3.5.2` — Apache-2.0
- `io.ktor:ktor-websockets-jvm:3.5.2` — Apache-2.0
- `org.apache.commons:commons-compress:1.28.0` — Apache-2.0
- `org.apache.commons:commons-lang3:3.18.0` — Apache-2.0
- `org.connectbot:termlib:0.1.0` — Apache-2.0 (termlib); embedded libvterm is MIT
- `org.jetbrains:annotations:23.0.0` — Apache-2.0
- `org.jetbrains.kotlin:kotlin-bom:1.8.22` — Apache-2.0
- `org.jetbrains.kotlin:kotlin-parcelize-runtime:2.3.21` — Apache-2.0
- `org.jetbrains.kotlin:kotlin-stdlib:2.3.21` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-io-bytestring:0.9.1` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-io-bytestring-jvm:0.9.1` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-io-core:0.9.1` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-io-core-jvm:0.9.1` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-bom:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-json-io:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-json-io-jvm:1.11.0` — Apache-2.0
- `org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0` — Apache-2.0
- `org.jspecify:jspecify:1.0.0` — Apache-2.0
- `org.slf4j:slf4j-api:2.0.18` — MIT

---

# Appendix B — SLF4J 2.0.18 MIT License

Copyright (c) 2004-2022 QOS.ch Sarl (Switzerland)
All rights reserved.

Permission is hereby granted, free  of charge, to any person obtaining
a  copy  of this  software  and  associated  documentation files  (the
"Software"), to  deal in  the Software without  restriction, including
without limitation  the rights to  use, copy, modify,  merge, publish,
distribute,  sublicense, and/or sell  copies of  the Software,  and to
permit persons to whom the Software  is furnished to do so, subject to
the following conditions:

The  above  copyright  notice  and  this permission  notice  shall  be
included in all copies or substantial portions of the Software.

THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

# Appendix C — libvterm MIT License

The MIT License

Copyright (c) 2008 Paul Evans <leonerd@leonerd.org.uk>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

---

# Appendix D — Apache Commons NOTICE Files

## Apache Commons Codec 1.19.0 NOTICE

Apache Commons Codec
Copyright 2002-2025 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).

## Apache Commons Compress 1.28.0 NOTICE

Apache Commons Compress
Copyright 2002-2025 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).

## Apache Commons IO 2.20.0 NOTICE

Apache Commons IO
Copyright 2002-2025 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).

## Apache Commons Lang 3.18.0 NOTICE

Apache Commons Lang
Copyright 2001-2025 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).

---

# Release compliance checklist

Before freezing the public release:

1. Keep this file with the released source/repository documentation.
2. Include `licenses/APACHE-2.0.txt`.
3. Include `licenses/GPL-2.0.txt` and `licenses/GPL-3.0.txt`.
4. Include `licenses/LGPL-3.0.txt`.
5. Include `licenses/LIBANDROID-SHMEM-BSD-3-CLAUSE.txt`.
6. Keep the four Apache Commons `NOTICE` files under `licenses/` with the release notice bundle.
7. Keep `licenses/SLF4J-MIT.txt` and `licenses/LIBVTERM-MIT.txt` with the release notice bundle.
8. Preserve corresponding source and Atlas build modifications for the exact distributed PRoot runtime.
9. Preserve the exact libandroid-shmem 0.7 source and Atlas build recipe used for the distributed native runtime.
10. Preserve the exact talloc 2.4.3 source archive corresponding to the statically linked library.
11. Keep the PRoot, PRoot loader, native-source, build-recipe, and Ubuntu SHA-256 values with the release provenance.
12. Keep `docs/native-runtime/` with the public release provenance.
13. Regenerate `releaseRuntimeClasspath` after any dependency change and update this notice before shipping.
