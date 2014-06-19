package com.hurix.chain.impl;

import java.io.File;
import java.util.List;

import pdftron.PDF.Convert;
import pdftron.PDF.Convert.SVGOutputOptions;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFDraw;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;

public class ConvertPdf2image implements Chain {
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
		System.out.println("Pdf2image bean process");
		try{
			int dpi = 150;
			PDFDoc inPdf= (PDFDoc)request.getObj();
			
			String workDirectory = request.getWorkDirectory() + "/images/";
			 File folder = new File(workDirectory);
			 if(!folder.exists()){
				 folder.mkdirs();
			 }
			 
			PageIterator itr = null;
			int cnt = 1;
			for (itr = inPdf.getPageIterator(); itr.hasNext(); ){
				Page pg = (Page)(itr.next());
				String imgPath = workDirectory + "/page" + String.format("%04d", cnt) +".png";
				PDFDraw draw=new PDFDraw();
				draw.setPageBox(Page.e_crop);
				draw.setDPI(dpi);
				draw.setAntiAliasing(true);
				draw.setThinLineAdjustment(true, false);
				draw.setImageSmoothing(true);
				
				draw.export(pg, imgPath,"PNG");
				
				draw.destroy();
				cnt++;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}

}
