package com.hurix.util;

import pdftron.PDF.Rect;

public class RectUtility {

	public static String rectToString(Rect bbox){
		try{
			return bbox.getX1() + ":" + bbox.getY1() + ":" + bbox.getX2() + ":" + bbox.getY2();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
}
