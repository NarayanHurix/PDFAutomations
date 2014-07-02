package com.hurix.chain.impl;

import java.io.FileWriter;
import java.util.List;

import com.amazonaws.util.json.JSONObject;
import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.Book;
import com.hurix.model.Line;
import com.hurix.model.ParaBlock;
import com.hurix.model.Word;

public class CreateJson implements Chain {
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
		/*System.out.println("CreateJson bean process");
		try{
			Book book = request.getBook();
			JSONObject jBook = new JSONObject();
			jBook.accumulate("id", book.getBookId());
			
			List<Chapter> chapters = book.getChapters();
			for (Chapter chapter : chapters) {
				JSONObject jChapter = new JSONObject();
				jBook.append("chapter", jChapter);
				
				List<com.hurix.model.Page> pages = chapter.getPages();
				for (com.hurix.model.Page page : pages) {
					JSONObject jPage = new JSONObject();
					jChapter.append("page", jPage);
					
					jPage.accumulate("id", page.getPageId());
					jPage.accumulate("width", page.getWidth());
					jPage.accumulate("height", page.getHeight());
					jPage.accumulate("cropBox", page.getCropBox());
					jPage.accumulate("mediaBox", page.getMediaBox());
					jPage.accumulate("trimBox", page.getTrimBox());
					jPage.accumulate("artBox", page.getArtBox());
					jPage.accumulate("bleedBox", page.getBleedBox());
					jPage.accumulate("rotation", page.getRotation());
					
					List<Para> paras = page.getParas();
					for (Para para : paras) {
						JSONObject jPara = new JSONObject();
						jPage.append("para", jPara);
						
						List<Line> lines = para.getLines();
						for (Line line : lines) {
							JSONObject jLine = new JSONObject();
							jPara.append("line", jLine);
							
							jLine.accumulate("x", line.getX());
							jLine.accumulate("y", line.getY());
							jLine.accumulate("width", line.getWidth());
							jLine.accumulate("height", line.getHeight());
							jLine.accumulate("bbox", line.getBbox());
							jLine.accumulate("rotation", line.isRotation());
							jLine.accumulate("color", line.getColor());
							jLine.accumulate("fontName", line.getFontName());
							jLine.accumulate("fontSize", line.getFontSize());
							jLine.accumulate("weight", line.getWeight());
		
							List<Word> words = line.getWords();
							for (Word word : words) {
								JSONObject jWord = new JSONObject();
								jLine.append("word", jWord);
								
								
								jWord.accumulate("x", word.getX());
								jWord.accumulate("y", word.getY());
								jWord.accumulate("text", word.getText());
								jWord.accumulate("color", word.getColor());
								jWord.accumulate("fontSize", word.getFontSize());
								jWord.accumulate("width", word.getWidth());
								jWord.accumulate("height", word.getHeight());
								jWord.accumulate("fontName", word.getFontName());
								jWord.accumulate("weight", word.getWeight());
								jWord.accumulate("italic", word.isItalic());
								jWord.accumulate("serif", word.isSerif());
								jWord.accumulate("bbox", word.getBbox());
								jWord.accumulate("matrix", word.getMatrix());
							}
						}
					}
				}
				
			}
			
			FileWriter jsonOut = new FileWriter(request.getWorkDirectory() + "/db.json");
			jsonOut.write(jBook.toString());
			jsonOut.flush();
			jsonOut.close();
			
			System.out.println("CreateJson>>>>>" + jBook);
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}

}
