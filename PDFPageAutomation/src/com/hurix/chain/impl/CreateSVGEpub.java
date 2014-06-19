package com.hurix.chain.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import pdftron.PDF.PDFDoc;

import com.hurix.chain.define.Chain;
import com.hurix.chain.input.ChainRequest;
import com.hurix.model.EpubFileMetaData;
import com.hurix.util.FileUtility;
import com.hurix.util.MIMEProperties;
import com.hurix.util.TemplateUtil;
import com.hurix.util.pdf2htmltron.pdf2html;
import com.hurix.util.pdf2htmltron.pdf2html.epubType;

public class CreateSVGEpub implements Chain {
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
		System.out.println("CreateSVGEpub bean process");
		try{
			String workspace = request.getWorkDirectory();
			String epubWorkLocation = workspace + "/svg_epub/OPS/";
			(new File(epubWorkLocation)).delete();
			String bgFolder = epubWorkLocation + "/images";

			URL url = TemplateUtil.class.getResource("/com");
			String location = url.getFile().toString().replaceAll("/bin/com", "/workfiles");
			
			FileUtils.copyDirectory(new File(location), new File(workspace + "/svg_epub/")); 
			
			(new File(bgFolder)).mkdirs();
			
			FileUtils.copyDirectory(new File(workspace + "/svg"), new File(bgFolder));
			
			FileUtils.copyFile(new File(workspace + "/xml.sqlite"), new File(epubWorkLocation + "/db.json"));
			
			pdf2html htmlGen = new  pdf2html(epubWorkLocation, epubType.svgbased, (PDFDoc)request.getObj());
			htmlGen.generateXhtml();
			
			List<EpubFileMetaData> allEpubFiles = new ArrayList<EpubFileMetaData>();
			List<String> allFiles = FileUtility.listOnlyFileNamesRecursive(epubWorkLocation);
			String replaceTill = "OPS\\";
			for (String filePath : allFiles) {
				if (!filePath.endsWith("xhtml") && !filePath.endsWith("_text.jpg") 
						&& !filePath.endsWith("cover.png") && !filePath.endsWith("opf") && !filePath.endsWith("ncx")) {
					filePath = filePath.substring(filePath.lastIndexOf(replaceTill) + replaceTill.length());
					EpubFileMetaData bean = new EpubFileMetaData();
					bean.setFileName(filePath);
					String id = filePath.substring(	0,	filePath.lastIndexOf("."));
					bean.setFileID(id);
					String extension = filePath.substring(filePath.lastIndexOf(".")+1);
					String contentType = MIMEProperties.getProperty(extension);
					bean.setMinetype(contentType);
					allEpubFiles.add(bean);
				}
			}
			int noOfPages = ((PDFDoc)request.getObj()).getPageCount();
			for (int i = 1; i <= noOfPages; i++) {
				EpubFileMetaData bean = new EpubFileMetaData();
				String pageNum = String.format("%04d", i);
				bean.setFileName("page" + pageNum + ".xhtml");
				bean.setFileID("page" + pageNum);
				bean.setMinetype("application/xhtml+xml");
				allEpubFiles.add(bean);
			}
			
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("files", allEpubFiles);
			input.put("book_title", "book_title");
			input.put("book_isbn", "222222222");
			input.put("book_copyright", "book_copyright");
			input.put("book_published", (new Date()).toString());
			
			
			
			TemplateUtil.process("fixedcontent.opf", epubWorkLocation + "/content.opf", input);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.nextInChain != null){
			this.nextInChain.process(request);
		}
	}

}
