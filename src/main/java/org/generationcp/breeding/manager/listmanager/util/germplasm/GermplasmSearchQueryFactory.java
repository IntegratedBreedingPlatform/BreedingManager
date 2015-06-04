
package org.generationcp.breeding.manager.listmanager.util.germplasm;

import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

/**
 * An implementation of QueryFactory which is needed for using the LazyQueryContainer.
 *
 * Reference: https://vaadin.com/wiki/-/wiki/Main/Lazy%20Query%20Container/#section
 * -Lazy+Query+Container-HowToImplementCustomQueryAndQueryFactory
 *
 * @author Joyce Avestro
 *
 */
public class GermplasmSearchQueryFactory implements QueryFactory {

	private final GermplasmDataManager germplasmDataManager;
	private final String searchValue;
	private final String searchChoice;

	@SuppressWarnings("unused")
	private QueryDefinition definition;

	public GermplasmSearchQueryFactory(GermplasmDataManager germplasmDataManager, String searchChoice, String searchValue) {
		this.germplasmDataManager = germplasmDataManager;
		this.searchChoice = searchChoice;
		this.searchValue = searchValue;
	}

	/**
	 * Create the Query object to be used by the LazyQueryContainer. Sorting is not yet supported so the parameters to this method are not
	 * used.
	 */
	@Override
	public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
		return new GermplasmSearchQuery(this.germplasmDataManager, this.searchChoice, this.searchValue);
	}

	@Override
	public void setQueryDefinition(QueryDefinition definition) {
		this.definition = definition;
	}
}
