/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.study;

import java.util.List;

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
public class RepresentationDatasetQueryFactory implements QueryFactory{

    private StudyDataManager dataManager;
    private Integer representationId;
    private List<String> columnIds;
    private QueryDefinition definition;

    /**
     * The constructor should be given the parameters which are then passed to
     * RepresentationDataSetQuery which uses them to retrieve the datasets by
     * using the Middleware.
     * 
     * @param dataManager
     * @param representationId
     *            - id of the selected Representation of a Study
     * @param columnIds
     *            - List of column ids used for the Vaadin Table displaying the
     *            dataset
     */
    public RepresentationDatasetQueryFactory(StudyDataManager dataManager, Integer representationId,
	    List<String> columnIds) {
	super();
	this.dataManager = dataManager;
	this.representationId = representationId;
	this.columnIds = columnIds;
    }

    /**
     * Create the Query object to be used by the LazyQueryContainer. Sorting is
     * not yet supported so the parameters to this method are not used.
     */
    public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
	return new RepresentationDataSetQuery(dataManager, representationId, columnIds);
    }

    public void setQueryDefinition(QueryDefinition definition) {
	// not sure how a QueryDefinition is used and how to create one
	// for the current implementation this is not used and I just copied
	// this method declaration
	// from the reference
	this.definition = definition;
    }

}
