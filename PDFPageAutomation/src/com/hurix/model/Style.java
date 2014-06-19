package com.hurix.model;

public class Style {
	private String fontName;
	private String fontColor;
	private double fontSize;
	
	public Style(String fontName, String fontColor, double fontSize) {
		super();
		this.fontName = fontName;
		this.fontColor = fontColor;
		this.fontSize = fontSize;
	}
	
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	public double getFontSize() {
		return fontSize;
	}
	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fontColor == null) ? 0 : fontColor.hashCode());
		result = prime * result
				+ ((fontName == null) ? 0 : fontName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(fontSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Style other = (Style) obj;
		if (fontColor == null) {
			if (other.fontColor != null)
				return false;
		} else if (!fontColor.equals(other.fontColor))
			return false;
		if (fontName == null) {
			if (other.fontName != null)
				return false;
		} else if (!fontName.equals(other.fontName))
			return false;
		if (Double.doubleToLongBits(fontSize) != Double
				.doubleToLongBits(other.fontSize))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Style [fontName=" + fontName + ", fontColor=" + fontColor
				+ ", fontSize=" + fontSize + "]";
	}
	
	
}
