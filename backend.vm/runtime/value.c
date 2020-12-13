#include <stdlib.h>
#include <stdio.h>

#include "value.h"
#include "utils.h"

value_t *value_create(value_type_t type) {
    value_t *value = malloc(sizeof(value_t));

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

char *value_to_str(value_t *value) {
    char *str = NULL;

    switch (value->type) {
        case V_TYPE_BOOL:
            sprintf(str, "%d", value->obj._bool);
        case V_TYPE_DOUBLE:
            sprintf(str, "%f", value->obj._double);
        case V_TYPE_INT:
            sprintf(str, "%d", value->obj._int);
        case V_TYPE_OBJ:
            // TODO CREATE OBJ TYPE IN VM
            return NULL;
        case V_TYPE_STR:
            str = value->obj._string;
    }

    return str;
}

void value_array_write(value_array_t *array, value_t value) {
#ifdef VALUE_DEBUG
    printf("value_array_write(array = UNKNOWN, value = %s)\n", value_to_str(&value));
#endif

    if (array->capacity < array->count + 1) {
        size_t old_capacity = array->capacity;
        array->capacity = GROW_CAPACITY(array->capacity);
        array->values = GROW_ARRAY(value_t, array->values, old_capacity, array->capacity);
    }

    array->values[array->count] = value;
    array->count++;
}

char *value_array_dump(value_array_t *array) {
    char *str = "[";

    for (size_t i = 0; i < array->capacity; ++i) {
        sprintf(str, "%s, %s", str, value_to_str(&array->values[i]));
    }

    *str += ']';

    return str;
}
