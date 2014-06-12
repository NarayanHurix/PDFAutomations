package com.hurix.chain.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pdftron.PDF.Element;
import pdftron.PDF.ElementBuilder;
import pdftron.PDF.ElementReader;
import pdftron.PDF.ElementWriter;
import pdftron.PDF.GState;
import pdftron.PDF.Image;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFDraw;
import pdftron.PDF.Page;
import pdftron.PDF.Rect;
import pdftron.SDF.Obj;
import pdftron.SDF.ObjSet;
import pdftron.SDF.SDFDoc;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;

public class Optimizer implements Chain {
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
	public void process(ChainRequest request) {
		System.out.println("Optimization bean process");
		try{
			PDFDoc inPdf= (PDFDoc)request.getObj();
			String workDirectory = request.getWorkDirectory();
			 File folder = new File(workDirectory);
			 if(!folder.exists()){
				 folder.mkdirs();
			 }
			 int totalPages = inPdf.getPageCount();
			 
			 PDFDoc optDoc=new PDFDoc();
			
			for (int cnt = 1 ; cnt <=totalPages ;cnt++ ){
				
				PDFDoc singlePage=new PDFDoc();
				singlePage.insertPages(0, inPdf, cnt, cnt, PDFDoc.e_none, null);
				
				int pageNum = 1;
				String imageName = workDirectory + System.nanoTime() + ".tiff";
				
				// we need 2 copies of this doc...since they will be modified by image & text creator methods.... 
				// image pdf should not be flattened else we will loose sharpness...
				byte[] forImage=singlePage.save(SDFDoc.e_remove_unused, null);
				
				// inout for text needs to be flattened else we have problem with position.
				pdftron.PDF.Flattener fl = new pdftron.PDF.Flattener();
				//fl.Process(singlePage,pdftron.PDF.Flattener.e_very_strict);
				fl.destroy();
				
				byte[] forText=singlePage.save(SDFDoc.e_remove_unused, null);
				singlePage.close();
				
				Page pgWorking = null;
				
				PDFDoc inImagePdf = new PDFDoc(forImage);
				inImagePdf.initSecurityHandler();
				pgWorking = inImagePdf.getPage(pageNum);
				createImage(pgWorking,imageName);

				
				PDFDoc inTextPdf = new PDFDoc(forText);
				inTextPdf.initSecurityHandler();
				pgWorking = inTextPdf.getPage(pageNum);
				PDFDoc textDoc = createTextPdf(inTextPdf,pgWorking,imageName);
				
				(new File(imageName)).delete();
				
				Page optPage = (Page)(textDoc.getPage(pageNum));
				optDoc.pagePushBack(optPage);
				
				inImagePdf.close();
				inTextPdf.close();
				textDoc.close();
				
			}
			optDoc.save(workDirectory + "/ebook.pdf", SDFDoc.e_remove_unused, null);
			inPdf.close();
			
			request.setObj(optDoc);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}
	
	public PDFDoc createTextPdf(PDFDoc doc,Page pg,String imageName) throws Exception{
		
		ElementWriter  writer=new ElementWriter();
		writer.begin(pg);
		
		ElementReader reader=new ElementReader();
		reader.begin(pg);
		Element element = null;
		
		ElementBuilder eb=new ElementBuilder();	
		Image img = Image.create(doc.getSDFDoc(), imageName);
		if(pg.getRotation() == Page.e_90 || pg.getRotation() == Page.e_270){
			element = eb.createImage(img, 0, 0, pg.getCropBox().getHeight(), pg.getCropBox().getWidth());
		}else{
			element = eb.createImage(img, 0, 0, pg.getCropBox().getWidth(), pg.getCropBox().getHeight());
		}
		
		writer.writeElement(element);
		writer.flush();
		writer.end();
		
		writer.begin(pg,ElementWriter.e_overlay, false);
		while ((element = reader.next()) != null){
			if (element.getType() == Element.e_text ||
					element.getType() == Element.e_text_begin ||
					element.getType() == Element.e_text_end ||
					element.getType() == Element.e_text_new_line){	
				writer.writeElement(element);
			}
		}
		
		
		writer.flush();
		writer.end();
		reader.end();
		
		writer.destroy();
		reader.destroy();
		return doc;
	}
	
	
	public void createImage(Page pg,String imageName) throws Exception{
		int dpi = 300;
		PDFDraw draw=new PDFDraw();
			
		ElementWriter  writer=new ElementWriter();
		writer.begin(pg,ElementWriter.e_replacement, false);
		
		ElementReader reader=new ElementReader();
		reader.begin(pg);
		
		
		List<Rect> pathElements = new ArrayList<Rect>();
		Element element;
		while ((element = reader.next()) != null){
			if (element.getType() == Element.e_path){
				pathElements.add(element.getBBox());
			}
		}
		
		reader.begin(pg);
		while ((element = reader.next()) != null){
			if (element.getType() == Element.e_text){
				boolean overlaps = false;
				for (Rect pathBox : pathElements) {
					if(element.getBBox().intersectRect(element.getBBox(), pathBox) &&
							pathBox.getWidth() != pg.getPageWidth() &&
							pathBox.getHeight() != pg.getPageHeight()){
						overlaps = true;
						break;
					}
				}
				if(overlaps == false){
					GState gs = element.getGState();
					gs.setTextRenderMode(3);
				}	
				writer.writeElement(element);
			}else{
				writer.writeElement(element);
			}
		}
		
		writer.flush();
		writer.end();
		writer.destroy();
		reader.end();
		reader.destroy();
		
		draw.setDPI(dpi);

		
		//java.awt.Image image = draw.getBitmap(pg);	
		draw.export(pg, imageName,"TIFF");

		draw.destroy();
				
	}

}
