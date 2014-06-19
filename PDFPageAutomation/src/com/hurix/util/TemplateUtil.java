package com.hurix.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class TemplateUtil {
	public static void process(String templateName,String outputFile,Map<String, Object> input){
		try{
			Configuration cfg = new Configuration();
			URL url = TemplateUtil.class.getResource("/templates");
			String location = url.getFile().toString();
			location = location.replaceFirst("/", "");
		    cfg.setDefaultEncoding("UTF-8");
		    cfg.setLocale(Locale.US);
		    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		    cfg.setDirectoryForTemplateLoading(new File(location));
		   
		    FileOutputStream xmlFile = new FileOutputStream(outputFile);
			Writer out = new OutputStreamWriter(xmlFile, "UTF-8");
		    
		    Template template = cfg.getTemplate(templateName);
		    template.process(input, out);
		    
		    out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
