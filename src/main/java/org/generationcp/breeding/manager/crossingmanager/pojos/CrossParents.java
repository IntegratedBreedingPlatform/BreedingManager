
package org.generationcp.breeding.manager.crossingmanager.pojos;

public class CrossParents {

	private final GermplasmListEntry femaleParent;

	private final GermplasmListEntry maleParent;

	private String seedSource;

	public CrossParents(GermplasmListEntry femaleParent, GermplasmListEntry maleParent) {
		this.femaleParent = femaleParent;
		this.maleParent = maleParent;
	}

	public GermplasmListEntry getFemaleParent() {
		return this.femaleParent;
	}

	public GermplasmListEntry getMaleParent() {
		return this.maleParent;
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
		result = prime * result + (this.femaleParent == null ? 0 : this.femaleParent.hashCode());
		result = prime * result + (this.maleParent == null ? 0 : this.maleParent.hashCode());
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
		} else if (!this.maleParent.hasEqualGidWith(other.maleParent)) {
			return false;
		}
		return true;
	}

}
