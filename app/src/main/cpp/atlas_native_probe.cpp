
#include <jni.h>

// Phase 3: Atlas native AI foundation.
//
// Verifies that Kotlin can communicate with the
// native C++ library through JNI.
//
// No AI inference or Ubuntu commands run here.

extern "C"
JNIEXPORT jint JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasNativeProbe_nativeVersion(
        JNIEnv* env,
        jobject instance
) {
    (void) env;
    (void) instance;

    return 1;
}
