package org.generationcp.breeding.manager.listimport.actions;

/**
 * This interface a specific DataProvider for Germplasm generation.
 * Despite the source from which we are trying to create a Germplasm, this interface contains everything
 * the GermplasmBuilder and NameBuilder require to fulfil it's task.
 */
public interface GermplasmDataProvider extends DataProvider {

	public int getGid() ;
	public int getProgenitors() ;
	public int getFemaleParent();
	public int getMaleParent();
	public int getUserId() ;
	public int getDateValue();
	public int getGrplce();
	public int getReferenceId();
	public int getMgid();
	public int getLocationId();
	public int getMethodId();
	public int getLgid();


	public int getNameDateValue();
	public int getTypeId();
	public String getName();
	public int getNstat();

}
