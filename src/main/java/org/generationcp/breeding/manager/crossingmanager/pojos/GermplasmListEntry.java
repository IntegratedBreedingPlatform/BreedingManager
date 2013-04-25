package org.generationcp.breeding.manager.crossingmanager.pojos;

import java.io.Serializable;

/**
 * POJO for storing basic information about a Germplasm List Entry.
 * Comparison logic is by entryId field (ascending order)
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
		builder.append("]");
		return builder.toString();
	}

}
