#ifndef RUNTIME_VALUE_H
#define RUNTIME_VALUE_H

#include <stdbool.h>

#include "heap.h"

#define NUM_VALUE(value) value_create(V_TYPE_DOUBLE, \
    (obj_as_t) { ._double = (value) })

#define BOOL_VALUE(value) value_create(V_TYPE_BOOL, \
    (obj_as_t) { ._bool = (value) })

#define STR_VALUE(value) value_create(V_TYPE_STR, \
    (obj_as_t) { ._string = (value) })

typedef enum value_type {
    V_TYPE_OBJ,
    V_TYPE_INT,
    V_TYPE_DOUBLE,
    V_TYPE_BOOL,
    V_TYPE_STR,
} value_type_t;

typedef union obj_as {
    int _int;
    double _double;
    char *_string;
    bool _bool;
} obj_as_t;

typedef struct value {
    value_type_t type;
    obj_as_t obj;
} value_t;

typedef struct value_array {
    int count;
    int capacity;
    value_t *values;
} value_array_t;

// value functions>
value_t *value_create(value_type_t type, obj_as_t obj);

void value_dispose(value_t *value);

char *value_to_str(value_t *value);

// value_array functions>
value_array_t *value_array_create(int count, int capacity);

void value_array_write(value_array_t *array, value_t value);

char *value_array_dump(value_array_t *array);

void value_array_dispose(value_array_t *array);

#endif //RUNTIME_VALUE_H
