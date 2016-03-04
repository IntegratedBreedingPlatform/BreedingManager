package org.generationcp.breeding.manager.listimport.actions;

/**
 * This interface a specific DataProvider for Germplasm generation.
 * Despite the source from which we are trying to create a Germplasm, this interface contains everything
 * the GermplasmBuilder require to fulfil it's task.
 */
public interface GermplasmDataProvider extends DataProvider {

	public Integer getGID() ;
	public Integer getProgenitors() ;
	public Integer getGPID1();
	public Integer getGPID2();
	public Integer getUserId() ;
	public Integer getDateValue();
	public Integer getGrplce();
	public Integer getReferenceId();
	public Integer getMgid();
	public Integer getLocationId();
	public Integer getMethodId();
	public Integer getLgid();

}
