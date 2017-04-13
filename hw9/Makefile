# on osx (for openmp support, pt. to gcc rather than clang)
export CC = `which gcc-6`
# on linux
#CC = gcc

CFLAGS= -Wall -lm -fopenmp

all: crazy_scientist crazy_scientist_v1 crazy_scientist_v2 crazy_scientist_v3 crazy_scientist_v4

crazy_scientist: crazy_scientist.c
	$(CC) $(CFLAGS) crazy_scientist.c -o crazy_scientist.o

crazy_scientist_v1: crazy_scientist_v1.c
	$(CC) $(CFLAGS) crazy_scientist_v1.c -o crazy_scientist_v1.o

crazy_scientist_v2: crazy_scientist_v2.c
	$(CC) $(CFLAGS) crazy_scientist_v2.c -o crazy_scientist_v2.o

crazy_scientist_v3: crazy_scientist_v3.c
	$(CC) $(CFLAGS) crazy_scientist_v3.c -o crazy_scientist_v3.o

crazy_scientist_v4: crazy_scientist_v4.c
	$(CC) $(CFLAGS) crazy_scientist_v4.c -o crazy_scientist_v4.o

#main.o: main.cpp
#	$(CC) $(CFLAGS) main.cpp

#factorial.o: factorial.cpp
#	$(CC) $(CFLAGS) factorial.cpp

#hello.o: hello.cpp
#	$(CC) $(CFLAGS) hello.cpp

clean:
	rm *.o 