#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <sys/time.h>
#include <omp.h>

#define SIZE 50
#define NUM_THREADS 2

double do_crazy_computation(int i,int j);

int main(int argc, char **argv) {
    struct timeval start, end;
    int t0_num, t1_num;
    double elapsed, elapsed_t0, elapsed_t1;  // names may not actually match with thread num
    gettimeofday(&start, NULL);
    
    double mat[SIZE][SIZE];
    int i,j;
  
    #pragma omp parallel \
        num_threads(NUM_THREADS) \
        shared(mat, t0_num, t1_num, elapsed_t0, elapsed_t1) \
        private(i, j)
    {
        struct timeval start_t, end_t;
        gettimeofday(&start_t, NULL);
        
        for (i=0; i<SIZE; i++) { /* loop over the rows */
            #pragma omp for \
                schedule(dynamic, 1) \
                nowait
            for (j=0; j<SIZE; j++) {  /* loop over the columns */
                mat[i][j] = do_crazy_computation(i, j);
                //printf("Thread-%d: Processed mat[%d][%d]\n", omp_get_thread_num(), i, j);
                fprintf(stderr, ".");
            }
        }
        
        gettimeofday(&end_t, NULL);
        switch (omp_get_thread_num()) {
            case 0:
                elapsed_t0 = ((end_t.tv_sec*1000000.0 + end_t.tv_usec) -
                              (start_t.tv_sec*1000000.0 + start_t.tv_usec)) / 1000000.00;
                t0_num = omp_get_thread_num();
                break;
            case 1:
                elapsed_t1 = ((end_t.tv_sec*1000000.0 + end_t.tv_usec) -
                              (start_t.tv_sec*1000000.0 + start_t.tv_usec)) / 1000000.00;
                t1_num = omp_get_thread_num();
                break;
        }
    }
    
    gettimeofday(&end, NULL);
    printf("Thread #%d: Execution time: %.2f seconds\n", t0_num, elapsed_t0);
    printf("Thread #%d: Execution time: %.2f seconds\n", t1_num, elapsed_t1);
    elapsed = ((end.tv_sec*1000000.0 + end.tv_usec) -
             (start.tv_sec*1000000.0 + start.tv_usec)) / 1000000.00;
    printf("Elapsed time: %.2f seconds\n",elapsed);
    printf("Load imbalance: %.2f\n", fabs(elapsed_t0 - elapsed_t1));
    
    exit(0);
}

/* Crazy Computation */
double do_crazy_computation(int x,int y) {
    int iter;
    double value=0.0;
   
    for (iter = 0; iter < 5*x*x*x+1 + y*y*y+1; iter++) {
        value +=  (cos(x*value) + sin(y*value));
    }

    return value;
}

    
    
    
