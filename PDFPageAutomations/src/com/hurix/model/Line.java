package com.hurix.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Line 
{
   
   Rectangle2D.Double bbox;
	List<CharData> words = new ArrayList<CharData>();

	public Line(Rectangle2D.Double bbox) {
	   this.bbox = bbox;
	}
	 
	public List<CharData> getWords() {
		return words;
	}

	public void setWords(List<CharData> words) {
		this.words = words;
	}

	@Override
	public String toString() {
		return "\n\tLine [words=" + words + "]";
	}
	
}
