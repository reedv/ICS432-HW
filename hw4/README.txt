Reed Villanueva
ICS432
Due March 5

Exercise #1: Thread-safe bounded stack [40pts]
In this exercise you must find problems in a series of broken implementations of a Thread-safe bounded stack of integers written by a "co-worker." All code is written in pseudo-code and the specific syntax does not matter.

Question #1 [5pts]
Your co-worker has produced the following code, with the semantic that the application simply _terminates_ if there is a pop on an empty stack or a push on a full stack. These are not great semantics, but before you can voice your discontent your co-worker says: "I have added two functions to check on the empty/full status of the stack, isEmpty() and isFull(), and users can _avoid program termination_ without using any locks themselves _by calling these functions to check_ that the stack isn't full/empty before pushing/popping".

Explain why your co-worker's statement above is wrong by giving a simple sequence of events when more than one thread manipulates the stack.

Stack {
  int size;
  int items[SIZE];
  Lock mutex;
}

void push(Stack stack, int value) {
  lock(stack.mutex);
  if (stack.size == SIZE) {
    print "Stack full";
    exit;
  } 
  stack.items[stack.size] = value;
  stack.size ++;
  unlock(stack.mutex);
}

int pop(Stack stack) {
  lock(stack.mutex);
  if (stack.size == 0) {
    print "Stack empty";
    exit;
  }
  int value = stack.items[stack.size - 1];
  stack.size --;
  unlock(stack.mutex);
  return value;
}

int isEmpty(Stack stack) {
  return (stack.size == 0);
}

int isFull(Stack stack) {
  return (stack.size == SIZE);
}

Answer #1:
	The conditionals are _outside_ of the CS: The problem is that the isEmpty() and isFull() checks have to be placed by the user, _outside of the critical sections_ of the other accessing operations. Suppose we have a program where we only push(s, v) if(isFull(s)) and only pop(s) if(isEmpty(s)). Also suppose we have two threads t1 and t2 acting on a stack s:
		* say we start with stack s having one element
		* t1 runs the line "if(isEmpty(s))" which leads to a call to pop(s)
		* OS then context switches to t2 (before t1 can call pop(s))
		* t2 also runs "if(isEmpty(s))" which also leads to calling pop(s)
		* t2 successfully pops from stack s (leaving s empty)
		* context switch back to t1, which then runs pop(s)
		* at this point, t1 sees that stack.size == 0 and exits the program
	Thus the inclusion of these comparison functions does not successfully guard against the program termination that they were implemented to avoid because _they both access information about the stack without being protected by a mutual exclusion lock_. 




Question #2 [5pts]
Convinced by your argument, your co-worker goes back to the drawing board and produces a new implementation. The new semantic is that push() and pop() return a success code, a failure code, or an integer value. However, the code he shows you below is broken. Explain why and refer to problematic line numbers.

Stack {
  int size;
  int items[SIZE];
  Lock mutex;
}

int push(Stack stack, int value) {
  lock(stack.mutex);
  if (stack.size == SIZE) {
    return STACK_ERROR;
  } 
  stack.items[stack.size] = value;
  stack.size ++;
  unlock(stack.mutex);
  return STACK_SUCCESS;
}

int pop(Stack stack) {
  lock(stack.mutex);
  if (stack.size == 0) {
    return STACK_ERROR;
  }
  int value = stack.items[stack.size - 1];
  stack.size --;
  unlock(stack.mutex);
  return value;
}

Answer #2:
	Locking w/out unlocking: The problem here is that if either operation falls into the case where they must return a STACK_ERROR, they _fail to unlock the stack's mutex_ before doing so.
		* suppose stack s is empty
		* t1 calls pop(s), locking stack s's mutex and returning STACK_ERROR
		* context switch to t2 which calls push(s, v)
		* t2 will never be able to enter the critical section because s.mutex is still locked (same goes for if any other thread tried to do a pop(s) operation)
	What should be done to fix this problem would be to add "unlock(stack.mutex)" between lines (9,10) and (20,21) in the given code. 	



