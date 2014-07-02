package com.hurix.chain.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.PDF.Element;
import pdftron.PDF.ElementReader;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;
import pdftron.PDF.Rect;
import pdftron.PDF.TextExtractor;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.Book;
import com.hurix.model.Line;
import com.hurix.model.ParaBlock;
import com.hurix.model.Word;
import com.hurix.util.RectUtility;

public class FindData implements Chain {
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
		System.out.println("Data find bean process");
		try{
			PDFDoc inPdf= (PDFDoc)request.getObj();
			Book book = request.getBook();
			
			
			
			TextExtractor txt = new TextExtractor();
			
			PageIterator itr = null;
			TextExtractor.Line line = null;
			TextExtractor.Word word = null;
			TextExtractor.Style style, line_style = null;
			int cur_para_id=-1;
			int cnt = 1;
			com.hurix.model.Page mypage = null;
			ParaBlock mypara = null;
			Line myline = null;
			Word myword = null;
			
			for (itr = inPdf.getPageIterator(); itr.hasNext(); ){
				Page pg = (Page)(itr.next());
				
				Matrix2D matrix = pg.getDefaultMatrix();
				
				mypage = new com.hurix.model.Page();
			
				
				book.getPages().add(mypage);
				
				txt.begin(pg);
				for (line=txt.getFirstLine(); line.isValid(); line=line.getNextLine()){
					if (line.getNumWords() == 0 ) 
						continue;
					if (cur_para_id != line.getParagraphID() || cur_para_id == -1) {
                  cur_para_id = line.getParagraphID();
						mypara = new ParaBlock(cur_para_id);
						mypage.getBlocks().add(mypara);
					}
					line_style = line.getStyle();
					
					java.awt.geom.Point2D.Double lxy = matrix.multPoint(line.getBBox().getX1(), line.getBBox().getY1());
					java.awt.geom.Point2D.Double lxy2 = matrix.multPoint(line.getBBox().getX2(), line.getBBox().getY2());
					
					myline = new Line((Rectangle2D.Double)line.getBBox().getRectangle());
				
					mypara.getLines().add(myline);
					
					for (word=line.getFirstWord(); word.isValid(); word=word.getNextWord()){
						style = word.getStyle();
						
						java.awt.geom.Point2D.Double wxy = matrix.multPoint(word.getBBox().getX1(), word.getBBox().getY1());
						java.awt.geom.Point2D.Double wxy2 = matrix.multPoint(word.getBBox().getX2(), word.getBBox().getY2());
						
						myword = new Word();
						myword.setText(word.getString());
						
						myline.getWords().add(myword);
					}
				}
				
				cnt++;
			}
			
			txt.destroy();
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}
	
	String findRotation(Page pg, Rect pos) throws PDFNetException {
		//System.out.println("LOOKING FOR : " + RectUtility.rectToString(pos));
		String matrix="";
		Matrix2D mat = pg.getDefaultMatrix();
		Point2D.Double d1 = mat.multPoint(pos.get()[0], pos.get()[1]);
		Point2D.Double d2 = mat.multPoint(pos.get()[2], pos.get()[3]);
		
		pos = new Rect(d1.getX(), d1.getY(), d2.getX(), d2.getY());
		
		ElementReader reader=new ElementReader();
		reader.begin(pg);
		Element element; 
		while ((element = reader.next()) != null){
			if (element.getType() == Element.e_text){
				
				Rect bbox=element.getBBox();
				if (bbox == null) continue;
				
				
				
				if(bbox.intersectRect(bbox, pos)){
					Matrix2D ctm = element.getCTM();
					Matrix2D text_mtx = element.getTextMatrix();
					Matrix2D mtx = ctm.multiply(text_mtx);
					double scale_factor = Math.sqrt(mtx.getB()*mtx.getB() + mtx.getD()*mtx.getD());
					matrix = mtx.getA()/scale_factor+"," + mtx.getB()/scale_factor+"," + mtx.getC()/scale_factor+"," + mtx.getD()/scale_factor+"," + mtx.getH()+"," + mtx.getV();
					break;
				}
					
			}
		}
		reader.end();
		reader.destroy();
		return matrix;
	}

}
