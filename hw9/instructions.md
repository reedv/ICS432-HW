<!DOCTYPE html>
<html>
<head>
  <title> Homework Assignment #9 | ICS 432 Spring 2017 </title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta charset="utf-8">

  <!--  Load bootswatch-based Morea theme file. -->
  <link rel="stylesheet" href="/ics432_spring2017/css/themes/cerulean_red/bootstrap.min.css">
  <link rel="stylesheet" href="/ics432_spring2017/css/style.css">
  <link rel="stylesheet" href="/ics432_spring2017/css/syntax.css">
  <link rel="shortcut icon" href="/ics432_spring2017/favicon.ico" type="image/x-icon"/>

  <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
  <!--[if lt IE 9]>
  <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.2/html5shiv.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.2.0/respond.js"></script>
  <![endif]-->

  <!-- Load Bootstrap JavaScript components -->
  <script src="//code.jquery.com/jquery.min.js"></script>
  <script src="//netdna.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>

  
  <script type="text/javascript"
          src="//cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML"></script>
  

  

  <script>
    $(window).load(function () {
      // If on a page associated with a navbar item, make its navbar item 'active'.
      $('.navbar-nav').find('a[href="' + location.pathname + '"]').parents('li').addClass('active');
    });
  </script>

</head>
<body>
<!-- Responsive navbar -->
<div class="navbar navbar-default navbar-fixed-top" role="navigation">
  <div class="container">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
        <!--  Display three horizontal lines when navbar collapsed. -->
        <span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="/ics432_spring2017/index.html"> ICS 432 Spring 2017 </a>
    </div>
    <div class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        
        <li><a href="/ics432_spring2017/modules/">Modules</a></li>
        
        <li><a href="/ics432_spring2017/outcomes/">Outcomes</a></li>
        
        
        <li><a href="/ics432_spring2017/readings/">Readings</a></li>
        
        
        <li><a href="/ics432_spring2017/experiences/">Experiences</a></li>
        
        
        <li><a href="/ics432_spring2017/assessments/">Assessments</a></li>
        
        
      </ul>
    </div>
  </div>
</div>


<div class="breadcrumb-bar">
  <div class="container">
    <ol class="breadcrumb">
      
      <li><a href="/ics432_spring2017/">Home</a></li>
      <li><a href="/ics432_spring2017/modules">Modules</a></li>
      <li><a href="/ics432_spring2017/modules/openmp">OpenMP</a></li>
      <li class="active">Homework Assignment #9</li>
    </ol>
  </div>
</div>


<div class="container">
  <h2 id="programming-assignment----openmp">Programming Assignment – OpenMP</h2>

<hr />

<p>You are expected to do your own work on all homework assignments.  You
may (and are encouraged to) engage in <i>general</i> discussions with
your classmates regarding the assignments, but specific details of a
solution, including the solution itself, must always be your
own work. (See the statement of Academic Dishonesty on the
<a href="/ics432_spring2017/morea/GettingStarted/reading-syllabus.html">Syllabus</a>.)</p>

<p><b><font face="sans-serif"> What to turn in? </font></b><br /></p>

<p>You should turn in an electronic archive (.zip, .tar., .tgz, etc.). The
archive must contain a single top-level directory called ics432_hwX_NAME,
where “NAME” is your UH username and “X” is the programming assignment
number (e.g., ics432_hw10_henric). Inside that directory you should have all
your code (no binaries, .class, etc.) and requested files, <b>named
exactly</b> as specified in the questions below.  <b>We should be able to
do a “make” in that archive and compile all your code without
errors.</b></p>

<p>Assignments need to be turned in via <a href="https://laulima.hawaii.edu/portal">Laulima</a>. Check the <a href="/ics432_spring2017/morea/GettingStarted/reading-syllabus.html">Syllabus</a> for
the late assignment policy for this course.</p>

<hr />

<h4 id="what-platform-to-run-on">What platform to run on?</h4>

<p>This assignment requires a POSIX-compliant thread implementation, e.g., any Linux or Mac OSX system you have access to.</p>

<hr />

<h3 id="exercise-1-30--10-pts-parallelization-and-load-balancing-in-c">Exercise #1 [30 + 10 pts]: Parallelization and Load Balancing in C</h3>

<p>You just joined a company that develops new data filtering techniques  and
your  boss gave you a sequential C program written by one of the crazy
scientists in the basement. You have no idea what the program does, and in
fact it’s not clear it does anything useful, but your job is to parallelize
it for execution on the multi-core machine that is on your desk.</p>

<p>Download the scientist’s program, 
<a href="./crazy_scientist.c">crazy_scientist.c</a>. You will use it as a starting point
for creating parallel versions of it as described in the questions below.
The program just fills a 2-D array with numbers. (It may need to be linked
with the <tt>-lm</tt> flag as it uses functions from the math library.)</p>

<p><b>IMPORTANT:</b> Because OpenMP does so much for us, the questions below require
<b>very little code</b> in order to implement concurrency.</p>

<h4 id="question-1-20pts">Question #1 [20pts]</h4>

<p>Using OpenMP, write a parallel version of this program, called
<tt>crazy_scientist_v1.c</tt> so that <b>2
threads</b> are used. In this question, make it so that the first thread computes
the top part of the array (i.e., rows 0 to 24) and the second thread
computes the bottom part of the array (i.e., rows 25 to 49).</p>

<p>Your program must reports the overall execution time.
To this
end use the <tt>gettimeofday</tt> function as follows:</p>

