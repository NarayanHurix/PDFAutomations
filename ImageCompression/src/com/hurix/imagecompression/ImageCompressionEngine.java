package com.hurix.imagecompression;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import com.hurix.imageutility.ImageUtils;
import com.hurix.utils.FileFilterFactory;
import com.hurix.utils.UIUtils;

public class ImageCompressionEngine {
	
	public static void main(String[] args) 
	{
		String workDirectory = "/Users/narayan/Development/ImageSamples/";
		Vector<File> files = UIUtils.getSelectedFiles(new Frame(), 
		  	 					     FileFilterFactory.IMAGE_FILE_FILTER, false);
		if (files == null) { System.exit(0); return; }
		float compressRatio = 0.2f;
		while (compressRatio < 1.0f) 
		{
			try {
				File file = files.elementAt(0);
				BufferedImage rawImg = ImageUtils.loadImage(file);
				BufferedImage img = ImageUtils.compressImage(rawImg, 
															 compressRatio);
				File newFile = new File(workDirectory+file.getName()+
									    "_"+compressRatio+".jpeg");
				compressRatio += 0.1;
				ImageUtils.saveImageToFile(img, newFile, "jpeg");
			} catch(Exception ex) {
				System.out.println("Error: "+ex.getMessage());
				ex.printStackTrace();
				System.exit(0);
			}
		}
		System.exit(0);
	}
}
