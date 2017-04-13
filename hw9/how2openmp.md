<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<!-- This was created automatically by buildhtml - do not edit -->
<head>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<meta content="text/html;charset=ISO-8859-1" http-equiv="Content-Type">
  
  <title>How to Compile and Run An OpenMP Program</title><style type="text/css"><!--
body {
	background-color: white;
	color: black;
}
pre {
	margin-left: 1em;
	padding: 0.5em;
	background-color: #f0f0f0;
	border: 1px solid black;
}
h1 {
	text-align: center;
}

--></style>
  <meta name="author" content="Susan Schwarz">
  <meta name="description" content="description of OpenMP"></head>
<body bgcolor=WHITE>
<font size=+1>
<table width=100%><tr>
 <td align="left"><font size=-1><a href="/cgi-bin/betsie.cgi">Text-only</a></font></td>
 <td align="right"><font size=-1>Table of Contents (<a href="index.html" target=_top>frame</a>/
<a href="slide_list_noframes.html" target="_top">no frame</a>)</font></td>
 </tr><tr>
 <td align=LEFT>(9) How to Compile and Run An OpenMP Program</td>
 <td align=RIGHT>
 <a href="openmp_functions.html" accesskey="p"><img src="left.gif" border=0 alt="Previous"></a>
<a href="#top" accesskey="t"><img src="up.gif" border=0 alt="Top"></a> 
 <a href="OpenMP_Clauses.html" accesskey="n"><img src="right.gif" border=0 alt="Next"></a>
 </td>
</tr></table>
<a name="top"></a>

<div align="left">

<a name="L2-How to Compile and Run an OpenMP Program"><h2 style="text-align: center;">How to Compile and Run an OpenMP Program</h2></a><br><table style="text-align: left; width: 80%;" border="1" cellpadding="2" cellspacing="2"><tbody><tr><td style="text-align: center;"><span style="font-weight: bold;">Compiler</span></td><td style="text-align: center;"><span style="font-weight: bold;">Compiler Options</span></td><td style="text-align: center;"><span style="font-weight: bold;">Default behavior for # of threads<br>(OMP_NUM_THREADS not set)</span></td></tr><tr><td>GNU (gcc, g++, gfortran)</td><td>-fopenmp</td><td>as many threads as available cores</td></tr><tr><td>Intel (icc ifort)</td><td>-openmp</td><td>as many threads as available cores</td></tr><tr><td>Portland Group (pgcc,pgCC,pgf77,pgf90)</td><td>-mp</td><td>one thread</td></tr></tbody></table><br><br>Environment Variable &nbsp;<span style="font-weight: bold;">OMP_NUM_THREADS</span> sets the number of threads<br><br><br><div style="text-align: left;"><span style="font-weight: bold;">GNU Compiler Example</span><br></div><pre><big><big><code><small style="font-family: monospace;">$ gcc -o omp_helloc -fopenmp omp_hello.c<br>$ export OMP_NUM_THREADS=2<br>$ ./omp_helloc<br>Hello World from thread = 0<br>Hello World from thread = 1<br>Number of threads = 2<br>$<br>$ gfortran -o omp_hellof -fopenmp omp_hello.f<br>$  export OMP_NUM_THREADS=4<br>$ ./omp_hellof<br> Hello World from thread =            2<br> Hello World from thread =            1<br> Hello World from thread =            3<br> Hello World from thread =            0</small><br style="font-family: monospace;"><span style="font-family: monospace;"> </span><small style="font-family: monospace;">Number of threads =            4</small><br></code></big></big></pre><br style="font-weight: bold;"><span style="font-weight: bold;">Intel Compiler Example</span><br><pre><big><code><span style="font-family: monospace;">$ icc -o omp_helloc -openmp omp_hello.c</span><br style="font-family: monospace;"><span style="font-family: monospace;">omp_hello.c(22): (col. 1) remark: OpenMP DEFINED REGION WAS PARALLELIZED.</span><br style="font-family: monospace;"><span style="font-family: monospace;">$ export OMP_NUM_THREADS=3</span><br style="font-family: monospace;"><span style="font-family: monospace;">$ ./omp_helloc</span><br style="font-family: monospace;"><span style="font-family: monospace;">Hello World from thread = 0</span><br style="font-family: monospace;"><span style="font-family: monospace;">Hello World from thread = 2</span><br style="font-family: monospace;"><span style="font-family: monospace;">Hello World from thread = 1</span><br style="font-family: monospace;"><span style="font-family: monospace;">Number of threads = 3</span><br style="font-family: monospace;"><span style="font-family: monospace;">$</span><br style="font-family: monospace;"><span style="font-family: monospace;">$ ifort -o omp_hellof -openmp omp_hello.f</span><br style="font-family: monospace;"><span style="font-family: monospace;">omp_hello.f(20): (col. 7) remark: OpenMP DEFINED REGION WAS PARALLELIZED.</span><br style="font-family: monospace;"><span style="font-family: monospace;">$ export OMP_NUM_THREADS=2</span><br style="font-family: monospace;"><span style="font-family: monospace;">$ ./omp_hellof</span><br style="font-family: monospace;"><span style="font-family: monospace;"> Hello World from thread =            0</span><br style="font-family: monospace;"><span style="font-family: monospace;"> Number of threads =            2</span><br style="font-family: monospace;"><span style="font-family: monospace;"> Hello World from thread =            1</span><br><br></code></big></pre><span style="font-weight: bold;"></span><br><span style="font-weight: bold;">Portland Group Example</span><pre><big><code><big><small style="font-family: monospace;">[sas@discovery intro_openmp]$ pgcc -o omp_helloc -mp omp_hello.c<br>[sas@discovery intro_openmp]$ export OMP_NUM_THREADS=2<br>[sas@discovery intro_openmp]$ ./omp_helloc<br>Hello World from thread = 0<br>Number of threads = 2<br>Hello World from thread = 1<br>$<br>$ pgf90 -o omp_hellof -mp omp_hello.f<br>$ export OMP_NUM_THREADS=2<br>$ ./omp_hellof<br> Hello World from thread =             0<br> Number of threads =             2<br> Hello World from thread =             1</small><br></big></code></big></pre><br><hr size="4">
<p></p>
<br>
</div>
<br>
<br>
<p align=CENTER>
<a href="openmp_functions.html#top"><img src="left.gif" border=0 alt="Previous"></a> 
<a href="#top"><img src="up.gif" border=0 alt="Top"></a> 
<a href="OpenMP_Clauses.html#top"><img src="right.gif" border=0 alt="Next"></a>
<br><hr>
<table width=100%><tr>
<td align="left" width=20% ><font size=-1>compile_run.src&nbsp;&nbsp;last modified Mar 23, 2009</font></td>
<td align="center" width=20%><a href="what_is_openmp.html">Introduction</a></td>
<td align="center" width=20%>Table of Contents<br><font size=-1>(<a href="index.html" target=_top>frame</a>/<a href="slide_list_noframes.html" target="_top">no frame</a>)</font></td>
<td align="center" width=20%><a href="print_pages.shtml" target="_top">Printable<br><font size=-1>(single file)</font></a></td>
<td align="right"><font size=-1>&copy; Dartmouth College</font></td>
</tr></table>
</font></body></html>
