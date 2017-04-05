/**
 * Semaphore implemented using java monitors
 */
public class Sem {
	volatile int value;
		
	public Sem(int value) {
		this.value = value;
	}
	
	public synchronized void acquire() throws InterruptedException {
		while (value == 0) {
			this.wait();
		}
		value--;
	}
	
	public synchronized void release() {
		value++;
		this.notify();
	}
}