/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.germplasm.containers;

import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

/**
 * An implementation of QueryFactory which is needed for using the
 * LazyQueryContainer.
 * 
 * Reference:
 * https://vaadin.com/wiki/-/wiki/Main/Lazy%20Query%20Container/#section
 * -Lazy+Query+Container-HowToImplementCustomQueryAndQueryFactory
 * 
 * @author Kevin Manansala
 * 
 */
public class ListsForGermplasmQueryFactory implements QueryFactory{

    private GermplasmListManager dataManager;
    private Integer gid;
    @SuppressWarnings("unused")
    private QueryDefinition definition;

    public ListsForGermplasmQueryFactory(GermplasmListManager dataManager, Integer gid) {
        super();
        this.dataManager = dataManager;
        this.gid = gid;
    }

    /**
     * Create the Query object to be used by the LazyQueryContainer. Sorting is
     * not yet supported so the parameters to this method are not used.
     */
    @Override
    public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
        return new ListsForGermplasmQuery(dataManager, gid);
    }

    @Override
    public void setQueryDefinition(QueryDefinition definition) {
        // not sure how a QueryDefinition is used and how to create one
        // for the current implementation this is not used and I just copied
        // this method declaration
        // from the reference
        this.definition = definition;
    }

}
