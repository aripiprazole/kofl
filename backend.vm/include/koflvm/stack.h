#ifndef KOFLVM_STACK_H
#define KOFLVM_STACK_H

#include "value.h"

namespace koflvm {

class Stack {
  int top_{};
  size_t capacity_{};
  Value *values_{};

 public:
  explicit Stack(size_t capacity);

  int Top();
  Value* Get(int index);

  bool Push(Value *value);
  Value *Peek();
  Value *Pop();

  void Dispose();
};

}

#endif //KOFLVM_STACK_H