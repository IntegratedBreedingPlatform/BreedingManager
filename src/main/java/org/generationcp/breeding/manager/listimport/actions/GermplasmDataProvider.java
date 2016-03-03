package org.generationcp.breeding.manager.listimport.actions;


public interface GermplasmDataProvider {
	public Integer getGID() ;
	public Integer getProgenitors() ;
	public Integer getGPID1();
	public Integer getGPID2();
	public Integer getUserId() ;
	public Integer getDateValue();

	Integer getLocationId();

	int getMethodId();

	int getLgid();
}
