cmake_minimum_required(VERSION 3.22.1)

project(atlas_native LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Preserve the existing Android-compatible llama.cpp configuration.
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)

# Working JNI probe, unchanged.
add_library(atlas_native_probe SHARED atlas_native_probe.cpp)
target_compile_features(atlas_native_probe PRIVATE cxx_std_17)

add_subdirectory(third_party/llama.cpp ${CMAKE_CURRENT_BINARY_DIR}/llama_build)
target_link_libraries(atlas_native_probe PRIVATE llama)

# Separate native GGUF loader, without changing the working probe.
add_library(atlas_local_ai SHARED atlas_local_ai.cpp)
target_compile_features(atlas_local_ai PRIVATE cxx_std_17)
target_include_directories(atlas_local_ai PRIVATE
    third_party/llama.cpp/include
    third_party/llama.cpp/ggml/include
)
target_link_libraries(atlas_local_ai PRIVATE llama ggml)
