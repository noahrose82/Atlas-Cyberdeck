
#include <jni.h>
#include <algorithm>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#include "llama.h"
#include "ggml-backend.h"

namespace {

    std::mutex model_mutex;
    llama_model *loaded_model = nullptr;
    bool backend_ready = false;

    constexpr int32_t CONTEXT_SIZE = 2048;
    constexpr int32_t BATCH_SIZE = 256;
    constexpr int32_t MAX_PROMPT_TOKENS = 1536;

    std::string to_string(JNIEnv *env, jstring value) {
        if (value == nullptr) return {};

        const char *raw = env->GetStringUTFChars(value, nullptr);
        if (raw == nullptr) return {};

        std::string result(raw);
        env->ReleaseStringUTFChars(value, raw);
        return result;
    }

    void throw_error(JNIEnv *env, const char *message) {
        jclass cls = env->FindClass(
                "java/lang/IllegalStateException"
        );

        if (cls != nullptr) {
            env->ThrowNew(cls, message);
            env->DeleteLocalRef(cls);
        }
    }

    struct BatchGuard {
        llama_batch batch;

        BatchGuard()
                : batch(llama_batch_init(BATCH_SIZE, 0, 1)) {}

        ~BatchGuard() {
            llama_batch_free(batch);
        }

        BatchGuard(const BatchGuard &) = delete;
        BatchGuard &operator=(const BatchGuard &) = delete;
    };

    void set_batch(
            llama_batch &batch,
            const llama_token *tokens,
            int32_t count,
            int32_t start_position
    ) {
        batch.n_tokens = count;

        for (int32_t i = 0; i < count; ++i) {
            batch.token[i] = tokens[i];
            batch.pos[i] = start_position + i;
            batch.n_seq_id[i] = 1;
            batch.seq_id[i][0] = 0;
            batch.logits[i] = (i == count - 1);
        }
    }

