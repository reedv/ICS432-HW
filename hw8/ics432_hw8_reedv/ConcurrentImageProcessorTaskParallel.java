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
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.locks.ReentrantLock;

/**
 * Similar to ConcurrentImageProcessor but uses 1 reader, multiple filterers, prodconLimit=1 writer thread.
 * WARN: becuase of how the multiple producer multiple consumer scheme is done between filter and writer
 * threads, may end up with 1 more writer threads than expected if prodconLimit > 1 
 * (if filter threads dont evenly divide writer threads).
 * @author reedvilanueva
 *
 */
public class ConcurrentImageProcessorTaskParallel {

  /**
   * max number of processing threads.
   * Note, this only changes the number of filter and writer threads,
   * since reading is very fast (always only have 1 reader).
   */
  private int prodconLimit;
  
  /**
   * For this assignment, needed to have 1 reader, multiple filterers, and 1 writer.
   * So had to break the 1-to-1 filterers-writers ratio of ConcurrentImageProcessor
   * based on the prodconLimit. Does not affect the multiple poison pill scheme of the reader
   * so long as number of filterers > number of writers.
   * I know its ugly, but thats what we're supposed to do.
   */
  private final int numFilterers;
  
  /**
   * size of the prodcon buffers and semaphore resource counters
   */
  private final int prodconResources;

  // note: no longer track cumulative processTime, insead each thread tracks its own
  private volatile double readTime = 0, /*processTime = 0,*/ writeTime = 0, totalTime = 0;

  /**
   * buffer resource counters to implement ProdCon design from reader to filterers
   */
  private Sem freeFilters, filledFilters;
  /**
   * buffer resource counters to implement ProdCon design from filterers to writers
   */
  private Sem freeWriters, filledWriters;
  
  private Lock readerFilterMutex;
  private Lock filterWriterMutex;
    
  /**
   * global buffer to hold images to be processed
   */
  private volatile ArrayList<FileNamePair> imgsToFilter;
  private volatile int currentImgToFilter;
  /**
   * global buffer to hold images to be written
   */
  private volatile ArrayList<FileNamePair> imgsToWrite;
  private volatile int currentImgToWrite;
  
  /**
   * progress bar for representing files written to disk
   */
  volatile ProdconProgressBar pbar;
  /**
   * tracks the number of files written to disk so far
   */
  volatile int filesWritten = 0;

  public ConcurrentImageProcessorTaskParallel(int resources, int numFilterers) {
	prodconLimit = 1;
	this.numFilterers = numFilterers;
	prodconResources = resources;
	
    freeFilters = new Sem(prodconResources);
    filledFilters = new Sem(0);
    readerFilterMutex = new Lock();
    // init filters' resource buffer
    imgsToFilter = new ArrayList<>();
    currentImgToFilter = -1;

    freeWriters = new Sem(prodconResources);
    filledWriters = new Sem(0);
    filterWriterMutex = new Lock();
    // init writers' resource buffer
    imgsToWrite = new ArrayList<>();
    currentImgToWrite = -1;
  }

  public static void main(String args[]) {
	  try {
		  final int numThreads = Integer.parseInt(args[0]);
		  ConcurrentImageProcessorTaskParallel cip_tp = 
				  new ConcurrentImageProcessorTaskParallel(8, numThreads);
		  cip_tp.concurrentImageProcessorTaskParallel(args);
	  } catch (ArrayIndexOutOfBoundsException e) {
		  e.printStackTrace();
		  System.out.println("Usage: java -cp .:Filters.jar ConcurrentImageProcessor "
		      		+ "<number filter threads> <filter name> <directory path>");
	      System.exit(0);
	  }
  }

