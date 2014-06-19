package com.hurix.chain.input;

import com.hurix.model.Book;

public class ChainRequest 
{
   
   // Represents PDF Object
	private Object obj;
	
	// Holds the PDF File Path
	private String pdfFilePath;
	
	// Sets the PDF Working Directory
	private String workDirectory;
	
	// Holds the Book Data constructed from PDF Elements
	private Book book;
	
	public Object getObj() {
		return obj;
	}
	
	public void setObj(Object obj) {
		this.obj = obj;
	}
	
   public String getPdfFilePath() {
      return this.pdfFilePath;
  }
  
  public void setPdfFilePath(String pdfFilePath) {
      this.pdfFilePath = pdfFilePath;
  }
  
	public String getWorkDirectory() {
		return workDirectory;
	}
	
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	
	public Book getBook() {
		return book;
	}
	
	public void setBook(Book book) {
		this.book = book;
	}
	
}
