package com.hurix.util;

import java.util.ResourceBundle;



public class MIMEProperties {
private static ResourceBundle bundle = ResourceBundle.getBundle("resources.mimeMap");
	
	public static String getProperty(String key){
		try{
			return bundle.getString(key);
		}catch (Exception e) {
			return "";
		}
	}
}
