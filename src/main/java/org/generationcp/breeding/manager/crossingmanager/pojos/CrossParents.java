
package org.generationcp.breeding.manager.crossingmanager.pojos;

import java.util.List;

public class CrossParents {

	private final GermplasmListEntry femaleParent;

	private final GermplasmListEntry maleParent;

	private final List<GermplasmListEntry> maleParents;

	private String seedSource;

	public CrossParents(GermplasmListEntry femaleParent, GermplasmListEntry maleParent) {
		this.femaleParent = femaleParent;
		this.maleParent = maleParent;
		this.maleParents = null;
	}

	public CrossParents(GermplasmListEntry femaleParent, List<GermplasmListEntry> maleParents) {
		this.femaleParent = femaleParent;
		this.maleParent = null;
		this.maleParents = maleParents;
	}

	public GermplasmListEntry getFemaleParent() {
		return this.femaleParent;
	}

	public GermplasmListEntry getMaleParent() {
		return this.maleParent;
	}

	public List<GermplasmListEntry> getMaleParents() {
		return this.maleParents;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.femaleParent != null && this.femaleParent.getGid() != null ? this.femaleParent.getGid().hashCode() : 0);
		result = prime * result + (this.maleParent != null && this.maleParent.getGid() != null ? this.maleParent.getGid().hashCode() : 0);
		result = prime * result + (this.maleParents != null ? this.maleParents.hashCode() : 0);
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
		CrossParents other = (CrossParents) obj;
		if (this.femaleParent == null) {
			if (other.femaleParent != null) {
				return false;
			}
		} else if (!this.femaleParent.hasEqualGidWith(other.femaleParent)) {
			return false;
		}
		if (this.maleParent == null) {
			if (other.maleParent != null) {
				return false;
			}
		} else if (this.maleParent != null && !this.maleParent.hasEqualGidWith(other.maleParent)) {
			return false;
		} else if (this.maleParents != null && !this.maleParents.equals(other.maleParents)) {
			return false;
		}
		return true;
	}

}
