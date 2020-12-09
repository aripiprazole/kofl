#!/bin/bash

[ ! -d "./runtime/build" ] && mkdir "./runtime/build"

cd runtime || exit 1

cmake -G "Unix Makefiles" -S . -B build

cd build || exit 1

make

cd ../..

echo "Success compiled kofl interpreted runtime."

#
#cinterop -def ./runtime/runtime.def \
#  -compiler-option -I/usr/local/include \
#  -compiler-option -I./runtime \
#  -o ./build/libs/runtime