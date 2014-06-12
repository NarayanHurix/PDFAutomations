package com.hurix.chain.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.Filters.FilterReader;
import pdftron.Filters.MappedFile;
import pdftron.PDF.CharData;
import pdftron.PDF.CharIterator;
import pdftron.PDF.ElementBuilder;
import pdftron.PDF.ElementReader;
import pdftron.PDF.ElementWriter;
import pdftron.PDF.Element;
import pdftron.PDF.Font;
import pdftron.PDF.GState;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;
import pdftron.SDF.SDFDoc;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.util.FileUtility;

public class PDFWordSeperator  implements Chain {

	private Chain nextInChain;
	private List<String> env;
	
	@Override
	public void setNextInChain(Chain nextInChain) {
		this.nextInChain = nextInChain;
	}

	@Override
	public void setEnv(List<String> options) {
		this.env = options;
	}
	
	@Override
	public void process(ChainRequest request) 
	{
		try {
			PDFDoc doc = (PDFDoc)request.getObj();
			int num_pages = doc.getPageCount();
			ElementWriter writer = new ElementWriter();
			ElementReader reader = new ElementReader();
			int pgno = 0 ;
			for (int i = 1; i <= num_pages; ++i)
			{
				PageIterator itr = doc.getPageIterator(i);
				Page page = (Page)(itr.next());
				reader.begin(page);
            pgno = page.getIndex();
            System.out.println("Processing Page Num: "+pgno);
            Page new_page = doc.pageCreate();
            PageIterator next_page = itr;
            doc.pageInsert(next_page, new_page );       
            writer.begin(new_page);         
            processElements(reader, writer, pgno, doc);     
            writer.end();
            reader.end();
            new_page.setMediaBox(page.getCropBox());
            new_page.setRotation(page.getRotation());
            doc.pageRemove(doc.getPageIterator(i));
			}
	      String modPDFPath = request.getWorkDirectory()+
                             FileUtility.getFileName(request.getPdfFilePath())+
                             ".pdf";
	      System.out.println("New File Name: "+ modPDFPath);
	      doc.save(modPDFPath, SDFDoc.e_remove_unused , null);

	      // Generating the New PDF 
	      PDFDoc newdDoc = generateNewPDFDoc(modPDFPath);
	      
	      // Modifying the Request Object
	      request.setPdfFilePath(modPDFPath);
	      request.setObj(newdDoc);
	      doc = null;
	      
		} catch(Exception e) {
		   System.out.println("Unable to Process PDF for Word Seperation");
			e.printStackTrace();
		}
		
      if (this.nextInChain != null) {
         this.nextInChain.process(request);
      }
	}
	
	private PDFDoc generateNewPDFDoc(String path) throws PDFNetException
	{
      // Reloading the PDFDoc into Memory. The intent is to load the low res
      // for Viewing and VDP Selection
	   MappedFile file = new MappedFile(path);
      long file_sz = file.fileSize();
        
      FilterReader file_reader = new FilterReader(file);

      byte[] buf = new byte[(int)file_sz];
      long byteRead = file_reader.read(buf);
      file_reader.destroy();
      System.out.println("PDF Filepath: "+path+" Bfor File Size: "+file_sz+
                         " After PDF Size: "+buf.length+" BytesRead: "+
                         byteRead);
      PDFDoc doc = new PDFDoc(buf);
      doc.initSecurityHandler();
      
      return doc;	   
	}

	/**
	 * This method is invoked to process the PDF for Text Element. Check if the 
	 * text has multiple words and initiate a split to create pdf text element for
	 * every word.
	 *  
	 * @param reader The ElementReader Object to read PDF Element
	 * @param writer The ElementWriter to create pdf element for every word
	 * @param pageno The page number of the PDF being processed
	 * @param doc The PDF Doc Object
	 * @throws PDFNetException The Exception thrown by PDF Tron Library
	 */
   private void processElements(ElementReader reader, ElementWriter writer, 
                                int pageno, PDFDoc doc) throws PDFNetException 
   {   
      Element element = null; 
      while ((element = reader.next()) != null) 
      {
         switch (element.getType()) 
         {
            case Element.e_image: 
            case Element.e_inline_image: 
            case Element.e_path  :
               break; 
            case Element.e_text : 
               // Checking for space to split element text to create pdf element 
               // for every word
               if (checkforMultipleWordsinElement(element)) {
                  createNewElements(writer, element);
                  continue ;
               }    
               if (!element.hasTextMatrix()) {                                      
                     setCalculatedTextMatrix(writer, element);
                     continue;
               }
               break;
            case Element.e_form:   // Process form XObjects
               reader.formBegin(); 
               processElements(reader, writer, pageno, doc);
               reader.end(); 
               break; 
         } // End of switch (element.getType())
         writer.writeElement(element);
      } // End of while ((element = reader.next()) != null)
   }
	
   /**
    * This method checks for multiple words in text element
    * @param element
    * @return
    * @throws PDFNetException
    */
   private boolean checkforMultipleWordsinElement(Element element) 
                   throws PDFNetException 
   {
      String elemtext = element.getTextString();
      String whitespace = "[ \t\n\r\f]";
      Pattern pattern = Pattern.compile(whitespace);
      Matcher matcher = pattern.matcher(elemtext);
      while (matcher.find()) { 
         return true;
      }
      return false;
   } 
   
