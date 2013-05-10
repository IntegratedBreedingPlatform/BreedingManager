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
 * POJO for storing basic information about a Germplasm List Entry.
 * This class was created to implement a sorting logic for Germplasm List entries
 * of a Germplasm List by entryId;
 * 
 * @author Darla Ani
 *
 */
public class GermplasmListEntry implements Comparable<GermplasmListEntry>, Serializable{
	
	private static final long serialVersionUID = 4520653998347266903L;
	
	private Integer gid;
	private Integer entryId;
	private String designation;
	
	public GermplasmListEntry(Integer gid, Integer entryId){
		this.gid = gid;
		this.entryId = entryId;		
	}
	
	public GermplasmListEntry(Integer gid, Integer entryId, String designation){
		this.gid = gid;
		this.entryId = entryId;		
		this.designation = designation;
	}
	
	public Integer getGid() {
		return gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Integer getEntryId() {
		return entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	@Override
	public int compareTo(GermplasmListEntry entry) {
		return this.entryId - entry.getEntryId();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GermplasmListEntry [gid=");
		builder.append(gid);
		builder.append(", entryId=");
		builder.append(entryId);
		builder.append(", designation=");
		builder.append(designation);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((gid == null) ? 0 : gid.hashCode());
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
	    GermplasmListEntry other = (GermplasmListEntry) obj;
	    if (gid == null) {
			if (other.gid != null)
			    return false;
	    } else if (!gid.equals(other.gid))
	    	return false;
	    
	    return true;
	}
	
	

}
