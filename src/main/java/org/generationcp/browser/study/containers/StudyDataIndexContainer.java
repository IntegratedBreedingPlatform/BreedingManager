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
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.generationcp.middleware.pojos.Factor;
import org.generationcp.middleware.pojos.Scale;
import org.generationcp.middleware.pojos.Trait;
import org.generationcp.middleware.pojos.TraitMethod;
import org.generationcp.middleware.pojos.Variate;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class StudyDataIndexContainer{

    // Factor Object
    private static final Object FACTOR_NAME = "factorName";
    private static final Object VARIATE_NAME = "variateName";
    private static final Object DESCRIPTION = "description";
    private static final Object PROPERTY_NAME = "propertyName";
    private static final Object SCALE_NAME = "scaleName";
    private static final Object METHOD_NAME = "methodName";
    private static final Object DATATYPE = "dataType";

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

}
