package com.hurix.model;

import java.util.ArrayList;
import java.util.List;

import com.hurix.model.Constants.paraType;

public class ParaBlock extends Block
{
	private paraType type;
	public int paraId = -1;
	private List<Line> lines = null;
	
	public ParaBlock(int paraId) {
	   this.paraId = paraId;
	   lines = new ArrayList<Line>();
	}
	
	public paraType getType() {
		return type;
	}
	
	public void setType(paraType type) {
		this.type = type;
	}
	
	public List<Line> getLines() {
		return lines;
	}
	
	public void setLines(List<Line> lines) {
		this.lines = lines;
	}
	
	public void addLine(Line line) {
	   lines.add(line);
	}
	
	@Override
	public String toString() {
		return "\nPara [type=" + type + ", lines=" + lines + "]";
	}
}
