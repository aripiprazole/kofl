#include <stdlib.h>

#include "chunk.h"

chunk_t *chunk_create(int count, int capacity) {
    chunk_t *chunk = malloc(sizeof(chunk_t));

    chunk->count = count;
    chunk->capacity = capacity;
    chunk->code = malloc(capacity * sizeof(chunk_t));
    chunk->lines = malloc(capacity * sizeof(int));

    return chunk;
}

void chunk_write(chunk_t *chunk, unsigned int op, int line) {
    // TODO
}

int chunk_write_const(chunk_t *chunk, value_t const_) {
    // TODO

    return 0;
}