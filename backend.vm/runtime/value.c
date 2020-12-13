#include <stdlib.h>

#include "value.h"
#include "utils.h"

value_t* value_create(value_type_t type) {
    value_t* value = malloc(sizeof(value_t));

    value->type = type;

    return value;
}

value_array_t *value_array_create(int count, int capacity) {
    value_array_t *array = malloc(sizeof(value_array_t));

    array->capacity = capacity;
    array->count = count;
    array->values = malloc(capacity * sizeof(value_t));

    return array;
}

void value_array_write(value_array_t *array, value_t value) {
    if (array->capacity < array->count + 1) {
        size_t old_capacity = array->capacity;
        array->capacity = GROW_CAPACITY(array->capacity);
        array->values = GROW_ARRAY(value_t, array->values, old_capacity, array->capacity);
    }

    array->values[array->count] = value;
    array->count++;
}
