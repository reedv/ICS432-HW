import org.ics432.raytracer.ABunchOfSpheres;

import java.lang.System.*;

public class ThreadEx1 extends Thread
{
    ABunchOfSpheres  movie;
    // number of frames to process
    int num_frames;
    // starting ID of frames for this thread
    int frame_start;
    
    public ThreadEx1(ABunchOfSpheres  movie, int num_frames, int frame_start)
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
