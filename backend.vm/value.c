#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "value.h"
#include "utils.h"

// value functions>
Value *ValueCreate(ValueType type, ObjectValue obj) {
  Value *value = malloc(sizeof(Value));

  value->type = type;
  value->as = obj;

  return value;
}

char *ValueToStr(Value *value) {
  char *str = malloc(80 * sizeof(char));

  switch (value->type) {
    case V_TYPE_BOOL:sprintf(str, "%d", value->as._bool);
      break;
    case V_TYPE_DOUBLE:sprintf(str, "%f", value->as._double);
      break;
    case V_TYPE_INT:
            sprintf(str, "%d", value->as._int);
            break;
        case V_TYPE_OBJ:
            str = "OBJECT";
            break;
        case V_TYPE_STR:
            str = AS_CSTR(value->as._obj);
            break;
    }

    return str;
}

Value *StrValueCreate(char *str) {
  string_t *string = malloc(sizeof(string_t));

  string->values = str;
  string->length = strlen(str);

  return ValueCreate(V_TYPE_STR, (ObjectValue) {
      ._obj = (Object *) string
  });
}

// value array functions>
ValueArray *ValueArrayCreate(int count, int capacity) {
    ValueArray *array = malloc(sizeof(ValueArray));

    array->capacity = capacity;
  array->count = count;
  array->values = malloc(capacity * sizeof(Value));

    return array;
}

void ValueArrayWrite(ValueArray *array, Value value) {
#ifdef VALUE_DEBUG
  printf("value_array_write(array = UNKNOWN, value = %s)\n", value_to_str(&value));
#endif

  if (array->capacity < array->count + 1) {
    size_t old_capacity = array->capacity;
    array->capacity = GROW_CAPACITY(array->capacity);
    array->values = GROW_ARRAY(Value, array->values, old_capacity, array->capacity);
  }

  array->values[array->count] = value;
    array->count++;
}

char *ValueArrayDump(ValueArray *array) {
    char *str = malloc(80 * sizeof(char));

    for (size_t i = 0; i < array->capacity; ++i) {
        sprintf(str, "%s, %s", str, ValueToStr(&array->values[i]));
    }

    *str += ']';

    return str;
}

void ValueArrayDispose(ValueArray *array) {
    free(array->values);
    free(array);
}
