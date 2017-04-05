/**
 * Lock implemented using java monitors
 */
public class Lock {
	private boolean locked = false;
		
	public synchronized void lock() throws InterruptedException {
		while (locked) {
			this.wait();
		}
		locked = true;
	}
	
	public synchronized void unlock() {
		locked = false;
		this.notify();
	}
}