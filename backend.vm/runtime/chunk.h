#ifndef RUNTIME_CHUNK_H
#define RUNTIME_CHUNK_H

#include <inttypes.h>

#include "heap.h"
#include "value.h"

typedef enum opcode {
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
} opcode_t;

typedef struct chunk {
    int count;
    int capacity;
    int *lines;
    unsigned int *code;
    value_array_t *values;
} chunk_t;

// opcode functions>
opcode_t uint_to_opcode(unsigned int raw);

// chunk functions>
chunk_t *chunk_create(int count, int capacity);

void chunk_write(chunk_t *chunk, unsigned int, int line);

int chunk_write_const(chunk_t *chunk, value_t const_);

char *chunk_dump(chunk_t *chunk);

void chunk_dispose(chunk_t *chunk);

#endif //RUNTIME_CHUNK_H
