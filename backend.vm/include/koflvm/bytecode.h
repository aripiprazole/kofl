#ifndef KOFLVM_BYTECODE_H
#define KOFLVM_BYTECODE_H

#include "koflvm/chunk.h"

namespace koflvm {
Chunk *ParseChunk(const char *bytes);
}

#endif //KOFLVM_BYTECODE_H