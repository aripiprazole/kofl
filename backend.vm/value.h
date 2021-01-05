#ifndef RUNTIME_VALUE_H
#define RUNTIME_VALUE_H

#include <stdbool.h>

#include "heap.h"
#include "object.h"

#define NUM_VALUE(value) value_create(V_TYPE_DOUBLE, \
    (ObjectValue) { ._double = (value) })

#define BOOL_VALUE(value) value_create(V_TYPE_BOOL, \
    (ObjectValue) { ._bool = (value) })

#define STR_VALUE(value) value_str_create(value)

#define AS_STR(value) ((string_t*) (value))
#define AS_CSTR(value) AS_STR((value))->values

typedef enum {
  V_TYPE_OBJ,
  V_TYPE_INT,
  V_TYPE_DOUBLE,
  V_TYPE_BOOL,
  V_TYPE_STR,
} ValueType;

typedef union {
  int _int;
  double _double;
  Object *_obj;
  bool _bool;
} ObjectValue;

typedef struct {
  ValueType type;
  ObjectValue as;
} Value;

typedef struct {
  int count;
  int capacity;
  Value *values;
} ValueArray;

// value functions>
Value *ValueCreate(ValueType type, ObjectValue obj);

Value *StrValueCreate(char *str);

void ValueDispose(Value *value);

char *ValueToStr(Value *value);

// value_array functions>
ValueArray *ValueArrayCreate(int count, int capacity);

void ValueArrayWrite(ValueArray *array, Value value);

char *ValueArrayDump(ValueArray *array);

void ValueArrayDispose(ValueArray *array);

#endif //RUNTIME_VALUE_H
