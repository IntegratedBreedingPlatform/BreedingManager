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

package org.generationcp.browser.study;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.spring.InternationalizableComponent;
import org.generationcp.commons.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

@Configurable
public class StudyVariateComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3225098517785018744L;
    
    private StudyDataManager studyDataManager;
    private TraitDataManager traitDataManager;
    private int studyId;
    
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION"; 
    private static final String PROP = "PROPERTY";
    private static final String SCA = "SCALE";
    private static final String METH = "METHOD";
    private static final String DTYPE = "DATATYPE";

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public StudyVariateComponent(StudyDataManager studyDataManager, TraitDataManager traitDataManager, int studyId)
            throws QueryException {

    	this.studyDataManager = studyDataManager;
    	this.traitDataManager = traitDataManager;
    	this.studyId = studyId;
    	
    }
    

    @Override
    public void afterPropertiesSet() {
    	
        StudyDataIndexContainer dataIndexContainer = new StudyDataIndexContainer(studyDataManager, traitDataManager, studyId);
        IndexedContainer dataStudyFactor;
        
		try {
			
			dataStudyFactor = dataIndexContainer.getStudyVariate();

			this.setContainerDataSource(dataStudyFactor);
        
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        setSelectable(true);
        setMultiSelect(false);
        setImmediate(true); // react at once when something is
        setSizeFull();
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        // setColumnHeaders(new String[] { "NAME", "DESCRIPTION", "PROPERTY",
        // "SCALE", "METHOD", "DATATYPE" });
        setColumnHeaders(new String[] {
        		NAME,
                DESC,
                PROP,
                SCA,
                METH,
                DTYPE });
    	
    }
    
    @Override
    public void attach() {
    	
        super.attach();
        
        updateLabels();
    }
    
	@Override
	public void updateLabels() {
		
        messageSource.setColumnHeader(this, "NAME", Message.name_header);
        messageSource.setColumnHeader(this, "DESCRIPTION", Message.description_header);
        messageSource.setColumnHeader(this, "PROPERTY", Message.property_header);
        messageSource.setColumnHeader(this, "SCALE", Message.scale_header);
        messageSource.setColumnHeader(this, "METHOD", Message.method_header);
        messageSource.setColumnHeader(this, "DATATYPE", Message.datatype_header);
        
	}

}
