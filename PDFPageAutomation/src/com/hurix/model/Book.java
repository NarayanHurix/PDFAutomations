package com.hurix.model;

import java.util.ArrayList;
import java.util.List;

public class Book {
	List<Page> pages = new ArrayList<Page>();
	List<Style> styles = new ArrayList<Style>();
	
	
	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public List<Style> getStyles() {
		return styles;
	}

	public void setStyles(List<Style> styles) {
		this.styles = styles;
	}



	@Override
	public String toString() {
		return "Book [pages=" + pages + ", styles=" + styles + "]";
	}
	
	
	
}
