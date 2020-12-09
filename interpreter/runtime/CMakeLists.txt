cmake_minimum_required(VERSION 3.17)
project(runtime C)

set(CMAKE_C_STANDARD 11)

include_directories(.)

add_library(runtime-library
        runtime.c
        runtime.h)