  private void concurrentImageProcessorTaskParallel(String args[]) {
    totalTime = System.currentTimeMillis();

    String filterName = "", imagesPath = "";

    // init. input args
    try
    {
	  filterName = args[1].toLowerCase();
	  imagesPath = args[2];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      System.out.println("Usage: java -cp .:Filters.jar ConcurrentImageProcessor "
      		+ "<number filter threads> <filter name> <directory path>");
      System.exit(0);
    }

    // check for valid filter option
    if (!filterName.equals("oil1") && !filterName.equals("oil3") &&
        !filterName.equals("invert") &&
        !filterName.equals("smear") &&
        !filterName.equals("weird")) {
      System.out.println(filterName + " is not a valid filter name.\n" +
          "Valid filter names:\n" +
          "oil1, oil3, invert, smear, weird\n");
      System.out.println("args: <threads>="+numFilterers+
    		             "<filter name>="+filterName+
    		             " <dir path>="+imagesPath);
      System.exit(0);
    }

    // get image files to be read
    ArrayList<File> files = getFiles(imagesPath);
    // start progress bar
    final int epsilon = 2;
    pbar = new ProdconProgressBar(files.size(), epsilon);
    pbar.init(filterName + " filter");
    
    // if number of writer threads does not evenly divide filter threads
    // not all writers can collect enough poison pills to know to terminate.
    // May end up with unwritten filtered images left behind in filter-to-writer buffer.
    final int pillsToCollect = (numFilterers%prodconLimit == 0)?
    							numFilterers / prodconLimit:
    							numFilterers / ++prodconLimit;
    // spin off reader, filterers, and writers
    // only 1 reader, since only allowing 8 to-be-processed imgs at a time
    Reader_t reader = new Reader_t(files);
    Filter_t[] filters = new Filter_t[numFilterers];
    Writer_t[] writers = new Writer_t[prodconLimit];
    for (int i=0; i < numFilterers; i++) filters[i] = new Filter_t(filterName);
    for (int i=0; i < prodconLimit; i++) writers[i] = new Writer_t(imagesPath, pillsToCollect);

    reader.start();
    for (int i=0; i < numFilterers; i++) filters[i].start();
    for (int i=0; i < prodconLimit; i++) writers[i].start();

    // collect all threads once all images processed and written
    try {
      reader.join();
      for (int i = 0; i < numFilterers; i++) filters[i].join();
      for (int i = 0; i < prodconLimit; i++) writers[i].join();
    } catch (InterruptedException e) {
      System.err.println("Exception while trying to join threads");
      System.err.println(e);
      System.exit(1);
    }

    // log finishing times
    totalTime = (System.currentTimeMillis() - totalTime) / 1000;
    System.out.println("\nTime spent reading: "+ readTime +" sec.");
    /*System.out.println("Cumulative time spent processing: "+ processTime +" sec.");*/
    System.out.println("Cumulative spent writing: "+ writeTime +" sec.");
    System.out.println("Overall execution time: "+ totalTime +" sec.");
    /*System.out.println("IO intensivness: " + (readTime + writeTime) / processTime);*/

    // close progress bar if not closed yet if some files stalling
    //pbar.frame.dispose();
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


  
  /*****************************************
   * ProdCon Threads
   *****************************************/

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
				File img = files.remove(0);  
				timeDelta = System.currentTimeMillis();
				BufferedImage buffOut = file2BufferedImage(img);
				readTime += (System.currentTimeMillis() - timeDelta) / 1000;
				
				// TODO: move start of CS here?
				
				// add read image and name to filter buffer
				currentImgToFilter++;
				FileNamePair toProcess = new FileNamePair(buffOut, removeFilenameExt(img.getName()));
				//imgsToFilter[currentImgToFilter] = toProcess;
				imgsToFilter.add(toProcess);
				
				readerFilterMutex.unlock();
				filledFilters.release();
		    }
			
