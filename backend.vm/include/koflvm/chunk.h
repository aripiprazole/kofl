#ifndef KOFLVM_CHUNK_H
#define KOFLVM_CHUNK_H

#include <cinttypes>

#include "koflvm/heap.h"
#include "koflvm/value.h"

namespace koflvm {

enum Opcode {
  kOpRet,
  kOpConst,
  kOpNegate,
  kOpSum,
  kOpSub,
  kOpMult,
  kOpDiv,
  kOpTrue,
  kOpFalse,
  kOpNot,
  kOpConcat,
  kOpPop,
  kOpStoreGlobal,
  kOpAccessGlobal
};

class Chunk {
  int count_;
  int capacity_;
  int *lines_;
  uint32_t *code_;
  ValueArray *consts_;

 public:
  Chunk(int count, int capacity);

  uint32_t *Code();
  ValueArray *Consts();

  void Write(uint32_t op, int line);
  [[nodiscard]] int WriteConst(Value constant) const;

  [[nodiscard]] char *ToString() const;

  void Dispose();
};

}

#endif //KOFLVM_CHUNK_H