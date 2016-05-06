package org.generationcp.breeding.manager.listmanager.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.generationcp.middleware.pojos.Germplasm;

public class GermplasmSearchResultSorter {

	private List<Germplasm> results;

	/*
	 * sorts the germplasm results based on the Sorting Column returns a germplasm list based from the specified starting row and number of
	 * entries
	 */
	public List<Germplasm> getGermplasmSearchResults(final GermplasmSearchParameter germplasmSearchParameter) {
		TreeSet<Germplasm> germplasmResultsSet;
		switch (germplasmSearchParameter.getSortingColumn()) {
			case SEED_RES:
				germplasmResultsSet = new TreeSet<Germplasm>(new SeedResComparator());
				break;
			case GROUP_ID:
				germplasmResultsSet = new TreeSet<Germplasm>(new GroupIDComparator());
				break;
			case STOCK_ID:
				germplasmResultsSet = new TreeSet<Germplasm>(new StockIDComparator());
				break;
			default:
				germplasmResultsSet = new TreeSet<Germplasm>();
				break;
		}
		germplasmResultsSet.addAll(this.results);

		this.results = new ArrayList<Germplasm>(germplasmResultsSet);
		return this.getGermplasmSearchResultsToDisplay(germplasmSearchParameter);
	}

	/*
	 * returns a germplasm list based from the specified starting row and number of entries
	 */
	public List<Germplasm> getGermplasmSearchResultsToDisplay(final GermplasmSearchParameter germplasmSearchParameter) {
		final int startingRow = germplasmSearchParameter.getStartingRow();
		final int endRow = germplasmSearchParameter.getStartingRow() + germplasmSearchParameter.getNumberOfEntries();
		return this.results.subList(startingRow, this.results.size() >= endRow ? endRow : this.results.size());
	}

	public void setGermplasmResults(final List<Germplasm> results) {
		this.results = results;
	}

	class SeedResComparator implements Comparator<Germplasm> {

		@Override
		public int compare(final Germplasm germplasm1, final Germplasm germplasm2) {
			final Integer germplasm1ReservedLotCount = germplasm1.getInventoryInfo().getReservedLotCount();
			final Integer germplasm2ReservedLotCount = germplasm2.getInventoryInfo().getReservedLotCount();
			return germplasm1ReservedLotCount >= germplasm2ReservedLotCount ? 1 : -1;
		}
	}

	class GroupIDComparator implements Comparator<Germplasm> {

		@Override
		public int compare(final Germplasm germplasm1, final Germplasm germplasm2) {
			return germplasm1.getMgid() >= germplasm2.getMgid() ? 1 : -1;
		}
	}

	class StockIDComparator implements Comparator<Germplasm> {

		@Override
		public int compare(final Germplasm germplasm1, final Germplasm germplasm2) {
			final String germplasm1StockIDs = germplasm1.getInventoryInfo().getStockIDs();
			final String germplasm2StockIDs = germplasm2.getInventoryInfo().getStockIDs();
			return germplasm1StockIDs.compareTo(germplasm2StockIDs);
		}
	}
}
