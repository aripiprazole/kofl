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
} heap_t;

// heap functions>
heap_t *heap_create(size_t size);

void *heap_alloc(heap_t *heap, size_t size);

bool *heap_free(heap_t *heap, void *ptr);

void heap_dispose(heap_t *heap);

#endif //RUNTIME_HEAP_H
