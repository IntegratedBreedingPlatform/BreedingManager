
package org.generationcp.breeding.manager.inventory;

public class ListDataAndLotDetails {

	private Integer listId;
	private Integer sourceLrecId;
	private Integer savedLrecId;
	private Integer entryId;

	public ListDataAndLotDetails(Integer listId, Integer sourceLrecId, Integer entryId) {
		this.listId = listId;
		this.sourceLrecId = sourceLrecId;
		this.entryId = entryId;
	}

	public void setListId(Integer listId) {
		this.listId = listId;
	}

	public Integer getListId() {
		return this.listId;
	}

	public void setSourceLrecId(Integer sourceLrecId) {
		this.sourceLrecId = sourceLrecId;
	}

	public Integer getSourceLrecId() {
		return this.sourceLrecId;
	}

	public void setSavedLrecId(Integer savedLrecId) {
		this.savedLrecId = savedLrecId;
	}

	public Integer getSavedLrecId() {
		return this.savedLrecId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public Integer getEntryId() {
		return this.entryId;
	}

}
