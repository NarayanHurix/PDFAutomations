package com.hurix.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

public class CharData 
{
	public String text;
	public Style  style;
	public Point2D.Double position;
	public String rotationMatrix;
	Rectangle2D.Double bbox;
	
	public CharData(Rectangle2D.Double bbox) {
		super();
		this.bbox = bbox;
	}

	public CharData(String text, Style style, Point2D.Double pos, String rotMtx) 
	{
		this.text = text;
		this.style = style;
		this.position = pos;
		this.rotationMatrix = rotMtx;
	}

	@Override
	public String toString() {
		return "\n\t\tCharData [text=" + text + ", bbox=" + bbox + "]";
	}

	
	
	
}