   /**
    * This method separates the white spaces in the text element and creates  
    * a new pdf text element for every word  
    * @param writer PDF ElementWriter to create new Element
    * @param element The existing pdf text element 
    * @throws PDFNetException
    */
   private void createNewElements(ElementWriter writer, Element element) 
                throws PDFNetException 
   {
      String elemtext, wordtext = "", spacestring = "";
      boolean firstcharspace = false, isUnicode = false;
      double x, y, x1, y1, startx = 0, starty = 0;
      long char_code, prev_char = ' ';
      GState gs = element.getGState();
      elemtext = element.getTextString();
      if (elemtext.indexOf(' ') == 0)
         firstcharspace = true ;
     
      // Text and Element Matrices
      Matrix2D text_mtx = element.getTextMatrix();
      Matrix2D ctm = element.getCTM();
      Matrix2D mtx = ctm.multiply(text_mtx);   
      for (CharIterator itr = element.getCharIterator(); itr.hasNext();) 
      {
         CharData data=(CharData)(itr.next());
         char_code = data.getCharCode();
        
         // character positioning information
         x = data.getGlyphX();       
         y = data.getGlyphY();
        
         // Multiplying with the Matrix to get the Character Position wrt the page
         java.awt.geom.Point2D.Double t = mtx.multPoint(x, y);
         x1 = t.x;
         y1 = t.y;
         if (Character.isWhitespace((char)char_code))
         {
            // Once the Space is encountered and wordtext is not empty, then a 
            // pdf element is created for the word in the following code
            if (wordtext.length() > 0 )
            {  
               writeElement(writer, element, wordtext, startx, starty, isUnicode);
               wordtext = "";
               startx = x1;
               starty = y1;
               isUnicode = false;
            }
            spacestring = spacestring + String.valueOf((char)char_code);
            if (firstcharspace)
            {
               startx = x1;
               starty = y1;
               firstcharspace = false;
            }
            prev_char = char_code;
         }
         else 
         {
            // Here the whitespaces between text is written as a new pdf element
            if (spacestring.length() > 0)
            {
               writeElement(writer, element, spacestring, startx, starty, false);
               spacestring = "";
            }
            // Here the wordtext is appended with the new Char found in the 
            // original element
            if (char_code < 128) {             
               wordtext = wordtext + String.valueOf((char)char_code);
            } else { 
               isUnicode = true ;
               Font ft = gs.getFont();
               wordtext = wordtext + new String(ft.mapToUnicode(char_code));
            }
            if (Character.isWhitespace((char)prev_char)) {
               startx = x1 ;
               starty = y1 ;
            }
            prev_char = char_code ;                           
         }
      }

      if (wordtext.length() > 0 )
         writeElement(writer, element, wordtext, startx, starty, isUnicode);
      if (spacestring.length() > 0)
         writeElement(writer, element, spacestring, startx, starty, isUnicode);
   }

   /**
    * This method is called to create a new PDF Text Element for the text being 
    * passed to this function.
    * 
    * @param writer PDF ElementWriter to create new Element 
    * @param element The Original PDF Element
    * @param text The word text for the new PDF Text Element
    * @param startx The start x position for the word wrt page
    * @param starty The start y position for the word wrt page
    * @param unicode The boolean to indicate if the text is unicode text
    * @throws PDFNetException The exception thrown by PDF Tron Lib 
    */
   
   private void writeElement(ElementWriter writer, Element element, String text, 
                             double startx, double starty, boolean unicode ) 
                throws PDFNetException
   {
      Element elem = null;
      ElementBuilder eb = new ElementBuilder();
      
      // Setting the Graphic State to that of the original pdf element
      eb.reset(element.getGState());
      // Creating normal or unicode text element
      if (unicode) {            
           elem = eb.createUnicodeTextRun(new String(text.toCharArray()));         
      } else {
          elem = eb.createTextRun(text);
      }
      
      // Setting the Text Matrix
      Matrix2D textmtx = element.getTextMatrix();
      textmtx.setH(startx);
      textmtx.setV(starty);
      elem.setTextMatrix(textmtx);
      
      elem.setPosAdjustment(element.getPosAdjustment());
      writer.writeElement(elem);                                            
   }
   
   /**
    * This method is called if the PDF Text ELement has no preset Text Matrices  
    * @param writer The PDF ElementWriter Object to set the new Element
    * @param element The PDF Text Element to set the Text Matrix
    * @throws PDFNetException
    */
   
   private void setCalculatedTextMatrix( ElementWriter writer, Element element) 
                throws PDFNetException 
   {
      Matrix2D text_mtx = element.getTextMatrix();
      Matrix2D ctm = element.getCTM();
      Matrix2D mtx = ctm.multiply(text_mtx);
    
      // Looping through every character in the PDF Element and setting the 
      // Text Matrix
      double x, y;
      CharIterator itr = element.getCharIterator(); 
      if (itr.hasNext()) 
      {       
          CharData data=(CharData)(itr.next());
          x = data.getGlyphX();       // character positioning information
          y = data.getGlyphY();
          java.awt.geom.Point2D.Double t=mtx.multPoint(x, y);
          mtx.setH(t.x);
          mtx.setV(t.y);
          ElementBuilder eb = new ElementBuilder();
          eb.reset(element.getGState());
          Element elem = eb.createTextRun(element.getTextString());
          elem.setTextMatrix(mtx);         
          writer.writeElement(elem);
      }
   }
}
