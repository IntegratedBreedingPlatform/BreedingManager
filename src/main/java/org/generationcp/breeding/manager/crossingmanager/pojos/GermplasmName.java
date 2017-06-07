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

import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;

public class GermplasmName implements Comparable<GermplasmName>, Serializable {

	private static final long serialVersionUID = 4520653998347266903L;

	private Germplasm germplasm;
	private Name name;
	private Boolean isGidMatched;

	public GermplasmName(Germplasm germplasm, Name name) {
		this.germplasm = germplasm;
		this.name = name;
		this.isGidMatched = false;
	}

	public Germplasm getGermplasm() {
		return this.germplasm;
	}

	public void setGermplasm(Germplasm germplasm) {
		this.germplasm = germplasm;
	}

	public Name getName() {
		return this.name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	@Override
	public int compareTo(GermplasmName entry) {
		if (this.germplasm.getGid() == entry.getGermplasm().getGid() && this.name.getNval().equals(entry.getName().getNval())) {
			return 1;
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GermplasmName [gid=");
		builder.append(this.germplasm.getGid());
		builder.append(", name=");
		builder.append(this.name.getNval());
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return this.germplasm.hashCode() * 31 + this.name.hashCode() * 7;
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
		return true;
	}

	
	public Boolean isGidMatched() {
		return isGidMatched;
	}

	
	public void setIsGidMatched(Boolean isGidMatched) {
		this.isGidMatched = isGidMatched;
	}

}
