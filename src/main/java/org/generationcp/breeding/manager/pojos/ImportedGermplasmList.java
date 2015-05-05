package org.generationcp.breeding.manager.pojos;

import org.generationcp.commons.parsing.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ImportedGermplasmList extends ImportedDescriptionDetails {

	private static final Logger LOG = LoggerFactory.getLogger(ImportedGermplasmList.class);
	private List<ImportedGermplasm> importedGermplasms = new ArrayList<>();

	public static final String INVENTORY_AMOUNT_PROPERTY = "INVENTORY AMOUNT";

	public ImportedGermplasmList(String filename, String name, String title, String type,
			Date date) {
		super(filename,name,title,type,date);
	}

	public ImportedGermplasmList(String filename, String name, String title, String type, Date date
			, List<ImportedCondition> importedConditions, List<ImportedFactor> importedFactors
			, List<ImportedConstant> importedConstants, List<ImportedVariate> importedVariates
			, List<ImportedGermplasm> importedGermplasms) {
		this.filename = filename;
		this.name = name;
		this.title = title;
		this.type = type;
		this.date = date;
		this.importedConditions = importedConditions;
		this.importedFactors = importedFactors;
		this.importedConstants = importedConstants;
		this.importedVariates = importedVariates;
		this.importedGermplasms = importedGermplasms;
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

	private class ImportedGermplasmSorter implements Comparator<ImportedGermplasm> {

		@Override
		public int compare(ImportedGermplasm o1, ImportedGermplasm o2) {
			return o1.getEntryId().compareTo(o2.getEntryId());
		}

	}
}