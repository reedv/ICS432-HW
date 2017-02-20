Reed Villanueva
ICS432
HW3

Ex.1:
Question #1: Make it thread-safe [10pts]
The HashMap implementation is clearly not thread-safe. The current software engineer in place has even added the declaration for a lock (line 4 above). In the above code, only for insert() and delete(), state in between which code lines you would add calls to “lock(mutex)” and “unlock(mutex)”, attempting to make critical sections as short as possible (but without rewriting any of the existing code). A sample (wrong) answer would be:
	lock(mutex): 10-11, 32-33, 45-46.
    	unlock(mutex): 26-27, 35-36.

Answer #1:
	A piece of code is thread-safe if it manipulates shared data structures only in a manner that guarantees safe execution by multiple threads at the same time.
	In both insert() and delete(), we need the process of 'finding an open slot' and 'updating that slot (thus making it no longer free)' to be atomic.
	lock(mutex): 	9-10,  25-26
	unlock(mutex): 18-19, 38-39




Question #2: Multiple HashMaps [10pts]
It turns out that a common use-case for HashMap is as follows:

    There are many HashMap instances;
    There is a fixed universe of key-value pairs;
    Pairs are being moved (i.e., removed then added) between pairs of hashmaps;
    Multiple threads are doing these “moves”
    The semantic is that all threads should always see each pair in exactly one hashmap (i.e., never “the pair is in no hashmap” nor “the pair is in more than one hashmap”).

Your boss says “implement a move() that makes it possible to the above semantic”. You think about it for a minute and decide “it’s easy, just make the remove-insert sequence atomic with a lock”. So you add a new static method to your thread-safe HashMap implementation and a new static mutex, as follows:

class OpenHashMap {

  private Lock mutex;
  private static Lock move_mutex; // class variable!

  ...

  public static Status move(HashMap src, HashMap dst, KeyValuePair pair) {
    Status result;
    lock(move_mutex);
    result = src.delete(pair); 
    if (result == SUCCESS) {
       result = dst.insert(pair);
    }
    unlock(move_mutex);
    return result;
  }

Explain why the above does not fix the “each thread sees each pair in exactly one hashmap” semantic described in the previous section? Give a simple scenario that breaks the semantic.

Answer #2:
	This implementation does not allow the above semantic because…
		* suppose thread t1 calls move(src=map1, dst=map2, pair=p)
		* and suppose that after the 'src.delete.(p)' the cpu context switches to another thread t2.
		* t2 calls either map1.lookup(p) or map2.lookup(p). In either case, t2 will not be able to find p because t1 has yet to finish (p has effectively disappeared).
	
	Furthermore,
		* suppose thread t1 calls move(src=map1, dst=map2, pair=p)
		* and suppose that after the 'src.delete.(p)' the cpu context switches to another thread t2.
		* t2 calls map1.insert(p) (which has just had p deleted from it)
		* context switch back to t1 which then calls map2.insert(p)
		* now p exists in both map1 and map2. 




Question #3: Fixing it? [10pts]
Explain how you would fix the problem in the previous question by modifying all other methods in the HashMap implementation. This should lead to a perfectly correct implementation that respects the desired semantic.

Answer #3:
	The problem with the move implementation is that on a high level we expect the move operation to be atomic so that at any point, no matter when a context switch has happened, the item p in move(src, dst, p) has either been moved or it has not (ie. is not in some intermediate state of being in the process of being moved). Thus we need to disallow the intermediate operations that comprise a move() operation from being called by other threads while a move is happening for a particular pair of hashmaps.
	The way to model this kind of atomic behavior would be to place lock(move_mutex) and unlock(move_mutex) pairs at the beginning and ends of all of the other accessing methods (insert(), delete(), and lookup()). 




Question #4: Not so easy after all? [10pts]

Given your fix in the previous question, describe why your implementation leads to low performance. More precisely, give a scenario in which your implementation limits concurrency too much.

Answer #4:
	The fix given in answer#3 leads to low performance because the use of the move() operation on any src-dst pair of hashmaps locks down the use of those hashmaps by any other threads.
		* suppose t1 calls move(map1, map2, p1)
		* at the very start of the method, the cpu context switches to t2
		* t2 calls map1.lookup(p2). 
	At this point, t2 needs to wait for t1 to completely finish moving p1 from map1 even though the element p2 that t2 wants to lookup has nothing to do with p1 (especially since p1 has not even been deleted by t1 yet, so could not even affect the open addressing system of the hashmap).




Epilogue
The sobering conclusion here is that perhaps going for a non-thread-safe implementation and telling users to “get with the program and learn how to use your own locks” is a better approach, since only the users truly know what semantics they want.