Question #3 [12pts]
You're tired of this "do everything with locks" vibe, and of the lame semantics. You have taken ICS432 and thus proceed to teach your co-worker about condition variables, realizing in the process that teaching concurrency is harder than it looks. Your co-worker then produces the code below.

Before you even being to look at the code, from the basement comes Frank with his bushy beard, his FreeBSD t-shirt from 1993, and his rainbow suspenders. He looks at the code for .1 sec from 10 feet away and says: "It's broken! I've been doing threads since the 80's, before it was cool." After rambling for 10 minutes about some concurrency research he did at Xerox Park, he condescends to show everybody 3 big problems with the code.

What are these problems? (the "same problem" in pop() and push() counts only as one).	
Stack {
  int size;
  int items[SIZE];
  Lock mutex;
  Cond cond;
}

void push(Stack stack, int value) {
  if (stack.size == SIZE) {
    lock(stack.mutex);
    wait(stack.cond, stack.mutex);
  }
  stack.items[stack.size] = value;
  stack.size ++;
  unlock(stack.mutex);
  return;
}

int pop(Stack stack) {
  if (stack.size == 0) {
    lock(stack.mutex);
    wait(stack.cond, stack.mutex);
  }
  int value = stack.items[stack.size - 1];
  stack.size --;
  unlock(stack.mutex);
  return value;
}

Answer #3:
	1. Failing to initially acquire lock: The mutex locks should be before the conditional check on the stack.size. Having the locks() come right before the waits() is redundant because the wait() will just unlock the mutex. Furthermore, have the lock after the stack.size check means that a thread t1 could end up checking a stack.size that is potentially being manipulated by another thread t2:
		* say t1 calls pop() on an empty stack s
		* t1 sees that s.size == 0, but before it calls lock()…
		* context switch to t2, which calls push(s, v1), which runs completely
		* at this point stack.size != 0, and s has something to pop() off
		* context switch back to t1, which still locks, though s is not empty

	2. Complementary operations not signaling each other: In both operations, once the calling thread is put to sleep, it is never woken up/signal()ed again. Continuing the scenario in 3.1:
		* t1 is sleeping, thinking that stack s is empty
		* context switch to t2, which suppose calls push(s, v2)
		* say t2 runs completely, context switch back to t1
		* we see that t1 is still asleep, despite the fact that there are now two values that could be pop()ed from the stack. 
	What should have happened was that push() calls signal(stack.cond) after unlock()ing, but no such call is made and t1 waits forever.

	3. May cause 'false' wakeups: Finally, the blocks that the wait()s are in should be while-loops rather than if statements. Lets continue the example from 3.2:
		* at this point, t1 is waiting forever for a signal it wake up and pop() from the stack s which is now of s.size=2.
		* suppose now that rather than waiting forever, the push operation did in fact have a signal(stack.cond, stack.mutex) that was called by t2 before unlocking the mutex (waking up t1 and putting this thread in the ready queue).
		* suppose also that another thread t3 was context switched to and that t3 had no idea that the stack was ever empty (ie. it had not yet made made any conditional check on the stack.size)
		* suppose t3 than calls pop() twice, emptying the stack
		* context switch back to t1 who has now been woken up by t2's signal()
		* t1 _continues down past the wait()_, falls out of the "if" and accesses stack.items[stack.size - 1]
		* thus we have an array-out-of-bounds exception as well as a negative stack size value
	If there had been a while instead of an if, the thread would have seen that the stack was (once again) empty and gone back to sleep rather than continuing down its popping operation.




Question #4 [10pts]
Your boss, annoyed at this point, tasks you and Frank to work on this and leaves in a huff. Frank, however, insists that you use Semaphores for the implementation. You sit down with Frank and you start your IDE. At that point Frank makes some statement about using Emacs and writing Lisp macros, and quickly disappears in his office because he has to write a critical FreeBSD kernel patch. Left to your own devices and write the code below.

