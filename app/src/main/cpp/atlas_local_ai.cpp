#include <jni.h>
#include <mutex>
#include <string>
#include "llama.h"
#include "ggml-backend.h"

namespace {
std::mutex model_mutex;
llama_model *loaded_model = nullptr;
bool backend_ready = false;

std::string to_string(JNIEnv *env, jstring value) {
    if (value == nullptr) return {};
    const char *raw = env->GetStringUTFChars(value, nullptr);
    if (raw == nullptr) return {};
    std::string result(raw);
    env->ReleaseStringUTFChars(value, raw);
    return result;
}
}

// Model lifecycle is serialized and uses the application private model path.
// Never call this directly from the Android main/UI thread.
extern "C" JNIEXPORT jboolean JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeLoadModel(
        JNIEnv *env, jobject, jstring modelPath, jstring nativeLibraryDir) {
    const std::string path = to_string(env, modelPath);
    const std::string libs = to_string(env, nativeLibraryDir);
    if (path.empty() || libs.empty()) return JNI_FALSE;

    std::lock_guard<std::mutex> lock(model_mutex);
    if (!backend_ready) {
        ggml_backend_load_all_from_path(libs.c_str());
        llama_backend_init();
        backend_ready = true;
    }
    if (loaded_model != nullptr) {
        llama_model_free(loaded_model);
        loaded_model = nullptr;
    }

    llama_model_params params = llama_model_default_params();
    params.n_gpu_layers = 0;  // CPU-only first, no accelerator assumptions.
    loaded_model = llama_model_load_from_file(path.c_str(), params);
    return loaded_model != nullptr ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeIsModelLoaded(
        JNIEnv *, jobject) {
    std::lock_guard<std::mutex> lock(model_mutex);
    return loaded_model != nullptr ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeUnloadModel(
        JNIEnv *, jobject) {
    std::lock_guard<std::mutex> lock(model_mutex);
    if (loaded_model != nullptr) {
        llama_model_free(loaded_model);
        loaded_model = nullptr;
    }
}
