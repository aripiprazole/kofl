#include <cstdlib>
#include <cstdio>
#include <cstring>

#include "koflvm/value.h"
#include "utils.h"

using namespace koflvm;

Value::Value(ValueType type, ObjectValue as) : type(type), as(as) {
}

Value::Value(char *string) : type(ValueType::kTypeString) {
  auto *real_string = static_cast<String *>(malloc(sizeof(String)));

  real_string->values = string;
  real_string->length = strlen(string);

  as._obj = real_string;
}

void Value::Dispose() {
  free(this);
}

char *Value::ToString() {
  char *str = static_cast<char *>(malloc(80 * sizeof(char)));

  switch (type) {
    case kTypeBoolean:sprintf(str, "%d", as._bool);
      break;
    case kTypeDouble:sprintf(str, "%f", as._double);
      break;
    case kTypeInt:sprintf(str, "%d", as._int);
      break;
    case kTypeObject:str = "OBJECT";
      break;
    case kTypeString:str = AS_CSTR(as._obj);
      break;
  }

  return str;
}

// value array functions>
ValueArray::ValueArray(int count, int capacity) {
  capacity_ = capacity;
  count_ = count;
  values_ = static_cast<Value *>(calloc(capacity, sizeof(Value)));
}

int ValueArray::Count() const {
  return count_;
}

Value *ValueArray::Get(int index) {
  return &values_[index];
}

void ValueArray::Write(Value value) {
  if (capacity_ < count_ + 1) {
    size_t old_capacity = capacity_;
    capacity_ = GROW_CAPACITY(capacity_);
    values_ = GROW_ARRAY(Value, values_, old_capacity, capacity_);
  }

  values_[count_] = value;
  count_++;
}

char *ValueArray::ToString() {
  auto *string = static_cast<char *>(calloc(80, sizeof(char)));

  for (size_t i = 0; i < capacity_; ++i) {
    sprintf(string, "%s, %s", string, values_[i].ToString());
  }

  *string += ']';

  return string;
}

void ValueArray::Dispose() {
  free(values_);
  free(this);
}
