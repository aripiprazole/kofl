#include <stdlib.h>

#include "vm.h"

vm_t *vm_create(flags_t flags) {
    vm_t *vm = malloc(sizeof(vm_t));

    vm->heap = heap_create(flags.memory);
    vm->pc = NULL;
    vm->table = table_create(10);
    vm->stack = NULL; // TODO

    return vm;
}

interpret_result_t vm_eval(vm_t *vm, chunk_t *code) {
    return I_RESULT_ERROR;
}
