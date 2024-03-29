#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>

#include "vm.h"
#include "utils.h"

// vm functions>
Vm *VmCreate(Flags flags) {
  Vm *vm = malloc(sizeof(Vm));

  vm->pc = NULL;
  vm->chunk = NULL;
  vm->objects = NULL;
  vm->strings = table_create(10);
  vm->globals = table_create(10);
  vm->heap = HeapCreate(flags.memory);
  vm->stack = StackCreate(10);

  return vm;
}

InterpretResult VmEvalImpl(Vm *vm) {
  while (true) {
#ifdef VM_DEBUG_TRACE
    printf("=>> ");

    for (int i = 0; i < vm->stack->top; i++) {
      printf("[ '%s' ]", ValueToStr(&vm->stack->values[i]));
    }

    printf("\n");

    // TODO DISASSEMBLE CODE HERE
#endif

#define READ_INST() (*vm->pc++)
#define READ_NUMBER() (stack_pop(vm->stack)->as._double)
#define READ_BOOL() (stack_pop(vm->stack)->as._bool)
#define READ_OBJ() (stack_pop(vm->stack)->as._obj)

    Opcode op = READ_INST();

        switch (op) {
            // handle ret op
            case OP_RET:
#ifdef VM_DEBUG_TRACE
            printf("RET %s\n", ValueToStr(StackPop(vm->stack)));
#endif

            return kResultOK;

                // handle negate op
            case OP_NEGATE: {
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("NEGATE %f\n", d0);
#endif

              StackPush(vm->stack, NUM_VALUE(-d0));
                break;
            }


                // handle sum op
            case OP_SUM: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("SUM %f %f\n", d0, d1);
#endif

              StackPush(vm->stack, NUM_VALUE(d0 + d1));
                break;
            }
                // handle sub op
            case OP_SUB: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("SUB %f %f\n", d0, d1);
#endif

              StackPush(vm->stack, NUM_VALUE(d0 - d1));
                break;
            }

                // handle mult op
            case OP_MULT: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("MULT %f %f\n", d0, d1);
#endif

              StackPush(vm->stack, NUM_VALUE(d0 * d1));
                break;
            }

                // handle div op
            case OP_DIV: {
                double d1 = READ_NUMBER();
                double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
                printf("DIV %f %f\n", d0, d1);
#endif

              StackPush(vm->stack, NUM_VALUE(d0 / d1));
                break;
            }

                // handle true op
            case OP_TRUE: {
#ifdef VM_DEBUG_TRACE
                printf("TRUE\n");
#endif

              StackPush(vm->stack, BOOL_VALUE(true));
                break;
            }
                // handle false op
            case OP_FALSE: {
#ifdef VM_DEBUG_TRACE
                printf("FALSE\n");
#endif

              StackPush(vm->stack, BOOL_VALUE(false));
                break;
            }
                // handle not op
            case OP_NOT: {
                bool b0 = READ_BOOL();

#ifdef VM_DEBUG_TRACE
                printf("NOT %d\n", b0);
#endif

              StackPush(vm->stack, BOOL_VALUE(!b0));
                break;
            }
                // handle concat op
            case OP_CONCAT: {
                char *s1 = AS_CSTR(READ_OBJ());
                char *s0 = AS_CSTR(READ_OBJ());

#ifdef VM_DEBUG_TRACE
                printf("CONCAT %s %s\n", s0, s1);
#endif

              StackPush(vm->stack, STR_VALUE(strcat(s0, s1)));
                break;
            }
                // handle pop op
            case OP_POP: {
              Value *v = StackPop(vm->stack);

#ifdef VM_DEBUG_TRACE
              printf("POP %s\n", ValueToStr(v));
#endif

                break;
            }
                // handle store global op
            case OP_STORE_GLOBAL: {
              Value *v = StackPop(vm->stack);
              string_t *name = AS_STR(READ_OBJ());

              table_set(vm->globals, name, v);

#ifdef VM_DEBUG_TRACE
              printf("STORE_GLOBAL '%s' '%s'\n", name->values, ValueToStr(v));
#endif

              break;
            }
                // handle access global op
            case OP_ACCESS_GLOBAL: {
              string_t *name = AS_STR(READ_OBJ());

#ifdef VM_DEBUG_TRACE
              printf("ACCESS_GLOBAL %s\n", name->values);
#endif

              Value *v = table_get(vm->globals, name);
              if (v == NULL) return kResultNullPointer;

              StackPush(vm->stack, v);

              break;
            }
                // handle const op
            case OP_CONST: {
              Value *v = &vm->chunk->consts->values[READ_INST()];

#ifdef VM_DEBUG_TRACE
              printf("CONST %s\n", ValueToStr(v));
#endif

              StackPush(vm->stack, v);

                break;
            }

            default: {
              return kResultError;
            }
        }
#undef READ_INST
#undef READ_BOOL
#undef READ_OBJ
#undef READ_NUMBER
    }
}

InterpretResult VmEval(Vm *vm, Chunk *chunk) {
  vm->pc = chunk->code;
  vm->chunk = chunk;

  return VmEvalImpl(vm);
}

void VmDisposeObjects(Vm *vm) {
}

void VmDispose(Vm *vm) {
  HeapDispose(vm->heap);
  StackDispose(vm->stack);
  table_dispose(vm->globals);
  table_dispose(vm->strings);

  if (vm->objects != NULL) {
    VmDisposeObjects(vm);
  }

  if (vm->chunk != NULL) {
    free(vm->chunk);
  }

  if (vm->pc != NULL) {
    free(vm->pc);
  }

  free(vm);
}
