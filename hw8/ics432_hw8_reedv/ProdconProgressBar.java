import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import java.lang.Thread;

/**
 * Creates progress bar and provides method for updating progress bar value
 * @author reedvilanueva
 *
 */
@SuppressWarnings("serial")
public class ProdconProgressBar extends JPanel {
	JFrame frame;
	JProgressBar pbar;

	final int MINIMUM = 0;
	final int MAXIMUM;
	
	/**
	 * Used in case volatile updates miss progress update values
	 * in this.updateBarThread. Lets progrss bar close even when miss
	 * some updates values to stop from freezing.
	 */
	final int EPSILON;

	public ProdconProgressBar(int maximum, int eps) {
		// initialize Progress Bar
		pbar = new JProgressBar();
		this.EPSILON = eps;
		this.MAXIMUM = maximum;
		pbar.setMinimum(MINIMUM);
		pbar.setMaximum(MAXIMUM);
		// add to JPanel
		add(pbar);
	}
	
	/**
	 * initializes window to display progress bar
	 * @param barWindowName
	 */
	public void init(String barWindowName) {
	    frame = new JFrame(barWindowName);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setContentPane(pbar);
	    frame.pack();
	    frame.setBounds(150, 150, 
	    				300, 100);
	    frame.setVisible(true);
	}
	
	/**
	 * Runs a thread to update progress bar with new value percent
	 * @param percent
	 */
	public synchronized void updateBarThread(final int percent) {
		try {
	        SwingUtilities.invokeLater(new Runnable() {
	        	
	          public void run() {
	            pbar.setValue(percent);
	            if(percent+EPSILON >= MAXIMUM) frame.dispose();
	          }
	          
	        });
	        java.lang.Thread.sleep(100);
	      } catch (InterruptedException e) {
	        ;
	      }
	}
}