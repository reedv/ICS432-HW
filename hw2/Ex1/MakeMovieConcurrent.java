import org.ics432.raytracer.ABunchOfSpheres;

import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.NumberFormatException;


public class MakeMovieConcurrent
{
    /*
    class FrameProcessTask implements Runnable
    {
        ABunchOfSpheres  movie;
        // number of frames to process
        int num_frames;
        // starting ID of frames for this thread
        int frame_start;
        
        public FrameProcessTask(ABunchOfSpheres  movie, int num_frames, int frame_start)
        {
            this.movie = movie;  // moive with frames to process
            this.num_frames = num_frames;
            this.frame_start = frame_start;
        }
    
        public void run()
        {
            // process num_frames frames starting at frame_start
            for(int count = frame_start; count < (frame_start+num_frames); count++)
            {
                //System.out.println("count "+ count + " frame start "+frame_start);
                movie.render_scene("./frame_" + String.format("%05d", count) + ".png", count);
            }
        
        }
    }
     */

    private static void abort(String message) {
        System.err.println(message);
        System.exit(1);
    }

    public static void main(String[] args) {
        int num_frames = 0;
        int num_threads = 1;
        int frames_per_thread = 0;
        Thread[] threadArray = null;
        ABunchOfSpheres movie = new ABunchOfSpheres();
        // validate/init. input args
        try {
             // get args
             num_frames = Integer.parseInt(args[0]);
             num_threads = Integer.parseInt(args[1]);
             // uniformly distribute frame processing over each thread
             frames_per_thread = num_frames/num_threads;
             // init collection of threads
             threadArray = new Thread[num_threads];
        } catch (NumberFormatException e) {
            abort("Invalid number of frames and threads (should be an integer)");
        } catch (ArrayIndexOutOfBoundsException e) {
            abort("Usage: java MakeMovieSequential <num of frames> [number of threads]");
        }
        if (num_frames < 0 || num_threads < 0 || num_frames <= num_threads) {
            abort("Invalid number of frames and threads (should be positive)");
        }
        
        for(int count = 0;count < num_threads; count++)
        {
            // calculate stride for next frame processing thread to start at
            int frame_start = frames_per_thread * count;
            // spin off worker thread
            /*threadArray[count] = new Thread(new FrameProcessTask(movie, frames_per_thread, frame_start));*/
            threadArray[count] = new ThreadEx1(movie, frames_per_thread, frame_start);
            threadArray[count].start();
        }
        
        // wait for and collect all of the generated threads
        for (int j = 0; j < num_threads; j++)
        {
            try{
                threadArray[j].join();
            }
            catch(InterruptedException E)
            {
                System.out.println("interupted");
            }
        }
    }
}
