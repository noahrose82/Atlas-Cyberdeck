# Atlas Cyberdeck — Third-Party Notices

This document records third-party software used by the Atlas Cyberdeck Android release.

The Gradle inventory below was generated from the resolved `releaseRuntimeClasspath`.
Native/runtime components that are not represented in Gradle are documented separately.

Atlas Cyberdeck's own source code and assets remain governed by the project's top-level `LICENSE`.
Third-party software remains governed by its respective upstream license.

## Standard license texts distributed with this notice

- `licenses/APACHE-2.0.txt` — Apache License 2.0
- `licenses/GPL-2.0.txt` — GNU General Public License version 2
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

## PRoot runtime and external loader

**Upstream project:** `termux/proot`  
**Version:** 5.1.107.92  
**License:** GPL-2.0

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

The full GPL-2.0 text is included in `licenses/GPL-2.0.txt`.

Because Atlas distributes GPL-2.0-covered PRoot binaries, the public release process
must also make the corresponding source for the exact distributed PRoot build available
in a GPL-compliant manner. The source archive/version/hash above should be retained as
release provenance.

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
