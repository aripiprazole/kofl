#ifndef RUNTIME_VALUE_H
#define RUNTIME_VALUE_H

typedef union value {
    int int_;
    double double_;
    char *string_;
} value_t;

#endif //RUNTIME_VALUE_H
