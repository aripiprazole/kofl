#ifndef RUNTIME_VM_H
#define RUNTIME_VM_H

#include <stdbool.h>

#include "chunk.h"
#include "heap.h"
#include "value.h"
#include "table.h"
#include "stack.h"
#include "object.h"

typedef struct flags {
    bool verbose;
    size_t memory;
} flags_t;

typedef struct vm {
    stack_t *stack;
    chunk_t *chunk;
    opcode_t *pc;
    heap_t *heap;
    table_t *globals;
    object_t *objects;
} vm_t;

typedef enum interpret_result {
    I_RESULT_OK,
    I_RESULT_ERROR,
    I_NULL_POINTER
} interpret_result_t;

// vm functions>
vm_t *vm_create(flags_t flags);

interpret_result_t vm_eval(vm_t *vm, chunk_t *chunk);

void vm_dispose(vm_t *vm);

#endif //RUNTIME_VM_H
