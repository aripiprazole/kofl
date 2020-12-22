#ifndef RUNTIME_BYTECODE_H
#define RUNTIME_BYTECODE_H

#include "chunk.h"

chunk_t *parse_chunk(const char *bytes);

#endif //RUNTIME_BYTECODE_H
