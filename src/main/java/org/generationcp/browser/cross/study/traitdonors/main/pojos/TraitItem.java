package org.generationcp.browser.cross.study.traitdonors.main.pojos;

/**
 * POJO Item specifically purposed for inclusion in Vaadin BeanItemContainers
 * 
 * This item captures Trait information
 * 
 * @author rebecca
 * 
 */

public class TraitItem {

	private Integer stdVarId;
	private String traitName;
	private String stdVarName;

	public Integer getStdVarId() {
		return this.stdVarId;
	}

	public void setStdVarId(Integer stdVarId) {
		this.stdVarId = stdVarId;
	}

	public String getTraitName() {
		return this.traitName;
	}

	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	public String getStdVarName() {
		return this.stdVarName;
	}

	public void setStdVarName(String stdVarName) {
		this.stdVarName = stdVarName;
	}

	/**
	 * Hashcode is overriden here so that insertion into Vaadin tables makes use
	 * of the unique ListSet that backs them. Standard Variable Name is a unique
	 * key and used as such.
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.stdVarName == null ? 0 : this.stdVarName.hashCode());
		return result;
	}

	/**
	 * Equals is overriden here so that insertion into Vaadin tables makes use
	 * of the unique ListSet that backs them. Standard Variable Name is a unique
	 * key and used as such.
	 * 
	 */
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
		TraitItem other = (TraitItem) obj;
		if (this.stdVarName == null) {
			if (other.stdVarName != null) {
				return false;
			}
		} else if (!this.stdVarName.equals(other.stdVarName)) {
			return false;
		}
		return true;
	}

}
