package org.generationcp.breeding.manager.crossingmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.dialog.SelectLocationFolderDialog;
import org.generationcp.breeding.manager.listmanager.dialog.SelectLocationFolderDialogSource;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.breeding.manager.validator.DateValidator;
import org.generationcp.breeding.manager.validator.ListDescriptionValidator;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.breeding.manager.validator.ListTypeValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveCrossListSubComponent extends AbsoluteLayout 
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, SelectLocationFolderDialogSource {

	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(SaveCrossListSubComponent.class);
	
	private String headerCaption;
	private String saveAsCaption;
	
	private Label headerLabel;
	private Label saveAsLabel;
	private Label descriptionLabel;
	private Label listTypeLabel;
	private Label listDateLabel;
	
	private HorizontalLayout saveAsLayout;
	private Label folderToSaveListToLabel;
	private Label listNameLabel;
	private TextField listNameTextField;
	private Button saveListNameButton;
	
	private TextArea descriptionTextArea;
	private ComboBox listTypeComboBox;
	private DateField listDtDateField;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	SelectLocationFolderDialog selectFolderDialog;
	
	public SaveCrossListSubComponent(String headerCaption, String saveAsCaption){
		this.headerCaption = headerCaption;
		this.saveAsCaption = saveAsCaption;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		
		headerLabel = new Label();
		headerLabel.setValue(headerCaption);
		headerLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		saveAsLabel = new Label();
		saveAsLabel.setValue(markAsMandatoryField(saveAsCaption));
		saveAsLabel.addStyleName("bold");
		
		descriptionLabel = new Label();
		descriptionLabel.setCaption(markAsMandatoryField(messageSource.getMessage(Message.DESCRIPTION_LABEL)));
		
		listTypeLabel = new Label();
		listTypeLabel.setCaption(markAsMandatoryField(messageSource.getMessage(Message.LIST_TYPE)));
		
		listDateLabel = new Label();
		listDateLabel.setCaption(messageSource.getMessage(Message.LIST_DATE) + ":");
		
		saveAsLayout = new HorizontalLayout();
		saveAsLayout.setSpacing(true);
		
		folderToSaveListToLabel = new Label();
		folderToSaveListToLabel.setData(null);
		folderToSaveListToLabel.addStyleName("not-bold");
		folderToSaveListToLabel.setVisible(false);
		
		listNameLabel = new Label();
		listNameLabel.addStyleName("bold");
		listNameLabel.setVisible(false);
		
		listNameTextField = new TextField();
		listNameTextField.setVisible(false);
		listNameTextField.addValidator(new ListNameValidator(folderToSaveListToLabel,listNameTextField));
		
		saveListNameButton = new Button();
		saveListNameButton.setWidth("100px");
		saveListNameButton.setCaption(messageSource.getMessage(Message.CHOOSE_LOCATION));
		saveListNameButton.setStyleName(Reindeer.BUTTON_LINK);
		
		saveAsLayout.addComponent(saveAsLabel);
		saveAsLayout.addComponent(folderToSaveListToLabel);
		saveAsLayout.addComponent(listNameLabel);
		saveAsLayout.addComponent(saveListNameButton);
		
		descriptionTextArea = new TextArea();
		descriptionTextArea.setWidth("260px");
		descriptionTextArea.setHeight("50px");
		descriptionTextArea.addValidator(new ListDescriptionValidator(descriptionTextArea));
		
		listTypeComboBox = new ComboBox();
		listTypeComboBox.addValidator(new ListTypeValidator(listTypeComboBox));
		
		listDtDateField = new DateField();
		listDtDateField.setDateFormat(CrossingManagerMain.DATE_FORMAT);
		listDtDateField.addValidator(new DateValidator(listDtDateField));
	}
	
	public String markAsMandatoryField(String label){
		return label + ": *";
	}

	@Override
	public void initializeValues() {

		try {
			// initialize List Type ComboBox
			populateListType(listTypeComboBox);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in retrieving List Type", e);
			e.printStackTrace();
		}
		
		listDtDateField.setValue(new Date());
	}
	
	private void populateListType(ComboBox selectType) throws MiddlewareQueryException {
        List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
        
        for (UserDefinedField listType : listTypes) {
            String typeCode = listType.getFcode();
            selectType.addItem(typeCode);
            selectType.setItemCaption(typeCode, listType.getFname());
            //set "F1 Nursery List" as the default value
            if ("F1".equals(typeCode)) {
                selectType.setValue(typeCode);
            }
        }
    }

	@Override
	public void addListeners() {
		saveListNameButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				displaySelectFolderDialog(); 
			}
			
		});
	}

	@Override
	public void layoutComponents() {
		setHeight("150px");
		setWidth("800px");
		
		addComponent(headerLabel,"top:0px;left:0px");
		
		addComponent(saveAsLayout,"top:48px;left:0px");
		
		addComponent(descriptionLabel,"top:98px;left:0px");
		addComponent(descriptionTextArea,"top:80px;left:120px");
		
		addComponent(listTypeLabel,"top:98px;left:400px");
		addComponent(listTypeComboBox,"top:80px;left:480px");
		
		addComponent(listDateLabel,"top:128px;left:400px");
		addComponent(listDtDateField,"top:110px;left:480px");
	}
	
	private void displaySelectFolderDialog(){
		GermplasmList selectedFolder = (GermplasmList) folderToSaveListToLabel.getData();
		
		if(saveListNameButton.getCaption().equals(messageSource.getMessage(Message.CHANGE))){
			selectFolderDialog.setListNameTxtField(listNameLabel.getValue().toString());
		}
		else{
			if(selectedFolder != null){
				selectFolderDialog = new SelectLocationFolderDialog(this, selectedFolder.getId(), true);
			} else{
				selectFolderDialog = new SelectLocationFolderDialog(this, null, true);
			}
		}
		
		this.getWindow().addWindow(selectFolderDialog);
	}

	@Override
	public void updateLabels() {
		
	}

	@Override
	public void setSelectedFolder(GermplasmList folder) {
		try{
			Deque<GermplasmList> parentFolders = new ArrayDeque<GermplasmList>();
	        GermplasmListTreeUtil.traverseParentsOfList(germplasmListManager, folder, parentFolders);
	        
	        StringBuilder locationFolderString = new StringBuilder();
	        locationFolderString.append("Program Lists");
	        
	        while(!parentFolders.isEmpty())
	        {
	        	locationFolderString.append(" > ");
	        	GermplasmList parentFolder = parentFolders.pop();
	        	locationFolderString.append(parentFolder.getName());
	        }
	        
	        if(folder != null){
	        	locationFolderString.append(" > ");
	        	locationFolderString.append(folder.getName());
	        }
	        
	        if(folder != null && folder.getName().length() >= 40){
	        	this.folderToSaveListToLabel.setValue(folder.getName().substring(0, 47));
	        } else if(locationFolderString.length() > 47){
	        	int lengthOfFolderName = folder.getName().length();
	        	this.folderToSaveListToLabel.setValue(locationFolderString.substring(0, (47 - lengthOfFolderName - 6)) + "... > " + folder.getName());
	        } else{
	        	this.folderToSaveListToLabel.setValue(locationFolderString.toString());
	        }
	        
	        this.folderToSaveListToLabel.setValue(folderToSaveListToLabel.getValue().toString() + " > ");
	        this.folderToSaveListToLabel.setDescription(locationFolderString.toString() + " > ");
	        this.folderToSaveListToLabel.setData(folder);
	        
	        this.folderToSaveListToLabel.setVisible(true);
	        this.listNameLabel.setVisible(true);
	        this.saveListNameButton.setCaption(messageSource.getMessage(Message.CHANGE));
	    } catch(MiddlewareQueryException ex){
			LOG.error("Error with traversing parents of list: " + folder.getId(), ex);
		}
	}

	public void setListName(String listName) {
		this.listNameLabel.setValue(listName);
		this.listNameTextField.setValue(listName);
	}

	public Label getFolderToSaveListToLabel() {
		return folderToSaveListToLabel;
	}

	public void setFolderToSaveListToLabel(Label folderToSaveListToLabel) {
		this.folderToSaveListToLabel = folderToSaveListToLabel;
	}
	
	public GermplasmList getGermplasmList(){
		String listName = listNameLabel.getValue().toString();
        String listDescription = descriptionTextArea.getValue().toString();
        SimpleDateFormat formatter = new SimpleDateFormat(CrossingManagerMain.DATE_AS_NUMBER_FORMAT);
        Date date = (Date) listDtDateField.getValue();
        
        GermplasmList list = new GermplasmList();
        
        list.setName(listName);
        list.setDescription(listDescription);
        list.setDate(Long.parseLong(formatter.format(date)));
        list.setType(listTypeComboBox.getValue().toString()); // value = fCOde
        list.setUserId(0);
        list.setParent((GermplasmList) getFolderToSaveListToLabel().getData());
        
        return list;
	}
	
	public boolean validateAllFields(){
		
		String section = headerLabel.getValue().toString();
		
		try {
			
			listNameTextField.validate();
			descriptionTextArea.validate();
			listTypeComboBox.validate();
			listDtDateField.validate();
			
			return false;
			
		} catch (InvalidValueException e) {
			MessageNotifier.showWarning(getWindow(), 
					this.messageSource.getMessage(Message.INVALID_INPUT), 
					section + ": " + e.getMessage(), Notification.POSITION_CENTERED);
			return false;
		}
	}
	
}
