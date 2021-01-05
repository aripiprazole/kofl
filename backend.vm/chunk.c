#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "chunk.h"
#include "utils.h"

// opcode functions>
Opcode UintToOpcode(unsigned int raw) {
    return (Opcode) raw;
}

// chunk functions>
Chunk *ChunkCreate(int count, int capacity) {
  Chunk *chunk = malloc(sizeof(Chunk));

  chunk->count = count;
  chunk->capacity = capacity;
  chunk->consts = ValueArrayCreate(0, 0);
  chunk->code = malloc(capacity * sizeof(Opcode));
  chunk->lines = malloc(capacity * sizeof(int));

  return chunk;
}

void ChunkWrite(Chunk *chunk, unsigned int op, int line) {
#ifdef CHUNK_DEBUG
  printf("chunk_write(chunk = UNKNOWN, op = %d, line = %d)\n", op, line);
#endif

  if (chunk->capacity < chunk->count + 1) {
    size_t old_capacity = chunk->capacity;
    chunk->capacity = GROW_CAPACITY(chunk->capacity);
    chunk->code = GROW_ARRAY(unsigned int, chunk->code, old_capacity, chunk->capacity);
    chunk->lines = GROW_ARRAY(int, chunk->lines, old_capacity, chunk->capacity);
  }

    chunk->code[chunk->count] = op;
    chunk->lines[chunk->count] = line;
    chunk->count++;
}

int ChunkWriteConst(Chunk *chunk, Value const_) {
#ifdef VALUE_DEBUG
  printf("chunk_write_const(chunk = UNKNOWN, const_ = %s)\n", value_to_str(&const_));
#endif

  ValueArrayWrite(chunk->consts, const_);

  return chunk->consts->count - 1;
}

char *ChunkDump(Chunk *chunk) {
  char *str = malloc(1100 * sizeof(char));

  sprintf(str, "Chunk(count ) [");

  for (size_t i = 0; i < chunk->count; i++) {
    uint32_t code = chunk->code[i];
    int line = chunk->lines[i];

    sprintf(str, "%s, {code: %d, line: %d}", str, code, line);
  }

    strcat(str, "]");

    return str;
}

void ChunkDispose(Chunk *chunk) {
  ValueArrayDispose(chunk->consts);
  free(chunk->lines);
  free(chunk->code);
  free(chunk);
}
