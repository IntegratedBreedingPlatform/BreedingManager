
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

import com.google.common.base.Strings;

public class ImportedGermplasmList extends ImportedDescriptionDetails {

	/**
	 *
	 */
	private static final long serialVersionUID = -7039616815348588609L;
	private List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
	private boolean hasStockIDValues = false;

	public static final String INVENTORY_AMOUNT_PROPERTY = "INVENTORY AMOUNT";

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
		for (final ImportedGermplasm germplasm : this.getImportedGermplasms()) {
			if (stockId.equals(germplasm.getInventoryId())) {
				// oops we have retrieved an existing stockId in the list
				return false;
			}
		}

		return true;
	}

	public List<String> getStockIdsAsList() {
		final List<String> stockIDList = new ArrayList<>();

		for (final ImportedGermplasm germplasm : this.getImportedGermplasms()) {
			stockIDList.add(germplasm.getInventoryId());
		}

		return stockIDList;
	}

	public List<ImportedGermplasm> getImportedGermplasms() {
		return this.importedGermplasms;
	}

	public void setImportedGermplasms(final List<ImportedGermplasm> importedGermplasms) {
		this.importedGermplasms = importedGermplasms;
	}

	public void addImportedGermplasm(final ImportedGermplasm importedGermplasm) {
		this.importedGermplasms.add(importedGermplasm);
	}

	public void normalizeGermplasmList() {
		if (this.importedGermplasms != null) {
			Collections.sort(this.importedGermplasms, new ImportedGermplasmSorter());
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

	/**
	 * This will check if stockId exist in germplasm and inventory variable/seed amount is empty then return true.
	 */
	public boolean hasMissingInventoryVariable() {
		for (final ImportedGermplasm importedGermplasm : this.getImportedGermplasms()) {
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
}
