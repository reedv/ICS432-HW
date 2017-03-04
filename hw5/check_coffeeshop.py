# Script to check the output of the coffeeshop program
#
import sys
import re
from operator import eq

# Helper function
def matchThread(prefix, suffix, line, linenum):
  match = re.search(prefix+"(\d+)"+suffix,line)
  if (not match == None): 
    if (not match.groups()[0].isdigit()):
      print "Error: bogus output at line",linenum
      exit(1)
    else:
      return int(match.groups()[0])
  else:
    return -1


# Array of thread ids
threadids = []

# Get all lines from stdin
lines = sys.stdin.readlines()

##
## Check that all threads enter the coffee shop correctly
##

# Compute the set of customer ides that have entered the shop
for linenum in xrange(0,len(lines)):
  line = lines[linenum]

  threadid = matchThread("Thread "," enters the coffee shop", line, linenum)
  if (not threadid == -1):
      threadids.append(threadid)

# Is there any duplicate? 
if (not len(threadids) == len(list(set(threadids)))):
  print "Error: seems that some threads enter the coffee shop multiple times!"
  exit(1)

# Is there some missing thread?
if (not all(map(eq, sorted(threadids), range(0,len(threadids))))):
  print "Error: Seems that some threads never enter the coffee shop!"
  exit(1)


##
## Check that every thread does every activity 10 times and then leaves
##

drinking = [0] * len(threadids);
keygetting = [0] * len(threadids);
keyputting = [0] * len(threadids);
bathrooming = [0] * len(threadids);
leaving = [0] * len(threadids);

for linenum in xrange(0,len(lines)):
  line = lines[linenum]

  threadid = matchThread("Thread "," is drinking coffee", line, linenum)
  if (not threadid == -1):
      drinking[threadid] += 1

  threadid = matchThread("Thread "," is using the bathroom", line, linenum)
  if (not threadid == -1):
      bathrooming[threadid] += 1

  threadid = matchThread("Thread "," got a key", line, linenum)
  if (not threadid == -1):
      keygetting[threadid] += 1

  threadid = matchThread("Thread "," put a key back on the board", line, linenum)
  if (not threadid == -1):
      keyputting[threadid] += 1

  threadid = matchThread("Thread "," leaves the coffee shop", line, linenum)
  if (not threadid == -1):
      leaving[threadid] += 1

for x in xrange(0,len(threadids)):
  if (len(set(drinking)) > 1):
    print "Error: Some threads drink more than others!"
    exit(1);
  if (len(set(bathrooming)) > 1):
    print "Error: Some threads use the bathrooms more than others!"
    exit(1);
  if (len(set(keygetting)) > 1):
    print "Error: Some threads get keys more often than others!"
    exit(1);
  if (len(set(keyputting)) > 1):
    print "Error: Some threads return keys more often than others!"
    exit(1);
  if (len(set(leaving)) > 1):
    print "Error: Some threads leave the coffee shop more than others!"
    exit(1);

##
## Check that no more than 2 threads are in bathrooms at the same time
##

bathroom_users = 0
max_bathroom_users = 0

for linenum in xrange(0,len(lines)):
  line = lines[linenum]

  threadid = matchThread("Thread "," is using the bathroom", line, linenum)
  if (not threadid == -1):
    bathroom_users += 1
    if (bathroom_users > max_bathroom_users):
        max_bathroom_users = bathroom_users
    if (bathroom_users > 2):
      print "Error: too many threads in bathrooms at line",linenum
      exit(1)

  threadid = matchThread("Thread "," put a key back on the board", line, linenum)
  if (not threadid == -1):
    bathroom_users -= 1
    if (bathroom_users < 0):
      print "Error: negative number of threads in bathrooms at line",linenum
      exit(1)
   

##
## Check that with many threads, sometimes two are using the bathrooms concurrently
## 
if (max_bathroom_users < 2):
  print "Warning: the two bathrooms are never used concurrently. This is possible but highlight unlikely, especially with many threads. Re-run the code with many threads and if this keeps happening, it's likely a problem."


#
# As far as we can tell, all is ok
#
print "No errors detected"



