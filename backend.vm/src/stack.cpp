#include <cstdlib>

#include "koflvm/stack.h"

using namespace koflvm;

Stack::Stack(size_t capacity) : capacity_(capacity) {
  top_ = 0;
  values_ = static_cast<Value *>(calloc(capacity, sizeof(Value)));
}

int Stack::Top() {
  return top_;
}

Value *Stack::Get(int index) {
  return &values_[index];
}

bool Stack::Push(Value *value) {
  if (top_ >= capacity_) return false;

  values_[top_] = *value;
  top_++;

  return true;
}

Value *Stack::Peek() {
  if (top_ <= 0) return nullptr;

  return &values_[top_ - 1];
}

Value *Stack::Pop() {
  if (top_ <= 0) return nullptr;

  top_--;

  return &values_[top_];
}

void Stack::Dispose() {
  free(values_);
  free(this);
}
