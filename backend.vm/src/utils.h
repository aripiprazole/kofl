#ifndef KOFLVM_UTILS_H
#define KOFLVM_UTILS_H

#include <stddef.h>
#include <stdlib.h>

#define VM_DEBUG_TRACE

#define GROW_CAPACITY(capacity) ((capacity) < 8 ? 8 : (capacity) * 2)
#define GROW_ARRAY(type, ptr, old_count, new_count) \
    (type*) reallocate(ptr, sizeof(type) * (old_count), \
        sizeof(type) * (new_count))
#define ALLOCATE(type, count) (type*) reallocate(NULL, 0, sizeof(type) * (count))


void *reallocate(void *ptr, size_t old_size, size_t new_size);

#endif //KOFLVM_UTILS_H
