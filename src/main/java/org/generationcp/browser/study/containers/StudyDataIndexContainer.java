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

import org.generationcp.browser.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.generationcp.middleware.pojos.Factor;
import org.generationcp.middleware.pojos.Scale;
import org.generationcp.middleware.pojos.Study;
import org.generationcp.middleware.pojos.Trait;
import org.generationcp.middleware.pojos.TraitMethod;
import org.generationcp.middleware.pojos.Variate;
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
    
    
    private static final String STUDY_ID = "ID";
    private static final String STUDY_NAME = "NAME";

    private StudyDataManager studyDataManager;
    private TraitDataManager traitDataManager;
    private int studyId;

    public StudyDataIndexContainer(StudyDataManager studyDataManager, TraitDataManager traitDataManager, int studyId) {
        this.studyDataManager = studyDataManager;
        this.traitDataManager = traitDataManager;
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

            ArrayList<Factor> query = (ArrayList<Factor>) studyDataManager.getFactorsByStudyID(studyId);

            for (Factor f : query) {

                String description = getFactorDescription(f.getTraitId());
                String propertyName = getProperty(f.getTraitId());
                String scaleName = getScaleName(f.getScaleId());
                String methodName = getMethodName(f.getMethodId());

                addFactorData(container, f.getName(), description, propertyName, scaleName, methodName, f.getDataType());
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

            ArrayList<Variate> query;
            query = (ArrayList<Variate>) studyDataManager.getVariatesByStudyID(studyId);

            for (Variate v : query) {

                String description = getFactorDescription(v.getTraitId());
                String propertyName = getProperty(v.getTraitId());
                String scaleName = getScaleName(v.getScaleId());
                String methodName = getMethodName(v.getMethodId());

                addVariateData(container, v.getName(), description, propertyName, scaleName, methodName, v.getDataType());
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

    private String getFactorDescription(int traitId) throws MiddlewareQueryException{
        String factorDescription = "";
        Trait trait = traitDataManager.getTraitById(traitId);
        if (!(trait == null)) {
            factorDescription = trait.getDescripton();
        }
        return factorDescription;
    }

    private String getProperty(int traitId) throws MiddlewareQueryException {
        String propertyName = "";
        Trait trait = traitDataManager.getTraitById(traitId);
        if (!(trait == null)) {
            propertyName = trait.getName();
        }
        return propertyName;
    }

    private String getScaleName(int scaleId) throws MiddlewareQueryException {
        String scaleName = "";
        Scale scale = traitDataManager.getScaleByID(scaleId);
        if (!(scale == null)) {
            scaleName = scale.getName();
        }
        return scaleName;
    }

    private String getMethodName(int methodId)  throws MiddlewareQueryException{
        String methodName = "";
        TraitMethod method = traitDataManager.getTraitMethodById(methodId);
        if (!(method == null)) {
            methodName = method.getName();
        }
        return methodName;
    }

    public IndexedContainer getStudies(String name, String country, Season season, Integer date) throws InternationalizableException {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(STUDY_ID, Integer.class, "");
        container.addContainerProperty(STUDY_NAME, String.class, "");
        
        ArrayList<Study> studies = new ArrayList<Study>();

        try {
            if (date != null) {

                // Get from central
                studies.addAll(studyDataManager.getStudyBySDate(date, 0,
                        (int) studyDataManager.countStudyBySDate(date, Operation.EQUAL, Database.CENTRAL), Operation.EQUAL,
                        Database.CENTRAL));

                // Get from central
                studies.addAll(studyDataManager.getStudyBySDate(date, 0,
                        (int) studyDataManager.countStudyBySDate(date, Operation.EQUAL, Database.LOCAL), Operation.EQUAL, Database.LOCAL));
            }

            if (name != null) {

                // Get from central
                studies.addAll(studyDataManager.getStudyByName(name, 0,
                        (int) studyDataManager.countStudyByName(name, Operation.EQUAL, Database.CENTRAL), Operation.EQUAL, Database.CENTRAL));

                // Get from central
                studies.addAll(studyDataManager.getStudyByName(name, 0,
                        (int) studyDataManager.countStudyByName(name, Operation.EQUAL, Database.LOCAL), Operation.EQUAL, Database.LOCAL));

            }

            if (country != null) {
                // Get from central
                studies.addAll(studyDataManager.getStudyByCountry(country, 0,
                        (int) studyDataManager.countStudyByCountry(country, Operation.EQUAL, Database.CENTRAL), Operation.EQUAL,
                        Database.CENTRAL));

                // Get from central
                studies.addAll(studyDataManager.getStudyByCountry(country, 0,
                        (int) studyDataManager.countStudyByCountry(country, Operation.EQUAL, Database.LOCAL), Operation.EQUAL,
                        Database.LOCAL));
            }

            if (season != null) {

                // Get from central
                studies.addAll(studyDataManager.getStudyBySeason(season, 0,
                        (int) studyDataManager.countStudyBySeason(season, Database.CENTRAL), Database.CENTRAL));

                // Get from central
                studies.addAll(studyDataManager.getStudyBySeason(season, 0,
                        (int) studyDataManager.countStudyBySeason(season, Database.LOCAL), Database.LOCAL));
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
