#ifndef KOFLVM_VM_H
#define KOFLVM_VM_H

#include "koflvm/chunk.h"
#include "koflvm/heap.h"
#include "koflvm/value.h"
#include "koflvm/table.h"
#include "koflvm/stack.h"
#include "koflvm/object.h"

namespace koflvm {

struct Flags {
  bool verbose;
  size_t memory;
};

enum InterpretResult {
  kResultOK,
  kResultError,
  kResultNullPointer
};

class VM {
  Stack *stack_;
  Chunk *chunk_;
  Opcode *pc_;
  Heap *heap_;
  Table *globals_;
  Table *strings_;
  Object *objects_;

 public:
  explicit VM(Flags flags);

  InterpretResult Eval(Chunk* pc);

  void Dispose();
};
}

#endif //KOFLVM_VM_H