			// insert poison pills to back of buffer for each filter worker when all files read
			for (int i=0; i < numFilterers; i++) {
				freeFilters.acquire();
				readerFilterMutex.lock();
				
				// add all poison pill to filter buffer
				currentImgToFilter++;
				FileNamePair poison = new FileNamePair(null, null);
				//imgsToFilter[currentImgToFilter] = poison;
				imgsToFilter.add(poison);
				
				readerFilterMutex.unlock();
				filledFilters.release();
			}
	    } catch (InterruptedException e) {
	    	
	    }
		
		//System.out.println("Reader thread exiting: " + Thread.currentThread().getId());
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
    
    /**
     * Track processsing time of each individual filterer thread.
     */
    private double processTime = 0;
    private double timeDelta = 0;

    public Filter_t(String filterName) {
    	this.filterName = filterName;
    }

    public void run() {
    	 try {
    		 while (true) {    			
    			//System.out.println("Filter thread try acquire filter-sem.: currentImgToFilter=" + currentImgToFilter + ": " + Thread.currentThread().getId());
    			filledFilters.acquire();
    			readerFilterMutex.lock();
    			
				// get next image from buffer for processing
				BufferedImage input = imgsToFilter.get(0).image;
				String outputNameSuffix = imgsToFilter.get(0).imageName;
				imgsToFilter.remove(0);
				currentImgToFilter--;
				
				readerFilterMutex.unlock();
    			freeFilters.release();
    			
    			// check for poison pill
    			if (input==null && outputNameSuffix==null) {
    				break;
    			}

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
			    } else if (filterName.equals("invert")) {
			        // filter and record time
			        timeDelta = System.currentTimeMillis();
			        output = invertFilter(filter, input, output);
			        processTime += (System.currentTimeMillis() - timeDelta) /1000;
			    } else if (filterName.equals("smear")) {
			        // filter and record time
			        timeDelta = System.currentTimeMillis();
			        output = smearFilter(filter, input, output, 10);
			        processTime += (System.currentTimeMillis() - timeDelta) / 1000;
			    } else if (filterName.equals("weird")) {
			    	// filter and record time
			        timeDelta = System.currentTimeMillis();
			        output = weirdFilter(filter, input, output);
			        processTime += (System.currentTimeMillis() - timeDelta) / 1000;
			    }
				outputName = "./"+filterName+"_"+outputNameSuffix;
				
    			// pass image onto writers
				//System.out.println("Filter thread try acquire writer-sem.: currentImgToFilter=" + currentImgToFilter + ": " + Thread.currentThread().getId());
    			freeWriters.acquire();
    			filterWriterMutex.lock();
    			
    			currentImgToWrite++;
    			FileNamePair toWrite = new FileNamePair(output, outputName);
    			imgsToWrite.add(toWrite);    			
    			
    			filterWriterMutex.unlock();
    			filledWriters.release();
    		 }
    		 
    		 System.out.println("Time spent processing: "+ processTime +" sec.");
    		
    		// insert a single poison pill in the back of filterwriter buffer for a corresponding writer
    		freeWriters.acquire();
			filterWriterMutex.lock();
			
			currentImgToWrite++;
			FileNamePair poison = new FileNamePair(null, null);
			imgsToWrite.add(poison);    			
			
			filterWriterMutex.unlock();
			filledWriters.release();
    		 
    	} catch (InterruptedException e) {

    	}
    	
    	//System.out.println("Filter thread exiting: " + Thread.currentThread().getId());
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
	
	private BufferedImage weirdFilter(BufferedImageOp filter,
							BufferedImage input, BufferedImage output) {
		// init. output image
		output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		
		// Apply the Invert filter
		filter = new WeirdFilter();
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
	  /**
	   * relative path to write images to
	   */
	  private final String relativePath;
	  
	  private double timeDelta = 0;
	  
	  private final int pillsToCollect;
	  private int poisonPillCount = 0;
	  
	  public Writer_t(String relativePath, int pillsToCollect) {
		  this.relativePath = relativePath;
		  this.pillsToCollect = pillsToCollect;
	  }

	  public void run() {
		  while (true) {
			  try {
				  filledWriters.acquire();
				  filterWriterMutex.lock();
          
				  // get next image from buffer to write
				  BufferedImage img = imgsToWrite.get(0).image;
				  String name = imgsToWrite.get(0).imageName;
				  imgsToWrite.remove(0);
				  currentImgToWrite--;
          
				  filterWriterMutex.unlock();
				  freeWriters.release();
				  
				  // check for poison pill
				  if (img==null && name==null) {
					  poisonPillCount++;
					  if (poisonPillCount == pillsToCollect) break;
					  
					  continue;
				  }

				  // write file to disk and record time
				  timeDelta = System.currentTimeMillis();
				  saveImage(img, name, "jpg");
				  writeTime += (System.currentTimeMillis() - timeDelta) / 1000;
				  
				  // update progress bar
				  pbar.updateBarThread(++filesWritten);
				 
			  } catch (InterruptedException e) {
				  break;
			  }
		  }
		  
		  //System.out.println("Writer thread exiting: " + Thread.currentThread().getId());
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
          ImageIO.write(image, formatName, new File("./" + relativePath + "/" + filename + "." + formatName));

          // print to indicate that file has been written
          System.out.print("w");

        } catch (IOException e) {
          System.out.println("Cannot write file " + filename);
          System.exit(1);
        }
    }
  }

}






