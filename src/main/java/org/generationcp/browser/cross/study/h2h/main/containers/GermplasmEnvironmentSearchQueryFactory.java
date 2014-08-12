/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * @author Kevin L. Manansala
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.cross.study.h2h.main.containers;

import java.util.List;

import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
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
 * @author cloned by Rebecca from original GermplasmSearchQueryFactory by Joyce
 * 
 */
public class GermplasmEnvironmentSearchQueryFactory implements QueryFactory{

    private CrossStudyDataManager crossStudyDataManager; 
    private String searchValue;
    private String searchChoice;
    private List<Integer> environmentIds;
    
    @SuppressWarnings("unused")
    private QueryDefinition definition;

    public GermplasmEnvironmentSearchQueryFactory(CrossStudyDataManager crossStudyDataManager, String searchChoice, String searchValue, List<Integer> environmentIds) {
        this.crossStudyDataManager = crossStudyDataManager;
        this.searchChoice = searchChoice;
        this.searchValue = searchValue;
        this.environmentIds = environmentIds;
    }

    /**
     * Create the Query object to be used by the LazyQueryContainer. Sorting is
     * not yet supported so the parameters to this method are not used.
     */
    @Override
    public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
        return new GermplasmEnvironmentSearchQuery(crossStudyDataManager, searchChoice, searchValue, environmentIds);
    }

    @Override
    public void setQueryDefinition(QueryDefinition definition) {
        this.definition = definition;
    }
}
