package com.hurix.chain.impl;

import java.util.List;

import pdftron.PDF.PDFDoc;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;

public class ClosePdf implements Chain {
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
		System.out.println("Close bean process");
		try{
			PDFDoc inPdf= (PDFDoc)request.getObj();
			inPdf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}

}
