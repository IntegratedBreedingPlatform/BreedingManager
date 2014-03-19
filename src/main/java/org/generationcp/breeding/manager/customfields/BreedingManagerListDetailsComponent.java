package org.generationcp.breeding.manager.customfields;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMain;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class BreedingManagerListDetailsComponent extends VerticalLayout 
implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = 1L;
	
	private Label newListLabel;
	private Panel containerPanel;
	private Label indicatesMandatoryLabel;
	private VerticalLayout containerLayout;
	
	//Fields
	private ListNameField listNameField;
	private ListDescriptionField listDescriptionField;
	private ListTypeField listTypeField;
	private ListDateField listDateField;
	private ListNotesField listNotesField;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private GermplasmList germplasmList;
	
	public BreedingManagerListDetailsComponent(){
		super();
	}
	
	public BreedingManagerListDetailsComponent(GermplasmList germplasmList){
		super();
		this.germplasmList = germplasmList;  
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
		newListLabel = new Label(messageSource.getMessage(Message.NEW_LIST_DETAILS));
		newListLabel.setStyleName(Bootstrap.Typography.H6.styleName());
		
		indicatesMandatoryLabel = new Label(messageSource.getMessage(Message.INDICATES_A_MANDATORY_FIELD));
		indicatesMandatoryLabel.addStyleName("italic");
		listNameField = new ListNameField(messageSource.getMessage(Message.LIST_NAME), true);
		listDescriptionField = new ListDescriptionField(messageSource.getMessage(Message.DESCRIPTION_LABEL), true);
		listTypeField = new ListTypeField(messageSource.getMessage(Message.LIST_TYPE), true);
		listDateField = new ListDateField(messageSource.getMessage(Message.LIST_DATE), true);
		listNotesField = new ListNotesField(messageSource.getMessage(Message.NOTES), false);
	}
	@Override
	public void initializeValues() {
		if(germplasmList != null){
			listNameField.setValue(germplasmList.getName());
			listDescriptionField.setValue(germplasmList.getDescription());
			listTypeField.setValue(germplasmList.getType());
			listDateField.setValue(germplasmList.getDate());
			listNotesField.setValue(germplasmList.getNotes());
		}
	}
	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutComponents() {
		containerLayout = new VerticalLayout();
		containerLayout.setSpacing(true);
		containerLayout.setMargin(true);
		containerLayout.addComponent(indicatesMandatoryLabel);
		containerLayout.addComponent(listNameField);
		containerLayout.addComponent(listDescriptionField);
		containerLayout.addComponent(listTypeField);
		containerLayout.addComponent(listDateField);
		containerLayout.addComponent(listNotesField);
		
		containerPanel = new Panel();
		containerPanel.setWidth("345px");
		containerPanel.setHeight("283px");
		containerPanel.setContent(containerLayout);
		
		
		setSpacing(true);
		
		addComponent(this.newListLabel);
		addComponent(containerPanel);
	}
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean validate(){
		try {
			
			listNameField.validate();
			listDescriptionField.validate();
			listTypeField.validate();
			listDateField.validate();

			return false;
			
		} catch (InvalidValueException e) {
			MessageNotifier.showError(getWindow(), 
					this.messageSource.getMessage(Message.INVALID_INPUT), 
					e.getMessage(), Notification.POSITION_CENTERED);
			return false;
		}
	}
	
	public GermplasmList getGermplasmList(){
		String listName = listNameField.getValue().toString();
        String listDescription = listDescriptionField.getValue().toString();
        SimpleDateFormat formatter = new SimpleDateFormat(CrossingManagerMain.DATE_AS_NUMBER_FORMAT);
        Date date = (Date) listDateField.getValue();
        
        GermplasmList list = new GermplasmList();
        
        list.setName(listName);
        list.setDescription(listDescription);
        list.setType(listTypeField.getValue().toString()); // value = fCOde
        list.setDate(Long.parseLong(formatter.format(date)));
        list.setNotes(listNotesField.getValue().toString());
        list.setUserId(0);
        //list.setParent((GermplasmList) getFolderToSaveListToLabel().getData());
        return list;
	}

}
