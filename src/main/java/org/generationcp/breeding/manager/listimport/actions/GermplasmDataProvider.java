package org.generationcp.breeding.manager.listimport.actions;

/**
 * This interface a specific DataProvider for Germplasm generation.
 * Despite the source from which we are trying to create a Germplasm, this interface contains everything
 * the GermplasmBuilder require to fulfil it's task.
 */
public interface GermplasmDataProvider extends DataProvider {

	public int getGID() ;
	public int getProgenitors() ;
	public int getGPID1();
	public int getGPID2();
	public int getUserId() ;
	public int getDateValue();
	public int getGrplce();
	public int getReferenceId();
	public int getMgid();
	public int getLocationId();
	public int getMethodId();
	public int getLgid();

}
