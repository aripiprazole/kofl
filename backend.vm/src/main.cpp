#include <cstdio>
#include <cstdlib>
#include <cstring>

#include "koflvm/vm.h"
#include "koflvm/bytecode.h"
#include "koflvm/debug.h"

using namespace koflvm;

int PrintHelp() {
  printf("Usage: koflvm <file> [--verbose] [--memory <memory>]\n");

  return EXIT_FAILURE;
}

char *GetArg(char *arg_name, int argc, char **argv) {
  for (int i = 0; i < argc; ++i) {
    if (strcmp(arg_name, argv[i]) == 0) {
      if (i + 1 > argc) return nullptr;

      return argv[i + 1];
    }
  }

  return nullptr;
}

char *GetArgOr(char *arg_name, char *def, int argc, char **argv) {
  char *arg = GetArg(arg_name, argc, argv);

  if (arg == nullptr) return def;

  return arg;
}

char *ReadFile(char *file_path) {
  FILE *file = fopen(file_path, "rb");

  if (file == nullptr) return nullptr;

  fseek(file, 0, SEEK_END);
  long len = ftell(file);
  rewind(file);

  char *buffer = static_cast<char *>(calloc(len, sizeof(char)));

  fread(buffer, len, 1, file);
  fclose(file);

  return buffer;
}

int main(int argc, char **argv) {
  if (argc < 1) return PrintHelp();

  char *file_path = argv[1];

  if (file_path == nullptr) return PrintHelp();

  bool verbose = GetArg("--verbose", argc, argv) != nullptr;
  bool disassemble = GetArg("--disassemble", argc, argv) != nullptr;
  size_t memory = atol(GetArgOr("--memory", "512", argc, argv));

  Flags flags = {
      .verbose = verbose,
      .memory = memory,
  };

  char *bytes = ReadFile(file_path);
  if (bytes == nullptr) {
    printf("Failed to read file %s\n", file_path);
    return EXIT_FAILURE;
  }

  Chunk *bytecode = ParseChunk(bytes);
  if (bytecode == nullptr) {
    printf("Failed to read bytecode\n");

    return EXIT_FAILURE;
  }
  free(bytes);

  printf("Kofl vm\n\n");

  if (disassemble) {
    DisassembleChunk(bytecode);
  }

  VM *vm = new VM(flags);
  vm->Eval(bytecode);
  vm->Dispose();
}