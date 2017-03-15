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
    ArrayList<File> images = getFiles(imagePath);

    // process all images using specified filter
    if (filterName.startsWith("oil")){
      for (File img : images) {
        BufferedImage input = null;
        try {
          input = ImageIO.read(img);
        } catch (IOException e) {
          System.out.println("Cannot read file " + img.getName());
        }

        BufferedImage output = null;
        String outputNameSuffix = img.getName();
        oilFilter(filter, input, output, outputNameSuffix, Character.getNumericValue(filterName.charAt(3)));
      }
    } else if (filterName.equals("invert")) {
      for (File img : images) {
        BufferedImage input = null;
        try {
          input = ImageIO.read(img);
        } catch (IOException e) {
          System.out.println("Cannot read file " + img.getName());
        }

        BufferedImage output = null;
        String outputNameSuffix = img.getName();
        invertFilter(filter, input, output, outputNameSuffix);
      }
    } else if (filterName.equals("smear")) {
      for (File img : images) {
        BufferedImage input = null;
        try {
          input = ImageIO.read(img);
        } catch (IOException e) {
          System.out.println("Cannot read file " + img.getName());
        }

        BufferedImage output = null;
        String outputNameSuffix = img.getName();
        smearFilter(filter, input, output, outputNameSuffix, 10);
      }
    }
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

  private void oilFilter(BufferedImageOp filter, BufferedImage input, BufferedImage output, String outNameSuff, int range) {
    // init. output image
    output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

    // Apply the Oil1 filter and save image
    filter = new OilFilter();
    ((OilFilter)filter).setRange(range);
    filter.filter(input,output);

    // indicate that file has been processed
    System.out.print("p");

    saveImage(output, "./oil" + range + "_" + outNameSuff + ".jpg");
  }

  private void invertFilter(BufferedImageOp filter, BufferedImage input, BufferedImage output, String outNameSuff) {
    // init. output image
    output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

    // Apply the Invert filter
    filter = new InvertFilter();
    filter.filter(input,output);

    // indicate that file has been processed
    System.out.print("p");

    saveImage(output, "./invert_" + outNameSuff + ".jpg");
  }

  private void smearFilter(BufferedImageOp filter, BufferedImage input, BufferedImage output, String outNameSuff, int shape) {
    // init. output image
    output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

    // Apply the Smear filter
    filter = new SmearFilter();
    ((SmearFilter)filter).setShape(shape);
    filter.filter(input,output);

    // indicate that file has been processed
    System.out.print("p");

    saveImage(output, "./smear_" + outNameSuff + ".jpg");
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