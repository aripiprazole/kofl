#include <stdlib.h>

#include "heap.h"

// heap functions>
Heap *HeapCreate(size_t size) {
  Heap *heap = malloc(sizeof(Heap));
  heap->capacity = size;

  mem_info_t *root = malloc(size * sizeof(mem_info_t));

  root->next = NULL;
  root->size = size;
  root->is_free = 1;

  heap->root = root;

  return heap;
}

mem_info_t *HeapBlockAlloc(Heap *heap, size_t rem) {
  mem_info_t *new_block = heap->root;
  ++new_block;

  new_block->is_free = 1;
  new_block->size = rem - sizeof(mem_info_t);

  return new_block;
}

void *HeapAlloc(Heap *heap, size_t size) {
  mem_info_t *ptr = NULL;
  mem_info_t *block = heap->root;

  do {
    if (block->is_free && block->size >= size) {
      ptr = (block++);

      block->size = size;
      block->is_free = 0;

      if (block->next != NULL) {
        block->next = HeapBlockAlloc(heap, (block - 1)->size - size);
      }
      break;
    }
  } while (block != NULL);

    return ptr;
}

bool *HeapFree(Heap *heap, void *ptr) {
    return false;
}

void HeapDispose(Heap *heap) {
  free(heap->root);
  free(heap->end);
  free(heap);
}
