/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.crossingmanager.pojos;

import java.io.Serializable;

/**
 * POJO for storing progenitor information of a new Germplasm.
 * 
 * @author Darla Ani
 *
 */
public class Germplasm implements Serializable, Comparable<Germplasm> {
	
	private static final long serialVersionUID = 6563703544594754250L;
	
	private Integer tempGID;
	private Integer gpId1;
	private Integer gpId2;
	
	public Germplasm(Integer tempGID, Integer gpId1, Integer gpId2){
		this.tempGID = tempGID;
		this.gpId1 = gpId1;
		this.gpId2 = gpId2;
	}
	
	public Integer getTempGID() {
		return tempGID;
	}
	
	public void setTempGID(Integer tempGID) {
		this.tempGID = tempGID;
	}
	
	public Integer getGpId1() {
		return gpId1;
	}
	
	public void setGpId1(Integer gpId1) {
		this.gpId1 = gpId1;
	}
	
	public Integer getGpId2() {
		return gpId2;
	}
	
	public void setGpId2(Integer gpId2) {
		this.gpId2 = gpId2;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Germplasm [tempGID=");
		builder.append(tempGID);
		builder.append(", gpId1=");
		builder.append(gpId1);
		builder.append(", gpId2=");
		builder.append(gpId2);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(Germplasm germplasm) {
		return tempGID - germplasm.getTempGID();
	}
	
}
