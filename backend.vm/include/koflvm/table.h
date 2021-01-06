#ifndef KOFLVM_TABLE_H
#define KOFLVM_TABLE_H

#include <cstddef>

#include "koflvm/object.h"

namespace koflvm {

class Table {
  struct Node {
    String *key;
    void *value;
  };

  int count_;
  size_t capacity_;
  Node *nodes_;

  // INTERNALS

  /**
   * Implementation details:
   *   - Its returns the node when the found value
   *   has NULL key or equals @var key
   *
   *   - If the found value kas a different key
   *   than @var key then this will start probing
   *   the next element on the @var table->nodes_
   *   array, if not found, then will search the
   *   next
   *
   * @param key the string key
   * @return the table node
   */
  Node *FindEntry(String *key);

  void Adjust(size_t capacity);
  //

 public:
  explicit Table(size_t capacity);

  void *Get(String *key);
  bool Delete(String *key);

  /**
   * @param key the string key
   * @param length the string key length
   * @param value the value
   * @return if the node is new
   */
  bool Set(String *key, void *value);

  void Dispose();
};

}

#endif //KOFLVM_TABLE_H