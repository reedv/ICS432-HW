
import java.awt.image.BufferedImage;


/**
 * Helper class pair type for holding images and their names to be written
 */
public class FileNamePair {
  public BufferedImage image;
  public String imageName;

  public FileNamePair(BufferedImage buffimg, String imgname) {
    this.image = buffimg;
    this.imageName = imgname;
  }
}