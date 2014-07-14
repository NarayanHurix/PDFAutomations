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
		return "\n\n\n\tParaBlock [type=" + type + ", paraId=" + paraId + ", bbox=" + bbox + ", \n\tlines="
				+ lines  + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + paraId;
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
		ParaBlock other = (ParaBlock) obj;
		if (paraId != other.paraId)
			return false;
		return true;
	}
	
	
}
