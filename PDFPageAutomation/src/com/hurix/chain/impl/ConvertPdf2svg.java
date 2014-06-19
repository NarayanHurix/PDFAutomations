package com.hurix.chain.impl;

import java.io.File;
import java.util.List;

import pdftron.PDF.Convert;
import pdftron.PDF.Convert.SVGOutputOptions;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;

public class ConvertPdf2svg implements Chain {
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
		System.out.println("Pdf2svg bean process");
		try{
			PDFDoc inPdf= (PDFDoc)request.getObj();
			SVGOutputOptions opts = new SVGOutputOptions();
			opts.setEmbedFonts(true);
			opts.setEmbedImages(true);
			opts.setSvgFonts(true);
			//opts.setCompress(true);
			opts.setCreateXmlWrapper(false);
			opts.setFlattenContent(Convert.e_off);
			
			
			String workDirectory = request.getWorkDirectory() + "/svg/";
			 File folder = new File(workDirectory);
			 if(!folder.exists()){
				 folder.mkdirs();
			 }
			 
			PageIterator itr = null;
			int cnt = 1;
			for (itr = inPdf.getPageIterator(); itr.hasNext(); ){
				Page pg = (Page)(itr.next());
				String svgPath = workDirectory + "/page" + String.format("%04d", cnt) +".svg";
				Convert.toSvg(pg, svgPath, opts);
				//(new File(workDirectory + "/page_" + cnt +".xml")).delete();
				(new File(workDirectory + "/page" + String.format("%04d", cnt) +"_thumb.jpg")).delete();
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
