#include <cstdlib>
#include <cstdio>
#include <cstring>

#include "koflvm/vm.h"
#include "utils.h"

using namespace koflvm;

// vm functions>
VM::VM(Flags flags) {
  pc_ = nullptr;
  chunk_ = nullptr;
  objects_ = nullptr;
  strings_ = new Table(10);
  globals_ = new Table(10);
  heap_ = new KoflHeap(flags.memory);
  stack_ = new Stack(10);
}

InterpretResult VM::Eval(Chunk *pc) {
  pc_ = reinterpret_cast<Opcode *>(pc->Code());
  chunk_ = pc;

  while (true) {
#ifdef VM_DEBUG_TRACE
    printf("=>> ");

    for (int i = 0; i < stack_->Top(); i++) {
      printf("[ '%s' ]", stack_->Get(i)->ToString());
    }

    printf("\n");

    // TODO DISASSEMBLE CODE HERE
#endif

#define READ_INST() (*pc_++)
#define READ_NUMBER() (stack_->Pop()->as._double)
#define READ_BOOL() (stack_->Pop()->as._bool)
#define READ_OBJ() (stack_->Pop()->as._obj)

    Opcode op = READ_INST();

    switch (op) {
      // handle ret op
      case kOpRet:
#ifdef VM_DEBUG_TRACE
        printf("RET %s\n", stack_->Pop()->ToString());
#endif

        return kResultOK;

        // handle negate op
      case kOpNegate: {
        double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
        printf("NEGATE %f\n", d0);
#endif

        stack_->Push(NUM_VALUE(-d0));
        break;
      }


        // handle sum op
      case kOpSum: {
        double d1 = READ_NUMBER();
        double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
        printf("SUM %f %f\n", d0, d1);
#endif

        stack_->Push(NUM_VALUE(d0 + d1));
        break;
      }
        // handle sub op
      case kOpSub: {
        double d1 = READ_NUMBER();
        double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
        printf("SUB %f %f\n", d0, d1);
#endif

        stack_->Push(NUM_VALUE(d0 - d1));
        break;
      }

        // handle mult op
      case kOpMult: {
        double d1 = READ_NUMBER();
        double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
        printf("MULT %f %f\n", d0, d1);
#endif

        stack_->Push(NUM_VALUE(d0 * d1));
        break;
      }

        // handle div op
      case kOpDiv: {
        double d1 = READ_NUMBER();
        double d0 = READ_NUMBER();

#ifdef VM_DEBUG_TRACE
        printf("DIV %f %f\n", d0, d1);
#endif

        stack_->Push(NUM_VALUE(d0 / d1));
        break;
      }

        // handle true op
      case kOpTrue: {
#ifdef VM_DEBUG_TRACE
        printf("TRUE\n");
#endif

        stack_->Push(BOOL_VALUE(true));
        break;
      }
        // handle false op
      case kOpFalse: {
#ifdef VM_DEBUG_TRACE
        printf("FALSE\n");
#endif

        stack_->Push(BOOL_VALUE(false));
        break;
      }
        // handle not op
      case kOpNot: {
        bool b0 = READ_BOOL();

#ifdef VM_DEBUG_TRACE
        printf("NOT %d\n", b0);
#endif

        stack_->Push(BOOL_VALUE(!b0));
        break;
      }
        // handle concat op
      case kOpConcat: {
        char *s1 = AS_CSTR(READ_OBJ());
        char *s0 = AS_CSTR(READ_OBJ());

#ifdef VM_DEBUG_TRACE
        printf("CONCAT %s %s\n", s0, s1);
#endif

        stack_->Push(STR_VALUE(strcat(s0, s1)));
        break;
      }
        // handle pop op
      case kOpPop: {
        Value *v = stack_->Pop();

#ifdef VM_DEBUG_TRACE
        printf("POP %s\n", v->ToString());
#endif

        break;
      }
        // handle store global op
      case kOpStoreGlobal: {
        Value *v = stack_->Pop();
        String *name = AS_STR(READ_OBJ());

        globals_->Set(name, v);

#ifdef VM_DEBUG_TRACE
        printf("STORE_GLOBAL '%s' '%s'\n", name->values, v->ToString());
#endif

        break;
      }
        // handle access global op
      case kOpAccessGlobal: {
        String *name = AS_STR(READ_OBJ());

#ifdef VM_DEBUG_TRACE
        printf("ACCESS_GLOBAL %s\n", name->values);
#endif

        auto *v = static_cast<Value *>(globals_->Get(name));
        if (v == nullptr) return kResultNullPointer;

        stack_->Push(v);

        break;
      }
        // handle const op
      case kOpConst: {
        Value *v = chunk_->Consts()->Get(READ_INST());

#ifdef VM_DEBUG_TRACE
        printf("CONST %s\n", v->ToString());
#endif

        stack_->Push(v);

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

void VM::Dispose() {
  heap_->Dispose();
  stack_->Dispose();
  globals_->Dispose();
  strings_->Dispose();

  if (objects_ != nullptr) {
    // TODO
  }

  if (chunk_ != nullptr) {
    free(chunk_);
  }

  if (pc_ != nullptr) {
    free(pc_);
  }

  free(this);
}
