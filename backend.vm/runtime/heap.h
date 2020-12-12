#ifndef RUNTIME_HEAP_H
#define RUNTIME_HEAP_H

#include <errno.h>
#include <stddef.h>

typedef struct mem_info {
    struct mem_info *next;
    size_t size;
    _Bool is_free;
} mem_info_t;

typedef struct {
    mem_info_t *root;
    size_t capacity;
    char *end;
} heap_t;

heap_t *create_heap(int size);

void *heap_alloc(heap_t *heap, size_t size);

_Bool *heap_free(heap_t * heap, void *ptr);

#endif //RUNTIME_HEAP_H
