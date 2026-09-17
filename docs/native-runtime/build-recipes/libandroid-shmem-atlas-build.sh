TERMUX_PKG_HOMEPAGE=https://github.com/termux/libandroid-shmem
TERMUX_PKG_DESCRIPTION="Atlas Cyberdeck System V shared memory compatibility"
TERMUX_PKG_LICENSE="BSD 3-Clause"
TERMUX_PKG_MAINTAINER="@termux"
TERMUX_PKG_VERSION=0.7
TERMUX_PKG_SRCURL=https://github.com/termux/libandroid-shmem/archive/refs/tags/v${TERMUX_PKG_VERSION}.tar.gz
TERMUX_PKG_SHA256=1e5ff8459bc0a8c229dd8a94b27d119987e09ef3414331c2b5ebfff20b98e867
TERMUX_PKG_BUILD_IN_SRC=true

termux_step_pre_configure() {

    local source_file="$TERMUX_PKG_SRCDIR/shmem.c"

    test -f "$source_file" ||
        termux_error_exit \
            "Atlas shmem build: shmem.c not found."

    python3 - "$source_file" <<'PY'
import pathlib
import sys

path = pathlib.Path(sys.argv[1])
text = path.read_text()

old_define = '#define ASHV_KEY_SYMLINK_PATH _PATH_TMP "ashv_key_%d"'
new_define = '#define ASHV_KEY_SYMLINK_NAME "ashv_key_%d"'

old_call = '\t\tsprintf(symlink_path, ASHV_KEY_SYMLINK_PATH, key);'

new_call = '''\t\tconst char* tmpdir = getenv("TMPDIR");

\t\tif (tmpdir == NULL || tmpdir[0] == '\\0') {
\t\t\tDBG("%s: TMPDIR is unavailable", __PRETTY_FUNCTION__);
\t\t\terrno = ENOENT;
\t\t\tpthread_mutex_unlock(&mutex);
\t\t\treturn -1;
\t\t}

\t\tconst char* separator =
\t\t\ttmpdir[strlen(tmpdir) - 1] == '/' ? "" : "/";

\t\tint written = snprintf(
\t\t\tsymlink_path,
\t\t\tsizeof(symlink_path),
\t\t\t"%s%s" ASHV_KEY_SYMLINK_NAME,
\t\t\ttmpdir,
\t\t\tseparator,
\t\t\tkey);

\t\tif (
\t\t\twritten < 0 ||
\t\t\t(size_t) written >= sizeof(symlink_path)
\t\t) {
\t\t\tDBG("%s: TMPDIR path is too long", __PRETTY_FUNCTION__);
\t\t\terrno = ENAMETOOLONG;
\t\t\tpthread_mutex_unlock(&mutex);
\t\t\treturn -1;
\t\t}'''

if old_define not in text:
    raise SystemExit(
        "Atlas shmem build: expected ASHV_KEY_SYMLINK_PATH definition not found."
    )

if old_call not in text:
    raise SystemExit(
        "Atlas shmem build: expected symlink path call not found."
    )

text = text.replace(
    old_define,
    new_define,
    1
)

text = text.replace(
    old_call,
    new_call,
    1
)

path.write_text(text)

print("Atlas shmem runtime TMPDIR rewrite applied.")
PY

    echo
    echo "=== ATLAS SHMEM SOURCE CHECK ==="

    grep -nE \
        'TMPDIR|ASHV_KEY_SYMLINK|ashv_key' \
        "$source_file"

    echo
}
