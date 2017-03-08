CC = gcc
CFLAGS = -Wall -pthread

all: coffeeshop coffeeshop_semaphore

clean: 
	rm coffeeshop coffeeshop_semaphore

coffeeshop:
	$(CC) $(CFLAGS) -o coffeeshop coffeeshop.c
coffeeshop_semaphore:
	$(CC) $(CFLAGS) -o coffeeshop_semaphore coffeeshop_semaphore.c