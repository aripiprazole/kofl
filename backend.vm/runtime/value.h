#ifndef RUNTIME_VALUE_H
#define RUNTIME_VALUE_H

#include "heap.h"

typedef enum value_type {
    V_TYPE_OBJ,
    V_TYPE_INT,
    V_TYPE_DOUBLE,
    V_TYPE_BOOL,
    V_TYPE_STR,
} value_type_t;

typedef struct value {
    value_type_t type;
    union {
        int _int;
        double _double;
        char *_string;
        _Bool _bool;
    } obj;
} value_t;

typedef struct value_array {
    int count;
    int capacity;
    value_t *values;
} value_array_t;

// value functions>
value_t *value_create(value_type_t type);

void value_dispose(value_t *value);

char *value_to_str(value_t *value);

// value_array functions>
value_array_t *value_array_create(int count, int capacity);

void value_array_write(value_array_t *array, value_t value);

char *value_array_dump(value_array_t *array);

void value_array_dispose(value_array_t *array);

#endif //RUNTIME_VALUE_H
