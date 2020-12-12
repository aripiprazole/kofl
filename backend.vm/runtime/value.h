#ifndef RUNTIME_VALUE_H
#define RUNTIME_VALUE_H

#include "heap.h"

typedef union value {
    int int_;
    double double_;
    char *string_;
} value_t;

typedef struct value_array {
    int count;
    int capacity;
    value_t *values;
} value_array_t;

value_array_t* value_array_create(int count, int capacity);

void value_array_write(value_t value);

#endif //RUNTIME_VALUE_H
