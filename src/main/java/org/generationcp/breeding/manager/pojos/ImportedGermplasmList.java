package org.generationcp.breeding.manager.pojos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.commons.parsing.pojo.ImportedDescriptionDetails;
import org.generationcp.commons.parsing.pojo.ImportedFactor;

public class ImportedGermplasmList extends ImportedDescriptionDetails {

	private List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
	private boolean hasStockIDValues = false;

	public static final String INVENTORY_AMOUNT_PROPERTY = "INVENTORY AMOUNT";

	public ImportedGermplasmList(String filename, String name, String title, String type,
			Date date) {
		super(filename,name,title,type,date);
	}

	public void removeImportedFactor(String factorName) {
		for (ImportedFactor factor : getImportedFactors()) {
			if (factor.getFactor().equalsIgnoreCase(factorName)) {
				importedFactors.remove(factor);
				break;
			}
		}
	}

	public boolean isUniqueStockId(String stockId) {
		for (ImportedGermplasm germplasm : getImportedGermplasms()) {
			if (stockId.equals(germplasm.getInventoryId())) {
				// oops we have retrieved an existing stockId in the list
				return false;
			}
		}

		return true;
	}

	public List<String> getStockIdsAsList() {
		List<String> stockIDList = new ArrayList<>();

		for (ImportedGermplasm germplasm : getImportedGermplasms()) {
			stockIDList.add(germplasm.getInventoryId());
		}

		return stockIDList;
	}


	public List<ImportedGermplasm> getImportedGermplasms() {
		return importedGermplasms;
	}

	public void setImportedGermplasms(List<ImportedGermplasm> importedGermplasms) {
		this.importedGermplasms = importedGermplasms;
	}

	public void addImportedGermplasm(ImportedGermplasm importedGermplasm) {
		this.importedGermplasms.add(importedGermplasm);
	}

	public void normalizeGermplasmList() {
		if (importedGermplasms != null) {
			Collections.sort(importedGermplasms, new ImportedGermplasmSorter());
		}
	}

	public boolean isUniqueStockId() {
		for (String stockId : getStockIdsAsList()) {
			if (!isUniqueStockId(stockId)) {
				return false;
			}
		}

		return true;
	}

	public String getDuplicateStockIdIfExists() {
		Set<String> set = new HashSet<>();

		for (String stockId : getStockIdsAsList()) {
			if (set.contains(stockId)) {
				return stockId;
			} else {
				set.add(stockId);
			}
		}

		return "";
	}

	public boolean isHasStockIDValues() {
		return hasStockIDValues;
	}

	public void setHasStockIDValues(boolean hasStockIDValues) {
		this.hasStockIDValues = hasStockIDValues;
	}
	
	public boolean hasMissingStockIDValues(){
		for (ImportedGermplasm importedGermplasm : getImportedGermplasms()){
			if (this.hasStockIDValues && importedGermplasm.getSeedAmount() != 0 && "".equals(importedGermplasm.getInventoryId())) {
				return true;
			}
		}
		return false;
	}

	private class ImportedGermplasmSorter implements Comparator<ImportedGermplasm> {

		@Override
		public int compare(ImportedGermplasm o1, ImportedGermplasm o2) {
			return o1.getEntryId().compareTo(o2.getEntryId());
		}

	}
}