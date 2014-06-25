package com.hurix.chain.impl;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pdftron.Common.Matrix2D;
import pdftron.PDF.CharData;
import pdftron.PDF.CharIterator;
import pdftron.PDF.ColorPt;
import pdftron.PDF.ColorSpace;
import pdftron.PDF.Element;
import pdftron.PDF.ElementReader;
import pdftron.PDF.Font;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.Page;
import pdftron.PDF.Rect;
import pdftron.PDF.Struct.SElement;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.Book;
import com.hurix.model.Constants.paraType;
import com.hurix.model.Line;
import com.hurix.model.ParaBlock;
import com.hurix.model.Style;
import com.hurix.model.CharData;

public class ReadTagPdf  implements Chain  {
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
		try{
			
			Map<String, String> glyphListMap = new HashMap<String, String>();
			
			Scanner scan = new Scanner(new File("/Users/narayan/Documents/Development/PDFSamples/glyphlist.txt"));
			
			while (scan.hasNextLine()) {
				String charMap = scan.nextLine();
				String[] charVsCode = charMap.split(";");
				glyphListMap.put(charVsCode[0], charVsCode[1]);
			}
			scan.close();
			
			PDFDoc inPdf= (PDFDoc)request.getObj();
			int pageCnt = inPdf.getPageCount();
			Book book = request.getBook();
			double prevY = 0;
			double prevGlyphX = 0;
			double prevGlyphW = 0;
			double prevMCID = 0;
			ParaBlock paraModel = null;
			for(int i=1;i<=pageCnt;i++){
				
				com.hurix.model.Page pageModel = new com.hurix.model.Page();
				
				book.getPages().add(pageModel);
				

				Line line = null;
				CharData word = null;
				
				ElementReader reader = new ElementReader();
				Page page = inPdf.getPage(1);
				reader.begin(page);
				
				double cropBoxX1 = page.getCropBox().getX1();
				double cropBoxY1 = page.getCropBox().getY1();
				
				Element element; 
				while ((element = reader.next()) != null){
					Font font = null;
					
					switch (element.getType()){
					case Element.e_text:
						{
							Rect bbox=element.getBBox();
							if (bbox == null) continue;
							
							if(!page.getCropBox().intersectRect(page.getCropBox(), bbox)){
								continue;
							}
							
							SElement struct_parent = element.getParentStructElement();
							if (struct_parent.isValid()) {
								//System.out.print(" Type: " + struct_parent.getType() 
								//		+ ", MCID: " + element.getStructMCID());
								if(prevMCID != struct_parent.getSDFObj().getObjNum()){
									paraModel = new ParaBlock(-1);
									paraModel.setType(detectParaType(struct_parent.getType()));
									pageModel.getBlocks().add(paraModel);
									prevMCID = struct_parent.getSDFObj().getObjNum();
									
								}
							}else{
								continue;
							}
							

							font = element.getGState().getFont();
							String[] fontGylphNames = null;
							if (Font.e_Type0 != font.getType()) {
								fontGylphNames = font.getEncoding();
							}

							// CTM (current transformation matrix).
							Matrix2D ctm = element.getCTM();
							Matrix2D text_mtx = element.getTextMatrix();
							Matrix2D mtx = ctm.multiply(text_mtx);
							ColorSpace cs_fill = element.getGState()
									.getFillColorSpace();
							ColorPt fill = element.getGState().getFillColor();

							ColorPt fillout = cs_fill.convert2RGB(fill);

							String fillhex = String.format("#%02x%02x%02x",
									(int) (fillout.get(0) * 255),
									(int) (fillout.get(1) * 255),
									(int) (fillout.get(2) * 255)).toUpperCase();
				
							double scale_factor = Math.sqrt(mtx.getB() * mtx.getB() + mtx.getD() * mtx.getD());
							double page_font_sz = element.getGState().getFontSize()	* scale_factor;
							double horiz_scale = element.getGState().getHorizontalScale() / 100.0;
							
							String fontName = element.getGState().getFont().getName().toLowerCase();
							if(fontName.indexOf("+") != -1){
								fontName = fontName.substring(fontName.indexOf("+") + 1);
							}
							
							double x, y;
							CharIterator itr = element.getCharIterator();
							while (itr.hasNext()) {
								CharData chardata = (CharData) itr.next();

								x = chardata.getGlyphX();
								y = chardata.getGlyphY();
								java.awt.geom.Point2D.Double t = mtx.multPoint(x, y);
								
								// set first value
								if (prevY == 0) {
									prevY = t.y;
									line = new Line(null);
									word = null;
									paraModel.getLines().add(line);
								}
								if (prevY != t.y) {
									prevY = t.y;
									if(line == null || line.getWords().size() > 0){
										line = new Line(null);
										word = null;
										paraModel.getLines().add(line);
									}
								}

								// word is in current line.... but gap is huge than lets
								// break them into 2 lines.
								if (prevY == t.y && prevGlyphX != 0 && prevGlyphW != 0) {
									double currentGlyphX = (t.x - cropBoxX1);
									if ((currentGlyphX - (prevGlyphX + prevGlyphW)) > 20) {
										if(line == null || line.getWords().size() > 0){
											line = new Line(null);
											word = null;
											paraModel.getLines().add(line);
										}
									}
								}

								// ok... may be author did not put space in pdf so check it
								// we need to split the word.
								if (((t.x - cropBoxX1) - (prevGlyphX + prevGlyphW)) > page_font_sz * 0.2 || word == null) {	
									if(word == null || word.getText().length() > 0){
										word = new CharData();
										line.getWords().add(word);
									}
								}


								String glyphName = null;
								String hexCode = null;
								if (fontGylphNames != null) {
									glyphName = fontGylphNames[(int) chardata.getCharCode()];
									hexCode = glyphListMap.get(glyphName);
								}
								String gly = "";
								if (hexCode == null) {
									gly = new String(font.mapToUnicode(chardata
											.getCharCode()));
								} else {
									gly = (char) Integer.parseInt(hexCode, 16) + "";
					
								}

								if (" ".equals(gly)) {
									if(word.getText().length() == 0){
										line.getWords().remove(word);
									}
									word = null;
									continue;
								}
								
								double dx = 0;
								if (font.isSimple()) {
									Rect gbox = GetGlyphBBox(chardata.getCharCode(), t.x,
											t.y, font, horiz_scale, page_font_sz,
											font.getAscent(), font.getDescent());
									gbox = GetBBoxTransfRect(gbox, mtx);
									dx = gbox.getWidth();
								}
								
								if (gly.charAt(0) < 32) {
									gly = "";
								}
						
								prevGlyphX = (t.x - cropBoxX1);
								prevGlyphW = dx;
								
								word.setText(gly);
								word.setRotationMatrix(mtx.getA()/scale_factor+"," + mtx.getB()/scale_factor+"," + mtx.getC()/scale_factor+"," + mtx.getD()/scale_factor+"," + mtx.getH()+"," + mtx.getV());
								word.getPositions().add(new Point2D.Double((t.x-cropBoxX1),(t.y-cropBoxY1) + ((font.getAscent()*page_font_sz)/1000)));
								Style style = new Style(fontName, fillhex, page_font_sz);
								if(book.getStyles().contains(style)){
									word.getStyles().add(book.getStyles().get(book.getStyles().indexOf(style)));
									style = null;
								}else{
									book.getStyles().add(style);
									word.getStyles().add(style);
								}
							}
							break;
						}
					}
				}
		
				reader.end();
				reader.destroy();
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}
	
	private paraType detectParaType(String tagName){
		if("H1".equalsIgnoreCase(tagName)){
			return paraType.title;
		}else if("H2".equalsIgnoreCase(tagName)){
			return paraType.section;
		}else if("H3".equalsIgnoreCase(tagName) || "H4".equalsIgnoreCase(tagName) 
				||"H5".equalsIgnoreCase(tagName) || "H6".equalsIgnoreCase(tagName)){
			return paraType.subsection;
		}
		return paraType.normal;
	}
	
	private Rect GetBBoxTransfRect(Rect inn, Matrix2D mtx)
			throws Exception {
		double p1x = inn.getX1(), p1y = inn.getY1(), p2x = inn.getX2(), p2y = inn
				.getY1(), p3x = inn.getX2(), p3y = inn.getY2(), p4x = inn
				.getX1(), p4y = inn.getY2();
		mtx.multPoint(p1x, p1y);
		mtx.multPoint(p2x, p2y);
		mtx.multPoint(p3x, p3y);
		mtx.multPoint(p4x, p4y);

		return new Rect(Math.min(Math.min(Math.min(p1x, p2x), p3x), p4x),
				Math.min(Math.min(Math.min(p1y, p2y), p3y), p4y), Math.max(
						Math.max(Math.max(p1x, p2x), p3x), p4x), Math.max(
						Math.max(Math.max(p1y, p2y), p3y), p4y));
	}
	
	private Rect GetGlyphBBox(long charCode, double currentX, double currentY,
			pdftron.PDF.Font font, double horiz_scale, double font_sz,
			double ascent, double descent) throws Exception {
		Rect out_bbox = new Rect();

		out_bbox.setX1(currentX);// = itr.Current().x;
		out_bbox.setY1(currentY);// = itr.Current().y;

		double dx = 0;

		dx = font.getWidth(charCode) / 1000.0;
		dx *= horiz_scale * font_sz;

		out_bbox.setX2(out_bbox.getX1() + dx);
		out_bbox.setY1(currentY + descent);
		out_bbox.setY2(currentY + ascent);

		return out_bbox;
	}
}
