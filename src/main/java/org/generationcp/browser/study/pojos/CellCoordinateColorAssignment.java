package org.generationcp.browser.study.pojos;

public class CellCoordinateColorAssignment {

	private CellCoordinate cellCoordinate;
	private String cssClassName;

	public CellCoordinateColorAssignment(CellCoordinate cellCoordinate, String cssClassName){
		this.cellCoordinate = cellCoordinate;
		this.cssClassName = cssClassName;
	}
	
	public void setCellCoordinate(CellCoordinate cellCoordinate){
		this.cellCoordinate = cellCoordinate;
	}

	public CellCoordinate getCellCoordinate(){
		return cellCoordinate;
	}
	
	public void setCssClassName(String cssClassName){
		this.cssClassName = cssClassName;
	}	
	
	public String getCssClassName(){
		return cssClassName;
	}

}
