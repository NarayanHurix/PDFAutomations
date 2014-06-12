package com.hurix.util.pdf2htmltron;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFNet;
import pdftron.PDF.Page;

import com.hurix.util.cssparser.Rule;

public class pdf2html {
	public static final String pagePrefix = "page";
	public static final String stylePrefix = "p";
	public String baseDir;
	public epubType type; 
	private PDFDoc pdfDoc;


	public static enum epubType {
		imagebased,svgbased
	}

	public pdf2html(String baseDir,epubType type,PDFDoc pdfDoc) {
		this.baseDir = baseDir;
		this.type=type;
		this.pdfDoc = pdfDoc;
	}

	public static void main(String[] args) throws Exception{
		PDFNet.initialize();
		PDFDoc inPdf=new PDFDoc("T:/pdf/work/mouse/ebook.pdf");
		inPdf.initSecurityHandler();
		
		pdf2html p2h = new pdf2html("T:/pdf/work/mouse", epubType.imagebased,inPdf);
		p2h.generateXhtml();
		inPdf.close();
		 PDFNet.terminate();
	}
	
	public void generateXhtml() throws Exception {
	
		String htmlFolder = baseDir;
	
		String cssFolder = htmlFolder + "css/";

		new File(cssFolder).mkdirs();

		String bookCss = cssFolder + "template.css";
		
		StringBuilder bodyStyle = new StringBuilder();
		bodyStyle.setLength(0);
		List<Rule> ruleList = new ArrayList<Rule>();
	
		
		Map<String,Rule> reverseSytles = new HashMap<String, Rule>();
		int styleCount = 1;
		int noOfPages = pdfDoc.getPageCount();
		for(int i=1;i <= noOfPages ; i++){
			
			String outputHtml = htmlFolder + pagePrefix + String.format("%04d", i) + ".xhtml";
			String pageCss = cssFolder + pagePrefix+ String.format("%04d", i) + ".css";
			String imageFile = "";
			
			if(type == epubType.svgbased){
				imageFile = pagePrefix + String.format("%04d", i) + ".svg";
			}else{
				imageFile = pagePrefix + String.format("%04d", i) + ".png";
			}
			
			TransformXMLtoHTML xmltohtml = new TransformXMLtoHTML(styleCount);
			
			Page pg = pdfDoc.getPage(i);
			List<Rule> pageRuleList = xmltohtml.processHTMLTransformation(outputHtml,pageCss,bookCss,imageFile,pagePrefix + String.format("%04d", i),
					ruleList,reverseSytles,type,i,pg);
			styleCount = xmltohtml.getStyleCount();

			saveCssFile(pageCss,pageRuleList);

			if(i == 1){
				saveCssFile(bookCss, ruleList);
			}
		}
			

	}

	
	private void saveCssFile(String fileName,List<Rule> rules) throws Exception{
		Writer fw = new BufferedWriter(new OutputStreamWriter(	new FileOutputStream(fileName), "UTF8"));
		for (Rule rule : rules) {
			fw.write(rule.toString());
		}
		if(fileName.contains("template.css")){
			fw.write(".selected{outline: 1px solid green; }\n"); 
			fw.write(".pagebreak{margin:0;}\n");
			fw.write("figure{margin:0;}\n");
			fw.write("figure#bodyimage img{width: 100%;}\n");
			fw.write(".-epub-media-overlay-active {	background-color: yellow;}\n");
		}
		
		fw.flush();
		fw.close();
	}


	public static String getPrefix() {
		return pagePrefix;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}	
}
