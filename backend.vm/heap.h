#ifndef RUNTIME_HEAP_H
#define RUNTIME_HEAP_H

#include <errno.h>
#include <stddef.h>
#include <stdbool.h>

typedef struct mem_info {
    struct mem_info *next;
    size_t size;
    _Bool is_free;
} mem_info_t;

typedef struct heap {
    mem_info_t *root;
    size_t capacity;
    char *end;
} Heap;

// heap functions>
Heap *HeapCreate(size_t size);

void *HeapAlloc(Heap *heap, size_t size);

bool *HeapFree(Heap *heap, void *ptr);

void HeapDispose(Heap *heap);

#endif //RUNTIME_HEAP_H
