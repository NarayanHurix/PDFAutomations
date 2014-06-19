package com.hurix.chain.impl;

import java.util.List;

import pdftron.PDF.PDFDoc;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;

public class LoadPdf implements Chain {
	private Chain nextInChain;
	private List<String> env;
	@Override
	public void setNextInChain(Chain nextInChain) {
		this.nextInChain = nextInChain;
	}

	@Override
	public void setEnv(List<String> options) {
		this.env = options;
		System.out.println("Env Options: "+this.env);
	}
	
	@Override
	public void process(ChainRequest request) 
	{
	   System.out.println("Load bean process");
		try {
			PDFDoc inPdf = new PDFDoc(request.getPdfFilePath());
			inPdf.initSecurityHandler();
			
			// Updating the request with PDFDoc Object
			request.setObj(inPdf); 
			
		} catch(Exception e){
			e.printStackTrace();
		}
		this.nextInChain.process(request);
	}

	

}
