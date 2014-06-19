package com.hurix.model;

import java.util.ArrayList;
import java.util.List;


public class Page 
{
   
   public enum PageType {
      UNIT_PAGE, CHAPTER_PAGE, NORMAL_PAGE;
   }
   
	public List<Block> blocks = new ArrayList<Block>();

	public List<Block> getBlocks() {
      return blocks;
   }

   public void setBlocks(List<Block> blocks) {
      this.blocks = blocks;
   }

   @Override
	public String toString() {
		return "Page [paras=" + blocks + "]\n";
	}
	
}
