#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "chunk.h"
#include "utils.h"

chunk_t *chunk_create(int count, int capacity) {
    chunk_t *chunk = malloc(sizeof(chunk_t));

    chunk->count = count;
    chunk->capacity = capacity;
    chunk->values = value_array_create(0, 0);
    chunk->code = malloc(capacity * sizeof(opcode_t));
    chunk->lines = malloc(capacity * sizeof(int));

    return chunk;
}

void chunk_write(chunk_t *chunk, unsigned int op, int line) {
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

int chunk_write_const(chunk_t *chunk, value_t const_) {
#ifdef VALUE_DEBUG
    printf("chunk_write_const(chunk = UNKNOWN, const_ = %s)\n", value_to_str(&const_));
#endif

    value_array_write(chunk->values, const_);

    return chunk->values->count - 1;
}

opcode_t uint_to_opcode(unsigned int raw) {
    return (opcode_t) raw;
}

char *chunk_dump(chunk_t *chunk) {
    char *str = malloc(1100 * sizeof(char));

    sprintf(str, "Chunk(count ) [");

    for (size_t i = 0; i < chunk->count; i++) {
        int code = chunk->code[i];
        int line = chunk->lines[i];

        sprintf(str, "%s, {code: %d, line: %d}", str, code, line);
    }

    strcat(str, "]");

    return str;
}