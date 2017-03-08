#include <stdio.h>
#include <pthread.h>
#include <semaphore.h>
#include <stdlib.h>
#include <unistd.h>



////lock and condition variables
//pthread_mutex_t key_rack, bathroom_1, bathroom_2;
//pthread_cond_t key_available;


typedef struct ArgsStruct {
	int id;
	int iterations;
	int max_usleep;
	sem_t *key_rack;
} ArgsStruct;



/*
 * Sleep for amount within range [min, max]
 * see http://stackoverflow.com/a/17554531
 * */
void rand_sleep (unsigned int min, unsigned int max) {
	int r;
	const unsigned int range = 1 + max - min;
	const unsigned int buckets = RAND_MAX / range;
	const unsigned int limit = buckets * range;

	/* Create equal size buckets all in a row, then fire randomly towards
	 * the buckets until you land in one of them. All buckets are equally
	 * likely. If you land off the end of the line of buckets, try again. */
	do
	{
		r = rand();
	} while (r >= limit);

	usleep(min + (r / buckets));
}


void *customer_worker (void *args_struct) {
	// unpack args
	ArgsStruct *args = (ArgsStruct *)args_struct;
	int id = args->id;
	int iterations = args->iterations;
	int max = args->max_usleep;
	sem_t *key_rack = args->key_rack;

	printf("Thread %d enters the coffee shop\n", id);

	// simulate customer behavior iterations times
	int i;
	for (i = 0; i < iterations; ++i) {
		// drink coffee
		printf("Thread %d is drinking coffee\n", id);
		rand_sleep(0, max);

		// get a key
		printf("Thread %d is going to the bathroom\n", id);
		sem_wait(key_rack);
		printf("Thread %d got a key\n", id);

		// use bathroom
		printf("Thread %d is using the bathroom\n", id);
		rand_sleep(0, max);

		// return key
		printf("Thread %d put a key back on the board\n", id);
		sem_post(key_rack);
	}

	printf("Thread %d leaves the coffee shop\n", id);

	return (void *)0;
}


int main(int argc, char **argv) {
	int num_customers;
	int seed;
	int max_usleep;
	int iterations;

	// track number of available bathroom keys
	int key_count = 2;

	// check for current amount of args
	if (argc != 5) {
		fprintf(stderr,
				"Usage: %s <# number of customers> <# rand seed> <# max sleep time (us)> <# iterations>\n",
				argv[0]);
		exit(1);
	}

	// check that all args assigned and valid
	if ((sscanf(argv[1],"%d",&num_customers) != 1) 	||
		(sscanf(argv[2],"%d",&seed) != 1) 			||
		(sscanf(argv[3],"%d",&max_usleep) != 1)	 	||
		(sscanf(argv[4],"%d",&iterations) != 1) 	||
		(num_customers < 1) 						||
		(seed < 0) 									||
		(max_usleep < 0 || 1000000 < max_usleep) 	||
		(iterations < 0)) {
			fprintf(stderr,"Invalid arguments\n");
			exit(1);
	}

	// seed rng
	srand(seed);

	// init resouce/key counting semaphore
	sem_t key_rack;
	if(sem_open(&key_rack, 0, key_count-1) < 0){
		fprintf(stderr,"Error while creating semaphore. Function sem_inti() not supported on osx\n");
		exit(1);
	}

	// simulate customer behaviors iterations times
	pthread_t customer_threads[num_customers];
	int i;
	// spin off all customers
	for (i=0; i < num_customers; i++) {
		ArgsStruct *args = (ArgsStruct *)calloc(1, sizeof(ArgsStruct));
		args->id = i;
		args->iterations = iterations;
		args->max_usleep = max_usleep;
		args->key_rack = &key_rack;

		if ( pthread_create(&(customer_threads[i]), NULL, customer_worker, (void *)args) ) {
			fprintf(stderr,"Error while creating thread (id=%d)\n", args->id);
			exit(1);
		}
	}

	// collect all customers
	for (i=0; i < num_customers; i++) {
		if ( pthread_join(customer_threads[i], NULL) ) {
			fprintf(stderr,"Error while joining with child thread\n");
			exit(1);
		}
	}

	return 0;
}
