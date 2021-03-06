package com.hurix.imageutility;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

public class ImageUtils 
{

	/**
	 * Loads Image from the given file name and returns the BufferedImage
	 * @param filename the image file to be loaded
	 * @return the Java BufferedImage
	 */
	public static BufferedImage loadImage(String filename) 
	{
		return loadImage(new File(filename));
	}

	/**
	 * Loads Image from the given file and returns the BufferedImage
	 * @param imgFile the image file to be loaded
	 * @return the Java BufferedImage
	 */
	public static BufferedImage loadImage(File imgFile) 
	{
		BufferedImage bimg = null;
		try {
			System.out.println("Image File Size: " + imgFile.length());
			bimg = ImageIO.read(imgFile);
		} catch (Exception e) {
			System.out.println("Exception at LoadImage");
			e.printStackTrace();
		}
		return bimg;
	}
	
	/**
	 * This method Saves the BufferedImage to the file given the filename and the
	 * format 
	 * @param bimg the image to be saved
	 * @param filename the file name of the saved image
	 * @param outformat the output format
	 */
	public static void saveImageToFile(BufferedImage bimg, String filename,
			String outformat) {
		File f = new File(filename);
		if (outformat.equalsIgnoreCase("JPEG")
				|| outformat.equalsIgnoreCase("JPG"))
			saveJPEG(bimg, f);
		else
			saveImageToFile(bimg, f, outformat);
	}
	
	/**
	 * This method Saves the BufferedImage to the file and to the given format 
	 * 
	 * @param bimg the image to be saved
	 * @param f the file in which to save image
	 * @param outformat the output format
	 */
	public static void saveImageToFile(BufferedImage bimg, File f,
			String outformat) {
		if (outformat.equalsIgnoreCase("JPEG")
				|| outformat.equalsIgnoreCase("JPG"))
			saveJPEG(bimg, f);
		try {
			ImageIO.write(bimg, outformat, f);
		} catch (Exception e) {
			System.out.println("SaveImage");
			e.printStackTrace();
		}
	}

	/**
	 * This method saves the image to the file in jpeg format 
	 * @param bimg
	 * @param file
	 */
	
	public static void saveJPEG(BufferedImage bimg, File file) 
	{
		// For JPEG output files, this code is required to take the color from  
		// the nearest pixel 
//		AffineTransform s = new AffineTransform();
//		AffineTransformOp sop = new AffineTransformOp(s,
//									AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//		BufferedImage jpgimage = new BufferedImage(bimg.getWidth(),
//									 bimg.getHeight(), BufferedImage.TYPE_INT_RGB);
//		sop.filter(bimg, jpgimage);
		
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter writer = iter.next();

		// instantiate an ImageWriteParam object with default compression
		// options
		ImageWriteParam iwp = writer.getDefaultWriteParam();

		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		
		// An integer between 0 and 1
		// 1 specifies minimum compression and maximum quality
		iwp.setCompressionQuality(1); 

		try {
			FileImageOutputStream output = new FileImageOutputStream(file);
			writer.setOutput(output);
			IIOImage image = new IIOImage(bimg, null, null);
			writer.write(null, image, iwp);
			output.close();
		} catch (Exception e) {
			System.out.println("SaveJPEG");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method does jpeg compression for non transparent image. No compression
	 * is done for transparent image 
	 * @param image the image to be compressed
	 * @param quality the compression ratio which is from 0.1 to 0.99
	 * @return the compressed image
	 * @throws Exception
	 */
	public static BufferedImage compressImage(BufferedImage image, float quality)
				  throws Exception 
    {
		if (hasAlpha(image)) return image;
		if (quality < 0) quality = 0.5f;
		// Get a ImageWriter for jpeg format.
		ImageWriter writer = getJPEGImageWriter();
		// Create the ImageWriteParam to compress the image.
		ImageWriteParam param = createImageWriteParam(writer, quality);
		// The output will be a ByteArrayOutputStream (in memory)
		ByteArrayOutputStream bos = new ByteArrayOutputStream(32768);
		ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(image, null, null), param);
		ios.flush(); // otherwise the buffer size will be zero!
		// From the ByteArrayOutputStream create a RenderedImage.
		ByteArrayInputStream in = new ByteArrayInputStream(bos.toByteArray());
		BufferedImage bimg = ImageIO.read(in);
		int size = bos.toByteArray().length;
		System.out.println("Compressed Image Size: " + size + " at quality: "+quality);
		return bimg;
	}

	/**
	 * This method returns true if the specified image has transparent pixels
	 * @param image the image object to check for transparency 
	 * @return true for transparent image else false
	 */
	public static boolean hasAlpha(Image image) 
	{
		// Get the image's color model
		ColorModel cm = getColorModel(image);
		// System.out.println("Color Model Class: "+(cm.getClass().getName())+" Data: "+cm);
		return cm.hasAlpha();
	}
	
	/**
	 * This method retrieves the ColorModel of the supplied image
	 * @param image the AWT Image object 
	 * @return the color model of the supplied image
	 */
	public static ColorModel getColorModel(Image image) 
	{
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel();
		}

		// Use a pixel grabber to retrieve the image's color model, grabbing a
		// single
		// pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm;
	}

	/**
	 * This method returns JPEG Image Writer
	 * @return Returns Image Writer in jpeg format
	 */
	public static ImageWriter getJPEGImageWriter() 
	{
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
		if (!writers.hasNext())
			throw new IllegalStateException("No writers found");
		ImageWriter writer = (ImageWriter) writers.next();
		return writer;
	}

	/**
	 * Create the ImageWriteParam to compress the image.
	 * @param writer the ImageWriter Object to be set in the param
	 * @param quality the quality of image to be set in the param
	 * @return the ImageWriteParam object
	 */
	public static ImageWriteParam createImageWriteParam(ImageWriter writer,
			float quality) {
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality);
		return param;
	}

	public static Image loadImage(String filePath, int imageSize) {
		ImageIcon imgIcon = new ImageIcon(filePath);
		if (imgIcon != null) 
			return getScaledImage(imgIcon.getImage(), imageSize);
		return null;
	}

	public static Image getScaledImage(Image image, int imageSize) {
		if (imageSize == -1 || image.getWidth(null) <= imageSize)
			return image;
		// Reseampling the Image to create Thumbview. The getScaledInstance
		// method maintains the Aspect Ratio.
		image = image.getScaledInstance(imageSize, -1, Image.SCALE_DEFAULT);
		return image;
	}

	
	
}
