package com.hurix.chain.impl;

import java.util.List;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.Book;
import com.hurix.util.GenerateDbSqlLiteService;

public class CreateSqlLite implements Chain {
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
		System.out.println("CreateSqlLite bean process");
		try{
			Book book = request.getBook();
			
			GenerateDbSqlLiteService sql = new GenerateDbSqlLiteService(request.getWorkDirectory()+"/");
			sql.createPdfExtract(book);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}

}
