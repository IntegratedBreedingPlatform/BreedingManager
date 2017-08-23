/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.breeding.manager.containers;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.vaadin.ui.Table;

/**
 * A factory that creates a list of germplasm details by implementing QueryFactory
 *
 */
public class GermplasmQueryFactory implements QueryFactory {

	private final ListManagerMain listManagerMain;
	private final Table matchingGermplasmsTable;
	private final GermplasmSearchParameter searchParameter;
	private Query query;
	private boolean viaToolUrl = true;
	private boolean showAddToList = true;
	private QueryDefinition definition;

	public GermplasmQueryFactory(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList,
			final GermplasmSearchParameter searchParameter, final Table matchingGermplasmsTable) {
		super();
		this.listManagerMain = listManagerMain;
		this.viaToolUrl = viaToolUrl;
		this.showAddToList = showAddToList;
		this.matchingGermplasmsTable = matchingGermplasmsTable;
		this.searchParameter = searchParameter;
	}

	public GermplasmQueryFactory(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList,
			final GermplasmSearchParameter searchParameter, final Table matchingGermplasmsTable, final QueryDefinition queryDefinition) {
		this(listManagerMain, viaToolUrl, showAddToList, searchParameter, matchingGermplasmsTable);

		this.setQueryDefinition(queryDefinition);
	}

	/**
	 * Create the Query object to be used by the LazyQueryContainer. Sorting is not yet supported so the parameters to this method are not
	 * used.
	 */
	@Override
	public Query constructQuery(final Object[] sortPropertyIds, final boolean[] sortStates) {

		// this will set up sort states if any (for sorting the germplasm query results)
		this.searchParameter.setSortState(sortPropertyIds, sortStates);

		this.query = new GermplasmQuery(this.listManagerMain, this.viaToolUrl, this.showAddToList, this.searchParameter,
				this.matchingGermplasmsTable, this.definition);
		return this.query;
	}

	@Override
	public void setQueryDefinition(final QueryDefinition definition) {
		this.definition = definition;
	}

	public int getNumberOfItems() {
		if (this.query != null) {
			return this.query.size();
		}
		return 0;
	}

}
