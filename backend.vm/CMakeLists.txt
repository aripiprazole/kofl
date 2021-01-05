cmake_minimum_required(VERSION 3.16)
project(koflvm)

set(CMAKE_CXX_STANDARD 20)

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
