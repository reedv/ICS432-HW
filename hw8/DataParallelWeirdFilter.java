

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;


public  class DataParallelWeirdFilter implements BufferedImageOp {
    
	private static final int invalidRGB = -10000000;
	private int filterers;
	
	public DataParallelWeirdFilter(int numThreads) {
		filterers = numThreads;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param image
	 * @return array of neighboring int pixels in the order:
	 * {NW, N, NE, W, E, SW, S, SE}. OutOfBounds pixels are set to null.
	 */
    public static int[] getNeighbors(int x, int y, BufferedImage image)
    {
    	// {NW, N, NE, W, E, SW, S, SE}
        int[] n = new int[8];
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // pixel neighbor coordinates
        int west = x-1, east = x+1, north = y+1, south = y-1;
        
        // pixel lays on north edge
        if (north >= height) {
            n[0] =  invalidRGB;
            n[1] =  invalidRGB;
            n[2] =  invalidRGB;
        }
        // pixel lays on east edge
        if (east >= width) { 
        	n[2] =  invalidRGB;
        	n[4] =  invalidRGB;
        	n[7] =  invalidRGB;
    	}
        // pixel lays on south edge
        if (south < 0) { 
        	n[5] =  invalidRGB;
        	n[6] =  invalidRGB;
        	n[7] =  invalidRGB;
    	}
        // pixel lays on west edge
        if (west < 0) {
	        n[0] =  invalidRGB;
	        n[3] =  invalidRGB;
	        n[5] =  invalidRGB;
        }
        
        // assign remaining neighbor pixels
        if(n[0] != invalidRGB) n[0] = image.getRGB(west, north);
        
        if(n[1] != invalidRGB) n[1] = image.getRGB(x, north);     
        
        if(n[2] != invalidRGB) n[2] = image.getRGB(east, north);  
        
        if(n[3] != invalidRGB) n[3] = image.getRGB(west, y);  
        
        if(n[4] != invalidRGB) n[4] = image.getRGB(east, y);  
        
        if(n[5] != invalidRGB) n[5] = image.getRGB(west, south); 
        
        if(n[6] != invalidRGB) n[6] = image.getRGB(x, south);  
        
        if(n[7] != invalidRGB) n[7] = image.getRGB(east, south);  
        
        return n;
    }
    
    public synchronized static int weirdFilter(int x, int y, BufferedImage image)
    { 
        //get neighbor pixels
        int[] neighbors = getNeighbors(x, y, image);
        int rgb = image.getRGB(x, y);
        
        byte[] newpixel_bytes = RGB.intToBytes(rgb); 
        newpixel_bytes[0] = 0;  // newpixel.red = 0
        newpixel_bytes[1] = 0;  // newpixel.green = 0
        newpixel_bytes[2] = 0;  // newpixel.blue = 0
        int notExistingPixels = 0;

        for(int neighbor: neighbors) {
            if(neighbor == invalidRGB) {
                // pixel is on edge of photo
                notExistingPixels++;
            }
            else {
            	/*
            	Alter input pixel based on all neighbors:
            	newpixel.red   += max(neighbor.red, 40) + 10*cos(neighbor.red);
  				newpixel.green += min(neighbor.green, 100);
  				newpixel.blue  += min(exp(neighbor.blue), 40);
            	 * */
                byte[] neighbor_bytes = RGB.intToBytes(neighbor);
                newpixel_bytes[0]  += max(neighbor_bytes[0], 40) + 10*cos(neighbor_bytes[0]);
                newpixel_bytes[1]  += min(neighbor_bytes[1], 100);
                newpixel_bytes[2]  += min(exp(neighbor_bytes[2]), 40);
            }
        }
        
        int numberOfNeighbors = neighbors.length - notExistingPixels;
        newpixel_bytes[0] /= numberOfNeighbors;
        newpixel_bytes[1] /= numberOfNeighbors;
        newpixel_bytes[2] /= numberOfNeighbors;
        
        return RGB.bytesToInt(newpixel_bytes);
    }
    
    /*
     * (non-Javadoc) 
     * @see java.awt.image.BufferedImageOp#filter(java.awt.image.BufferedImage, java.awt.image.BufferedImage)
     */
    @Override
    public BufferedImage filter(BufferedImage inputImage, BufferedImage outputImage) {
    	ExecutorService pool = Executors.newFixedThreadPool(filterers); 
    	
    	// apply weird filter to all pixels from top-left to bottom-right
		for (int i = 0; i < inputImage.getWidth(); i++) {				
			pool.execute(new Filter_t(i, inputImage, outputImage));
		}
		pool.shutdown();
		
        return outputImage;
	}

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        throw new UnsupportedOperationException("Not supported"); 
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        throw new UnsupportedOperationException("Not supported"); 
    }

    @Override
    public RenderingHints getRenderingHints() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    
    /*********************
     * filter worker
     ********************/
    class Filter_t extends Thread {
    	private final int x; 
    	private final BufferedImage inImage;
    	private BufferedImage outImage;
    	
    	public Filter_t(int x, BufferedImage inImage, BufferedImage outImage) {
    		this.x = x;
    		this.inImage = inImage;
    		this.outImage = outImage;
    	}
	
    	public void run() {
			try {
				// note: _method_ manipulation will alter the objects, 
				// since the references point to the original objects. 
				// But since the references are copies, swaps will fail
				 for (int y = 0; y < inImage.getHeight(); y++) {
					outImage.setRGB(x, y, DataParallelWeirdFilter.weirdFilter(x, y, inImage));
				 }
				 System.out.print("*");
			} catch (Exception e) {
				System.err.println(e);
			}
    	}
     
    }
    
}
