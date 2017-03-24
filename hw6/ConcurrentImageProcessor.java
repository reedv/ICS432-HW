import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import com.jhlabs.image.*;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import java.lang.Thread;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class ConcurrentImageProcessor {

  // max number of processing threads
  private final int prodconLimit = 8;

  private double timeDelta = 0, readTime = 0, processTime = 0, writeTime = 0, totalTime = 0;

  // resource counters to implement ProdCon design
  private Semaphore freeFilters, filledFilters,
                    freeWriters, filledWriters;

  // global buffers to hold images to be processed and written
  private volatile BufferedImage[] filterImgs;
  private volatile FileNamePair[] writerImgs;


  public ConcurrentImageProcessor() {
    freeFilters = new Semaphore(prodconLimit);
    filledFilters = new Semaphore(0);
    // init filters' resource buffer
    filterImgs = new BufferedImage[prodconLimit];

    freeWriters = new Semaphore(prodconLimit);
    filledWriters = new Semaphore(0);
    // init writers' resource buffer
    writerImgs = new FileNamePair[prodconLimit];
  }

  public static void main(String args[]) {
    ConcurrentImageProcessor cip = new ConcurrentImageProcessor();
    cip.concurrentImageProcessor(args);
  }

  private void concurrentImageProcessor(String args[]) {
    totalTime = System.currentTimeMillis();

    String filterName = "", imagesPath = "";

    // init. input args
    try
    {
      filterName = args[0];
      imagesPath = args[1];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      System.out.println("Usage: java -cp  .:Filters.jar ConcurrentImageProcessor <filter name> <directory path>");
      System.exit(0);
    }

    // check for valid filter option
    if (!filterName.equals("oil1") && !filterName.equals("oil3") &&
        !filterName.equals("invert") &&
        !filterName.equals("smear")) {
      System.out.println(filterName + " is not a valid filter name.\n" +
          "Valid filter names:\n" +
          "oil1, oil3, invert, smear\n");
      System.exit(0);
    }

    // get image files to be read
    ArrayList<File> files = getFiles(imagesPath);

    // spin off reader, filterers, and writers
    // only 1 reader, since only allowing 8 to-be-processed imgs at a time
    Reader_t reader = new Reader_t(files);
    Filter_t[] filters = new Filter_t[prodconLimit];
    Writer_t[] writers = new Writer_t[prodconLimit];

    reader.start();
    for (int i=0; i < prodconLimit; i++) {
      filters[i] = new Filter_t(filterName);
      filters[i].start();
      writers[i] = new Writer_t();
      writers[i].start();
    }

    // collect all threads once all images processed ad written
    try {
      reader.join();
      for (int i = 0; i < prodconLimit; i++) filters[i].join();
      for (int i = 0; i < prodconLimit; i++) writers[i].join();
    } catch (InterruptedException e) {
      System.err.println("Exception while trying to join threads");
      System.err.println(e);
      System.exit(1);
    }

    // log finishing times
    totalTime = (System.currentTimeMillis() - totalTime) / 1000;
    System.out.println("\nTime spent reading: "+ readTime +" sec.");
    System.out.println("Time spent processing: "+ processTime +" sec.");
    System.out.println("Time spent writing: "+ writeTime +" sec.");
    System.out.println("Overall execution time: "+ totalTime +" sec.");

  }

  /*
  * Return list of Files from a given dir. path
  * */
  private ArrayList<File> getFiles(String directoryPath) {
    ArrayList<File> files = new ArrayList<>();

    Path path = Paths.get(directoryPath);

    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path, "image_*.jpg")) {
      for (Path p : dirStream) {
        files.add(p.toFile());
      }
    } catch (IOException e) {
      System.err.println(e);
      System.exit(1);
    }

    return files;
  }


  /*********************
   * ProdCon Threads
   ********************/

  class Reader_t extends Thread {
    private ArrayList<File> files;

    public Reader_t(ArrayList<File> files) {
      this.files = this.files;
    }

    public void run() {
      while (files.size() > 0) {
        try {
          freeFilters.acquire();

          // sync using this inner class
          synchronized (this) {
            // read in next image
            BufferedImage buffOut = file2BufferedImage(files.get(0));

            // remove image from waiting queue
            files.remove(0);
          }

          filledFilters.release();
        } catch (InterruptedException e) {
          break;
        }
      }
    }

    private BufferedImage file2BufferedImage(File file) {
      BufferedImage buffOut = null;
      try {
        // read in File to BufferedImage
        buffOut = ImageIO.read(file);
        // indicate that file has been read
        System.out.print("r");
      } catch (IOException e) {
        System.out.println("Cannot read file " + file.getName());
        return null;
      }

      return buffOut;
    }
  }

  class Filter_t extends Thread {
    private String filterName;

    public Filter_t(String filterName) {
      this.filterName = filterName;
    }

    public void run() {
      while (true) {
        try {
          filledFilters.acquire();
          // sync using this inner class
          synchronized (this) {
            // get next image from buffer

            // process image

          }
          freeFilters.release();

          // pass image onto writers
          freeFilters.acquire();
          synchronized (this) {

          }
          filledWriters.release();
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }

  class Writer_t extends Thread {
    public Writer_t() {

    }

    public void run() {
      while (true) {
        try {
          filledWriters.acquire();
          synchronized (this) {
            // get next image from buffer

            // write image to disk
          }
          freeWriters.release();
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }

}

/**
 * Helper class pair type for holding images and thier names to be written
 */
class FileNamePair {
  public final BufferedImage image;
  public final String imageName;

  public FileNamePair(BufferedImage bi, String in) {
    this.image = bi;
    this.imageName = in;
  }
}