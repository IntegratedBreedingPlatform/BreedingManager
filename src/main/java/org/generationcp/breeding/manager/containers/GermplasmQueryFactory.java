/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.containers;

import java.util.List;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.middleware.pojos.Germplasm;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.vaadin.ui.Table;

/**
 * A factory that creates a list of study details by implementing QueryFactory
 * 
 */
public class GermplasmQueryFactory implements QueryFactory {

	private Query query;

	private final List<Germplasm> germplasmSearchResult;
	private final ListManagerMain listManagerMain;
	private boolean viaToolUrl = true;
	private boolean showAddToList = true;
	private final Table matchingGermplasmsTable;

	public GermplasmQueryFactory(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList,
			final List<Germplasm> germplasmSearchResult, final Table matchingGermplasmsTable) {
		super();
		this.listManagerMain = listManagerMain;
		this.viaToolUrl = viaToolUrl;
		this.showAddToList = showAddToList;
		this.germplasmSearchResult = germplasmSearchResult;
		this.matchingGermplasmsTable = matchingGermplasmsTable;
	}

	/**
	 * Create the Query object to be used by the LazyQueryContainer. Sorting is not yet supported so the parameters to this method are not
	 * used.
	 */
	@Override
	public Query constructQuery(final Object[] sortPropertyIds, final boolean[] sortStates) {
		this.query =
				new GermplasmQuery(this.listManagerMain, this.viaToolUrl, this.showAddToList, this.germplasmSearchResult,
						this.matchingGermplasmsTable);
		return this.query;
	}

	@Override
	public void setQueryDefinition(final QueryDefinition arg0) {
		// no yet used
	}

	public int getNumberOfItems() {
		if (this.query != null) {
			return this.query.size();
		}
		return 0;
	}

}
