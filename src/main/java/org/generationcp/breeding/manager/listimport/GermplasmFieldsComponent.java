
package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants.CssStyles;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.breeding.manager.customfields.BreedingLocationFieldSource;
import org.generationcp.breeding.manager.customfields.BreedingMethodField;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.fields.BmsDateField;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.ExecutionException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmFieldsComponent extends AbsoluteLayout implements InternationalizableComponent, InitializingBean,
		BreedingManagerLayout, BreedingLocationFieldSource {

	private static final long serialVersionUID = -1180999883774074687L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmFieldsComponent.class);

	private static final String DEFAULT_NAME_TYPE = "Line Name";
	public static final String ERROR_MESSAGE_CAPTION = "Error!";

	private Label addGermplasmDetailsLabel;
	private Label addGermplasmDetailsMessage;

	private BreedingMethodField methodComponent;
	private BreedingLocationField locationComponent;
	private BreedingLocationField seedLocationComponent;

	private Label germplasmDateLabel;
	private Label nameTypeLabel;

	private ComboBox nameTypeComboBox;

	private BmsDateField germplasmDateField;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private ContextUtil contextUtil;

	@Autowired
	private CodeNamesLocator codeNamesLocator;

	private Window parentWindow;

	private int leftIndentPixels = 130;

	private boolean hasInventoryAmounts = false;

	private static final Integer STORAGE_LOCATION_TYPEID = 1500;

	private String programUniqueId;

	public GermplasmFieldsComponent(Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
	}

	public GermplasmFieldsComponent(Window parentWindow, int pixels) {
		super();
		this.parentWindow = parentWindow;
		this.leftIndentPixels = pixels;
	}

	public GermplasmFieldsComponent(int pixels) {
		super();
		this.leftIndentPixels = pixels;
	}

	@Override
	public void afterPropertiesSet() {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void instantiateComponents() {
		this.addGermplasmDetailsLabel = new Label(this.messageSource.getMessage(Message.ADD_GERMPLASM_DETAILS).toUpperCase());
		this.addGermplasmDetailsLabel.addStyleName(Bootstrap.Typography.H4.styleName());

		this.addGermplasmDetailsMessage = new Label();
		this.addGermplasmDetailsMessage
				.setValue("You can specify following details to apply to the imported germplasm. These details are optional.");

		if (this.parentWindow != null) {
			this.methodComponent = new BreedingMethodField(this.parentWindow, 200, false, false);
		} else {
			this.methodComponent = new BreedingMethodField(200, false, false);
		}
		this.methodComponent.setCaption(this.messageSource.getMessage(Message.GERMPLASM_BREEDING_METHOD_LABEL) + ":");

		if (this.parentWindow != null) {
			this.locationComponent = new BreedingLocationField(this, this.parentWindow, 200);
		} else {
			this.locationComponent = new BreedingLocationField(this, 200);
		}
		this.locationComponent.setCaption(this.messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL) + ":");

		if (this.parentWindow != null) {
			this.seedLocationComponent =
					new BreedingLocationField(this, this.parentWindow, 200, GermplasmFieldsComponent.STORAGE_LOCATION_TYPEID);
		} else {
			this.seedLocationComponent = new BreedingLocationField(this, 200, GermplasmFieldsComponent.STORAGE_LOCATION_TYPEID);
		}
		this.seedLocationComponent.setCaption(this.messageSource.getMessage(Message.SEED_STORAGE_LOCATION_LABEL) + ":");

		this.germplasmDateLabel = new Label(this.messageSource.getMessage(Message.GERMPLASM_DATE_LABEL) + ":");
		this.germplasmDateLabel.addStyleName(CssStyles.BOLD);

		this.germplasmDateField = new BmsDateField();
		this.germplasmDateField.setValue(new Date());

		this.nameTypeLabel = new Label(this.messageSource.getMessage(Message.GERMPLASM_NAME_TYPE_LABEL) + ":");
		this.nameTypeLabel.addStyleName(CssStyles.BOLD);
		this.nameTypeComboBox = new ComboBox();
		this.nameTypeComboBox.setWidth("400px");
		this.nameTypeComboBox.setNullSelectionAllowed(false);
		this.nameTypeComboBox.setImmediate(true);

		this.programUniqueId = this.contextUtil.getCurrentProgramUUID();

	}

	@Override
	public void initializeValues() {

		try {
			this.populateNameTypes();
		} catch (MiddlewareQueryException e) {
			GermplasmFieldsComponent.LOG.error(e.getMessage(), e);
		}

	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		if (this.hasInventoryAmounts) {
			this.setHeight("330px");
		} else {
			this.setHeight("270px");
		}

		this.addComponent(this.addGermplasmDetailsLabel, "top:0px;left:0px");
		this.addComponent(this.addGermplasmDetailsMessage, "top:32px;left:0px");

		this.addComponent(this.methodComponent, "top:60px;left:0px");

		this.addComponent(this.locationComponent, "top:120px;left:0px");

		if (this.hasInventoryAmounts) {
			this.addComponent(this.seedLocationComponent, "top:180px;left:0px");

			this.addComponent(this.germplasmDateLabel, "top:245px;left:0px");
			this.addComponent(this.germplasmDateField, "top:240px;left:" + this.getLeftIndentPixels() + "px");

			this.addComponent(this.nameTypeLabel, "top:280px;left:0px");
			this.addComponent(this.nameTypeComboBox, "top:275px;left:" + this.getLeftIndentPixels() + "px");

		} else {
			this.addComponent(this.germplasmDateLabel, "top:185px;left:0px");
			this.addComponent(this.germplasmDateField, "top:180px;left:" + this.getLeftIndentPixels() + "px");

			this.addComponent(this.nameTypeLabel, "top:220px;left:0px");
			this.addComponent(this.nameTypeComboBox, "top:215px;left:" + this.getLeftIndentPixels() + "px");
		}
	}

	public ComboBox getBreedingMethodComboBox() {
		return this.methodComponent.getBreedingMethodComboBox();
	}

	public ComboBox getLocationComboBox() {
		return this.locationComponent.getBreedingLocationComboBox();
	}

	public ComboBox getSeedLocationComboBox() {
		return this.seedLocationComponent.getBreedingLocationComboBox();
	}

	public ComboBox getNameTypeComboBox() {
		return this.nameTypeComboBox;
	}

	public BmsDateField getGermplasmDateField() {
		return this.germplasmDateField;
	}

	public BreedingLocationField getLocationComponent() {
		return this.locationComponent;
	}

	public BreedingLocationField getSeedLocationComponent() {
		return this.seedLocationComponent;
	}

	protected void populateNameTypes() {
		List<UserDefinedField> userDefinedFieldList = null;
		try {
			userDefinedFieldList = codeNamesLocator.locateNonCodeNames();
			boolean hasDefault = false;
			for (UserDefinedField userDefinedField : userDefinedFieldList) {
				this.nameTypeComboBox.addItem(userDefinedField.getFldno());
				this.nameTypeComboBox.setItemCaption(userDefinedField.getFldno(), userDefinedField.getFname());
				if (GermplasmFieldsComponent.DEFAULT_NAME_TYPE.equalsIgnoreCase(userDefinedField.getFname())) {
					this.nameTypeComboBox.setValue(userDefinedField.getFldno());
					hasDefault = true;
				}
			}
			if (!hasDefault && userDefinedFieldList.size()>0) {
				this.nameTypeComboBox.setValue(userDefinedFieldList.get(0).getFldno());
			}
		} catch (ExecutionException e) {
			MessageNotifier.showError(this.getWindow(), ERROR_MESSAGE_CAPTION,e.getMessage());
		}


	}

	public void setGermplasmBreedingMethod(String breedingMethod) {
		this.getBreedingMethodComboBox().setNullSelectionAllowed(false);
		this.getBreedingMethodComboBox().addItem(breedingMethod);
		this.getBreedingMethodComboBox().setValue(breedingMethod);
	}

	public void setGermplasmDate(Date germplasmDate) throws ParseException {
		this.germplasmDateField.setValue(germplasmDate);
	}

	public void setGermplasmLocation(String germplasmLocation) {
		this.getLocationComboBox().setNullSelectionAllowed(false);
		this.getLocationComboBox().addItem(germplasmLocation);
		this.getLocationComboBox().setValue(germplasmLocation);
	}

	public void setGermplasmListType(String germplasmListType) {
		this.nameTypeComboBox.setNullSelectionAllowed(false);
		this.nameTypeComboBox.addItem(germplasmListType);
		this.nameTypeComboBox.setValue(germplasmListType);
	}

	protected int getLeftIndentPixels() {
		return this.leftIndentPixels;
	}

	public void refreshLayout(boolean hasInventoryAmount) {
		this.hasInventoryAmounts = hasInventoryAmount;

		this.removeAllComponents();
		this.layoutComponents();
		this.requestRepaint();
	}

	@Override
	public void updateAllLocationFields() {
		if (this.getLocationComboBox().getValue() != null) {
			this.getLocationComponent().populateHarvestLocation(Integer.valueOf(this.getLocationComboBox().getValue().toString()),
					this.programUniqueId);
		} else {
			this.getLocationComponent().populateHarvestLocation(null, this.programUniqueId);
		}

		if (this.getSeedLocationComboBox().getValue() != null) {
			this.getSeedLocationComponent().populateHarvestLocation(Integer.valueOf(this.getSeedLocationComboBox().getValue().toString()),
					this.programUniqueId);
		} else {
			this.getSeedLocationComponent().populateHarvestLocation(null, this.programUniqueId);
		}
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setCodeNamesLocator(CodeNamesLocator codeNamesLocator) {
		this.codeNamesLocator = codeNamesLocator;
	}
}
