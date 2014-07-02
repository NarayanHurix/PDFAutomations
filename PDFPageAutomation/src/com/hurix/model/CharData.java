package com.hurix.model;

import java.awt.geom.Point2D;

public class CharData 
{
	public String text;
	public Style  style;
	public Point2D.Double position;
	public String rotationMatrix;
	
	public CharData(String text, Style style, Point2D.Double pos, String rotMtx) 
	{
		this.text = text;
		this.style = style;
		this.position = pos;
		this.rotationMatrix = rotMtx;
	}
}
