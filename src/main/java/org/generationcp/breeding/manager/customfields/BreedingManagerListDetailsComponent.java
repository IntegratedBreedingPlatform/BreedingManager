
package org.generationcp.breeding.manager.customfields;

import java.text.ParseException;
import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

@Configurable
public class BreedingManagerListDetailsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerListDetailsComponent.class);

	private String defaultListType;

	private Label headerListLabel;
	private Panel containerPanel;
	private Label indicatesMandatoryLabel;
	private VerticalLayout containerLayout;

	// Fields
	private ListNameField listNameField;
	private ListDescriptionField listDescriptionField;
	private ListTypeField listTypeField;
	private ListDateField listDateField;
	private ListNotesField listNotesField;
	private ListOwnerField listOwnerField;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private BreedingManagerService breedingManagerService;

	@Autowired
	private ContextUtil contextUtil;

	private GermplasmList germplasmList;

	public BreedingManagerListDetailsComponent() {
		super();
	}

	public BreedingManagerListDetailsComponent(GermplasmList germplasmList) {
		super();
		this.germplasmList = germplasmList;
	}

	public BreedingManagerListDetailsComponent(String defaultListType, GermplasmList germplasmList) {
		super();
		this.defaultListType = defaultListType;
		this.germplasmList = germplasmList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.headerListLabel = new Label();
		this.headerListLabel.setValue(this.messageSource.getMessage(Message.LIST_DETAILS));
		this.headerListLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		this.headerListLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.indicatesMandatoryLabel = new Label(this.messageSource.getMessage(Message.INDICATES_A_MANDATORY_FIELD));
		this.indicatesMandatoryLabel.addStyleName("italic");
		this.listNameField = new ListNameField(this.messageSource.getMessage(Message.LIST_NAME), true);
		this.listDescriptionField = new ListDescriptionField(this.messageSource.getMessage(Message.DESCRIPTION_LABEL), true);
		this.listTypeField = new ListTypeField(this.messageSource.getMessage(Message.LIST_TYPE), true);
		this.listDateField = new ListDateField(this.messageSource.getMessage(Message.LIST_DATE), true);
		this.listNotesField = new ListNotesField(this.messageSource.getMessage(Message.NOTES), false);
		this.listOwnerField = new ListOwnerField(this.messageSource.getMessage(Message.LIST_OWNER_LABEL), false);
	}

	@Override
	public void initializeValues() {
		this.setGermplasmListDetails(this.germplasmList);
	}

	@Override
	public void addListeners() {
		// do nothing

	}

	@Override
	public void layoutComponents() {
		this.containerLayout = new VerticalLayout();
		this.containerLayout.setSpacing(true);
		this.containerLayout.setMargin(true);
		this.containerLayout.addComponent(this.indicatesMandatoryLabel);
		this.containerLayout.addComponent(this.listNameField);
		this.containerLayout.addComponent(this.listOwnerField);
		this.containerLayout.addComponent(this.listDescriptionField);
		this.containerLayout.addComponent(this.listTypeField);
		this.containerLayout.addComponent(this.listDateField);
		this.containerLayout.addComponent(this.listNotesField);

		this.containerPanel = new Panel();
		this.containerPanel.setWidth("340px");
		this.containerPanel.setHeight("320px");
		this.containerPanel.setContent(this.containerLayout);

		this.setSpacing(false);

		this.headerListLabel.setHeight("26px");
		this.addComponent(this.headerListLabel);
		this.addComponent(this.containerPanel);
	}

	@Override
	public void updateLabels() {
		// do nothing

	}

	public boolean validate() {
		try {

			this.listNameField.validate();
			this.listDescriptionField.validate();
			this.listTypeField.validate();
			this.listDateField.validate();

			return true;

		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(this.getWindow(), e.getMessage());
			BreedingManagerListDetailsComponent.LOG.error(e.getMessage(), e);
			return false;
		}
	}

	public GermplasmList getGermplasmList() {
		String listName = this.listNameField.getValue().toString();
		String listDescription = this.listDescriptionField.getValue().toString();
		Date date = this.listDateField.getValue();

		GermplasmList list = new GermplasmList();

		list.setName(listName);

		if (listDescription != null) {
			list.setDescription(listDescription);
		}

		if (this.listTypeField != null && this.listTypeField.getValue() != null) {
			list.setType(this.listTypeField.getValue().toString());
		}

		if (date != null) {
			list.setDate(DateUtil.getCurrentDateAsLongValue());
		}
		list.setNotes(this.listNotesField.getValue().toString());
		list.setUserId(0);

		list.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

		return list;
	}

	public void setGermplasmListDetails(GermplasmList germplasmList) {
		this.germplasmList = germplasmList;

		if (germplasmList != null) {
			this.listNameField.setValue(germplasmList.getName());
			this.resetListNameFieldForExistingList(germplasmList);
			this.listDescriptionField.setValue(germplasmList.getDescription());
			this.listTypeField.setValue(germplasmList.getType());

			Date germplasmDate = new Date();
			try {
				germplasmDate = this.getParsedDate(germplasmList.getDate().toString());
			} catch (ReadOnlyException e) {
				BreedingManagerListDetailsComponent.LOG.error(e.getMessage(), e);
			} catch (ConversionException e) {
				BreedingManagerListDetailsComponent.LOG.error(e.getMessage(), e);
			} catch (ParseException e) {
				BreedingManagerListDetailsComponent.LOG.error(e.getMessage(), e);
			}
			this.listDateField.setValue(germplasmDate);

			String notes = germplasmList.getNotes() == null ? "" : germplasmList.getNotes();
			this.listNotesField.setValue(notes);
		} else {
			this.listNameField.setValue("");
			this.listDescriptionField.setValue("");
			this.listDateField.setValue(new Date());
			this.listTypeField.setValue(this.defaultListType);
			this.listNotesField.setValue("");
		}

		// set list owner
		String listOwner = this.getListOwnerValue(germplasmList);
		this.listOwnerField.setValue(listOwner);
	}

	protected String getListOwnerValue(GermplasmList germplasmList) {
		String listOwner = "";
		try {
			if (germplasmList != null) {
				listOwner = this.breedingManagerService.getOwnerListName(germplasmList.getUserId());
			} else {
				listOwner = this.breedingManagerService.getDefaultOwnerListName();
			}
		} catch (MiddlewareQueryException e) {
			BreedingManagerListDetailsComponent.LOG.error(e.getMessage(), e);
		}

		return listOwner;
	}

	protected Date getParsedDate(String dateToParse) throws ParseException {
		String finalDateToParse = dateToParse;
		if (finalDateToParse.length() < 8) {
			finalDateToParse = this.getParsableDateString(finalDateToParse);
		}
		return DateUtil.parseDate(finalDateToParse, DateUtil.DATE_AS_NUMBER_FORMAT);
	}

	protected String getParsableDateString(String dateToParse) {
		int dateLenght = dateToParse.length();

		StringBuilder parsedDate = new StringBuilder();
		for (int i = 1; i <= 8 - dateLenght; i++) {
			parsedDate.append("0");
		}
		parsedDate.append(dateToParse);

		return parsedDate.toString();
	}

	public void resetListNameFieldForExistingList(GermplasmList germplasmList) {
		ListNameValidator listNameValidator = this.getListNameField().getListNameValidator();
		if (germplasmList.getId() != null) {
			listNameValidator.setCurrentListName(germplasmList.getName());
		}

		GermplasmList parentList = germplasmList.getParent();
		if (parentList != null) {
			listNameValidator.setParentFolder(parentList.getName());
		}
	}

	public void resetFields() {
		this.listNameField.setValue("");
		this.listDescriptionField.setValue("");
		this.listTypeField.setValue(this.listTypeField.getDEFAULT_LIST_TYPE());
		this.listDateField.setValue(new Date());
		this.listNotesField.setValue("");

		this.setChanged(false);
	}

	public boolean isChanged() {
		if (this.listNameField.isChanged() || this.listDescriptionField.isChanged() || this.listTypeField.isChanged()
				|| this.listDateField.isChanged() || this.listNotesField.isChanged()) {
			return true;
		}
		return false;
	}

	public void setChanged(boolean changed) {
		// Reset Marked Changes
		this.listNameField.setChanged(changed);
		this.listDescriptionField.setChanged(changed);
		this.listTypeField.setChanged(changed);
		this.listDateField.setChanged(changed);
		this.listNotesField.setChanged(changed);
	}

	// SETTERS and GETTERS
	public Label getHeaderListLabel() {
		return this.headerListLabel;
	}

	public void setHeaderListLabel(String header) {
		this.headerListLabel.setValue(header);
	}

	public ListNameField getListNameField() {
		return this.listNameField;
	}

	public void setListNameField(ListNameField listNameField) {
		this.listNameField = listNameField;
	}

	public ListDescriptionField getListDescriptionField() {
		return this.listDescriptionField;
	}

	public void setListDescriptionField(ListDescriptionField listDescriptionField) {
		this.listDescriptionField = listDescriptionField;
	}

	public ListTypeField getListTypeField() {
		return this.listTypeField;
	}

	public void setListTypeField(ListTypeField listTypeField) {
		this.listTypeField = listTypeField;
	}

	public ListDateField getListDateField() {
		return this.listDateField;
	}

	public void setListDateField(ListDateField listDateField) {
		this.listDateField = listDateField;
	}

	public ListNotesField getListNotesField() {
		return this.listNotesField;
	}

	public void setListNotesField(ListNotesField listNotesField) {
		this.listNotesField = listNotesField;
	}

	public Panel getContainerPanel() {
		return this.containerPanel;
	}

	public GermplasmList getCurrentGermplasmList() {
		return this.germplasmList;
	}

	public void setBreedingManagerService(BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public ListOwnerField getListOwnerField() {
		return this.listOwnerField;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
