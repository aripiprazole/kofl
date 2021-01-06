#include <cstdlib>
#include <cinttypes>
#include <cstring>

#include "utils.h"
#include "koflvm/table.h"

using namespace koflvm;

/**
 * Will refill the table when its gets 75% full
 *
 * Its 75% 'cause the table never gets full, so
 * can never collide on every element
 */
#define TABLE_MAX_LOAD 0.75

/**
 * Implements the FNV-1a hash algorithm
 * @a http://www.isthe.com/chongo/tech/comp/fnv
 * @param key the string key
 * @param length the length of string
 * @return the hash of string
 */
long table_hash(const char *key, int length) {
    uint32_t hash = 2166136261u;
    uint32_t FNV_prime = 16777619;
    for (int i = 0; i < length; i++) {
        hash ^= key[i];
        hash *= FNV_prime;
    }

    return hash;
}

Table::Table(size_t capacity) : capacity_(capacity) {
  count_ = 0;
  nodes_ = static_cast<Node *>(calloc(capacity, sizeof(Node)));
}

Table::Node *Table::FindEntry(String *key) {
  uint32_t index = table_hash(key->values, key->length) % capacity_;
  Table::Node *tombstone = nullptr;

  while (true) {
    Table::Node *node = &nodes_[index];

    // return the tombstone if has one,
    // to reduce wasting space in the array
    // on table set
    if (node->key == nullptr) {
      if (node->value == nullptr) {
        return tombstone != nullptr ? tombstone : node;
      } else {
        if (tombstone == nullptr) {
          tombstone = node;
        }
      }
    }

    if (strcmp(node->key->values, key->values) == 0 || node->key == nullptr) {
      return node;
    }

    index = (index + 1) % capacity_;
  }
}

void Table::Adjust(size_t capacity) {
  auto *nodes = ALLOCATE(Table::Node, capacity);

  for (int i = 0; i < capacity; i++) {
    nodes[i].key = nullptr;
    nodes[i].value = nullptr;
  }

  // to mitigate collisions, this re build the node array
  count_ = 0;
  for (int i = 0; i < capacity_; i++) {
    Table::Node *node = &nodes_[i];
    if (node->key == nullptr) continue;

    Table::Node *dest = FindEntry(node->key);

    dest->key = node->key;
    dest->value = node->value;

    count_++;
  }

  free(nodes_);
  nodes_ = nodes;
  capacity_ = capacity;
}

void *Table::Get(String *key) {
  if (count_ == 0) return nullptr;

  Table::Node *node = FindEntry(key);
  if (node->key == nullptr) return nullptr;

  return node->value;
}

/**
 * @param key the string key
 * @return if the operation was successful
 */
bool Table::Delete(String *key) {
  if (count_ == 0) return false;

  Table::Node *node = FindEntry(key);
  if (node->key == nullptr) return false;

  node->key = nullptr;
  node->value = (void *) true;

  return true;
}

bool Table::Set(String *key, void *value) {
  Table::Node *node = FindEntry(key);

  if (count_ + 1 > (capacity_ + 1) * TABLE_MAX_LOAD) { // NOLINT(cppcoreguidelines-narrowing-conversions)
    Adjust(GROW_CAPACITY(capacity_));
  }

  bool is_new = node->key == nullptr;
  if (is_new && node->value == nullptr) {
    count_++;
  }

  node->key = key;
  node->value = value;

  return is_new;
}

void Table::Dispose() {
  free(nodes_);
  free(this);
}
