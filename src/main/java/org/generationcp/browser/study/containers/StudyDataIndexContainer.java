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

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.pojos.Study;
import org.generationcp.middleware.v2.domain.FactorDetails;
import org.generationcp.middleware.v2.domain.ObservationDetails;
import org.generationcp.middleware.v2.domain.StudyNode;
import org.generationcp.middleware.v2.domain.StudyQueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class StudyDataIndexContainer{
    
    private static final Logger LOG = LoggerFactory.getLogger(StudyDataIndexContainer.class);
    
    // Factor Object
    private static final Object FACTOR_NAME = "factorName";
    private static final Object VARIATE_NAME = "variateName";
    private static final Object DESCRIPTION = "description";
    private static final Object PROPERTY_NAME = "propertyName";
    private static final Object SCALE_NAME = "scaleName";
    private static final Object METHOD_NAME = "methodName";
    private static final Object DATATYPE = "dataType";
    
    
    public static final String STUDY_ID = "ID";
    public static final String STUDY_NAME = "NAME";

    private org.generationcp.middleware.v2.manager.api.StudyDataManager studyDataManagerv2;
    private int studyId;

    public StudyDataIndexContainer(org.generationcp.middleware.v2.manager.api.StudyDataManager studyDataManagerv2, int studyId) {
        this.studyDataManagerv2 = studyDataManagerv2;
        this.studyId = studyId;
    }

    public IndexedContainer getStudyFactor() throws InternationalizableException {
        try {
            IndexedContainer container = new IndexedContainer();

            // Create the container properties
            container.addContainerProperty(FACTOR_NAME, String.class, "");
            container.addContainerProperty(DESCRIPTION, String.class, "");
            container.addContainerProperty(PROPERTY_NAME, String.class, "");
            container.addContainerProperty(SCALE_NAME, String.class, "");
            container.addContainerProperty(METHOD_NAME, String.class, "");
            container.addContainerProperty(DATATYPE, String.class, "");

            List<FactorDetails> factorDetails = studyDataManagerv2.getFactors(Integer.valueOf(studyId));
            for(FactorDetails factorDetail : factorDetails){
                String name = factorDetail.getName();
                String description = factorDetail.getDescription();
                String propertyName = factorDetail.getProperty();
                String scaleName = factorDetail.getScale();
                String methodName = factorDetail.getMethod();
                String dataType = factorDetail.getDataType();
                
                addFactorData(container, name, description, propertyName, scaleName, methodName, dataType);
            }

            return container;

        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_STUDY_FACTOR);
        }
    }

    private static void addFactorData(Container container, String factorName, String description, String propertyName, String scale,
            String method, String datatype) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(FACTOR_NAME).setValue(factorName);
        item.getItemProperty(DESCRIPTION).setValue(description);
        item.getItemProperty(PROPERTY_NAME).setValue(propertyName);
        item.getItemProperty(SCALE_NAME).setValue(scale);
        item.getItemProperty(METHOD_NAME).setValue(method);
        item.getItemProperty(DATATYPE).setValue(datatype);
    }

    public IndexedContainer getStudyVariate() throws InternationalizableException {
        try {
            IndexedContainer container = new IndexedContainer();

            // Create the container properties
            container.addContainerProperty(VARIATE_NAME, String.class, "");
            container.addContainerProperty(DESCRIPTION, String.class, "");
            container.addContainerProperty(PROPERTY_NAME, String.class, "");
            container.addContainerProperty(SCALE_NAME, String.class, "");
            container.addContainerProperty(METHOD_NAME, String.class, "");
            container.addContainerProperty(DATATYPE, String.class, "");

            List<ObservationDetails> variateDetails = studyDataManagerv2.getObservations(Integer.valueOf(studyId));
            for(ObservationDetails variateDetail : variateDetails){
                String name = variateDetail.getName();
                String description = variateDetail.getDescription();
                String propertyName = variateDetail.getProperty();
                String scaleName = variateDetail.getScale();
                String methodName = variateDetail.getMethod();
                String dataType = variateDetail.getDataType();
                
                addVariateData(container, name, description, propertyName, scaleName, methodName, dataType);
            }
            
            return container;
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_STUDY_VARIATE);
        }
    }

    private static void addVariateData(Container container, String variateName, String description, String propertyName, String scale,
            String method, String datatype) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(VARIATE_NAME).setValue(variateName);
        item.getItemProperty(DESCRIPTION).setValue(description);
        item.getItemProperty(PROPERTY_NAME).setValue(propertyName);
        item.getItemProperty(SCALE_NAME).setValue(scale);
        item.getItemProperty(METHOD_NAME).setValue(method);
        item.getItemProperty(DATATYPE).setValue(datatype);
    }

    public IndexedContainer getStudies(String name, String country, Season season, Integer date) throws InternationalizableException {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(STUDY_ID, Integer.class, "");
        container.addContainerProperty(STUDY_NAME, String.class, "");
        
        ArrayList<Study> studies = new ArrayList<Study>();
        
        try {
            StudyQueryFilter filter = new StudyQueryFilter();
            filter.setName(name);
            filter.setCountry(country);
            filter.setSeason(season);
            filter.setStartDate(date);
            filter.setStart(0);
            filter.setNumOfRows(Integer.MAX_VALUE);
            List<StudyNode> studyNodes = studyDataManagerv2.searchStudies(filter);

            for(StudyNode node : studyNodes){
                Study study = new Study();
                study.setId(node.getId());
                study.setName(node.getName());
                studies.add(study);
            }

        } catch (MiddlewareQueryException e) {
            LOG.error("Error encountered while searching for studies", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR);
        }

        for (Study study : studies) {
            addStudyData(container, study.getId(), study.getName()); 
        }
        return container;
    }

    private static void addStudyData(Container container, Integer id, String name) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(STUDY_ID).setValue(id);
        item.getItemProperty(STUDY_NAME).setValue(name);
    }

}
