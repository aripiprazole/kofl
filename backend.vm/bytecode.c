#include "bytecode.h"

value_t *parse_value(const char *bytes, int i) {
    return NUM_VALUE(10);
}

chunk_t *parse_chunk(const char *bytes) {
    char chunk_count = bytes[2];
    char chunk_capacity = bytes[3];
    char lines_size = bytes[4];
    char consts_size = bytes[5];
    chunk_t *chunk = chunk_create(chunk_count, chunk_capacity);

    int code_offset = 8 + chunk_count + 1;

    unsigned int code[chunk_capacity];
    int lines[lines_size];
    value_t *consts[consts_size];

    for (int i = 8; i < code_offset - 1; ++i) {
        code[i] = bytes[i];
    }

    for (int i = code_offset; i < code_offset + lines_size - 1; ++i) {
        lines[i] = bytes[i];
    }

    for (int i = code_offset + lines_size; i < code_offset + lines_size + consts_size - 1; ++i) {
        consts[i] = parse_value(bytes, i);
    }


    return chunk;
}
