#include <stdlib.h>
#include <stdio.h>
#include <inttypes.h>
#include <string.h>

#include "utils.h"
#include "table.h"

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

table_t *table_create(size_t capacity) {
    table_t *table = malloc(sizeof(table_t));

    table->capacity = capacity;
    table->count = 0;
    table->nodes = calloc(capacity, sizeof(table_node_t));

    return table;
}

/**
 * Implementation details:
 *   - Its returns the node when the found value
 *   has NULL key or equals @var key

 *   - If the found value kas a different key
 *   than @var key then this will start probing
 *   the next element on the @var table->nodes
 *   array, if not found, then will search the
 *   next
 *
 * @param table the target table
 * @param key the string key
 * @param length the string key length
 * @return the table node
 */
table_node_t *table_find_entry(table_t *table, char *key, int length) {
    uint32_t index = table_hash(key, length) % table->capacity;
    table_node_t *tombstone = NULL;

    while (true) {
        table_node_t *node = &table->nodes[index];

        // return the tombstone if has one,
        // to reduce wasting space in the array
        // on table set
        if (node->key == NULL) {
            if (node->value == NULL) {
                return tombstone != NULL ? tombstone : node;
            } else {
                if (tombstone == NULL) {
                    tombstone = node;
                }
            }
        }

        if (strcmp(node->key, key) == 0 || node->key == NULL) {
            return node;
        }

        index = (index + 1) % table->capacity;
    }
}

void table_adjust(table_t *table, size_t capacity) {
    table_node_t *nodes = ALLOCATE(table_node_t, capacity);

    for (int i = 0; i < capacity; i++) {
        nodes[i].key = NULL;
        nodes[i].value = NULL;
    }

    // to mitigate collisions, this re build the node array
    table->count = 0;
    for (int i = 0; i < table->capacity; i++) {
        table_node_t *node = &table->nodes[i];
        if (node->key == NULL) continue;

        table_node_t *dest = table_find_entry(table, node->key, node->length);

        dest->key = node->key;
        dest->length = node->length;
        dest->value = node->value;

        table->count++;
    }

    free(table->nodes);
    table->nodes = nodes;
    table->capacity = capacity;
}

void *table_get(table_t *table, char *key, int length) {
    if (table->count == 0) return NULL;

    table_node_t *node = table_find_entry(table, key, length);
    if (node->key == NULL) return NULL;

    return node->value;
}

/**
 * @param table the target table
 * @param key the string key
 * @param length the string key length
 * @return if the operation was successful
 */
bool table_remove(table_t *table, char *key, int length) {
    if (table->count == 0) return false;

    table_node_t *node = table_find_entry(table, key, length);
    if (node->key == NULL) return false;

    node->key = NULL;
    node->value = (void *) true;

    return true;
}

/**
 * @param table the target table
 * @param key the string key
 * @param length the string key length
 * @param value the value
 * @return if the node is new
 */
bool table_set(table_t *table, char *key, int length, void *value) {
    table_node_t *node = table_find_entry(table, key, length);

    if (table->count + 1 > (table->capacity + 1) * TABLE_MAX_LOAD) { // NOLINT(cppcoreguidelines-narrowing-conversions)
        table_adjust(table, GROW_CAPACITY(table->capacity));
    }

    bool is_new = node->key == NULL;
    if (is_new && node->value == NULL) {
        table->count++;
    }

    node->key = key;
    node->length = length;
    node->value = value;

    return is_new;
}

void table_dispose(table_t *table) {
    free(table->nodes);
    free(table);
}
