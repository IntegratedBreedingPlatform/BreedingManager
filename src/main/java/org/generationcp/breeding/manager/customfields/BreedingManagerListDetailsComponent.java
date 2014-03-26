package org.generationcp.breeding.manager.customfields;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMain;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class BreedingManagerListDetailsComponent extends VerticalLayout 
implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerListDetailsComponent.class);
	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
	
	private Label headerListLabel;
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
		headerListLabel = new Label(messageSource.getMessage(Message.LIST_DETAILS));
		headerListLabel.setStyleName(Bootstrap.Typography.H6.styleName());
		
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
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
            try {
                this.listDateField.setValue(simpleDateFormat.parse(germplasmList.getDate().toString()));
            } catch (ReadOnlyException e) {
                LOG.error("Error in parsing date field.", e);
                e.printStackTrace();
            } catch (ConversionException e) {
                LOG.error("Error in parsing date field.", e);
                e.printStackTrace();
            } catch (ParseException e) {
                LOG.error("Error in parsing date field.", e);
                e.printStackTrace();
            }
            
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
		
		addComponent(this.headerListLabel);
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

			return true;
			
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

        return list;
	}
	
	public void setGermplasmListDetails(GermplasmList germplasmList){
		this.germplasmList = germplasmList;
		
		listNameField.setValue(germplasmList.getName());
		
		resetListNameFieldForExistingList(germplasmList);
		
		listDescriptionField.setValue(germplasmList.getDescription());
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
        try {
            this.listDateField.setValue(simpleDateFormat.parse(germplasmList.getDate().toString()));
        } catch (ReadOnlyException e) {
            LOG.error("Error in parsing date field.", e);
            e.printStackTrace();
        } catch (ConversionException e) {
            LOG.error("Error in parsing date field.", e);
            e.printStackTrace();
        } catch (ParseException e) {
            LOG.error("Error in parsing date field.", e);
            e.printStackTrace();
        }
		
		listTypeField.setValue(germplasmList.getType());
		listNotesField.setValue(germplasmList.getNotes());
	}
	
	public void resetListNameFieldForExistingList(GermplasmList germplasmList){
		ListNameValidator listNameValidator = listNameField.getListNameValidator();
		listNameValidator.setCurrentListName(germplasmList.getName());

		GermplasmList parentList = germplasmList.getParent();
		if(parentList != null){
			listNameValidator.setParentFolder(parentList.getName());
		}		
	}
	
	public void resetFields() {
		listNameField.setValue("");
		listDescriptionField.setValue("");
		listTypeField.setValue(listTypeField.getDEFAULT_LIST_TYPE());
		listDateField.setValue(new Date());
		listNotesField.setValue("");
	}

	//SETTERS and GETTERS
	public Label getHeaderListLabel() {
		return headerListLabel;
	}

	public void setHeaderListLabel(String header) {
		this.headerListLabel.setValue(header);
	}
	
	public ListNameField getListNameField() {
		return listNameField;
	}

	public void setListNameField(ListNameField listNameField) {
		this.listNameField = listNameField;
	}

	public ListDescriptionField getListDescriptionField() {
		return listDescriptionField;
	}

	public void setListDescriptionField(ListDescriptionField listDescriptionField) {
		this.listDescriptionField = listDescriptionField;
	}

	public ListTypeField getListTypeField() {
		return listTypeField;
	}

	public void setListTypeField(ListTypeField listTypeField) {
		this.listTypeField = listTypeField;
	}

	public ListDateField getListDateField() {
		return listDateField;
	}

	public void setListDateField(ListDateField listDateField) {
		this.listDateField = listDateField;
	}

	public ListNotesField getListNotesField() {
		return listNotesField;
	}

	public void setListNotesField(ListNotesField listNotesField) {
		this.listNotesField = listNotesField;
	}

	public Panel getContainerPanel(){
		return containerPanel;
	}
}
