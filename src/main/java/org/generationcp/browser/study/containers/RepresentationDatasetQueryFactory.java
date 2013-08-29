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

package org.generationcp.browser.study.containers;

import java.util.List;

import org.generationcp.middleware.manager.StudyDataManagerImpl;
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

    private StudyDataManagerImpl studyDataManager;
    private Integer datasetId;
    private List<String> columnIds;
    @SuppressWarnings("unused")
    private QueryDefinition definition;
    private boolean fromUrl;                //this is true if this component is created by accessing the Study Details page directly from the URL

    /**
     * The constructor should be given the parameters which are then passed to
     * RepresentationDataSetQuery which uses them to retrieve the datasets by
     * using the Middleware.
     * 
     * @param dataManager
     * @param datasetId
     *            - id of the selected Representation of a Study
     * @param columnIds
     *            - List of column ids used for the Vaadin Table displaying the
     *            dataset
     */
    public RepresentationDatasetQueryFactory(StudyDataManagerImpl studyDataManager
            , Integer datasetId, List<String> columnIds, boolean fromUrl) {
        super();
        this.studyDataManager = studyDataManager;
        this.datasetId = datasetId;
        this.columnIds = columnIds;
        this.fromUrl = fromUrl;
    }

    /**
     * Create the Query object to be used by the LazyQueryContainer. Sorting is
     * not yet supported so the parameters to this method are not used.
     */
    @Override
    public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
        return new RepresentationDataSetQuery(studyDataManager, datasetId, columnIds, fromUrl);
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
