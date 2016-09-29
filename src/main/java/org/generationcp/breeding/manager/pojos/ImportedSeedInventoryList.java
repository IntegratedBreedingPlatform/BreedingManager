package org.generationcp.breeding.manager.pojos;

import java.util.List;

public class ImportedSeedInventoryList {

	private String filename;

	private String listName;

	private List<ImportedSeedInventory> importedSeedInventoryList;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public ImportedSeedInventoryList(final String originalFinalName){
		this.filename  = originalFinalName;
	}

	public List<ImportedSeedInventory> getImportedSeedInventoryList() {
		return importedSeedInventoryList;
	}

	public void setImportedSeedInventoryList(List<ImportedSeedInventory> importedSeedInventoryList) {
		this.importedSeedInventoryList = importedSeedInventoryList;
	}
}
