package com.hurix.chain.impl;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.PDF.CharIterator;
import pdftron.PDF.Element;
import pdftron.PDF.ElementReader;
import pdftron.PDF.Font;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;
import pdftron.PDF.Rect;
import pdftron.PDF.TextExtractor;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.Block;
import com.hurix.model.Book;
import com.hurix.model.Constants.paraType;
import com.hurix.model.Line;
import com.hurix.model.ParaBlock;

public class PDFBlockDetector implements Chain 
{
	private static DecimalFormat df = new DecimalFormat("###.#");
	static Map<String, String> glyphListMap = new HashMap<String, String>();
	
	static {
		try {
			Scanner scan = new Scanner(new File("/Users/narayan/Documents/Development/PDFSamples/glyphlist.txt"));
			while (scan.hasNextLine()) {
				String charMap = scan.nextLine();
				String[] charVsCode = charMap.split(";");
				glyphListMap.put(charVsCode[0], charVsCode[1]);
			}
			scan.close();
		} catch (Exception ex) {
			
		}
	}
	
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
      List<ParaBlock> pageParaBlocks = null;
      
      try {
    	  Book book = request.getBook();
    	  
         PDFDoc doc = (PDFDoc)request.getObj();
         int num_pages = doc.getPageCount();
        
         int pgno = 0 ;
         for (int i = 1; i <= num_pages; ++i)
         {
        	 ElementReader reader = new ElementReader();
        	 com.hurix.model.Page pg = new com.hurix.model.Page();
        	 book.getPages().add(pg);
        	 
        	 Map<Rectangle2D.Double,Line> positionVsLine = new HashMap<Rectangle2D.Double,Line>();
            PageIterator itr = doc.getPageIterator(i);
            Page page = (Page)(itr.next());
            reader.begin(page);
            pgno = page.getIndex();
            System.out.println("Processing Page Num: "+pgno);
            pageParaBlocks = createPageParaBBox(page,positionVsLine);
            processElements(doc, page, reader, pageParaBlocks,positionVsLine,pg);   
            reader.destroy();
         }
      } catch(Exception e) {
         System.out.println("Unable to Process PDF for Word Seperation");
         e.printStackTrace();
      }
      
