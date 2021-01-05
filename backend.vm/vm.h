#ifndef RUNTIME_VM_H
#define RUNTIME_VM_H

#include <stdbool.h>

#include "chunk.h"
#include "heap.h"
#include "value.h"
#include "table.h"
#include "stack.h"
#include "object.h"

typedef struct {
  bool verbose;
  size_t memory;
} Flags;

typedef struct {
  Stack *stack;
  Chunk *chunk;
  Opcode *pc;
  Heap *heap;
  Table *globals;
  Table *strings;
  Object *objects;
} Vm;

typedef enum interpret_result {
  kResultOK,
  kResultError,
  kResultNullPointer
} InterpretResult;

// vm functions>
Vm *VmCreate(Flags flags);

InterpretResult VmEval(Vm *vm, Chunk *chunk);

void VmDispose(Vm *vm);

#endif //RUNTIME_VM_H
