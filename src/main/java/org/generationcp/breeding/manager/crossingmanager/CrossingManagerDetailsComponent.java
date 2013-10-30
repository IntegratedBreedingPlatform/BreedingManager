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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingManagerDetailsComponent extends AbsoluteLayout 
    implements InitializingBean, InternationalizableComponent, CrossesMadeContainer {
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerDetailsComponent.class);
    
    private CrossingManagerMain source;
    private Accordion accordion;
    
    private Component previousScreen;
    
    private Label germplasmListNameLabel;
    private Label germplasmListDescriptionLabel;
    private Label germplasmListTypeLabel;
    private Label germplasmListDateLabel;
    private TextField germplasmListName;
    private TextField germplasmListDescription;
    private ComboBox germplasmListType;
    private DateField germplasmListDate;
    private Label warnOnClickDone;
    private Button backButton;
    private Button doneButton;

    public static final String DONE_BUTTON_ID = "done button";
    public static final String BACK_BUTTON_ID = "back button";
    public static final String DEFAULT_GERMPLASM_LIST_TYPE = "F1";

    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private CrossesMade crossesMade;
        
    
    public CrossingManagerDetailsComponent(CrossingManagerMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
        
    }

    @Override
    public CrossesMade getCrossesMade() {
        return this.crossesMade;
    }


    @Override
    public void setCrossesMade(CrossesMade crossesMade) {
        this.crossesMade = crossesMade;
        
    }
    
    public void setPreviousScreen(Component backScreen){
            this.previousScreen = backScreen; 
    }

    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");

        germplasmListNameLabel = new Label();
        germplasmListDescriptionLabel = new Label();
        germplasmListTypeLabel = new Label();
        germplasmListDateLabel = new Label();
        germplasmListName = new TextField();
        germplasmListDescription = new TextField();
        germplasmListType = new ComboBox();
        germplasmListDate = new DateField();
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        doneButton = new Button();
        doneButton.setData(DONE_BUTTON_ID);
        warnOnClickDone = new Label();
        
        germplasmListName.setWidth("250px");
        germplasmListDescription.setWidth("250px");
        germplasmListType.setWidth("250px");
        germplasmListType.setNullSelectionAllowed(false);
        
        addComponent(germplasmListNameLabel, "top:45px; left:170px;");
        addComponent(germplasmListDescriptionLabel, "top:75px; left:170px;");
        addComponent(germplasmListTypeLabel, "top:105px; left:170px;");
        addComponent(germplasmListDateLabel, "top:135px; left:170px;");
        
        addComponent(germplasmListName, "top:25px; left:340px;");
        addComponent(germplasmListDescription, "top:55px; left:340px;");
        addComponent(germplasmListType, "top:85px; left:340px;");
        addComponent(germplasmListDate, "top:115px; left:340px;");
        
        addComponent(warnOnClickDone, "top:240px; left: 130px;");
        addComponent(backButton, "top:165px; left: 340px;");
        addComponent(doneButton, "top:165px; left: 410px;");
        
        germplasmListDate.setResolution(DateField.RESOLUTION_DAY);
        germplasmListDate.setDateFormat(CrossingManagerMain.DATE_FORMAT);
        germplasmListDate.setResolution(DateField.RESOLUTION_DAY);
        germplasmListDate.setValue(new Date());
        germplasmListDate.setWidth("250px");

        initializeListTypeComboBox();

        CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
        doneButton.addListener(listener);
        backButton.addListener(listener);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(germplasmListNameLabel, Message.GERMPLASM_LIST_NAME);
        messageSource.setCaption(germplasmListDescriptionLabel, Message.GERMPLASM_LIST_DESCRIPTION);
        messageSource.setCaption(germplasmListTypeLabel, Message.GERMPLASM_LIST_TYPE);
        messageSource.setCaption(germplasmListDateLabel, Message.GERMPLASM_LIST_DATE);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(doneButton, Message.DONE);
        messageSource.setCaption(warnOnClickDone, Message.CLICKING_ON_DONE_WOULD_MEAN_THE_LIST_LIST_ENTRIES_AND_GERMPLASM_RECORDS_WILL_BE_SAVED_IN_THE_DATABASE);
    }


    private void initializeListTypeComboBox() throws MiddlewareQueryException {
        List<UserDefinedField> germplasmListTypes = germplasmListManager.getGermplasmListTypes();
        for(int i = 0 ; i < germplasmListTypes.size() ; i++){
            UserDefinedField userDefinedField = germplasmListTypes.get(i);
            germplasmListType.addItem(userDefinedField.getFcode());
            germplasmListType.setItemCaption(userDefinedField.getFcode(), userDefinedField.getFname());
            if(DEFAULT_GERMPLASM_LIST_TYPE.equalsIgnoreCase(userDefinedField.getFcode())){
                germplasmListType.setValue(userDefinedField.getFcode());
            }
        }
    }

    @SuppressWarnings("serial")
    public void doneButtonClickAction() throws InternationalizableException{
    	
		Boolean proceedWithSave = true;
		
		try {
			Long matchingNamesCountOnLocal = germplasmListManager.countGermplasmListByName(((String) germplasmListName.getValue()).trim(), Operation.EQUAL, Database.LOCAL);
			Long matchingNamesCountOnCentral = germplasmListManager.countGermplasmListByName(((String) germplasmListName.getValue()).trim(), Operation.EQUAL, Database.CENTRAL);
			if(matchingNamesCountOnLocal>0 || matchingNamesCountOnCentral>0){
				getWindow().showNotification("There is already an existing germplasm list with that name","",Notification.TYPE_ERROR_MESSAGE);
				proceedWithSave = false;
			}
		} catch (MiddlewareQueryException e) {
			
		}
    	
    	
        if (validateRequiredFields() && proceedWithSave){
            updateCrossesMadeContainer();
        
            saveRecords();
            
            //Bypass prompt
            //ConfirmDialog.show(this.getWindow(), messageSource.getMessage(Message.SAVE_CROSSES_MADE), 
            //    messageSource.getMessage(Message.CONFIRM_RECORDS_WILL_BE_SAVED_FOR_CROSSES_MADE), 
            //    messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL_LABEL), 
            //    new ConfirmDialog.Listener() {
            //        public void onClose(ConfirmDialog dialog) {
            //            if (dialog.isConfirmed()) {
            //            saveRecords();
            //            }
            //        }
            //    }
            //);
        }
    }
    
    //Save records into DB and redirects to GermplasmListBrowser to view created list
    private void saveRecords() {
        SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction();

        try {
            Integer listId = saveAction.saveRecords(crossesMade);
            MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
                    messageSource.getMessage(Message.CROSSES_SAVED_SUCCESSFULLY), 3000, Notification.POSITION_CENTERED);
            
            this.source.viewGermplasmListCreated(listId);
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage() + " " + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
                messageSource.getMessage(Message.ERROR_IN_SAVING_CROSSES_DEFINED), Notification.POSITION_CENTERED);
        }
        
    }
    
    //save GermplasmList info to CrossesMadeContainer
    private void updateCrossesMadeContainer(){
        String listName = ((String) germplasmListName.getValue()).trim();
        String listDescription = ((String) germplasmListDescription.getValue()).trim();
        SimpleDateFormat formatter = new SimpleDateFormat(CrossingManagerMain.DATE_AS_NUMBER_FORMAT);
        Date date = (Date)germplasmListDate.getValue();
        
        GermplasmList list = new GermplasmList();
        list.setName(listName);
        list.setDescription(listDescription);
        list.setDate(Long.parseLong(formatter.format(date)));
        list.setType((String) germplasmListType.getValue()); // value = fCOde
        list.setUserId(0);
        
        this.crossesMade.setGermplasmList(list);
    }

    
    private boolean validateRequiredFields(){
        return 
        BreedingManagerUtil.validateRequiredStringField(getWindow(), germplasmListName, 
            messageSource, (String) germplasmListNameLabel.getCaption())
        
        && BreedingManagerUtil.validateRequiredStringField(getWindow(), germplasmListDescription, 
            messageSource,     (String) germplasmListDescriptionLabel.getCaption())
            
        && BreedingManagerUtil.validateRequiredField(getWindow(), germplasmListType, 
            messageSource, (String) germplasmListTypeLabel.getCaption())
            
        && BreedingManagerUtil.validateRequiredField(getWindow(), germplasmListDate, 
            messageSource, (String) germplasmListDateLabel.getCaption());
    }

    
    public void backButtonClickAction(){
        source.enableWizardTabs();
        accordion.setSelectedTab(previousScreen);
    }


}