      if (this.nextInChain != null) {
         this.nextInChain.process(request);
      }
   }
   
   /**
    * This method creates the Para Block BBox for the PDF Page
    * @param page The PDF Page to be processed
    * @return The List of ParaBlock for the Page
    * @throws PDFNetException
    */
   
   private List<ParaBlock> 
           createPageParaBBox(Page page,Map<Rectangle2D.Double,Line> positionVsLine) throws PDFNetException
   {
	   Matrix2D matrix = page.getDefaultMatrix();
	   
      ParaBlock paraBlock = null;
      GeneralPath paraBBox = null;
      List<ParaBlock> pageParaBlocks = null;
      Map<Integer, ParaBlock> paraBlockMap = null;
      Map<Integer, GeneralPath> paraBBoxMap = null;
      
      pageParaBlocks = new ArrayList<ParaBlock>();
      paraBlockMap = new Hashtable<Integer, ParaBlock>();
      paraBBoxMap = new Hashtable<Integer, GeneralPath>();
      
      // Text Extractor to extract Para BBox Blocks 
      TextExtractor txt = new TextExtractor();
      txt.begin(page);  // Read the page.
      TextExtractor.Line line;
      
      // Building the Para Blocks and the corresponding BBox of the Para
      for ( line = txt.getFirstLine(); line.isValid(); line=line.getNextLine()){  
    	
    	  //get crop box adjusted...
    	  java.awt.geom.Point2D.Double lxy = matrix.multPoint(line.getBBox().getX1(), line.getBBox().getY1());
			
         int paraId = line.getParagraphID();
         paraBBox = paraBBoxMap.get(paraId);
         paraBlock = paraBlockMap.get(paraId);
         if (paraBBox == null) {
            paraBBox = new GeneralPath();
            paraBlock = new ParaBlock(paraId);
            paraBlock.setType(paraType.normal);
            pageParaBlocks.add(paraBlock);
            paraBBoxMap.put(paraId, paraBBox);
            paraBlockMap.put(paraId, paraBlock);
         }
         Rectangle2D.Double lineBox = new Rectangle2D.Double(roundOff(lxy.getX()),roundOff(lxy.getY()),roundOff(line.getBBox().getWidth()),roundOff(line.getBBox().getHeight()));
         Line ln = new Line(lineBox);
         
         paraBBox.append(lineBox, true);
         paraBlock.addLine(ln);
         positionVsLine.put(lineBox, ln);
      }
      
      // Setting the BBox of the Para Blocks based on the Bounds set in the 
      // GeneralPath of paraBBoxMap variable
      for (ParaBlock temp : pageParaBlocks) {
         paraBBox = (GeneralPath) paraBBoxMap.get(temp.paraId);
         temp.setBbox(new Rectangle2D.Double(paraBBox.getBounds2D().getX(),paraBBox.getBounds2D().getY(),paraBBox.getBounds2D().getWidth(),paraBBox.getBounds2D().getHeight()));
      }
      
      txt.destroy();
      return pageParaBlocks;
   }

   private static double roundOff(double input){
		return Double.parseDouble(df.format(input));
	}
   
   private void processElements(PDFDoc doc, Page page, ElementReader reader,  
                                List<ParaBlock> pageParaBlocks,Map<Rectangle2D.Double,Line> positionVsLine,com.hurix.model.Page pg) throws PDFNetException 
   {
      Element element = null; 
     
      while ((element = reader.next()) != null) 
      {

    	  Block block = null;
          switch (element.getType()) 
          {  
              // Handle Text Element
              case Element.e_text:
            	  Rect bbox = element.getBBox();
  					if (bbox == null) continue;
  					
            	  block = processText(page, element, pageParaBlocks,positionVsLine);
            	  break;
            	  // Handle Image Elements
              case Element.e_image:                  
              case Element.e_inline_image: 
            	  //block = processImage(element);
            	  break;
              // Handle Graphic Paths or Shapes
              case Element.e_path  :
            	 // block = processPath(reader, element);
            	  break; 
              // Process Form XObject Element which internally comprise of Image, 
              // Text or Path Elements  
              case Element.e_form:
            	  reader.formBegin(); 
            	  processElements(doc, page, reader, pageParaBlocks,positionVsLine,pg);
            	  reader.end();
            	  break; 
         } 
          if(block != null){
        	  
        	  if(!pg.getBlocks().contains(block)){
        		 
        		  pg.getBlocks().add(block);
        	  }
        	  
          }
          
          
      } 
      
   }

   /**
    * 
    * @param page_reader
    * @return
    * @throws PDFNetException
    */
   
   private Block processText(Page page,  Element element, 
   								  List<ParaBlock> pageParaBlocks,Map<Rectangle2D.Double,Line> positionVsLine) throws PDFNetException {
     
	   Matrix2D matrix = page.getDefaultMatrix();
	   Rect bbox = element.getBBox();
	   double cropBoxX1 = roundOff(page.getCropBox().getX1());
		double cropBoxY1 = roundOff(page.getCropBox().getY1());
		Line lineRef = null;
		Rectangle2D.Double runRectRef = null;
		
		java.awt.geom.Point2D.Double wxy = matrix.multPoint(bbox.getX1(), bbox.getY1());
		java.awt.geom.Point2D.Double wxy2 = matrix.multPoint(bbox.getX2(), bbox.getY2());

		
		Matrix2D ctm = element.getCTM();
		Matrix2D text_mtx = element.getTextMatrix();
		Matrix2D mtx = ctm.multiply(text_mtx);
		Font font = element.getGState().getFont();
		double x, y;					
		CharIterator itr = element.getCharIterator();
		
		double scale_factor = Math.sqrt(mtx.getB()*mtx.getB() + mtx.getD()*mtx.getD());
	
		double page_font_sz = element.getGState().getFontSize() * scale_factor;
		double horiz_scale = element.getGState().getHorizontalScale() / 100.0;
		
		com.hurix.model.CharData cd = null;
		boolean firstRun = true;
		while ( itr.hasNext())	{
			pdftron.PDF.CharData chardata =(pdftron.PDF.CharData)itr.next(); 
		
			x = chardata.getGlyphX(); 
			y = chardata.getGlyphY();
			
			java.awt.geom.Point2D.Double t=mtx.multPoint(x, y);
			
			double posX =  roundOff(t.x-cropBoxX1);
			double posY =  roundOff((t.y-cropBoxY1)  + ((font.getDescent() * page_font_sz)/1000));
			double height = wxy2.y - wxy.y + ((font.getDescent() * page_font_sz)/1000);
		
			if(firstRun){
				Rectangle2D.Double runRect = new Rectangle2D.Double(posX, posY, bbox.getWidth(), height);
				runRectRef = runRect;
				
				for (Map.Entry<Rectangle2D.Double,Line> entry : positionVsLine.entrySet()) {
					if(findSelection(entry.getKey(), runRect)){
						lineRef = positionVsLine.get(entry.getKey());
							break;
					}
				}
				
				firstRun = false;
			}
			
			//if still null.... meaning either this is hidden text or out of crop box.. ignore
			if(lineRef == null){
				 return null;
			}

			
			if(cd == null){
				cd = new com.hurix.model.CharData(runRectRef);
				lineRef.charDataList.add(cd);
			}
			
			if(" ".equalsIgnoreCase((char)chardata.getCharCode() + "")){
				cd = null;
				continue;
			}
			
			if(cd.text == null){
				cd.text = (char)chardata.getCharCode()+"";
			}else{
				cd.text = cd.text + (char)chardata.getCharCode();
			}

		}
		
		for (ParaBlock paraBlock : pageParaBlocks) {
			if(paraBlock.getLines().contains(lineRef)){
				return paraBlock;
			}
		}
		
             
      return null;
   }
   

   private boolean findSelection(Rectangle2D.Double contBBox, Rectangle2D.Double elemBBox) 
   {
	   if(contBBox.intersects(elemBBox)){
			
			double lineY = contBBox.y;
			double lineY2 = contBBox.y - contBBox.height;
			double lineX = contBBox.x;
			double lineX2 = contBBox.x + contBBox.width;
			

			
			double eleY = elemBBox.y;

			double eleX = elemBBox.x;


			if(
					eleY <= lineY && eleY >= lineY2 &&
					eleX <= lineX2 && eleX >= lineX					
					){
				return true;
			}
			
			
			
		}
		return false;
   }
}
