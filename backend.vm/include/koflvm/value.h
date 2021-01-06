#ifndef KOFLVM_VALUE_H
#define KOFLVM_VALUE_H

#include "koflvm/heap.h"
#include "koflvm/object.h"

namespace koflvm {

#define NUM_VALUE(value) new Value(ValueType::kTypeDouble, \
    (ObjectValue) { ._double = (value) })

#define BOOL_VALUE(value) new Value(ValueType::kTypeBoolean, \
    (ObjectValue) { ._bool = (value) })

#define STR_VALUE(value) new Value(value)

#define AS_STR(value) ((String*) (value))
#define AS_CSTR(value) AS_STR((value))->values

enum ValueType {
  kTypeObject,
  kTypeInt,
  kTypeDouble,
  kTypeBoolean,
  kTypeString,
};

union ObjectValue {
  int _int;
  double _double;
  Object *_obj;
  bool _bool;
};

struct Value {
  ValueType type;
  ObjectValue as{};

  explicit Value(char *string);
  Value(ValueType type, ObjectValue obj);

  char *ToString();
  void Dispose();
};

class ValueArray {
  int count_;
  int capacity_;
  Value *values_;

 public:
  ValueArray(int count, int capacity);

  [[nodiscard]] int Count() const;
  Value* Get(int index);
  void Write(Value value);

  char *ToString();
  void Dispose();
};

}

#endif //KOFLVM_VALUE_H