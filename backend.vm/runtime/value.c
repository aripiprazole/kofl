#include <stdlib.h>
#include "value.h"

value_array_t *value_array_create(int count, int capacity) {
    value_array_t *array = malloc(sizeof(value_array_t));

    array->capacity = capacity;
    array->count = count;
    array->values = malloc(capacity * sizeof(value_t));

    return array;
}

void value_array_write(value_t value) {
    // TODO
}
