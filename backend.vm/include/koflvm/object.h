#ifndef KOFLVM_OBJECT_H
#define KOFLVM_OBJECT_H

#include <cstddef>

namespace koflvm {
enum ObjectType {
  OBJ_T_STR,
};

struct Object {
  struct Object *next;
};

struct String : public Object {
  size_t length;
  char *values;
};
}

#endif //KOFLVM_OBJECT_H