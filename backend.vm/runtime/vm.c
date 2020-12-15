#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>

#include "vm.h"
#include "utils.h"

// vm functions>
vm_t *vm_create(flags_t flags) {
    vm_t *vm = malloc(sizeof(vm_t));

    vm->pc = NULL;
    vm->chunk = NULL;
    vm->objects = NULL;
    vm->heap = heap_create(flags.memory);
    vm->globals = table_create(10);
    vm->stack = stack_create(10);

    return vm;
}

interpret_result_t vm_eval_impl(vm_t *vm) {
    while (true) {
#ifdef VM_DEBUG_TRACE
        printf("=>> ");

        for (int i = 0; i < vm->stack->top; i++) {
            printf("[ '%s' ]", value_to_str(&vm->stack->values[i]));
        }

        printf("\n");

        // TODO DISASSEMBLE CODE HERE
#endif

#define READ_INST() (*vm->pc++)
#define READ_NUMBER() (stack_pop(vm->stack)->as._double)
#define READ_BOOL() (stack_pop(vm->stack)->as._bool)
#define READ_OBJ() (stack_pop(vm->stack)->as._obj)

        opcode_t op = READ_INST();

        switch (op) {
            // handle ret op
            case OP_RET:
#ifdef VM_DEBUG_TRACE
                printf("RET %s\n", value_to_str(stack_pop(vm->stack)));
#endif

                return I_RESULT_OK;

                // handle negate op
            case OP_NEGATE: {
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("NEGATE %f\n", d0);
#endif

                stack_push(vm->stack, NUM_VALUE(-d0));
                break;
            }


                // handle sum op
            case OP_SUM: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("SUM %f %f\n", d0, d1);
#endif

                stack_push(vm->stack, NUM_VALUE(d0 + d1));
                break;
            }
                // handle sub op
            case OP_SUB: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("SUB %f %f\n", d0, d1);
#endif

                stack_push(vm->stack, NUM_VALUE(d0 - d1));
                break;
            }

                // handle mult op
            case OP_MULT: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("MULT %f %f\n", d0, d1);
#endif

                stack_push(vm->stack, NUM_VALUE(d0 * d1));
                break;
            }

                // handle div op
            case OP_DIV: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("DIV %f %f\n", d0, d1);
#endif

                stack_push(vm->stack, NUM_VALUE(d0 / d1));
                break;
            }

                // handle true op
            case OP_TRUE: {
#ifdef VM_DEBUG_TRACE
                printf("TRUE\n");
#endif

                stack_push(vm->stack, BOOL_VALUE(true));
                break;
            }
                // handle false op
            case OP_FALSE: {
#ifdef VM_DEBUG_TRACE
                printf("FALSE\n");
#endif

                stack_push(vm->stack, BOOL_VALUE(false));
                break;
            }
                // handle not op
            case OP_NOT: {
                bool b0 = READ_BOOL();

#ifdef VM_DEBUG_TRACE
                printf("NOT %d\n", b0);
#endif

                stack_push(vm->stack, BOOL_VALUE(!b0));
                break;
            }
                // handle concat op
            case OP_CONCAT: {
                char *s1 = AS_CSTR(READ_OBJ());
                char *s0 = AS_CSTR(READ_OBJ());

#ifdef VM_DEBUG_TRACE
                printf("CONCAT %s %s\n", s0, s1);
#endif

                stack_push(vm->stack, STR_VALUE(strcat(s0, s1)));
                break;
            }
                // handle pop op
            case OP_POP: {
                value_t *v = stack_pop(vm->stack);

#ifdef VM_DEBUG_TRACE
                printf("POP %s\n", value_to_str(v));
#endif

                break;
            }
                // handle store global op
            case OP_STORE_GLOBAL: {
                value_t *v = stack_pop(vm->stack);
                string_t *name = AS_STR(READ_OBJ());

                table_set(vm->globals, name->values, name->length, v);

#ifdef VM_DEBUG_TRACE
                printf("STORE_GLOBAL '%s' '%s'\n", name->values, value_to_str(v));
#endif

                break;
            }
                // handle access global op
            case OP_ACCESS_GLOBAL: {
                string_t *name = AS_STR(READ_OBJ());

#ifdef VM_DEBUG_TRACE
                printf("ACCESS_GLOBAL %s\n", name->values);
#endif

                value_t *v = table_get(vm->globals, name->values, name->length);
                if (v == NULL) return I_NULL_POINTER;

                stack_push(vm->stack, v);

                break;
            }
                // handle const op
            case OP_CONST: {
                value_t *v = &vm->chunk->consts->values[READ_INST()];

#ifdef VM_DEBUG_TRACE
                printf("CONST %s\n", value_to_str(v));
#endif

                stack_push(vm->stack, v);

                break;
            }

            default: {
                return I_RESULT_ERROR;
            }
        }
#undef READ_INST
#undef READ_BOOL
#undef READ_OBJ
#undef READ_NUMBER
    }
}

interpret_result_t vm_eval(vm_t *vm, chunk_t *chunk) {
    vm->pc = chunk->code;
    vm->chunk = chunk;

    return vm_eval_impl(vm);
}

void vm_dispose_objects(vm_t *vm) {
}

void vm_dispose(vm_t *vm) {
    heap_dispose(vm->heap);
    stack_dispose(vm->stack);
    table_dispose(vm->globals);

    if (vm->chunk != NULL) {
        free(vm->chunk);
    }

    if (vm->pc != NULL) {
        free(vm->pc);
    }

    free(vm);
}
