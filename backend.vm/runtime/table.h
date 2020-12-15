#ifndef RUNTIME_TABLE_H
#define RUNTIME_TABLE_H

#include <stddef.h>
#include <stdbool.h>

#include "object.h"

typedef struct table_node {
    string_t *key;
    void *value;
} table_node_t;

typedef struct table {
    int count;
    size_t capacity;
    table_node_t *nodes;
} table_t;

table_t *table_create(size_t capacity);

bool table_set(table_t *table, string_t *key, void *value);

bool table_remove(table_t *table, string_t *key);

void *table_get(table_t *table, string_t *key);

void table_dispose(table_t* table);

#endif //RUNTIME_TABLE_H
