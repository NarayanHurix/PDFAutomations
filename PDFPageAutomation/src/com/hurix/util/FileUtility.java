package com.hurix.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtility 
{
   /**
    * This function takes the file Name with extension and returns file name
    * without extension
    * 
    * @param file file name or path with extension
    * @return file name without extension or null 
    */
   public static String getFileName(String file) 
   {
      String fileName = null;
      
      if (file.contains("/")) {
         int index = file.lastIndexOf('/');
         if (index > 0 &&  index < file.length() - 1) 
            fileName = file.substring(index+1, file.length());
      }
      
      int i = fileName.lastIndexOf('.');
      
      if (i > 0 &&  i < fileName.length() - 1) {
         fileName = fileName.substring(0, i);
      }
      return fileName;
   }
   
	public static List<String> listOnlyFileNamesRecursive(String filePath)
			throws IOException {
		return listOnlyFileNamesRecursive(filePath, "");
	}
	
	public static List<String> listOnlyFileNamesRecursive(String filePath,
			String excludeFolder) throws IOException {
		List<File> sourceFiles = new ArrayList<File>();
		List<String> paths = new ArrayList<String>();

		sourceFiles = listFiles(filePath, sourceFiles, excludeFolder);

		for (File file : sourceFiles) {
			paths.add(file.getAbsolutePath());
		}
		return paths;
	}
	
	public static List<File> listFiles(String filePath, List<File> files,
			String excludeFolder) {
		File file = new File(filePath);
		String[] folders = null;
		if (excludeFolder != null && !"".equals(excludeFolder)) {
			folders = excludeFolder.split(",");
		}
		boolean skip = false;
		if (file.isDirectory()) {
			for (File file1 : file.listFiles()) {
				if (file1.isDirectory()) {
					// check if we need to skip this folder.

					if (null != folders) {

						for (String folder : folders) {
							if (file1.getAbsolutePath().endsWith("/" + folder)) {
								skip = true;
								break;
							} else {
								skip = false;
							}
						}
					}
					if (skip) {
						continue;
					}
					listFiles(file1.getAbsolutePath(), files, excludeFolder);
				} else {
					files.add(file1);
				}
			}
		} else {
			files.add(file);
		}
		return files;
	}
}
