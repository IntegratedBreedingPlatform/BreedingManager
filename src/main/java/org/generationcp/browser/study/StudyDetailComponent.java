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
import org.generationcp.middleware.pojos.Study;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class StudyDetailComponent extends GridLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1738426765643928293L;

    private Label lblName;
    private Label lblTitle;
    private Label lblObjective;
    private Label lblType;
    private Label lblStartDate;
    private Label lblEndDate;
    
    private StudyDataManager studyDataManager;
    private int studyId;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public StudyDetailComponent(StudyDataManager studyDataManager, int studyId) {

    	this.studyDataManager = studyDataManager;
    	this.studyId = studyId;

    }
    
    @Override
    public void afterPropertiesSet() {
    	
        setRows(7);
        setColumns(3);
        setSpacing(true);
        setMargin(true);

        lblName = new Label(); // "Name"
        lblTitle = new Label(); // "Title"
        lblObjective = new Label(); // "Objective"
        lblType = new Label(); // "Type"
        lblStartDate = new Label(); // "Start Date"
        lblEndDate = new Label(); // "End Date"

        addComponent(lblName, 1, 1);
        addComponent(lblTitle, 1, 2);
        addComponent(lblObjective, 1, 3);
        addComponent(lblType, 1, 4);
        addComponent(lblStartDate, 1, 5);
        addComponent(lblEndDate, 1, 6);

        // get Study Detail

        Study study;
        
		try {
			
			study = studyDataManager.getStudyByID(studyId);

	        Label studyName = new Label(study.getName());
	        Label studyTitle = new Label(study.getTitle());
	        Label studyObjective = new Label(study.getObjective());
	        Label studyType = new Label(study.getType());
	        Label studyStartDate = new Label(String.valueOf(study.getStartDate()));
	        Label studyEndDate = new Label(String.valueOf(study.getEndDate()));
	
	        addComponent(studyName, 2, 1);
	        addComponent(studyTitle, 2, 2);
	        addComponent(studyObjective, 2, 3);
	        addComponent(studyType, 2, 4);
	        addComponent(studyStartDate, 2, 5);
	        addComponent(studyEndDate, 2, 6);
        
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @Override
    public void attach() {
    	
        super.attach();
        
        updateLabels();
    }
    

	@Override
	public void updateLabels() {
		
		messageSource.setCaption(lblName, Message.name_label);
		messageSource.setCaption(lblTitle, Message.title_label);
		messageSource.setCaption(lblObjective, Message.objective_label);
		messageSource.setCaption(lblType, Message.type_label);
		messageSource.setCaption(lblStartDate, Message.start_date_label);
		messageSource.setCaption(lblEndDate, Message.end_date_label);
        
		
	}

}