    std::string generate_text(
            const std::string &message,
            int32_t max_tokens
    ) {
        const llama_vocab *vocab =
                llama_model_get_vocab(loaded_model);

        if (vocab == nullptr) {
            return {};
        }

        // Use the chat template embedded in the GGUF.
        const char *chat_template =
                llama_model_chat_template(loaded_model, nullptr);

        if (chat_template == nullptr) {
            return {};
        }

        const std::string system_message =
                "You are Atlas, the offline AI partner inside "
                "Atlas Cyberdeck. Be helpful, clear, and concise. "
                "You cannot execute commands or access files "
                "unless a separate authorized feature does so.";

        llama_chat_message messages[2] = {
                {"system", system_message.c_str()},
                {"user", message.c_str()}
        };

        int32_t prompt_length = llama_chat_apply_template(
                chat_template,
                messages,
                2,
                true,
                nullptr,
                0
        );

        if (prompt_length <= 0 ||
            prompt_length > 32768) {
            return {};
        }

        std::vector<char> formatted(
                static_cast<size_t>(prompt_length) + 1, '\0'
        );

        int32_t written = llama_chat_apply_template(
                chat_template,
                messages,
                2,
                true,
                formatted.data(),
                static_cast<int32_t>(formatted.size())
        );

        if (written <= 0 ||
            written > prompt_length) {
            return {};
        }

        std::string prompt(
                formatted.data(),
                static_cast<size_t>(written)
        );

        // First call determines required token capacity.
        int32_t token_count = llama_tokenize(
                vocab,
                prompt.data(),
                static_cast<int32_t>(prompt.size()),
                nullptr,
                0,
                true,
                true
        );

        if (token_count < 0) {
            token_count = -token_count;
        }

        if (token_count <= 0 ||
            token_count > MAX_PROMPT_TOKENS) {
            return {};
        }

        std::vector<llama_token> tokens(token_count);

        int32_t actual_tokens = llama_tokenize(
                vocab,
                prompt.data(),
                static_cast<int32_t>(prompt.size()),
                tokens.data(),
                token_count,
                true,
                true
        );

        if (actual_tokens <= 0) {
            return {};
        }

        tokens.resize(actual_tokens);

        llama_context_params context_params =
                llama_context_default_params();

        context_params.n_ctx = CONTEXT_SIZE;
        context_params.n_batch = BATCH_SIZE;
        context_params.n_ubatch = BATCH_SIZE;

        std::unique_ptr<llama_context, decltype(&llama_free)>
                context(
                llama_init_from_model(
                        loaded_model,
                        context_params
                ),
                &llama_free
        );

        if (!context) {
            return {};
        }

        std::unique_ptr<llama_sampler, decltype(&llama_sampler_free)>
                sampler(
                llama_sampler_init_greedy(),
                &llama_sampler_free
        );

        if (!sampler) {
            return {};
        }

        BatchGuard batch_guard;
        llama_batch &batch = batch_guard.batch;

        int32_t position = 0;

        // Evaluate the prompt in memory-bounded batches.
        while (position < actual_tokens) {
            int32_t count = std::min(
                    BATCH_SIZE,
                    actual_tokens - position
            );

            set_batch(
                    batch,
                    tokens.data() + position,
                    count,
                    position
            );

            if (llama_decode(context.get(), batch) != 0) {
                return {};
            }

            position += count;
        }

        std::string output;

        // Generate only text. No tools or shell execution.
        for (int32_t i = 0; i < max_tokens; ++i) {
            llama_token next_token = llama_sampler_sample(
                    sampler.get(),
                    context.get(),
                    -1
            );

            if (llama_vocab_is_eog(vocab, next_token)) {
                break;
            }

            int32_t piece_length = llama_token_to_piece(
                    vocab,
                    next_token,
                    nullptr,
                    0,
                    0,
                    false
            );

            if (piece_length < 0) {
                piece_length = -piece_length;
            }

            if (piece_length > 0) {
                std::vector<char> piece(piece_length);

                int32_t bytes = llama_token_to_piece(
                        vocab,
                        next_token,
                        piece.data(),
                        static_cast<int32_t>(piece.size()),
                        0,
                        false
                );

                if (bytes < 0) {
                    return {};
                }

                output.append(piece.data(), bytes);
            }

            if (i == max_tokens - 1) {
                break;
            }

            set_batch(
                    batch,
                    &next_token,
                    1,
                    position
            );

            if (llama_decode(context.get(), batch) != 0) {
                return {};
            }

            position++;
        }

        return output;
    }

} // namespace

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeLoadModel(
        JNIEnv *env,
        jobject,
        jstring modelPath,
        jstring nativeLibraryDir
) {
    const std::string path = to_string(env, modelPath);
    const std::string libs = to_string(env, nativeLibraryDir);

    if (path.empty() || libs.empty()) {
        return JNI_FALSE;
    }

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

    llama_model_params params =
            llama_model_default_params();

    params.n_gpu_layers = 0;

    loaded_model = llama_model_load_from_file(
            path.c_str(),
            params
    );

    return loaded_model != nullptr ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeIsModelLoaded(
        JNIEnv *,
        jobject
) {
    std::lock_guard<std::mutex> lock(model_mutex);

    return loaded_model != nullptr ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeUnloadModel(
        JNIEnv *,
        jobject
) {
    std::lock_guard<std::mutex> lock(model_mutex);

    if (loaded_model != nullptr) {
        llama_model_free(loaded_model);
        loaded_model = nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noahrose_pocketlab_feature_partner_local_AtlasLocalAiEngine_nativeGenerate(
        JNIEnv *env,
        jobject,
        jstring message,
        jint maxTokens
) {
    const std::string input = to_string(env, message);

    if (input.empty() || input.size() > 4096 ||
        maxTokens < 1 || maxTokens > 256) {
        throw_error(env, "Invalid offline generation request.");
        return nullptr;
    }

    // Protect generation against concurrent model unload/reload.
    std::lock_guard<std::mutex> lock(model_mutex);

    if (loaded_model == nullptr) {
        throw_error(env, "Load a local GGUF model first.");
        return nullptr;
    }

    const std::string result = generate_text(
            input,
            maxTokens
    );

    if (result.empty()) {
        throw_error(env, "Offline generation produced no text.");
        return nullptr;
    }

    return env->NewStringUTF(result.c_str());
}
