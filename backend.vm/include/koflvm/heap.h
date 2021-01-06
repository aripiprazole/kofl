#ifndef KOFLVM_HEAP_H
#define KOFLVM_HEAP_H

#include <cstddef>

namespace koflvm {
/**
 * The vm's heap_. Should be used to handle ALL vm
 * info.
 */
class Heap {
  size_t capacity_;

 public:
  virtual void *Alloc(unsigned long size) = 0;
  virtual bool Free(void *ptr) = 0;

  virtual void Dispose() = 0;
  explicit Heap(size_t capacity) : capacity_(capacity) {};
};

class KoflHeap : public Heap {
  struct MemBlock {
    struct MemBlock *next{};
    unsigned long size{};
    bool is_free{};
  };

  struct MemBlock *root_{};
  size_t capacity_{};
  char *end_{};

 public:
  explicit KoflHeap(unsigned long capacity);

  void *Alloc(unsigned long size) override;
  bool Free(void *ptr) override;

  void Dispose() override;
};
}

#endif //KOFLVM_HEAP_H