package com.hurix.util;

import java.io.File;
import java.util.List;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.hurix.model.Book;
import com.hurix.model.Chapter;
import com.hurix.model.Line;
import com.hurix.model.Page;
import com.hurix.model.ParaBlock;
import com.hurix.model.CharData;

public class GenerateDbSqlLiteService {
	
	String DB_NAME = "xml.sqlite";
	 String DB_PATH = "";
	 
	 
	 public GenerateDbSqlLiteService(String dB_PATH) {
		super();
		DB_PATH = dB_PATH;
	}

	private String pdfExtractStructure = "CREATE TABLE PdfExtract (" +
				"paraID        integer," +
				"lineID        integer," +
				"lineY       float," +
				"lineH      float," +
				"wordID      integer," +
				"wordX      float(50,2)," +
				"wordW      float(50,2)," + 
				"wordText	       text," +
				"pageID        integer" +
				");";
	 
	 
	 
	 public void createPdfExtract(Book book){
		 try{
			 
			 File dbFile = new File(DB_PATH+ DB_NAME);
			 dbFile.delete();
			 SqlJetDb db = createSqlLite(dbFile);
			 db.beginTransaction(SqlJetTransactionMode.WRITE);
			 generateSchema(db,pdfExtractStructure);
		     
			 db.beginTransaction(SqlJetTransactionMode.WRITE);
		     
		     ISqlJetTable table = db.getTable("PdfExtract");
		     int paraId =1 ;
		     int lineId = 1;
		     int wordId = 1;
		     List<Chapter> chapters = book.getChapters();
		     for (Chapter chapter : chapters) {
		    	 List<Page> pages = chapter.getPages();
		    	 for (Page page : pages) {
		    		 List<ParaBlock> paras = page.getParas();
		    		 for (ParaBlock para : paras) {
		    			 List<Line> lines = para.getLines();
		    			 for (Line line : lines) {
		    				 List<CharData> words = line.getWords();
		    				 for (CharData word : words) {
		    					 table.insert(paraId,lineId,line.getY(),line.getHeight(),wordId,word.getX(),word.getWidth(),word.getText(),page.getPageId());
		    					 wordId++;
							}
		    				lineId++;
						}
		    			paraId++; 
					}
				}
			}
		     db.commit();
		     db.close();
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }
	 
	 
	 private SqlJetDb createSqlLite(File dbFile) throws SqlJetException
		{
			
	        
	        // create database, table and two indices:
	        SqlJetDb db = SqlJetDb.open(dbFile, true);
	        // set DB option that have to be set before running any transactions: 
	        db.getOptions().setAutovacuum(true);
	        // set DB option that have to be set in a transaction: 
	        db.runTransaction(new ISqlJetTransaction() {
	            public Object run(SqlJetDb db) throws SqlJetException {
	                db.getOptions().setUserVersion(1);
	                return true;
	            }
	        }, SqlJetTransactionMode.WRITE);
	       return db;
		}
	 
	 private void generateSchema( SqlJetDb db , String createTableQuery)  throws SqlJetException
		{
			try {            
	            db.createTable(createTableQuery);
	        } finally {
	            db.commit();
	        }
	        
		}
}
