// Phase 3 native-build probe. Does not access Ubuntu or run AI inference.
extern "C" int atlas_native_probe_version() {
    return 1;
}
