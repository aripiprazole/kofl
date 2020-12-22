#ifndef RUNTIME_STACK_H
#define RUNTIME_STACK_H

#include <stdbool.h>

#include "value.h"

typedef struct stack {
    int top;
    size_t capacity;
    value_t *values;
} stack_t;

// stack functions>
stack_t *stack_create(size_t capacity);

bool stack_push(stack_t *stack, value_t* value);

value_t *stack_peek(stack_t *stack);

value_t *stack_pop(stack_t *stack);

void stack_dispose(stack_t *stack);

#endif //RUNTIME_STACK_H
