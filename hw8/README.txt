Reed Villanueva
ics432
hw8

To compile everything:
	javac -cp .:Filters.jar *.java

Q1:
How to test weird filter:
	javac RGB.java WeirdFilter.java WeirdFilterExample.java
	java WeirdFilterExample <path to image>
Should then create the resulting image in the current directory under the name weird_image.jpg. Uses either the sequential or dataparallel versions of the weird filter (need to comment/uncomment the different instantiations in the WeirdFilterExample source file).



Q2: see ConcurrentImageProcessorTaskParallel.java



Q3: 
threads 1:
Time spent processing: 416.6619999999999 sec.
Time spent reading: 4.986 sec.
Cumulative spent writing: 12.613000000000001 sec.
Overall execution time: 418.568 sec.

threads 2:
Time spent processing: 228.017 sec.
Time spent processing: 230.11700000000002 sec.
Time spent reading: 5.630999999999999 sec.
Cumulative spent writing: 15.21 sec.
Overall execution time: 232.061 sec.
Speedup (vs 1 thread): 44.6%

threads 3:
Time spent processing: 194.32799999999995 sec.
Time spent processing: 194.738 sec.
Time spent processing: 196.20000000000005 sec.
Time spent reading: 5.8790000000000004 sec.
Cumulative spent writing: 16.595999999999997 sec.
Overall execution time: 198.558 sec.
Speedup (vs 1 thread): 52.6%

threads 4:
Time spent processing: 170.87900000000002 sec.
Time spent processing: 172.33 sec.
Time spent processing: 172.774 sec.
Time spent processing: 176.382 sec.
Time spent reading: 5.896000000000002 sec.
Cumulative spent writing: 18.399000000000008 sec.
Overall execution time: 178.587 sec.
Speedup (vs 1 thread): 57.3%



Q4: see ConcurrentImageProcessorDataParallel.java



Q5: (using single large image)
threads 1:
Time spent reading: 0.344 sec.
Cumulative time spent processing: 7.95 sec.
Cumulative spent writing: 0.319 sec.
Overall execution time: 9.862 sec.

threads 2:
Time spent reading: 0.267 sec.
Cumulative time spent processing: 4.383 sec.
Cumulative spent writing: 0.332 sec.
Overall execution time: 6.195 sec.

threads 3:
Time spent reading: 0.272 sec.
Cumulative time spent processing: 3.77 sec.
Cumulative spent writing: 0.33 sec.
Overall execution time: 5.569 sec.

threads 4:
Time spent reading: 0.268 sec.
Cumulative time spent processing: 3.371 sec.
Cumulative spent writing: 0.339 sec.
Overall execution time: 5.17 sec.
