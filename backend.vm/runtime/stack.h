#ifndef RUNTIME_STACK_H
#define RUNTIME_STACK_H

#include "value.h"

typedef struct stack {
    int top;
    size_t capacity;
    value_t *values;
} stack_t;

// stack functions>
stack_t *stack_create(size_t capacity);

void stack_dispose(stack_t *stack);

#endif //RUNTIME_STACK_H
