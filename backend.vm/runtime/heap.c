#include <stdlib.h>
#include <unistd.h>

#include "heap.h"

mem_info_t *osalloc(size_t size) {
    mem_info_t *mem = sbrk(0);

    if (sbrk(size + sizeof(mem_info_t) - sizeof(value_t)) == (void *) -1) {
        return NULL;
    }

    return mem;
}

heap_t *create_heap(int size) {
    heap_t *heap = malloc(sizeof(heap_t));

    mem_info_t *root = malloc(size * sizeof(mem_info_t));

    root->next = NULL;
    root->size = size;
    root->is_free = 1;

    heap->root = root;

    return heap;
}

mem_info_t *heap_block_alloc(heap_t *heap, size_t rem) {
    mem_info_t *new_block = heap->root;
    ++new_block;

    new_block->is_free = 1;
    new_block->size = rem - sizeof(mem_info_t);
}

void *heap_alloc(heap_t *heap, size_t size) {
    mem_info_t *ptr;
    mem_info_t *block = heap->root;

    do {
        if (block->is_free && block->size >= size) {
            ptr =  (block ++);

            block->size = size;
            block->is_free = 0;

            if (block->next != NULL) {
                block->next = heap_block_alloc(heap, (block - 1)->size - size);
            }
        }
    } while (block != NULL);

    return &ptr->value;
}

_Bool *heap_free(heap_t *heap, void *ptr) {
    return 0;
}
