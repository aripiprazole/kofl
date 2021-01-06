#include <cstdlib>
#include <cstdio>
#include <cstring>

#include "koflvm/chunk.h"
#include "utils.h"

using namespace koflvm;

Chunk::Chunk(int count, int capacity) : count_(count), capacity_(capacity) {
  this->count_ = count;
  this->capacity_ = capacity;
  consts_ = new ValueArray(0, 0);
  code_ = static_cast<uint32_t *>(calloc(capacity, sizeof(Opcode)));
  lines_ = static_cast<int *>(calloc(capacity, sizeof(int)));
}

uint32_t *Chunk::Code() {
  return code_;
}

ValueArray *Chunk::Consts() {
  return consts_;
}

void Chunk::Write(uint32_t op, int line) {
  if (capacity_ < count_ + 1) {
    size_t old_capacity = capacity_;
    capacity_ = GROW_CAPACITY(capacity_);
    code_ = GROW_ARRAY(uint32_t, code_, old_capacity, capacity_);
    lines_ = GROW_ARRAY(int, lines_, old_capacity, capacity_);
  }

  code_[count_] = op;
  lines_[count_] = line;
  count_++;
}

int Chunk::WriteConst(Value constant) const {
  consts_->Write(constant);

  return consts_->Count() - 1;
}

char *Chunk::ToString() const {
  char *string = static_cast<char *>(calloc(1100, sizeof(char)));

  sprintf(string, "Chunk(count_ ) [");

  for (size_t i = 0; i < count_; i++) {
    uint32_t c = code_[i];
    int line = lines_[i];

    sprintf(string, "%s, {code_: %d, line: %d}", string, c, line);
  }

  strcat(string, "]");

  return string;
}

void Chunk::Dispose() {
  consts_->Dispose();
  free(lines_);
  free(code_);
  free(this);
}
