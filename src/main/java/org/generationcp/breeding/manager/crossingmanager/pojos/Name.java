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
 * POJO for storing Name entity
 * 
 * @author Darla Ani
 *
 */
public class Name implements Serializable {
	
	private static final long serialVersionUID = 1813264102216332667L;

	private String nVal;
	private Integer typeId;
	
	public Name(String nVal){
		this.nVal = nVal;
	}
	
	public Name(String nVal, Integer typeId){
		this.nVal = nVal;
		this.typeId = typeId;
	}	
	
	public String getnVal() {
		return nVal;
	}

	public void setnVal(String nVal) {
		this.nVal = nVal;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Name [nVal=");
		builder.append(nVal);
		builder.append(", typeId=");
		builder.append(typeId);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
