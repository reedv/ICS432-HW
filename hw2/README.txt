Reed Villanueva
ICS432
HW2

	1. Rendering 48 frames on quiescent machine sequentially: 00:01:41 (hr:min:s) (using osx 'time' commandline tool). 

	2. Rendering 48 frames on quiescent machine, uniformly dividing frame processing between 2 threads: 00:00:59 (hr:min:s) (using 'time' commandline tool). 		This represents a 52.5% speedup in seconds and is 1.71 times faster than the sequential time. The threaded version was nearly twice as fast as the sequential code, likely due to the fact that the machine being used had two cores and so was able to run both threads without as much context switching.
	
	3. Rendering 48 frames on quiescent machine, uniformly dividing frame processing between 3 threads: 00:01:5.39. Dividing frame processing between 4 threads: 00:01:03.32. 
	Just by inspection we can see that the speedup multiplier is not equal to amount of extra threads. This is may be due to the fact that this machine only has two cores, so adding more threads produces context switching that slow the execution time. 
	We saw the best performance in part(2) were using 2 threads to utilize all two of this machine's cores producee a 2x speedup and showing it is indeed worth using both for this program, provided that we are in fact scheduling a single thread per core.