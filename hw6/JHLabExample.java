import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import com.jhlabs.image.*;

public class JHLabExample {

	private static void saveImage(BufferedImage image, String filename){
		try {
			ImageIO.write(image, "jpg", new File(filename));
		} catch (IOException e) {
			System.out.println("Cannot write file "+filename);
			System.exit(1);
		}
	}

	public static void main(String args[]) {

		BufferedImage input=null;
		BufferedImage output;
		BufferedImageOp filter;

		try {
			input = ImageIO.read(new File("./image.jpg"));
		} catch (IOException e) {
			System.out.println("Cannot read file ./image.jpg");
			System.exit(1);
		}

		output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());  

		// Apply the Oil1 filter
		filter = new OilFilter();
		((OilFilter)filter).setRange(1);
		filter.filter(input,output);
		saveImage(output, "./oil1_image.jpg");

		// Apply the Oil6 filter
		filter = new OilFilter();
		((OilFilter)filter).setRange(5);
		filter.filter(input,output);
		saveImage(output, "./oil5_image.jpg");

		// Apply the Invert filter
		filter = new InvertFilter();
		filter.filter(input,output);
		saveImage(output, "./invert_image.jpg");

		// Apply the Smear filter
		filter = new SmearFilter();
		((SmearFilter)filter).setShape(0);
		filter.filter(input,output);
		saveImage(output, "./smear_image.jpg");

	}
}

