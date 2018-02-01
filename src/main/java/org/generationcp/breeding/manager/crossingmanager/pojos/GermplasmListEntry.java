/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.crossingmanager.pojos;

import java.io.Serializable;

/**
 * POJO for storing basic information about a Germplasm List Entry. This class was created to implement a sorting logic for Germplasm List
 * entries of a Germplasm List by entryId;
 *
 * @author Darla Ani
 *
 */
public class GermplasmListEntry implements Comparable<GermplasmListEntry>, Serializable {

	private static final long serialVersionUID = 4520653998347266903L;

	private Integer listDataId;
	private Integer gid;
	private Integer entryId;
	private String designation;
	private String seedSource;
	private boolean isFromFemaleTable;

	public GermplasmListEntry(Integer listDataId, Integer gid, Integer entryId) {
		this.listDataId = listDataId;
		this.gid = gid;
		this.entryId = entryId;
		this.isFromFemaleTable = false;
	}

	public GermplasmListEntry(Integer listDataId, Integer gid, Integer entryId, String designation) {
		this.listDataId = listDataId;
		this.gid = gid;
		this.entryId = entryId;
		this.designation = designation;
		this.isFromFemaleTable = false;
	}

	public GermplasmListEntry(Integer listDataId, Integer gid, Integer entryId, String designation, String seedSource) {
		this.listDataId = listDataId;
		this.gid = gid;
		this.entryId = entryId;
		this.designation = designation;
		this.seedSource = seedSource;
		this.isFromFemaleTable = false;
	}

	public GermplasmListEntry(Integer listDataId, Integer gid, Integer entryId, String designation, String seedSource,
			boolean isFromFemaleTable) {
		this.listDataId = listDataId;
		this.gid = gid;
		this.entryId = entryId;
		this.designation = designation;
		this.seedSource = seedSource;
		this.isFromFemaleTable = isFromFemaleTable;
	}

	public Integer getListDataId() {
		return this.listDataId;
	}

	public void setListDataId(Integer listDataId) {
		this.listDataId = listDataId;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Integer getEntryId() {
		return this.entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
	}

	public boolean isFromFemaleTable() {
		return this.isFromFemaleTable;
	}

	public void setFromFemaleTable(boolean isFromFemaleTable) {
		this.isFromFemaleTable = isFromFemaleTable;
	}

	@Override
	public int compareTo(GermplasmListEntry entry) {
		return this.entryId - entry.getEntryId();
	}

	public GermplasmListEntry copy() {
		return new GermplasmListEntry(this.listDataId, this.gid, this.entryId, this.designation, this.seedSource, this.isFromFemaleTable);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GermplasmListEntry [gid=");
		builder.append(this.gid);
		builder.append(", entryId=");
		builder.append(this.entryId);
		builder.append(", designation=");
		builder.append(this.designation);
		builder.append(", seedSource=");
		builder.append(this.seedSource);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.listDataId == null ? 0 : this.listDataId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		GermplasmListEntry other = (GermplasmListEntry) obj;
		if (this.listDataId == null) {
			if (other.listDataId != null) {
				return false;
			}
		} else if (!this.listDataId.equals(other.listDataId) || !this.gid.equals(other.gid)) {
			return false;
		}

		if (other.listDataId != this.listDataId || other.seedSource != this.seedSource || other.gid != this.gid
				|| other.entryId != this.entryId || other.designation != this.designation) {
			return false;
		}

		return true;
	}

	public boolean hasEqualGidWith(GermplasmListEntry entry) {
		if (this.gid == null) {
			if (entry.gid != null) {
				return false;
			}
		} else {
			if (entry.gid == null) {
				return false;
			} else {
				if (!this.gid.equals(entry.gid)) {
					return false;
				}
			}
		}

		return true;
	}

}
