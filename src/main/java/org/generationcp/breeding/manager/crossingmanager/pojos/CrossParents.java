package org.generationcp.breeding.manager.crossingmanager.pojos;


public class CrossParents {
    
    private GermplasmListEntry femaleParent;
    
    private GermplasmListEntry maleParent;
    
    private String seedSource;
    
    public CrossParents(GermplasmListEntry femaleParent, GermplasmListEntry maleParent){
        this.femaleParent = femaleParent;
        this.maleParent = maleParent;
    }
    
    public GermplasmListEntry getFemaleParent() {
        return femaleParent;
    }
    
    public GermplasmListEntry getMaleParent() {
        return maleParent;
    }

    
	public String getSeedSource() {
		return seedSource;
	}

	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((femaleParent == null) ? 0 : femaleParent.hashCode());
		result = prime * result
				+ ((maleParent == null) ? 0 : maleParent.hashCode());
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
		CrossParents other = (CrossParents) obj;
		if (femaleParent == null) {
			if (other.femaleParent != null)
				return false;
		} else if (!femaleParent.hasEqualGidWith(other.femaleParent))
			return false;
		if (maleParent == null) {
			if (other.maleParent != null)
				return false;
		} else if (!maleParent.hasEqualGidWith(other.maleParent))
			return false;
		return true;
	}

}
