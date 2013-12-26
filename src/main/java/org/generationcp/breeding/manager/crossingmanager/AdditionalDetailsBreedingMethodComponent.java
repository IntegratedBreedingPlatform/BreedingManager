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

package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.TemplateCrossingCondition;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUtil;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window.Notification;

/**
 * This class contains the absolute layout of UI elements in Breeding Method section
 * in "Enter Additional Details..." tab in Crossing Manager application
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsBreedingMethodComponent extends AbsoluteLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener{

    private static final long serialVersionUID = 2539886412902509326L;
    private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsBreedingMethodComponent.class);

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager; 
      
    private Label selectOptionLabel;
    private Label selectCrossingMethodLabel;
    private Label methodDescriptionLabel;
    private TextArea crossingMethodDescriptionTextArea;
    
    private OptionGroup crossingMethodOptionGroup;
    private ComboBox crossingMethodComboBox;
    private CheckBox favoriteMethodsCheckbox;
    private HashMap<String, Integer> mapMethods;
    
    private CrossesMadeContainer container;
    
    private List<Method> methods;
    private List<Integer> favoriteMethodIds;
    private List<Method> favoriteMethods;
    
    
    private enum CrossingMethodOption{
        SAME_FOR_ALL_CROSSES, BASED_ON_PARENTAL_LINES
    };
    
    @Override
    public void setCrossesMadeContainer(CrossesMadeContainer container) {
        this.container = container;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {  
        setHeight("180px");
        setWidth("700px");
        
        selectOptionLabel = new Label();
        
        crossingMethodOptionGroup = new OptionGroup();
        crossingMethodOptionGroup.addItem(CrossingMethodOption.SAME_FOR_ALL_CROSSES);
        crossingMethodOptionGroup.setItemCaption(CrossingMethodOption.SAME_FOR_ALL_CROSSES, 
                messageSource.getMessage(Message.CROSSING_METHOD_WILL_BE_THE_SAME_FOR_ALL_CROSSES));
        crossingMethodOptionGroup.addItem(CrossingMethodOption.BASED_ON_PARENTAL_LINES);
        crossingMethodOptionGroup.setItemCaption(CrossingMethodOption.BASED_ON_PARENTAL_LINES, 
                messageSource.getMessage(Message.CROSSING_METHOD_WILL_BE_SET_BASED_ON_STATUS_OF_PARENTAL_LINES));
        crossingMethodOptionGroup.select(CrossingMethodOption.BASED_ON_PARENTAL_LINES);
        crossingMethodOptionGroup.setImmediate(true);
        crossingMethodOptionGroup.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -818940519031621592L;

			@Override
            public void valueChange(ValueChangeEvent event) {
                boolean sameForAllCrossesOptionSelected = 
                	crossingMethodOptionGroup.getValue().equals(CrossingMethodOption.SAME_FOR_ALL_CROSSES);
				
                selectCrossingMethodLabel.setEnabled(sameForAllCrossesOptionSelected);
                methodDescriptionLabel.setEnabled(sameForAllCrossesOptionSelected);
                favoriteMethodsCheckbox.setEnabled(sameForAllCrossesOptionSelected);
                crossingMethodComboBox.setEnabled(sameForAllCrossesOptionSelected);
                if(sameForAllCrossesOptionSelected){
	                crossingMethodComboBox.focus();
	                populateBreedingMethods((Boolean)favoriteMethodsCheckbox.getValue());
					
                }else{
	                crossingMethodComboBox.removeAllItems();
	                resetMethodTextArea();
                }
            }

        });
        
        
        selectCrossingMethodLabel = new Label();
        selectCrossingMethodLabel.setEnabled(false);
        
        methodDescriptionLabel= new Label();
        methodDescriptionLabel.setEnabled(false);
        
        crossingMethodDescriptionTextArea=new TextArea();
        crossingMethodDescriptionTextArea.setWordwrap(true);
        crossingMethodDescriptionTextArea.setWidth("400px");
        crossingMethodDescriptionTextArea.setRows(2);
        crossingMethodDescriptionTextArea.addStyleName("mytextarea");
        crossingMethodDescriptionTextArea.setReadOnly(true);
                   
        crossingMethodComboBox = new ComboBox();
        crossingMethodComboBox.setWidth("280px");
        crossingMethodComboBox.setEnabled(false);
        crossingMethodComboBox.setImmediate(true);
        crossingMethodComboBox.setNullSelectionAllowed(false);
        // Change ComboBox back to TextField when it loses focus
        
        crossingMethodComboBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 6294894800193942274L;

			@Override
		    public void valueChange(ValueChangeEvent event) {
			if(crossingMethodComboBox.size() > 0){
	        		Integer breedingMethodSelected = mapMethods.get(event.getProperty().getValue());
	        		try {
	        		    String methodDescription=germplasmDataManager.getMethodByID(breedingMethodSelected).getMdesc();
	        		    crossingMethodDescriptionTextArea.setReadOnly(false);
	        		    crossingMethodDescriptionTextArea.setValue(methodDescription);
	        		    crossingMethodDescriptionTextArea.setReadOnly(true);
	     
	        		} catch (MiddlewareQueryException e) {
	        		    e.printStackTrace();
	        		}
			}
			 
		    }
		});

        favoriteMethodsCheckbox = new CheckBox(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
        favoriteMethodsCheckbox.setImmediate(true);
        favoriteMethodsCheckbox.setEnabled(false);
        favoriteMethodsCheckbox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				resetMethodTextArea();
				populateBreedingMethods(((Boolean) event.getProperty().getValue()));
			}
			
		});
        
        //layout components
        addComponent(selectOptionLabel, "top:20px;left:0px");
        addComponent(crossingMethodOptionGroup, "top:30px;left:0px");
        addComponent(selectCrossingMethodLabel, "top:100px;left:0px");
        addComponent(crossingMethodComboBox, "top:80px;left:155px");
        addComponent(favoriteMethodsCheckbox, "top:84px;left:435px");
        addComponent(methodDescriptionLabel, "top:130px;left:0px");
        addComponent(crossingMethodDescriptionTextArea, "top:110px;left:150px");
        
        methods = germplasmDataManager.getMethodsByType("GEN");
    }

	private void resetMethodTextArea() {
		crossingMethodDescriptionTextArea.setReadOnly(false);
		crossingMethodDescriptionTextArea.setValue("");
		crossingMethodDescriptionTextArea.setReadOnly(true);
	}

    public void populateBreedingMethod(){

    	crossingMethodComboBox.removeAllItems();
        mapMethods = new HashMap<String, Integer>();
        if (this.container != null && this.container.getCrossesMade() != null && 
                this.container.getCrossesMade().getCrossingManagerUploader() !=null){

            ImportedGermplasmCrosses importedCrosses = this.container.getCrossesMade().getCrossingManagerUploader().getImportedGermplasmCrosses();
            String breedingMethod = importedCrosses.getImportedConditionValue(TemplateCrossingCondition.BREEDING_METHOD.getValue());
            String beedingMethodId = importedCrosses.getImportedConditionValue(TemplateCrossingCondition.BREEDING_METHOD_ID.getValue());
            if(breedingMethod.length() > 0 && beedingMethodId.length() > 0){
                crossingMethodComboBox.addItem(breedingMethod);
                mapMethods.put(breedingMethod, Integer.valueOf(beedingMethodId));
                crossingMethodComboBox.select(breedingMethod);
            }else{
                crossingMethodComboBox.select("");
            }
        
        }
    
        for (Method m : methods) {
            crossingMethodComboBox.addItem(m.getMname());
            mapMethods.put(m.getMname(), new Integer(m.getMid()));
        }
        
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    private void populateBreedingMethods(boolean showOnlyFavorites) {
        crossingMethodComboBox.removeAllItems();

        mapMethods = new HashMap<String, Integer>();

        if(showOnlyFavorites){
        	populateWithFavoriteMethods();	
        } else {
        	populateBreedingMethod();
        }

    }

    
    private void populateWithFavoriteMethods() {
    	
    	crossingMethodComboBox.removeAllItems();
    	
        favoriteMethodIds = new ArrayList<Integer>();
        favoriteMethods = new ArrayList<Method>();
         
		try {
			Integer workbenchUserId;
			
			workbenchUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
	        User workbenchUser = workbenchDataManager.getUserById(workbenchUserId);
	        List<Project> userProjects = workbenchDataManager.getProjectsByUser(workbenchUser);
	        
	        //Get Method IDs
	        for(Project userProject : userProjects){
	        	favoriteMethodIds.addAll(workbenchDataManager.getFavoriteProjectMethods(userProject, 0, 10000));
	        }
	        
	        //Get Methods
	        if (!favoriteMethodIds.isEmpty()){
	        	favoriteMethods = germplasmDataManager.getMethodsByIDs(favoriteMethodIds);
	        }
	        
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

		for(Method favoriteMethod : favoriteMethods){
			crossingMethodComboBox.addItem(favoriteMethod.getMname());
	        mapMethods.put(favoriteMethod.getMname(), favoriteMethod.getMid());
		}
		
    }
    
        
    @Override
    public void updateLabels() {
        messageSource.setCaption(selectOptionLabel, Message.SELECT_AN_OPTION);
        messageSource.setCaption(selectCrossingMethodLabel, Message.SELECT_CROSSING_METHOD);
        messageSource.setCaption(methodDescriptionLabel, Message.METHOD_DESCRIPTION_LABEL);
    }
    
    private boolean sameBreedingMethodForAllSelected(){
        CrossingMethodOption option = (CrossingMethodOption) crossingMethodOptionGroup.getValue();
        return CrossingMethodOption.SAME_FOR_ALL_CROSSES.equals(option);
    }
    
    private boolean validateBreedingMethod(){
        if (sameBreedingMethodForAllSelected()){
            return BreedingManagerUtil.validateRequiredField(getWindow(), crossingMethodComboBox, messageSource, 
                    messageSource.getMessage(Message.CROSSING_METHOD));
        }
        return true;
    }

    @Override
    public boolean updateCrossesMadeContainer() {
        
        if (this.container != null && this.container.getCrossesMade() != null && 
                this.container.getCrossesMade().getCrossesMap()!= null && validateBreedingMethod()){
            
            //Use same breeding method for all crosses
            if (sameBreedingMethodForAllSelected()){
                Integer breedingMethodSelected = mapMethods.get(crossingMethodComboBox.getValue());
                for (Germplasm germplasm : container.getCrossesMade().getCrossesMap().keySet()){
                    germplasm.setMethodId(breedingMethodSelected);
                }
            
            // Use CrossingManagerUtil to set breeding method based on parents    
            } else {
                for (Germplasm germplasm : container.getCrossesMade().getCrossesMap().keySet()){
                    Integer femaleGid = germplasm.getGpid1();
                    Integer maleGid = germplasm.getGpid2();
                    
                    try {
                    	Germplasm female = germplasmDataManager.getGermplasmByGID(femaleGid);
                    	Germplasm male = germplasmDataManager.getGermplasmByGID(maleGid);
                    	
                    	Germplasm motherOfFemale = null;
                    	Germplasm fatherOfFemale = null;
                    	if(female != null){
                    		motherOfFemale = germplasmDataManager.getGermplasmByGID(female.getGpid1());
                    		fatherOfFemale = germplasmDataManager.getGermplasmByGID(female.getGpid2());
                    	}
                    	
                    	Germplasm motherOfMale = null;
                    	Germplasm fatherOfMale = null;
                    	if(male != null){
                    		motherOfMale = germplasmDataManager.getGermplasmByGID(male.getGpid1());
                    		fatherOfMale = germplasmDataManager.getGermplasmByGID(male.getGpid2());
                    	}
                    	CrossingManagerUtil.setCrossingBreedingMethod(germplasm, female, male, motherOfFemale, fatherOfFemale, motherOfMale, fatherOfMale);	
                    } catch (MiddlewareQueryException e) {
                        LOG.error(e.toString() + "\n" + e.getStackTrace());
                        e.printStackTrace();
                        MessageNotifier.showError(getWindow(), 
                                messageSource.getMessage(Message.ERROR_DATABASE),
                                messageSource.getMessage(Message.ERROR_IN_GETTING_BREEDING_METHOD_BASED_ON_PARENTAL_LINES),
                                Notification.POSITION_CENTERED);
                        return false;
                    }
                
                }
            }
            return true;
            
        }
        
        return false;
    }
    

}
