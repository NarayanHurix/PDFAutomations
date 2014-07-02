package com.hurix.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Line 
{
   
   Rectangle2D.Double bbox;
	List<Word> words = new ArrayList<Word>();
	public List<CharData> charDataList = new ArrayList<CharData>();

	public Line(Rectangle2D.Double bbox) {
	   this.bbox = bbox;
	}
	
	public Rectangle2D.Double getBbox() {
		return bbox;
	}
	 
	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	@Override
	public String toString() {
		return "\n\tLine [words=" + words + "]";
	}
	
}
