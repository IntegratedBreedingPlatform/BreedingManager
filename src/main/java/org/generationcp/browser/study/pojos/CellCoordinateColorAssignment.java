package org.generationcp.browser.study.pojos;

public class CellCoordinateColorAssignment {

	private CellCoordinate cellCoordinate;
	private String cssClassName;
	private int redValue;
	private int greenValue;
	private int blueValue;

	public CellCoordinateColorAssignment(CellCoordinate cellCoordinate, String cssClassName, int redValue, int greenValue, int blueValue){
		this.redValue = redValue;
		this.greenValue = greenValue;
		this.blueValue = blueValue;
		this.cellCoordinate = cellCoordinate;
		this.cssClassName = cssClassName;
	}
	
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

	public void setRedValue(int redValue) {
		this.redValue = redValue;
	}

	public int getRedValue() {
		return this.redValue;
	}

	public void setGreenValue(int greenValue) {
		this.greenValue = greenValue;
	}

	public int getGreenValue() {
		return this.greenValue;
	}

	public void setBlueValue(int blueValue) {
		this.blueValue = blueValue;
	}

	public int getBlueValue() {
		return this.blueValue;
	}

}
