package com.hurix.utils;

import java.io.File;

public class FileFilterFactory 
{
   public static final CommonFileFilter PDF_FILE_FILTER;
   public static final CommonFileFilter FONT_FILE_FILTER;
   public static final CommonFileFilter IMAGE_FILE_FILTER;
   public static final CommonFileFilter ALL_FILE_FILTER;
   public static final CommonFileFilter CSV_FILE_FILTER;
   public static final CommonFileFilter ZIP_FILE_FILTER;
   public static final CommonFileFilter SER_FILE_FILTER;

   static
   {
      // PDF Filter 
      String fileType = "PDF";
      String[] pdfFilters = {"pdf"};
      String filterDesc = "PDF Files(*.pdf)";
      PDF_FILE_FILTER = new CommonFileFilter(fileType, pdfFilters, filterDesc);
      
      // Font Filter 
      fileType = "FONT";
      //String[] fontFilters = {"ttf", "otf"};
      String[] fontFilters = {"ttf"};
      //filterDesc = "Font Files(*.ttf, *.otf)";
      filterDesc = "True Type Fonts(*.ttf)";
      FONT_FILE_FILTER = new CommonFileFilter(fileType, fontFilters, filterDesc);
      
      // Image Filter
      fileType = "IMAGE";
      String[] imageFilters = {"gif", "jpg", "png", "tiff"};
      filterDesc = "Image Files(*.gif, *.jpeg, *.png, *.tiff)";
      IMAGE_FILE_FILTER = new CommonFileFilter(fileType, imageFilters, filterDesc);

      // All File Filter 
      fileType = "ANY";
      String[] allFilters = {"pdf", "zip", "ppt", "doc", "pps", "ai", "cdr", "psd", "indd", "pub", "xls"};
      filterDesc = "All File Types(*.*)";
      ALL_FILE_FILTER = new CommonFileFilter(fileType, allFilters, filterDesc);

      // All File Filter
      fileType = "CSV";
      String[] csvFilters = {"csv"};
      filterDesc = "CSV Files(*.csv)";
      CSV_FILE_FILTER = new CommonFileFilter(fileType, csvFilters, filterDesc);
      
      // All File Filter
      fileType = "ZIP";
      String[] zipFilters = {"zip"};
      filterDesc = "ZIP Files(*.zip)";
      ZIP_FILE_FILTER = new CommonFileFilter(fileType, zipFilters, filterDesc);
      
      // SER File Filter
      fileType = "SER";
      String[] serFilters = {"ser"};
      filterDesc = "Serialization File(*.ser)";
      SER_FILE_FILTER = new CommonFileFilter(fileType, serFilters, filterDesc);
      
   }
   // All File Filter ALL_FILE_FILTER CSV_FILE_FILTER 
   public static CommonFileFilter getFileFilter4FileType(String fileType)
   {
      if (PDF_FILE_FILTER.acceptFileType(fileType)) return PDF_FILE_FILTER;
      if (FONT_FILE_FILTER.acceptFileType(fileType)) return FONT_FILE_FILTER;
      if (IMAGE_FILE_FILTER.acceptFileType(fileType)) return IMAGE_FILE_FILTER;
      if (ALL_FILE_FILTER.acceptFileType(fileType)) return ALL_FILE_FILTER;
      if (CSV_FILE_FILTER.acceptFileType(fileType)) return CSV_FILE_FILTER;
      if (ZIP_FILE_FILTER.acceptFileType(fileType)) return ZIP_FILE_FILTER;
      if (SER_FILE_FILTER.acceptFileType(fileType)) return SER_FILE_FILTER;
      return null;
   }
   
   public static CommonFileFilter getFileFilter(File file)
   {
      return getFileFilter(CommonFileFilter.getExtension(file));
   }
   
   public static CommonFileFilter getFileFilter(String ext)
   {
      if (PDF_FILE_FILTER.accept(ext)) return PDF_FILE_FILTER;
      if (FONT_FILE_FILTER.accept(ext)) return FONT_FILE_FILTER;
      if (IMAGE_FILE_FILTER.accept(ext)) return IMAGE_FILE_FILTER;
      if (CSV_FILE_FILTER.accept(ext)) return CSV_FILE_FILTER;
      if (ZIP_FILE_FILTER.accept(ext)) return ZIP_FILE_FILTER;
      if (ALL_FILE_FILTER.accept(ext)) return ALL_FILE_FILTER;
      return null;
   }
}