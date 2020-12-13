#ifndef RUNTIME_VM_H
#define RUNTIME_VM_H

#include <stdbool.h>

#include "chunk.h"
#include "heap.h"
#include "value.h"
#include "table.h"

typedef struct flags {
    bool verbose;
    size_t memory;
} flags_t;

typedef struct stack {
    int top;
    value_t *values;
} stack_t;

typedef struct vm {
    stack_t *stack;
    chunk_t *pc;
    heap_t *heap;
    table_t *table;
} vm_t;

typedef enum interpret_result {
    I_RESULT_OK,
    I_RESULT_ERROR
} interpret_result_t;

vm_t *vm_create(flags_t flags);

interpret_result_t vm_eval(vm_t *vm, chunk_t *code);

#endif //RUNTIME_VM_H
