#include<stdio.h>
#include "runtime.h"

char* doSomething(char* message) {
    printf(message + '\n');

    return message;
}
