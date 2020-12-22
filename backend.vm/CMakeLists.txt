cmake_minimum_required(VERSION 3.17)
project(runtime C)

set(CMAKE_C_STANDARD 11)

include_directories(.)

add_executable(koflvm main.c
        heap.c heap.h
        value.c value.h
        chunk.c chunk.h
        utils.c utils.h
        vm.c vm.h
        table.c table.h
        stack.c stack.h
        object.c object.h
        debug.c debug.h
        bytecode.c bytecode.h)

add_library(runtime
        heap.c heap.h
        value.c value.h
        chunk.c chunk.h
        utils.c utils.h
        vm.c vm.h
        table.c table.h
        stack.c stack.h
        object.c object.h
        main.c
        debug.c debug.h
        bytecode.c bytecode.h)
