#!/bin/bash

[ ! -d "./runtime/build" ] && mkdir "./runtime/build"

cd runtime || exit 1

cmake -G "Unix Makefiles" -S . -B build

cd build || exit 1

make

cd ../..

echo "Success compiled kofl runtime."