By looking at your ICS432 material, which you keep at hand at all times and will for the rest of your life, you quickly realize that this is not quite right. Explain why.

Stack {
  int size;
  int items[SIZE];
  Semaphore mutex = 1;
  Semaphore not_full = 1;
  Semaphore not_empty = 0;
}

void push(Stack stack, int value) {
  P(mutex);

  while (stack.size == SIZE) {
    P(not_full); 
  }

  stack.items[stack.size] = value;
  stack.size ++;
  V(not_empty);

  V(mutex);
  return;
}

int pop(Stack stack) {
  P(mutex);

  while (stack.size == 0) {
    P(not_empty);
  }

  int value = stack.items[stack.size - 1];
  stack.size --;
  V(not_full);

  V(mutex);
  return value;
}

Answer #4:
	Locking w/out unlocking: Recall that P(sem) acts like a wait() when sem == 0. In this case, when we fall into the stack.size==SIZE case (or stack.size==0 case) for push() (or pop()) could be putting the calling thread to sleep with the not_full (or not_empty) semaphore without first unlocking the the mutex semaphore. In addition to this problem, this implementation does not reacquire the mutex semaphore if the thread is ever placed back in the ready queue by the OS.
	* suppose t1 calls pop(s) on an empty stack s (now mutex==0)
	* t1 will then call P(not_empty) and go to sleep (still, mutex==0)
	* context switch to t2
	* t2 calls push(s, v), thus calling P(mutex)
	* mutex==0, so t2 gets put to sleep and we end up with a deadlock 



Question #5 [8pts]
You fix your code and have a working version. Frank comes out of his office after radically changing the way in which FreeBSD does virtual memory. He looks at your code and says "This is Semaphore code written by somebody who likes locks and condition variables, I will have none of it! Back at Xerox Park in the 80's ....."

Before you can throw something at him he says you can rewrite this without any if statement if you use two semaphores in addition to the mutex semaphore. He then writes the code below, but with cryptic semaphore names and missing statements "?????" that he says you can fill in yourself for your "training", making some dated "wax on / wax off" reference as he walks away....

For each ???? below (reference line numbers for each), say what code you should insert. Some of these could be "nothing". 

Stack {
  int size;
  int items[SIZE];
  Semaphore mutex = 1;
  Semaphore SemA = ????;
  Semaphore SemB = ????;
}

void push(Stack stack, int value) {
  ?????
  P(mutex);
  ?????
  stack.items[stack.size] = value;
  stack.size ++;
  ????
  V(mutex)
  ????
  return;
}

int pop(Stack stack) {
  ?????
  P(mutex);
  ?????
  int value = stack.items[stack.size - 1];
  stack.size --;
  ??????
  V(mutex);
  ??????
  return value;
}

Answer #5:
	 5: SIZE	// (we let semA denote current stack _capacity_)
	 6: 0		// (we let semB denote _occupied_ stack slots) 		

	10: P(semA)	// (decrement capacity)
	12: 
	15: 
	17: V(semB)	// (increment occupied)
	
	22: P(semB)	// (decrement occupied)
	24: 
	27: 
	29: V(semA)	// (increment capacity)
	
	Producer/Consumer pattern: With this setup, if a thread is sleeping on a P(semA) in a push(), it can be notified of more space by a thread doing a complementary V(semA) in a pop() operation (and vice versa for a thread sleeping on semB in a pop()).
		* suppose t1 attempts to push(s, v) on a full stack s (so semA==0)
		* t1 will call P(semA) and go to sleep
		* context switch to t2
		* say t2 calls pop(), which calls V(semA) after a successful pop
		* thus t1 can now go back in the ready queue to complete its push()
	It is important that these complementary signaling semaphores be called outside of the mutex protected sections, else a thread could end up going to sleep without unlocking the mutex to other threads that would then not have the ability to do the operations to wake that thread back up. 


