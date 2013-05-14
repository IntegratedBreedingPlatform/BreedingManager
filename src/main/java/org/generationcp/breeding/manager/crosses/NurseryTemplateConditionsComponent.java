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

package org.generationcp.breeding.manager.crosses;

import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Mark Agarrado
 *
 */
@Configurable
public class NurseryTemplateConditionsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

    /**
     * 
     */
    private static final long serialVersionUID = 6926035577490148208L;
    
    public static final String BACK_BUTTON_ID = "NurseryTemplateConditionsComponent Back Button";
    public static final String DONE_BUTTON_ID = "NurseryTemplateConditionsComponent Done Button";
    
    public static final String CONDITION_COLUMN = "Condition Column";
    public static final String DESCRIPTION_COLUMN = "Description Column";
    public static final String PROPERTY_COLUMN = "Property Column";
    public static final String SCALE_COLUMN = "Scale Column";
    public static final String VALUE_COLUMN = "Value Column";
    
    
    private Table nurseryConditionsTable;
    
    private Component buttonArea;
    private Button backButton;
    private Button doneButton;
    private ComboBox comboBoxSiteName;
    private TextField siteId;
    private ComboBox comboBoxBreedersName;
    private TextField breederId;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private UserDataManager userDataManager;
    
    private List<Location> locations;
    private List<User> users;

    private HashMap<String, Integer> mapSiteName;
    private HashMap<String, Integer> mapBreedersName;

    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
	
	comboBoxSiteName= new ComboBox();
	comboBoxSiteName.setImmediate(true);
	
	siteId=new TextField();
	siteId.setImmediate(true);
	
	comboBoxBreedersName= new ComboBox();
	comboBoxBreedersName.setImmediate(true);
	
	breederId=new TextField();
	breederId.setImmediate(true);
	
	generateConditionsTable();
        addComponent(nurseryConditionsTable);
        
        buttonArea = layoutButtonArea();
        addComponent(buttonArea);
        
    }
    
    protected void initializeValues() {
            
    }
    
    protected void initializeLayout() {
        setMargin(true);
        setSpacing(true);
        setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
    }
    
    protected void initializeActions() {
        
    }
    
    protected Component layoutButtonArea() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(true, false, false, false);

        backButton = new Button();
        buttonLayout.addComponent(backButton);
        doneButton = new Button();
        buttonLayout.addComponent(doneButton);
        return buttonLayout;
    }
    
    private void generateConditionsTable() {
        nurseryConditionsTable = new Table();
        nurseryConditionsTable.setStyleName("condition-rows");
        nurseryConditionsTable.setSizeFull();
        
        nurseryConditionsTable.addContainerProperty(CONDITION_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(DESCRIPTION_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(PROPERTY_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(SCALE_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(VALUE_COLUMN, Component.class, null);
        
        addConditionRows();
        nurseryConditionsTable.setPageLength(nurseryConditionsTable.size());
    }
    
    private void addConditionRows() {
        //TODO: populate this table using values read from the Nursery Template file
        nurseryConditionsTable.addItem(new Object[] {
                "NID", "NURSERY SEQUENCE NUMBER", "NURSERY", "NUMBER", new TextField()
        }, 
        "nid");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDER NAME", "PRINCIPAL INVESTIGATOR", "PERSON", "DBCV", getComboBoxBreedersName()
        }, 
        "breederName");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDER ID", "PRINCIPAL INVESTIGATOR", "PERSON", "DBID", getTextFieldBreederId()
        }, 
        "breederId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "SITE", "NURSERY SITE NAME", "LOCATION", "DBCV", getComboBoxSiteName()
        }, 
        "site");
        
        nurseryConditionsTable.addItem(new Object[] {
                "SITE ID", "NURSERY SITE ID", "LOCATION", "DBID", getTextFieldSiteId()
        }, 
        "siteId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDING METHOD", "Breeding method to be applied to this nursery", "METHOD", "DBCV", new ComboBox()
        }, 
        "breedingMethod");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDING METHOD ID", "ID of Breeding Method", "METHOD", "DBID", new TextField()
        }, 
        "breedingMethodId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "FEMALE LIST NAME", "FEMALE LIST NAME", "GERMPLASM LIST", "DBCV", new TextField()
        }, 
        "femaleListName");
        
        nurseryConditionsTable.addItem(new Object[] {
                "FEMALE LIST ID", "FEMALE LIST ID", "GERMPLASM LIST", "DBID", new TextField()
        }, 
        "femaleListId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "MALE LIST NAME", "MALE LIST NAME", "GERMPLASM LIST", "DBCV", new TextField()
        }, 
        "maleListName");
        
        nurseryConditionsTable.addItem(new Object[] {
                "MALE LIST ID", "MALE LIST ID", "GERMPLASM LIST", "DBID", new TextField()
        }, 
        "maleListId");
    }
    
    private ComboBox getComboBoxSiteName() {

	mapSiteName = new HashMap<String, Integer>();
	try {
	    locations=germplasmDataManager.getAllBreedingLocations();
	} catch (MiddlewareQueryException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	comboBoxSiteName.addItem("");
	for (Location loc : locations) {
	    if(loc.getLname().length()>0){
		comboBoxSiteName.addItem(loc.getLname());
		mapSiteName.put(loc.getLname(), new Integer(loc.getLocid()));
	    }
	}
	
	comboBoxSiteName.addListener(new Property.ValueChangeListener() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		siteId.setValue(String.valueOf(mapSiteName.get(comboBoxSiteName.getValue())));
	    }
	});
	return comboBoxSiteName;
    }
    
    private TextField getTextFieldSiteId(){

	siteId.addListener(new Property.ValueChangeListener() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		Location loc = new Location();
		boolean noError=true;

		try {
		    loc = germplasmDataManager.getLocationByID(Integer.valueOf(siteId.getValue().toString()));
		} catch (NumberFormatException e) {
		    noError=false;
		} catch (MiddlewareQueryException e) {
		    noError=false;
		}

		if(loc!=null && noError){
		    comboBoxSiteName.setValue(loc.getLname());
		}else{
		    getWindow().showNotification(messageSource.getMessage(Message.INVALID_SITE_ID));
		    comboBoxSiteName.select("");
		    siteId.setValue("");
		}
	    }
	});

	return siteId;
    }
    
    
    private ComboBox getComboBoxBreedersName() {

	mapBreedersName = new HashMap<String, Integer>();
	try {
	    users=userDataManager.getAllUsers();
	} catch (MiddlewareQueryException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	comboBoxBreedersName.addItem("");
	setComboBoxBreederDefaultValue();
	for (User u : users) {
	    Person p = new Person();
	    try {
		p = userDataManager.getPersonById(u.getPersonid());
	    } catch (MiddlewareQueryException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    String name=p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
	    comboBoxBreedersName.addItem(name);
	    mapBreedersName.put(name, new Integer(u.getUserid()));
	}
	
	comboBoxBreedersName.addListener(new Property.ValueChangeListener() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		breederId.setValue(String.valueOf(mapBreedersName.get(comboBoxBreedersName.getValue())));
	    }
	});
	return comboBoxBreedersName;
    }
    
    
    private void setComboBoxBreederDefaultValue() {
	try {
	    User user =workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
	    Integer projectId= workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue();
	    Integer ibdbUserId=workbenchDataManager.getLocalIbdbUserId(user.getUserid(),Long.valueOf(projectId));

	    User u=userDataManager.getUserById(ibdbUserId);
	    Person p=userDataManager.getPersonById(u.getPersonid());
	    
	    String name=p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
	    comboBoxBreedersName.addItem(name);
	    mapBreedersName.put(name, new Integer(u.getUserid()));
	    comboBoxBreedersName.select(name);
	    breederId.setValue(String.valueOf(u.getUserid()));
	    
	} catch (MiddlewareQueryException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    private TextField getTextFieldBreederId(){

	breederId.addListener(new Property.ValueChangeListener() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		Person p = new Person();
		User u= new User();
		String name="";
		boolean noError=true;

		try {
		    u=userDataManager.getUserById(Integer.valueOf(breederId.getValue().toString()));
		} catch (NumberFormatException e) {
		    noError=false;
		} catch (MiddlewareQueryException e) {
		    noError=false;
		}

		if(u!=null && noError){
		    try {
			p = userDataManager.getPersonById(u.getPersonid());
			name=p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
		    } catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    comboBoxBreedersName.setValue(name);
		}else{
		    getWindow().showNotification(messageSource.getMessage(Message.INVALID_BREEDER_ID));
		    comboBoxBreedersName.select("");
		    breederId.setValue("");
		}
	    }

	});

	return breederId;
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(nurseryConditionsTable, CONDITION_COLUMN, Message.CONDITION_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, DESCRIPTION_COLUMN, Message.DESCRIPTION_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, PROPERTY_COLUMN, Message.PROPERTY_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, SCALE_COLUMN, Message.SCALE_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, VALUE_COLUMN, Message.VALUE_HEADER);
        
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(doneButton, Message.DONE);
    }

}
