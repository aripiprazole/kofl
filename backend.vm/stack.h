#ifndef RUNTIME_STACK_H
#define RUNTIME_STACK_H

#include <stdbool.h>

#include "value.h"

typedef struct {
  int top;
  size_t capacity;
  Value *values;
} Stack;

// stack functions>
Stack *StackCreate(size_t capacity);

bool StackPush(Stack *stack, Value *value);

Value *StackPeek(Stack *stack);

Value *StackPop(Stack *stack);

void StackDispose(Stack *stack);

#endif //RUNTIME_STACK_H
