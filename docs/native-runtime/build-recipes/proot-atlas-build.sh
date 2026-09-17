source "$TERMUX_SCRIPTDIR/packages/proot/build.sh"

TERMUX_PKG_DESCRIPTION="Atlas Cyberdeck PRoot runtime"

TERMUX_PKG_BUILD_DEPENDS="libtalloc-static"

#
# Atlas bundles the PRoot loader instead of using
# Termux's fixed libexec/proot loader directory.
#
unset PROOT_UNBUNDLE_LOADER

termux_step_pre_configure() {

    CPPFLAGS+=" -DARG_MAX=131072 -DVERSION=\\\"${TERMUX_PKG_VERSION}\\\""

    local makefile="$TERMUX_PKG_SRCDIR/src/GNUmakefile"
    local cli_file="$TERMUX_PKG_SRCDIR/src/cli/cli.c"
    local temp_file="$TERMUX_PKG_SRCDIR/src/path/temp.c"

    local talloc_archive="$TERMUX_PREFIX/lib/libtalloc.a"

    #
    # Locate the Atlas-specific static shmem package.
    #
    local atlas_shmem_deb

    atlas_shmem_deb="$(
        find "$TERMUX_SCRIPTDIR/output" \
            -maxdepth 1 \
            -type f \
            -name 'libandroid-shmem-atlas-static_*_aarch64.deb' \
            | head -n 1
    )"

    test -n "$atlas_shmem_deb" ||
        termux_error_exit \
            "Atlas build: Atlas shmem static package not found."

    local atlas_shmem_dir="$TERMUX_PKG_BUILDDIR/atlas-shmem"

    rm -rf "$atlas_shmem_dir"
    mkdir -p "$atlas_shmem_dir"

    dpkg-deb -x \
        "$atlas_shmem_deb" \
        "$atlas_shmem_dir"

    local shmem_archive="$atlas_shmem_dir$TERMUX_PREFIX/lib/libandroid-shmem.a"

    echo
    echo "=== ATLAS PRE-BUILD CHECK ==="
    echo "PRoot Makefile : $makefile"
    echo "Temp source    : $temp_file"
    echo "Talloc archive : $talloc_archive"
    echo "Shmem package  : $atlas_shmem_deb"
    echo "Shmem archive  : $shmem_archive"
    echo

    test -f "$makefile" ||
        termux_error_exit \
            "Atlas build: PRoot GNUmakefile not found."

    test -f "$cli_file" ||
        termux_error_exit \
            "Atlas build: cli.c not found."

    test -f "$temp_file" ||
        termux_error_exit \
            "Atlas build: temp.c not found."

    test -f "$talloc_archive" ||
        termux_error_exit \
            "Atlas build: libtalloc.a is missing."

    test -f "$shmem_archive" ||
        termux_error_exit \
            "Atlas build: Atlas libandroid-shmem.a is missing."

    #
    # Verify expected PRoot linker rules.
    #
    grep -Eq \
        '(^|[[:space:]])-ltalloc([[:space:]]|$)' \
        "$makefile" ||
        termux_error_exit \
            "Atlas build: PRoot talloc link rule not found."

    grep -Eq \
        '(^|[[:space:]])-landroid-shmem([[:space:]]|$)' \
        "$makefile" ||
        termux_error_exit \
            "Atlas build: PRoot android-shmem link rule not found."

    #
    # Static talloc.
    #
    sed -i \
        "s|-ltalloc|$talloc_archive|g" \
        "$makefile"

    #
    # Atlas-specific static android-shmem.
    #
    # libandroid-shmem uses Android logging,
    # so liblog remains an Android system dependency.
    #
    sed -i \
        "s|-landroid-shmem|$shmem_archive -llog|g" \
        "$makefile"

    #
    # Remove Termux-specific runtime library search path.
    #
    LDFLAGS="$(
        printf '%s\n' "$LDFLAGS" |
            sed "s|-Wl,-rpath=$TERMUX_PREFIX/lib||g"
    )"

    #
    # Replace PRoot's compile-time P_tmpdir fallback.
    #
    # Runtime order becomes:
    #
    # PROOT_TMP_DIR
    #      ↓
    # TMPDIR
    #      ↓
    # /tmp
    #
    python3 - "$temp_file" <<'PY'
import pathlib
import sys

path = pathlib.Path(sys.argv[1])
text = path.read_text()

old = '''\
\ttemp_directory = getenv("PROOT_TMP_DIR");
\tif (temp_directory == NULL) {
\t\ttemp_directory = P_tmpdir;
\t}
'''

new = '''\
\ttemp_directory = getenv("PROOT_TMP_DIR");
\tif (temp_directory == NULL || temp_directory[0] == '\\0') {
\t\ttemp_directory = getenv("TMPDIR");
\t\tif (temp_directory == NULL || temp_directory[0] == '\\0') {
\t\t\ttemp_directory = "/tmp";
\t\t}
\t}
'''

if old not in text:
    raise SystemExit(
        "Atlas build: expected PRoot temp-directory block not found."
    )

text = text.replace(
    old,
    new,
    1
)

path.write_text(text)

print("Atlas PRoot runtime temp-directory rewrite applied.")
PY

    #
    # Remove the remaining Termux-specific diagnostic
    # wording. This does not alter runtime behavior.
    #
    python3 - "$cli_file" <<'PY'
import pathlib
import sys

path = pathlib.Path(sys.argv[1])
text = path.read_text()

old = (
    "It seems that termux-exec is active and is prepending "
    "/data/data/com.termux/... to executable paths"
)

new = (
    "An exec preload helper is active and may alter "
    "executable paths"
)

if old in text:
    text = text.replace(
        old,
        new,
        1
    )

path.write_text(text)
PY

    echo
    echo "=== ATLAS TEMP SOURCE ==="

    sed -n \
        '20,34p' \
        "$temp_file"

    echo
    echo "=== ATLAS PROOT LINK RULES ==="

    grep -nE \
        'talloc|android-shmem' \
        "$makefile"

    echo
}

#
# Atlas does not ship Termux's termux-chroot wrapper.
#
termux_step_post_make_install() {

    mkdir -p \
        "$TERMUX_PREFIX/share/man/man1"

    install \
        -m600 \
        "$TERMUX_PKG_SRCDIR/doc/proot/man.1" \
        "$TERMUX_PREFIX/share/man/man1/proot.1"
}
