package com.hurix.util.pdf2htmltron;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Entities.EscapeMode;

import pdftron.Common.Matrix2D;
import pdftron.PDF.Page;
import pdftron.PDF.TextExtractor;

import com.hurix.util.cssparser.PropertyValue;
import com.hurix.util.cssparser.Rule;
import com.hurix.util.cssparser.Selector;
import com.hurix.util.pdf2htmltron.pdf2html.epubType;


public class TransformXMLtoHTML {

	private int styleCount = 0;
	private double factor = 1.333333;
	
	public TransformXMLtoHTML(int styleCount) {
		super();
		this.styleCount = styleCount;
	}


	public List<Rule> processHTMLTransformation(String outputHtml, String pageCss, String bookCss,
			 String imageFile,String pageName,
			List<Rule> ruleList,Map<String,Rule> reverseSytles,epubType type,int pageSeq,Page pg) throws Exception {
		
		List<Rule> rulePageList = new ArrayList<Rule>();
		
		org.jsoup.nodes.Document htmlDocument = new org.jsoup.nodes.Document("");
		htmlDocument.outputSettings().escapeMode(EscapeMode.xhtml);
		org.jsoup.nodes.Element span = null;
		org.jsoup.nodes.Element div = null;
		org.jsoup.nodes.Element section = null;
		org.jsoup.nodes.Element figure = null;
		org.jsoup.nodes.Element image = null;
		org.jsoup.nodes.Element p = null;
		org.jsoup.nodes.Element body = null;

		body = htmlDocument.createElement("body");
		htmlDocument.appendChild(body);

		section = htmlDocument.createElement("section");
		section.attr("epub:type", "chapter");
		section.attr("id", pageName + "-div");
		body.appendChild(section);

		figure = htmlDocument.createElement("figure");
		figure.attr("id", "bodyimage");
		section.appendChild(figure);
		
		image = htmlDocument.createElement("img");
		image.attr("src", "images/" + imageFile);
		image.attr("alt", "");
		figure.appendChild(image);
		
		div = htmlDocument.createElement("div");
		div.attr("id", "parent-p"+pageSeq);
		section.appendChild(div);
		
		double pageH = pg.getPageHeight();
		double pageW = pg.getPageWidth();

		switch (type) {
			case imagebased:
				 createImageBased(htmlDocument, div, pageH, pageSeq, rulePageList,pg);
				break;
			case svgbased:
				 createImageBased(htmlDocument, div, pageH, pageSeq, rulePageList,pg);
				break;
			default:
				break;
		}

		
		p = htmlDocument.createElement("p");
		p.attr("class", "pagebreak");
		section.appendChild(p);

		span = htmlDocument.createElement("span");
		span.attr("epub:type", "pagebreak");
		span.attr("id", pageName);
		span.attr("title", pageName);
		p.appendChild(span);
		
		String etype= "";
		if(type == epubType.imagebased){
			etype= "<meta name=\"etype\" content=\"11\"/>";
		}else if(type == epubType.svgbased){
			etype= "<meta name=\"etype\" content=\"12\"/>";
		}
		String towrite = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE HTML> <html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:ibooks=\"http://apple.com/ibooks/html-extensions\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xml:lang=\"en-US\" lang=\"en-US\">"
			+ "         \n<head>\n <title>"
			+ pageName
			+ "</title>\n <meta charset=\"utf-8\" />\n"
			+etype
			+ " <meta content=\"width="
			+ Math.round(pageW * factor)
			+ ", height="
			+ Math.round(pageH * factor)
			+ "\" name=\"viewport\"/>\n<link href=\"css/template.css\" rel=\"stylesheet\" type=\"text/css\"></link>\n";
		towrite = towrite + "<link href=\"css/" + pageName 	+ ".css\" rel=\"stylesheet\" type=\"text/css\"></link>\n";
		
			
		
		FileOutputStream fos = new FileOutputStream(outputHtml);
		byte[] out =  (towrite + "\n</head>" + htmlDocument.toString() + "\n</html>").replaceAll("©", "&#169;").getBytes("UTF-8");
		fos.write(out);
		fos.flush();
		fos.close();
		
		if("page0001".equalsIgnoreCase(pageName)){
			// css body style and image style
			Rule bodyImage = new Rule();
			bodyImage.addSelector(new Selector("#bodyimage"));
			bodyImage.addPropertyValue(new PropertyValue("margin", "0"));
			bodyImage.addPropertyValue(new PropertyValue("width", ((pageW * factor)) + "px"));
			bodyImage.addPropertyValue(new PropertyValue("height",  ((pageH * factor)) + "px"));
			bodyImage.addPropertyValue(new PropertyValue("position", "absolute"));
			bodyImage.addPropertyValue(new PropertyValue("top", "0"));
			bodyImage.addPropertyValue(new PropertyValue("left", "0"));
			bodyImage.addPropertyValue(new PropertyValue("z-index", "-50"));
			bodyImage.addPropertyValue(new PropertyValue("transform-origin", "0 0"));
			bodyImage.addPropertyValue(new PropertyValue("-webkit-transform-origin", "0 0"));
			bodyImage.addPropertyValue(new PropertyValue("-ms-transform-origin", "0 0"));
			
			ruleList.add(bodyImage);
			
			
			Rule bodyRule = new Rule();
			bodyRule.addSelector(new Selector("body"));
			bodyRule.addPropertyValue(new PropertyValue("margin", "0"));
			bodyRule.addPropertyValue(new PropertyValue("width",  Math.round((pageW * factor)) + "px"));
			bodyRule.addPropertyValue(new PropertyValue("height",  Math.round((pageH * factor)) + "px"));
			
			ruleList.add(bodyRule);
			
		}
		
		return rulePageList;
	}

	
	
