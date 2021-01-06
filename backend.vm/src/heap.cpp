#include "koflvm/heap.h"

using namespace koflvm;

KoflHeap::KoflHeap(size_t capacity) : Heap(capacity) {
}

void *KoflHeap::Alloc(unsigned long size) {
  return nullptr;
}

bool KoflHeap::Free(void *ptr) {
  return false;
}

void KoflHeap::Dispose() {

}

