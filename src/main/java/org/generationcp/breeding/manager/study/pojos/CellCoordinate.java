
package org.generationcp.breeding.manager.study.pojos;

public class CellCoordinate {

	private String x;
	private final String y;

	public CellCoordinate(Object x, Object y) {
		this.x = x.toString();
		this.y = y.toString();
	}

	public CellCoordinate(String x, String y) {
		this.x = x;
		this.y = y;
	}

	public void setX(String x) {
		this.x = x;
	}

	public void setY(String y) {
		this.x = y;
	}

	public String getX() {
		return this.x;
	}

	public String getY() {
		return this.y;
	}
}
