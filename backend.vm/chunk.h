#ifndef RUNTIME_CHUNK_H
#define RUNTIME_CHUNK_H

#include <inttypes.h>

#include "heap.h"
#include "value.h"

typedef enum {
    OP_RET,
    OP_CONST,
    OP_NEGATE,
    OP_SUM,
    OP_SUB,
    OP_MULT,
    OP_DIV,
    OP_TRUE,
    OP_FALSE,
    OP_NOT,
    OP_CONCAT,
    OP_POP,
    OP_STORE_GLOBAL,
    OP_ACCESS_GLOBAL
} Opcode;

typedef struct {
  int count;
  int capacity;
  int *lines;
  unsigned int *code;
  ValueArray *consts;
} Chunk;

// opcode functions>
Opcode UintToOpcode(unsigned int raw);

// chunk functions>
Chunk *ChunkCreate(int count, int capacity);

void ChunkWrite(Chunk *chunk, unsigned int, int line);

int ChunkWriteConst(Chunk *chunk, Value const_);

char *ChunkDump(Chunk *chunk);

void ChunkDispose(Chunk *chunk);

#endif //RUNTIME_CHUNK_H
