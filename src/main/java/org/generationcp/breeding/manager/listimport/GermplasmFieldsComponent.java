
package org.generationcp.breeding.manager.listimport;

import java.text.MessageFormat;
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
public class GermplasmFieldsComponent extends AbsoluteLayout
		implements InternationalizableComponent, InitializingBean, BreedingManagerLayout, BreedingLocationFieldSource {

	private static final long serialVersionUID = -1180999883774074687L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmFieldsComponent.class);

	private static final String DEFAULT_NAME_TYPE = "Line Name";

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

	private Window parentWindow;

	private int leftIndentPixels = 130;

	private boolean hasInventoryVariable = false;

	private boolean hasInventoryAmount = false;

	private static final Integer STORAGE_LOCATION_TYPEID = 1500;

	private String programUniqueId;

	public GermplasmFieldsComponent(final Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
	}

	public GermplasmFieldsComponent(final Window parentWindow, final int pixels) {
		super();
		this.parentWindow = parentWindow;
		this.leftIndentPixels = pixels;
	}

	public GermplasmFieldsComponent(final int pixels) {
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
		this.addGermplasmDetailsLabel.setDebugId("addGermplasmDetailsLabel");
		this.addGermplasmDetailsLabel.addStyleName(Bootstrap.Typography.H4.styleName());

		this.addGermplasmDetailsMessage = new Label();
		this.addGermplasmDetailsMessage.setDebugId("addGermplasmDetailsMessage");
		this.addGermplasmDetailsMessage.setValue(this.getGermplasmDetailsInstructions());

		if (this.parentWindow != null) {
			this.methodComponent = new BreedingMethodField(this.parentWindow, 200, false, false);
			this.methodComponent.setDebugId("methodComponent");
		} else {
			this.methodComponent = new BreedingMethodField(200, false, false);
			this.methodComponent.setDebugId("methodComponent");
		}
		this.methodComponent.setCaption(this.messageSource.getMessage(Message.GERMPLASM_BREEDING_METHOD_LABEL) + ":");

		if (this.parentWindow != null) {
			this.locationComponent = new BreedingLocationField(this, this.parentWindow, 200, true);
			this.locationComponent.setDebugId("locationComponent");
		} else {
			this.locationComponent = new BreedingLocationField(this, 200, true);
			this.locationComponent.setDebugId("locationComponent");
		}
		this.locationComponent.setCaption(this.messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL) + ":");

		if (this.parentWindow != null) {
			this.seedLocationComponent =
					new BreedingLocationField(this, this.parentWindow, 200, GermplasmFieldsComponent.STORAGE_LOCATION_TYPEID, false);
		} else {
			this.seedLocationComponent = new BreedingLocationField(this, 200, GermplasmFieldsComponent.STORAGE_LOCATION_TYPEID, false);
			this.seedLocationComponent.setDebugId("seedLocationComponent");
		}
		this.seedLocationComponent.setCaption(this.messageSource.getMessage(Message.SEED_STORAGE_LOCATION_LABEL) + ":");

		this.germplasmDateLabel = new Label(this.messageSource.getMessage(Message.GERMPLASM_DATE_LABEL) + ":");
		this.germplasmDateLabel.setDebugId("germplasmDateLabel");
		this.germplasmDateLabel.addStyleName(CssStyles.BOLD);

		this.germplasmDateField = new BmsDateField();
		this.germplasmDateField.setDebugId("germplasmDateField");
		this.germplasmDateField.setValue(new Date());

		this.nameTypeLabel = new Label(this.messageSource.getMessage(Message.GERMPLASM_NAME_TYPE_LABEL) + ":");
		this.nameTypeLabel.setDebugId("nameTypeLabel");
		this.nameTypeLabel.addStyleName(CssStyles.BOLD);
		this.nameTypeComboBox = new ComboBox();
		this.nameTypeComboBox.setDebugId("nameTypeComboBox");
		this.nameTypeComboBox.setWidth("400px");
		this.nameTypeComboBox.setNullSelectionAllowed(false);
		this.nameTypeComboBox.setImmediate(true);

		this.programUniqueId = this.contextUtil.getCurrentProgramUUID();

	}

	String getGermplasmDetailsInstructions() {
		final StringBuilder sb = new StringBuilder(this.messageSource.getMessage(Message.SPECIFY_DETAILS_FOR_IMPORTED_GERMPLASM));
		if (this.hasInventoryAmount) {
			final String seedStorageRequiredMsg =
					MessageFormat.format(this.messageSource.getMessage(Message.SEED_STORAGE_REQUIRED_WHEN_INVENTORY_IS_PRESENT),
							this.messageSource.getMessage(Message.SEED_STORAGE_LOCATION_LABEL));
			sb.append(seedStorageRequiredMsg);
		} else {
			sb.append(this.messageSource.getMessage(Message.DETAILS_ARE_OPTIONAL));
		}
		return sb.toString();
	}

	@Override
	public void initializeValues() {

		try {
			this.populateNameTypes();
		} catch (final MiddlewareQueryException e) {
			GermplasmFieldsComponent.LOG.error(e.getMessage(), e);
		}

	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		if (this.hasInventoryVariable) {
			this.setHeight("360px");
		} else {
			this.setHeight("300px");
		}

		if (this.hasInventoryAmount) {
			this.addComponent(this.addGermplasmDetailsLabel, "top:0px;left:0px");
			this.addComponent(this.addGermplasmDetailsMessage, "top:32px;left:0px");
		} else {
			this.addComponent(this.addGermplasmDetailsLabel, "top:10px;left:0px");
			this.addComponent(this.addGermplasmDetailsMessage, "top:45px;left:0px");
		}

		this.addComponent(this.methodComponent, "top:85px;left:0px");
		this.addComponent(this.locationComponent, "top:145px;left:0px");

		if (this.hasInventoryVariable) {
			this.addComponent(this.seedLocationComponent, "top:225px;left:0px");

			this.addComponent(this.germplasmDateLabel, "top:300px;left:0px");
			this.addComponent(this.germplasmDateField, "top:300px;left:" + this.getLeftIndentPixels() + "px");

			this.addComponent(this.nameTypeLabel, "top:330px;left:0px");
			this.addComponent(this.nameTypeComboBox, "top:330px;left:" + this.getLeftIndentPixels() + "px");

		} else {
			this.addComponent(this.germplasmDateLabel, "top:230px;left:0px");
			this.addComponent(this.germplasmDateField, "top:225px;left:" + this.getLeftIndentPixels() + "px");

			this.addComponent(this.nameTypeLabel, "top:265px;left:0px");
			this.addComponent(this.nameTypeComboBox, "top:260px;left:" + this.getLeftIndentPixels() + "px");
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

	public Label getGermplasmDetailsMessage() {
		return this.addGermplasmDetailsMessage;
	}

	protected void populateNameTypes() {
		final List<UserDefinedField> userDefinedFieldList = this.germplasmListManager.getGermplasmNameTypes();
		Integer firstId = null;
		boolean hasDefault = false;
		for (final UserDefinedField userDefinedField : userDefinedFieldList) {
			if (firstId == null) {
				firstId = userDefinedField.getFldno();
			}
			this.nameTypeComboBox.addItem(userDefinedField.getFldno());
			this.nameTypeComboBox.setItemCaption(userDefinedField.getFldno(), userDefinedField.getFname());
			if (GermplasmFieldsComponent.DEFAULT_NAME_TYPE.equalsIgnoreCase(userDefinedField.getFname())) {
				this.nameTypeComboBox.setValue(userDefinedField.getFldno());
				hasDefault = true;
			}
		}
		if (!hasDefault && firstId != null) {
			this.nameTypeComboBox.setValue(firstId);
		}
	}

	public void setGermplasmBreedingMethod(final String breedingMethod) {
		this.getBreedingMethodComboBox().setNullSelectionAllowed(false);
		this.getBreedingMethodComboBox().addItem(breedingMethod);
		this.getBreedingMethodComboBox().setValue(breedingMethod);
	}

	public void setGermplasmDate(final Date germplasmDate) throws ParseException {
		this.germplasmDateField.setValue(germplasmDate);
	}

	public void setGermplasmLocation(final String germplasmLocation) {
		this.getLocationComboBox().setNullSelectionAllowed(false);
		this.getLocationComboBox().addItem(germplasmLocation);
		this.getLocationComboBox().setValue(germplasmLocation);
	}

	public void setGermplasmListType(final String germplasmListType) {
		this.nameTypeComboBox.setNullSelectionAllowed(false);
		this.nameTypeComboBox.addItem(germplasmListType);
		this.nameTypeComboBox.setValue(germplasmListType);
	}

	protected int getLeftIndentPixels() {
		return this.leftIndentPixels;
	}

	public void refreshLayout(final boolean hasInventoryVariable, final boolean hasInventoryAmount) {
		this.hasInventoryVariable = hasInventoryVariable;
		this.hasInventoryAmount = hasInventoryAmount;

		this.addGermplasmDetailsMessage.setValue(this.getGermplasmDetailsInstructions());
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

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setLocationComponent(final BreedingLocationField locationComponent) {
		this.locationComponent = locationComponent;
	}

	public void setSeedLocationComponent(final BreedingLocationField seedLocationComponent) {
		this.seedLocationComponent = seedLocationComponent;
	}

	public void setHasInventoryAmount(final boolean hasInventoryAmount) {
		this.hasInventoryAmount = hasInventoryAmount;
	}
}
