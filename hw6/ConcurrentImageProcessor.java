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
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentImageProcessor {

  /**
   * max number of processing threads
   */
  private int prodconLimit = 8;

  private volatile double readTime = 0, processTime = 0, writeTime = 0, totalTime = 0;

  /**
   * resource counters to implement ProdCon design from reader to filterers
   */
  private Semaphore freeFilters, filledFilters;
  /**
   * resource counters to implement ProdCon design from filterers to writers
   */
  private Semaphore freeWriters, filledWriters;
  
  private ReentrantLock readerFilterMutex;
  private ReentrantLock filterWriterMutex;

  /**
   * global buffer to hold images to be processed
   */
  private volatile FileNamePair[] imgsToFilter;
  private volatile int currentImgToFilter;
  /**
   * global buffer to hold images to be written
   */
  private volatile FileNamePair[] imgsToWrite;
  private volatile int currentImgToWrite;
  
  /**
   * poison pill set by reader worker to signal that all files have been read and written to processing buffer
   */
  private volatile boolean allFilesRead = false;
  /**
   * poison pill set by filter worker to signal that all files have been processed and written to writing buffer
   */
  private volatile boolean allFilesProcessed = false;


  public ConcurrentImageProcessor() {
    freeFilters = new Semaphore(prodconLimit);
    filledFilters = new Semaphore(0);
    readerFilterMutex = new ReentrantLock();
    // init filters' resource buffer
    imgsToFilter = new FileNamePair[prodconLimit];
    currentImgToFilter = -1;

    freeWriters = new Semaphore(prodconLimit);
    filledWriters = new Semaphore(0);
    filterWriterMutex = new ReentrantLock();
    // init writers' resource buffer
    imgsToWrite = new FileNamePair[prodconLimit];
    currentImgToWrite = -1;
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
      filterName = args[0].toLowerCase();
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

    // if more threads than files are spun, hey will stay asleep and wont
    // be able to consume poison-pill signal produced by reader thread to end them
    if (files.size() < prodconLimit) prodconLimit = files.size();
    
    // spin off reader, filterers, and writers
    // only 1 reader, since only allowing 8 to-be-processed imgs at a time
    Reader_t reader = new Reader_t(files);
    Filter_t[] filters = new Filter_t[prodconLimit];
    Writer_t[] writers = new Writer_t[prodconLimit];
    for (int i=0; i < prodconLimit; i++) {
        filters[i] = new Filter_t(filterName);
        writers[i] = new Writer_t();
      }

    reader.start();
    for (int i=0; i < prodconLimit; i++) filters[i].start();
    for (int i=0; i < prodconLimit; i++) writers[i].start();

    // collect all threads once all images processed and written
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
    System.out.println("Cumulative time spent processing: "+ processTime +" sec.");
    System.out.println("Cumulative spent writing: "+ writeTime +" sec.");
    System.out.println("Overall execution time: "+ totalTime +" sec.");

  }

  /**
  * Returns full list of image_*.jpg Files from a given dir. path
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


  
  /*******************************
   * ProdCon Threads
   *******************************/

  /*********************
   * reader worker
   ********************/
  class Reader_t extends Thread {
	/**
	 * list of all files that need to be read
	 */
    private ArrayList<File> files;
    private double timeDelta = 0;

    public Reader_t(ArrayList<File> files) {
    	this.files = files;
    }

    public void run() {
    	// go thru all files to read
		try {
			while (files.size() > 0) {
				freeFilters.acquire();
				readerFilterMutex.lock();
				
				// read in next image
				File img = files.get(0);
				timeDelta = System.currentTimeMillis();
				BufferedImage buffOut = file2BufferedImage(img);
				readTime += (System.currentTimeMillis() - readTime) / 1000;
				
				// add read image and name to filter buffer
				currentImgToFilter++;
				FileNamePair toProcess = new FileNamePair(buffOut, removeFilenameExt(img.getName()));
				imgsToFilter[currentImgToFilter] = toProcess;
		    
				// remove image from waiting queue
				files.remove(0);
				
				readerFilterMutex.unlock();
				filledFilters.release();
		    }
			
			// set poison pill when all files read and in buffer queue
			allFilesRead = true;			
	    } catch (InterruptedException e) {

	    }
		
		System.out.println("Reader thread exiting: " + Thread.currentThread().getId());
    }	

    private BufferedImage file2BufferedImage(File file) {
    	BufferedImage buffOut = null;
    	try {
    		// read in File to BufferedImage
    		buffOut = ImageIO.read(file);
    		// print indicator that file has been read
    		System.out.print("r");
    	} catch (IOException e) {
    		System.out.println("Cannot read file " + file.getName());
    		return null;
    	}
	
    	return buffOut;
    }
    
    /**
     * Simple filename extension remover
     * @param filename
     * @return filename truncated before first '.' character
     */
    private String removeFilenameExt(String filename) {
  	  if (filename.indexOf(".") > 0) {
            filename = filename.substring(0, filename.lastIndexOf("."));
        }
  	  return filename;
    }
  }

  
  /*********************
   * image processor worker
   ********************/
  class Filter_t extends Thread {
	/**
	 * name of the filter to be used
	 */
    private final String filterName;
    
    private double timeDelta = 0;

    public Filter_t(String filterName) {
    	this.filterName = filterName;
    }

    public void run() {
    	while (!allFilesRead || currentImgToFilter > -1) {
    		try {
    			System.out.println("Filter thread try acquire filter-sem. :" + Thread.currentThread().getId());
    			filledFilters.acquire();
    			readerFilterMutex.lock();
    			
				// get next image from buffer for processing
				BufferedImage input = imgsToFilter[currentImgToFilter].image;
				String outputNameSuffix = imgsToFilter[currentImgToFilter].imageName;
				currentImgToFilter--;
				
				readerFilterMutex.unlock();
    			freeFilters.release();

				// process image
				BufferedImageOp filter = null;
				BufferedImage output = null;
				String outputName = "";
				if (filterName.startsWith("oil")){
			        int range = Character.getNumericValue(filterName.charAt(3));

			        // filter and record time
			        timeDelta = System.currentTimeMillis();
			        output = oilFilter(filter, input, output, range);
			        processTime += (System.currentTimeMillis() - timeDelta) / 1000;

			        outputName = "./oil" + range + "_" + outputNameSuffix;
			    } else if (filterName.equals("invert")) {
			        // filter and record time
			        timeDelta = System.currentTimeMillis();
			        output = invertFilter(filter, input, output);
			        processTime += (System.currentTimeMillis() - processTime) /1000;

			        outputName = "./invert_" + outputNameSuffix;
			    } else if (filterName.equals("smear")) {
			        // filter and record time
			        timeDelta = System.currentTimeMillis();
			        output = smearFilter(filter, input, output, 10);
			        processTime += (System.currentTimeMillis() - processTime) / 1000;

			        outputName = "./smear_" + outputNameSuffix;
			    }

    			// pass image onto writers
    			System.out.println("Filter thread try acquire writer-sem. :" + Thread.currentThread().getId());
    			freeWriters.acquire();
    			filterWriterMutex.lock();
    			
    			currentImgToWrite++;
    			FileNamePair toWrite = new FileNamePair(output, outputName);
    			imgsToWrite[currentImgToWrite] = toWrite;
    			
    			// send poison pill signal to writers
    			if (allFilesRead && currentImgToFilter <= -1) {
					allFilesProcessed = true;
				}
    			
    			filterWriterMutex.unlock();
    			filledWriters.release();
    		} catch (InterruptedException e) {
    			break;
    		}
    	}
    	
    	System.out.println("Filter thread exiting: " + Thread.currentThread().getId());
	}
    
    private BufferedImage oilFilter(BufferedImageOp filter,
            BufferedImage input, BufferedImage output,
            int range) {
		// init. output image
		output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		
		// Apply the Oil1 filter and save image
		filter = new OilFilter();
		((OilFilter)filter).setRange(range);
		filter.filter(input, output);
		
		// indicate that file has been processed
		System.out.print("p");
		
		return output;
	}
		
	private BufferedImage invertFilter(BufferedImageOp filter,
	                        BufferedImage input, BufferedImage output) {
		// init. output image
		output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		
		// Apply the Invert filter
		filter = new InvertFilter();
		filter.filter(input, output);
		
		// indicate that file has been processed
		System.out.print("p");
		
		return output;
	}
		
	private BufferedImage smearFilter(BufferedImageOp filter,
	                       BufferedImage input, BufferedImage output,
	                       int shape) {
		// init. output image
		output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		
		// Apply the Smear filter
		filter = new SmearFilter();
		((SmearFilter)filter).setShape(shape);
		filter.filter(input, output);
		
		// indicate that file has been processed
		System.out.print("p");
		
		return output;
	}
	
  }

  
  /*********************
   * writer worker
   ********************/
  class Writer_t extends Thread {
	  private double timeDelta = 0;
	  public Writer_t() {

	  }

	  public void run() {
		  while (!allFilesProcessed || currentImgToWrite > -1) {
			  try {
				  filledWriters.acquire();
				  filterWriterMutex.lock();
          
				  // get next image from buffer to write
				  BufferedImage img = imgsToWrite[currentImgToWrite].image;
				  String name = imgsToWrite[currentImgToWrite].imageName;
				  currentImgToWrite--;
          
				  filterWriterMutex.unlock();
				  freeWriters.release();

				  // write file to disk and record time
				  timeDelta = System.currentTimeMillis();
				  saveImage(img, name, "jpg");
				  writeTime += (System.currentTimeMillis() - timeDelta) / 1000;
			  } catch (InterruptedException e) {
				  break;
			  }
		  }
		  
		  System.out.println("Writer thread exiting: " + Thread.currentThread().getId());
	  }
    
	/**
	 * Writes image to disk in current directory. Image name is the filename
	 * with the extension given by formatName, which will also be the format type of the file.
	 * @param image
	 * @param filename
	 * @param formatName
	 */
    private void saveImage(BufferedImage image, String filename, String formatName) {
        try {
          ImageIO.write(image, formatName, new File(filename + "." + formatName));

          // print to indicate that file has been written
          System.out.print("w");

        } catch (IOException e) {
          System.out.println("Cannot write file " + filename);
          System.exit(1);
        }
    }
  }

}


/**
 * Helper class pair type for holding images and their names to be written
 */
class FileNamePair {
  public BufferedImage image;
  public String imageName;

  public FileNamePair(BufferedImage bi, String in) {
    this.image = bi;
    this.imageName = in;
  }
}