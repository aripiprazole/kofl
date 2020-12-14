#include <stdlib.h>

#include "stack.h"

stack_t *stack_create(size_t capacity) {
    stack_t *stack = malloc(sizeof(stack_t));

    stack->capacity = capacity;
    stack->top = 0;
    stack->values = calloc(capacity, sizeof(value_t));

    return stack;
}

bool stack_push(stack_t *stack, value_t *value) {
    if (stack->top >= stack->capacity) return false;

    stack->values[stack->top] = *value;
    stack->top++;

    return true;
}

value_t *stack_peek(stack_t *stack) {
    if (stack->top <= 0) return NULL;

    return &stack->values[stack->top - 1];
}

value_t *stack_pop(stack_t *stack) {
    if (stack->top <= 0) return NULL;

    stack->top--;

    return &stack->values[stack->top];
}

void stack_dispose(stack_t *stack) {
    free(stack->values);
    free(stack);
}
