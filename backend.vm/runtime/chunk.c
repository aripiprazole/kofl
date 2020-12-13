#include <stdlib.h>

#include "chunk.h"
#include "utils.h"

chunk_t *chunk_create(int count, int capacity) {
    chunk_t *chunk = malloc(sizeof(chunk_t));

    chunk->count = count;
    chunk->capacity = capacity;
    chunk->values = value_array_create(0, 0);
    chunk->code = malloc(capacity * sizeof(chunk_t));
    chunk->lines = malloc(capacity * sizeof(int));

    return chunk;
}

void chunk_write(chunk_t *chunk, unsigned int op, int line) {
    if (chunk->capacity < chunk->count + 1) {
        size_t old_capacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(chunk->capacity);
        chunk->code = GROW_ARRAY(opcode_t, chunk->code, old_capacity, chunk->capacity);
        chunk->lines = GROW_ARRAY(int, chunk->code, old_capacity, chunk->capacity);
    }

    chunk->code[chunk->count] = op;
    chunk->lines[chunk->count] = line;
    chunk->count++;
}

int chunk_write_const(chunk_t *chunk, value_t const_) {
    value_array_write(chunk->values, const_);

    return chunk->values->count - 1;
}