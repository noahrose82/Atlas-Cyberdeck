cmake_minimum_required(VERSION 3.22.1)

project(atlas_native_probe LANGUAGES CXX)

add_library(atlas_native_probe SHARED atlas_native_probe.cpp)

target_compile_features(atlas_native_probe PRIVATE cxx_std_17)
