#include <stdlib.h>

#include "stack.h"

Stack *StackCreate(size_t capacity) {
  Stack *stack = malloc(sizeof(Stack));

  stack->capacity = capacity;
  stack->top = 0;
  stack->values = calloc(capacity, sizeof(Value));

  return stack;
}

bool StackPush(Stack *stack, Value *value) {
  if (stack->top >= stack->capacity) return false;

  stack->values[stack->top] = *value;
  stack->top++;

  return true;
}

Value *StackPeek(Stack *stack) {
  if (stack->top <= 0) return NULL;

  return &stack->values[stack->top - 1];
}

Value *StackPop(Stack *stack) {
  if (stack->top <= 0) return NULL;

  stack->top--;

  return &stack->values[stack->top];
}

void StackDispose(Stack *stack) {
    free(stack->values);
    free(stack);
}
