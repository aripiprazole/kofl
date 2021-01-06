#!/bin/bash

[ ! -d "./build" ] && mkdir "./build"

cmake -G "Unix Makefiles" -S . -B build

cd build || exit 1

make

cd ../..

echo "Successfully compiled koflvm."
