#ifndef RUNTIME_OBJECT_H
#define RUNTIME_OBJECT_H

#include <stddef.h>

typedef enum object_type {
    OBJ_T_STR,
} ObjectType;

typedef struct {
    struct object* next;
} Object;

typedef struct string {
    Object holder;
    size_t length;
    char *values;
} string_t;

#endif //RUNTIME_OBJECT_H
