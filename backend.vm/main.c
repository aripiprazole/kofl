#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

#include "vm.h"
#include "bytecode.h"
#include "debug.h"

int print_help() {
    printf("Usage: koflvm <file> [--verbose] [--memory <memory>]\n");

    return EXIT_FAILURE;
}

char *get_arg(char *arg_name, int argc, char **argv) {
    for (int i = 0; i < argc; ++i) {
        if (strcmp(arg_name, argv[i]) == 0) {
            if (i + 1 > argc) return NULL;

            return argv[i + 1];
        }
    }

    return NULL;
}

char *get_arg_or(char *arg_name, char *def, int argc, char **argv) {
    char *arg = get_arg(arg_name, argc, argv);

    if (arg == NULL) return def;

    return arg;
}

char *read_file(char *file_path) {
    FILE *file = fopen(file_path, "rb");

    if(file == NULL) return NULL;

    fseek(file, 0, SEEK_END);
    long len = ftell(file);
    rewind(file);

    char *buffer = malloc(len * sizeof(char));

    fread(buffer, len, 1, file);
    fclose(file);

    return buffer;
}

int main(int argc, char **argv) {
    if (argc < 1) return print_help();

    char *file_path = argv[1];

    if(file_path == NULL) return print_help();

    bool verbose = get_arg("--verbose", argc, argv) != NULL;
    bool disassemble = get_arg("--disassemble", argc, argv) != NULL;
    size_t memory = atol(get_arg_or("--memory", "512", argc, argv));

    flags_t flags = {
            .memory = memory,
            .verbose = verbose
    };

    char *bytes = read_file(file_path);
    if (bytes == NULL) {
        printf("Failed to read file %s\n", file_path);

        return EXIT_FAILURE;
    }

    chunk_t *bytecode = parse_chunk(bytes);
    if (bytecode == NULL) {
        printf("Failed to read bytecode\n");

        return EXIT_FAILURE;
    }
    free(bytes);

    printf("Kofl vm\n\n");

    if (disassemble) {
        chunk_disassemble(bytecode);
    }

    vm_t *vm = vm_create(flags);
    vm_eval(vm, bytecode);
    vm_dispose(vm);
}