<figure class="highlight"><pre><code class="language-c" data-lang="c"><span class="cp">#include &lt;sys/time.h&gt;
</span>
<span class="kt">int</span> <span class="nf">main</span><span class="p">(</span><span class="kt">int</span> <span class="n">argc</span><span class="p">,</span> <span class="kt">char</span> <span class="o">**</span><span class="n">argv</span><span class="p">)</span> <span class="p">{</span>
  <span class="k">struct</span> <span class="n">timeval</span> <span class="n">start</span><span class="p">,</span> <span class="n">end</span><span class="p">;</span>
  <span class="kt">double</span> <span class="n">elapsed</span><span class="p">;</span>

  <span class="n">gettimeofday</span><span class="p">(</span><span class="o">&amp;</span><span class="n">start</span><span class="p">,</span> <span class="nb">NULL</span><span class="p">);</span>
  <span class="p">...</span>
  <span class="n">gettimeofday</span><span class="p">(</span><span class="o">&amp;</span><span class="n">end</span><span class="p">,</span> <span class="nb">NULL</span><span class="p">);</span>
 
  <span class="n">elapsed</span> <span class="o">=</span> <span class="p">((</span><span class="n">end</span><span class="p">.</span><span class="n">tv_sec</span><span class="o">*</span><span class="mi">1000000</span><span class="p">.</span><span class="mi">0</span> <span class="o">+</span> <span class="n">end</span><span class="p">.</span><span class="n">tv_usec</span><span class="p">)</span> <span class="o">-</span>
            <span class="p">(</span><span class="n">start</span><span class="p">.</span><span class="n">tv_sec</span><span class="o">*</span><span class="mi">1000000</span><span class="p">.</span><span class="mi">0</span> <span class="o">+</span> <span class="n">start</span><span class="p">.</span><span class="n">tv_usec</span><span class="p">))</span> <span class="o">/</span> <span class="mi">1000000</span><span class="p">.</span><span class="mo">00</span><span class="p">;</span>

  <span class="n">printf</span><span class="p">(</span><span class="s">"Elapsed time: %.2f seconds</span><span class="se">\n</span><span class="s">"</span><span class="p">,</span><span class="n">elapsed</span><span class="p">);</span>

  <span class="n">exit</span><span class="p">(</span><span class="mi">0</span><span class="p">);</span></code></pre></figure>

<p>Here is an example execution of the program (with some of the dots ‘.’ removed):</p>

<figure class="highlight"><pre><code class="language-text" data-lang="text">% gcc -fopenmp crazy_scientist_v1.c -o crazy_scientist_v1

% ./crazy_scientist_v1
..................................................................................................................................................................................................................................................................................................................................................................................................................................................................Overall execution time: 18.48</code></pre></figure>

<hr />

<h4 id="question-2-10pts">Question #2 [10pts]</h4>

<p>Modify the program from Question #1, naming it <tt>crazy_scientist_v2.c</tt>
so that it computes (and outputs)
the load imbalance. 
The load imbalance is the 
absolute value of the difference between the completion times of the two threads. The
execution time of each tread should also be printed.</p>

<p>Here is an example execution of the program (with some of the dots ‘.’ removed):</p>

<figure class="highlight"><pre><code class="language-text" data-lang="text">./crazy_scientist_v2 
................................................................................................................................................................................................................................................................................................................................................................................................................Thread #0 Execution time: 2.93
Thread #1 Execution time: 18.53
Overall execution time: 18.53
Load imbalance: 15.60</code></pre></figure>

<p>In the above run one thread ran for 18.53s and the other for 2.93s, for a high load
impalance of 15.60.</p>

<p>Run your code 10 times and in a README file report on the the average load imbalance and
the average execution time (i.e., the average execution time of the slowest thread).</p>

<p><i>Hint: </i> This requires a little bit of creativity given the rigidity of OpenMP. 
Using separate <tt>#pragma omp parallel</tt> and <tt>#pragma omp for</tt> directives,
as well as the “nowait” option for <tt>#pragma omp for</tt> likely comes in handy.</p>

<hr />

<h4 id="question-3-20pts">Question #3 [20pts]</h4>

<p>Using OpenMP, write a new version of your program,
<tt>crazy_scientist_v3.c</tt> so that both threads compute <b>rows</b> of
the matrix “on demand”. In other words, a thread starts working on a new
row whenever it has finished computing a previous row (recall the
“scheduling” part of the lecture notes).</p>

<p>Run your code 10 times and in your REAME file report on the average load imbalance and
the average execution time. Any improvements compared to the
version in Question #2? Explain.</p>

<hr />

<h4 id="question-4-extra-credit-5pts">Question #4 [EXTRA CREDIT: 5pts]</h4>

<p>Still using OpenMP, write a new version of your program, <tt>crazy_scientist_v4.c</tt>, so that
each thread computes an <b>element</b> of the matrix in an on-demand fashion.</p>

<p>Run your code 10 times and in your README file report on the average
load imbalance and the average execution time.  Any improvements compared
to the version in Question #3? Explain.</p>

<hr />


</div>




<script src="/ics432_spring2017/js/scrollIfAnchor.js"></script>

<div class="footer-background hidden-print">
  <footer>
    <div class="container page-footer">
      
      No footer page content supplied.
      
      <p style="margin: 0">Powered by the <a href="http://morea-framework.github.io/">Morea Framework</a> (Theme: cerulean_red)<br>
        Last update on: <span>2017-04-12 13:23:56 -1000</span></p>

      <p style="margin: 0">
        
        17 modules
        
        | 9 outcomes
        
        
        | 37 readings
        
        
        | 13 experiences
        
        
        | 12 assessments
        
      </p>
    </div>
  </footer>
</div>
</body>
</html>
