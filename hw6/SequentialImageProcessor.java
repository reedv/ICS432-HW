import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import com.jhlabs.image.*;

import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class SequentialImageProcessor {

  public static void main(String args[]) {
    SequentialImageProcessor sip = new SequentialImageProcessor();
    sip.sequentialImageProcessor(args);
  }

  private void sequentialImageProcessor(String args[]) {
    double timeDelta = 0, readTime = 0, processTime = 0, writeTime = 0, totalTime = 0;
    totalTime = System.currentTimeMillis();

    String filterName = "", imagePath = "";
    BufferedImageOp filter = null;

    // init. input args
    try
    {
      filterName = args[0];
      imagePath = args[1];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      System.out.println("Usage: java -cp  .:Filters.jar SequentialImageProcessor <filter name> <directory path>");
      System.exit(0);
    }

    // check for valid filter option
    if (!filterName.startsWith("oil") && filterName.equals("invert") && filterName.equals("smear")) {
      System.out.println(filterName + " is not a valid filter name.\n" +
                         "Valid filter names:\n" +
                         "oil<single digit int>, invert, smear\n");
      System.exit(0);
    }

    // read in image files to process
    readTime = System.currentTimeMillis();
    ArrayList<File> images = getFiles(imagePath);
    readTime = (System.currentTimeMillis() - readTime) / 1000;

    // process all images using specified filter
    if (filterName.startsWith("oil")){
      for (File img : images) {
        // set input args
        BufferedImage input = file2BufferedImage(img);
        BufferedImage output = null;
        int range = Character.getNumericValue(filterName.charAt(3));
        String outputNameSuffix = img.getName();

        // filter and record time
        timeDelta = System.currentTimeMillis();
        output = oilFilter(filter, input, output, range);
        processTime += (System.currentTimeMillis() - timeDelta) / 1000;

        // write file and record time
        timeDelta = System.currentTimeMillis();
        saveImage(output, "./oil" + range + "_" + outputNameSuffix + ".jpg");
        writeTime += (System.currentTimeMillis() - timeDelta) / 1000;
      }
    } else if (filterName.equals("invert")) {
      for (File img : images) {
        // set input args
        BufferedImage input = file2BufferedImage(img);
        BufferedImage output = null;
        String outputNameSuffix = img.getName();

        // filter and record time
        timeDelta = System.currentTimeMillis();
        output = invertFilter(filter, input, output);
        processTime += (System.currentTimeMillis() - processTime) /1000;

        // write file and record time
        timeDelta = System.currentTimeMillis();
        saveImage(output, "./invert_" + outputNameSuffix + ".jpg");
        writeTime += (System.currentTimeMillis() - timeDelta) / 1000;
      }
    } else if (filterName.equals("smear")) {
      for (File img : images) {
        // set input args
        BufferedImage input = file2BufferedImage(img);
        BufferedImage output = null;
        String outputNameSuffix = img.getName();

        // filter and record time
        timeDelta = System.currentTimeMillis();
        output = smearFilter(filter, input, output, 10);
        processTime += (System.currentTimeMillis() - processTime) / 1000;

        // write file and record time
        timeDelta = System.currentTimeMillis();
        saveImage(output, "./smear_" + outputNameSuffix + ".jpg");
        writeTime += (System.currentTimeMillis() - timeDelta) / 1000;
      }
    }

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

        // indicate that file has been read
        System.out.print("r");
      }
    } catch (IOException e) {
      System.err.println(e);
      System.exit(1);
    }

    return files;
  }

  private BufferedImage file2BufferedImage(File file) {
    BufferedImage buffOut = null;
    try {
      buffOut = ImageIO.read(file);
    } catch (IOException e) {
      System.out.println("Cannot read file " + file.getName());
      return null;
    }

    return buffOut;
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

  private void saveImage(BufferedImage image, String filename) {
    try {
      ImageIO.write(image, "jpg", new File(filename));

      // indicate that file has been written
      System.out.print("w");

    } catch (IOException e) {
      System.out.println("Cannot write file "+filename);
      System.exit(1);
    }
  }
}