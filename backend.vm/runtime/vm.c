#include <stdlib.h>

#include "vm.h"

// vm functions>
vm_t *vm_create(flags_t flags) {
    vm_t *vm = malloc(sizeof(vm_t));

    vm->heap = heap_create(flags.memory);
    vm->pc = NULL;
    vm->table = table_create(10);
    vm->stack = stack_create(10);

    return vm;
}

interpret_result_t vm_eval(vm_t *vm, chunk_t *code) {
    return I_RESULT_ERROR;
}

void vm_dispose(vm_t *vm) {
    heap_dispose(vm->heap);
    stack_dispose(vm->stack);
    table_dispose(vm->table);

    if (vm->pc != NULL) {
        chunk_dispose(vm->pc);
    }

    free(vm);
}
