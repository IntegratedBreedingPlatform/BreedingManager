
package org.generationcp.breeding.manager.pojos;

import com.google.common.base.Strings;
import org.generationcp.commons.parsing.pojo.ImportedDescriptionDetails;
import org.generationcp.commons.parsing.pojo.ImportedFactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportedGermplasmList extends ImportedDescriptionDetails {

	private static final long serialVersionUID = -7039616815348588609L;
	private List<ImportedGermplasm> importedGermplasm = new ArrayList<>();
	private boolean hasStockIDValues = false;
	private boolean setImportedNameAsPreferredName = false;
	private String preferredNameCode = "";

	public ImportedGermplasmList(final String filename, final String name, final String title, final String type, final Date date) {
		super(filename, name, title, type, date);
	}

	public void removeImportedFactor(final String factorName) {
		for (final ImportedFactor factor : this.getImportedFactors()) {
			if (factor.getFactor().equalsIgnoreCase(factorName)) {
				this.importedFactors.remove(factor);
				break;
			}
		}
	}

	public boolean isUniqueStockId(final String stockId) {
		for (final ImportedGermplasm germplasm : this.getImportedGermplasm()) {
			if (stockId.equals(germplasm.getInventoryId())) {
				// oops we have retrieved an existing stockId in the list
				return false;
			}
		}

		return true;
	}

	public List<String> getStockIdsAsList() {
		final List<String> stockIDList = new ArrayList<>();

		for (final ImportedGermplasm germplasm : this.getImportedGermplasm()) {
			stockIDList.add(germplasm.getInventoryId());
		}

		return stockIDList;
	}

	public List<ImportedGermplasm> getImportedGermplasm() {
		return this.importedGermplasm;
	}

	public void setImportedGermplasm(final List<ImportedGermplasm> importedGermplasm) {
		this.importedGermplasm = importedGermplasm;
	}

	public void addImportedGermplasm(final ImportedGermplasm importedGermplasm) {
		this.importedGermplasm.add(importedGermplasm);
	}

	public void normalizeGermplasmList() {
		if (this.importedGermplasm != null) {
			Collections.sort(this.importedGermplasm, new ImportedGermplasmSorter());
		}
	}

	public boolean isUniqueStockId() {
		for (final String stockId : this.getStockIdsAsList()) {
			if (!this.isUniqueStockId(stockId)) {
				return false;
			}
		}

		return true;
	}

	public String getDuplicateStockIdIfExists() {
		final Set<String> set = new HashSet<>();

		for (final String stockId : this.getStockIdsAsList()) {
			if (set.contains(stockId)) {
				return stockId;
			} else {
				set.add(stockId);
			}
		}

		return "";
	}

	public boolean isHasStockIDValues() {
		return this.hasStockIDValues;
	}

	public void setHasStockIDValues(final boolean hasStockIDValues) {
		this.hasStockIDValues = hasStockIDValues;
	}

	public boolean isSetImportedNameAsPreferredName() {
		return this.setImportedNameAsPreferredName;
	}

	public void setSetImportedNameAsPreferredName(final boolean setImportedNameAsPreferredName) {
		this.setImportedNameAsPreferredName = setImportedNameAsPreferredName;
	}

	public String getPreferredNameCode() {
		return this.preferredNameCode;
	}

	public void setPreferredNameCode(final String preferredNameCode) {
		this.preferredNameCode = preferredNameCode;
	}

	/**
	 * This will check if stockId exist in germplasm and inventory variable/seed amount is empty then return true.
	 */
	public boolean hasMissingInventoryVariable() {
		for (final ImportedGermplasm importedGermplasm : this.getImportedGermplasm()) {
			if (this.hasStockIDValues && importedGermplasm.getSeedAmount() == 0
					&& !Strings.isNullOrEmpty(importedGermplasm.getInventoryId())) {
				return true;
			}
		}
		return false;
	}

	private class ImportedGermplasmSorter implements Comparator<ImportedGermplasm> {

		@Override
		public int compare(final ImportedGermplasm o1, final ImportedGermplasm o2) {
			return o1.getEntryId().compareTo(o2.getEntryId());
		}

	}

	/**
	 * @param importedFactors
	 */
	public void setImportedFactors(final List<ImportedFactor> importedFactors) {
		this.importedFactors = importedFactors;
	}

}
