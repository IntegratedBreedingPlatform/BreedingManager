package org.generationcp.breeding.manager.listimport.actions;

/**
 * This interface a specific DataProvider for Name generation.
 * Despite the source from which we are trying to create a Name, this interface contains everything
 * the GermplasmBuilder require to fulfil it's task.
 */
public interface NameDataProvider extends DataProvider {

	public int getUserId() ;
	public int getDateValue();
	public int getReferenceId();
	public int getLocationId();
	public int getTypeId();
	public String getName();
	public int getNstat();
}
