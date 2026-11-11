
cmake_minimum_required(VERSION 3.22.1)

project(atlas_native LANGUAGES C CXX)

# Phase 3: Atlas standalone AI engine.
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Android-compatible llama.cpp configuration.
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)

# Build only the libraries needed for inference.
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)

# Keep the native dependencies available as shared libraries.
set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)

# Existing verified native test library.
add_library(
        atlas_native_probe
        SHARED
        atlas_native_probe.cpp
)

target_compile_features(
        atlas_native_probe
        PRIVATE
        cxx_std_17
)

# Third-party llama.cpp inference engine.
add_subdirectory(
        third_party/llama.cpp
        ${CMAKE_CURRENT_BINARY_DIR}/llama_build
)

# Build and link the inference engine with Atlas.
target_link_libraries(
        atlas_native_probe
        PRIVATE
        llama
)
