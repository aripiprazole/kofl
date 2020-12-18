#ifndef RUNTIME_BYTECODE_H
#define RUNTIME_BYTECODE_H

#include "chunk.h"

chunk_t *interpret_chunk(char *bytes);

#endif //RUNTIME_BYTECODE_H
