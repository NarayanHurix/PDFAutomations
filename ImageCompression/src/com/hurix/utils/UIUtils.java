package com.hurix.utils;

import java.awt.Component;
import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;

public class UIUtils 
{

	public static Vector<File> getSelectedFiles(Component comp,
							   CommonFileFilter fileFilter, boolean allowMultiSel) 
    {
		// Show File Chooser to select appropriate font
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(fileFilter);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(allowMultiSel);

		// Setting the Preview for Image Filter
		ImagePreview imgPreview = new ImagePreview();
		fc.setAccessory(imgPreview);
		fc.addPropertyChangeListener(imgPreview);

		int returnVal = fc.showOpenDialog(comp);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;

		Vector<File> files = new Vector<File>();
		if (!allowMultiSel) {
			java.io.File selFile = fc.getSelectedFile();
			files.addElement(selFile);
			fc.setSelectedFile(null);
			return files;
		}
		File[] selFiles = fc.getSelectedFiles();
		for (int i = 0; i < selFiles.length; i++)
			files.addElement(selFiles[i]);
		fc.setSelectedFile(null);
		return files;
	}

	
	
}
