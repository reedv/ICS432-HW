Name: Reed Villanueva
Processor and OS: Ubuntu 16.04 on VirtualBox running on a 2.9 GHz Intel Core i7
Compiler: gcc version 5.4.0
Profiler: gprof version 2.26.1

1. On a quiescent system, measure the execution time with different levels of compiler optimizations supported by your compiler (-O0, -O1, …). Report these times in a table. What is the execution time reduction factor when going from no optimization to maximum optimization?

   Time measuered using 'perf stat ./tircis_process_cmd ./newproc.txt'
  	Optimization level 0 (-o0): 96.858518702 seconds
  	Optimization level 1 (-o1): 58.035937554 seconds
  	Optimization level 2 (-o2): 52.479030639 seconds
  	Optimization level 3 (-o3): 51.841398242 seconds
   Let noOpt = 96.85851870, maxOpt = 51.84139824.
   Percent difference = abs(noOpt - maxOpt) / (noOpt+maxOpt)/2 * 100
		       = 60.5476% difference speedup from no optimization to maximum compiler optimization. 


2. Using a profiler (whichever one you want), using the highest level of optimization, list the top 5 most expensive functions in terms of their individual contributions to the execution time. Show the part of the profiler output that allows you to figure this out.

   Top five most expensive functions:
   1. svdcmp_d
   2. void std::vector<float, std::allocator<float> >::_M_emplace_back_aux<float const&>(float const&)
   3. svdfit_d
   4. GeneProcess::geneFrame(unsigned short*, float*)
   5. suchi_utils::bb2temp(float, float)
   The section of the gprof output used to determine these functions is shown below.
 ----------------------------------------
 Each sample counts as 0.01 seconds.
  %   cumulative   self              self     total           
 time   seconds   seconds    calls   s/call   s/call  name    
 54.92      9.50     9.50   239436     0.00     0.00  svdcmp_d
 20.52     13.05     3.55   239436     0.00     0.00  void std::vector<float, std::allocator<float> >::_M_emplace_back_aux<float const&>(float const&)
  6.07     14.10     1.05   239436     0.00     0.00  svdfit_d
  3.53     14.71     0.61      735     0.00     0.02  GeneProcess::geneFrame(unsigned short*, float*)
  2.92     15.22     0.51 11907000     0.00     0.00  suchi_utils::bb2temp(float, float)


3. Based on the profiler’s output, how many times is function dvector called? How many different functions call it? Which of these functions calls it the most often? Show the part of the profiler output that allows you to figure this out.

  dvector function:
	Times called: 1197180
	Callers: polyfit, svbksb_d, svdcmp_d, svdfit_d
	Most frequent caller: svdfit_d
   These results were obtained using the call graph section of the gprof output and the relevant subsection is presented below.
-----------------------------------------------
                0.00    0.00  239436/1197180     polyfit(double*, double*, int, int, double*, double*) [6]
                0.00    0.00  239436/1197180     svbksb_d [13]
                0.00    0.00  239436/1197180     svdcmp_d [8]
                0.00    0.00  478872/1197180     svdfit_d [7]
[24]     0.1    0.01    0.00 1197180         dvector [24]
-----------------------------------------------


4. Let’s say that you “magically” make function suchi_utils::bb2rad (i.e., only the code in that function, not its callees) 100 times faster. Based on profiler output, what would be the new execution time? By what factor would you then have reduced the overall execution time of the program? Show your work/reasoning.

  Using the first section of the gprof output, the original execution time of suchi_utils::bb2rad is 0.08s.
  From the call graph of gprof output:
index % time    self  children    called     name
  -----------------------------------------------
                0.00    0.00     144/11907144     GeneProcess::genBBCurves() [26]
                0.08    0.00 11907000/11907144     GeneProcess::calcEmissivity() [10]
[18]     0.5    0.08    0.00 11907144         suchi_utils::bb2rad(float, float) [18]
-----------------------------------------------
  If the function were 100 times faster, its execution time would be reduced to 0.0008s.

  Let originalTime = 51.84139824 = (51.84139824 - 0.08) + 0.08 = 51.76139824 + 0.08
      newTime      = 51.76139824 + 0.0008 = 51.76219824

  Percent difference = abs(originalTime - newTime) / (originalTime + newTime)/2 * 100
		      = 0.15289% difference overall speedup.





