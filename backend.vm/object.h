#ifndef RUNTIME_OBJECT_H
#define RUNTIME_OBJECT_H

#include <stddef.h>

typedef enum object_type {
    OBJ_T_STR,
} object_type_t;

typedef struct object {
    struct object* next;
} object_t;

typedef struct string {
    object_t holder;
    size_t length;
    char *values;
} string_t;

#endif //RUNTIME_OBJECT_H