	public int getStyleCount() {
		return styleCount;
	}
	
	private void createImageBased(org.jsoup.nodes.Document htmlDocument,
			org.jsoup.nodes.Element div, double pageH,
			int pageSeq, List<Rule> rulePageList,Page pg){
		try{
			org.jsoup.nodes.Element span = null;			
			org.jsoup.nodes.Element p = null;
			
			p = htmlDocument.createElement("p");
			div.appendChild(p);

			TextExtractor txt = new TextExtractor();

			txt.begin(pg);
			TextExtractor.Line line = null;
			TextExtractor.Word word = null;
			int wCnt = 1;
			Matrix2D matrix = pg.getDefaultMatrix();
			
			for (line=txt.getFirstLine(); line.isValid(); line=line.getNextLine()){
				if (line.getNumWords() == 0 ) 
					continue;
				for (word=line.getFirstWord(); word.isValid(); word=word.getNextWord()){
					
					java.awt.geom.Point2D.Double wxy = matrix.multPoint(word.getBBox().getX1(), word.getBBox().getY1());
					java.awt.geom.Point2D.Double wxy2 = matrix.multPoint(word.getBBox().getX2(), word.getBBox().getY2());
					
					span = htmlDocument.createElement("span");
					p.appendChild(span);
					
					String wid = "p"+pageSeq + "-textid" + wCnt;
					span.attr("id", wid);
					wCnt++;
					Rule wordRule = new Rule();
					wordRule.addSelector(new Selector("#" + wid));
					wordRule.addPropertyValue(new PropertyValue("position", "absolute"));
					wordRule.addPropertyValue(new PropertyValue("white-space", "pre"));
					wordRule.addPropertyValue(new PropertyValue("top", ((pageH - wxy2.y) * factor) + "px"));
					wordRule.addPropertyValue(new PropertyValue("left", (wxy.x * factor) + "px"));
					wordRule.addPropertyValue(new PropertyValue("width", (word.getBBox().getWidth() * factor) + "px"));
					wordRule.addPropertyValue(new PropertyValue("height", (word.getBBox().getHeight() * factor) + "px"));
					wordRule.addPropertyValue(new PropertyValue("opacity", "0.5"));
					wordRule.addPropertyValue(new PropertyValue("color", "transparent"));
					span.appendText(word.getString());
					
					rulePageList.add(wordRule);
				}
			}
		
			txt.destroy();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

