#ifndef RUNTIME_TABLE_H
#define RUNTIME_TABLE_H

#include <stddef.h>
#include <stdbool.h>

typedef struct table_node {
    char *key;
    int length;
    void *value;
} table_node_t;

typedef struct table {
    size_t capacity;
    size_t count;
    table_node_t *nodes;
} table_t;

table_t *table_create(size_t capacity);

bool table_set(table_t *table, char *key, int length, void *value);

bool table_remove(table_t *table, char *key, int length);

void *table_get(table_t *table, char *key, int length);

#endif //RUNTIME_TABLE_H