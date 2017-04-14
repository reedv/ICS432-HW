Reed Villanueva
ICS432
HW9

Run on OSX: see top of Makefile
Run on Linux: see top of Makefile

Q1: see crazy_scientist_v1.c


Q2: see crazy_scientist_v2.c
	Avg. execution time: 18.79 seconds
	Avg. load imbalance: 15.92


Q3: see crazy_scientist_v3.c
	Avg. execution time: 11.24 seconds
	Avg. load imbalance: 0.56
	
	We see that there is a great improvement in load imbalance as compared to v2. This is due to the fact that rather than using static scheduling, as in v2, we are using dynamic scheduling to process elements as threads become available.
	Dynamic scheduling was useful here, because the duration of calculations increased as we began to calculate higher indexed matrix values, thus thread-1 which was responsible for computing this entire half of the matrix under static scheduling had a higher computational load. Using dynamic scheduling, we were able to split the matrix by time locality, rather than by data locality.


Q3: see crazy_scientist_v4.c
	Avg. execution time: 11.00 seconds
	Avg. load imbalance: 0.02

	We see that there is an on-average improvement in both execution time and load imbalance when computing each element on demand. This is because rather then splitting up the matrix in chunks by row, we are processing each index of the matrix as threads become available. Thus the increased time that it may take to process higher-indexed rows becomes less relevant to the overall work of each thread.