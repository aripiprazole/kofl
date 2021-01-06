cmake_minimum_required(VERSION 3.16)
project(koflvm CXX)

set(CMAKE_CXX_STANDARD 20)

include_directories(include)
include_directories(src)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/bin")

add_executable(koflvm src/main.cpp
        src/utils.cpp src/utils.h
        src/heap.cpp include/koflvm/heap.h
        src/value.cpp include/koflvm/value.h
        src/chunk.cpp include/koflvm/chunk.h
        src/vm.cpp include/koflvm/vm.h
        src/table.cpp include/koflvm/table.h
        src/stack.cpp include/koflvm/stack.h
        src/object.cpp include/koflvm/object.h
        src/debug.cpp include/koflvm/debug.h
        src/bytecode.cpp include/koflvm/bytecode.h)
