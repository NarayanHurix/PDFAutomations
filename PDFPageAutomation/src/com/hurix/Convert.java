package com.hurix;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pdftron.PDF.PDFNet;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.Book;

public class Convert {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PDFNet.initialize();
		 ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");
		 Chain chain = (Chain) applicationContext.getBean("startup");
		 
		 String workDirectory = "/Users/narayan/Documents/Development/PDFSamples/ModifiedPDF/";
		 File folder = new File(workDirectory);
		 if(!folder.exists()){
			 folder.mkdirs();
		 }
		 Book book = new Book();
		 
		 ChainRequest cr = new ChainRequest();
		 cr.setPdfFilePath("/Users/narayan/Documents/Development/PDFSamples/test_Part1_mod.pdf");
		 cr.setWorkDirectory(workDirectory);
		 cr.setBook(book);
		 
		 chain.process(cr);
		 
		 System.out.println(book);
		 System.out.println("DONE...");
		 
		 PDFNet.terminate();
	}

}
