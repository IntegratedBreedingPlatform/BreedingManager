
package org.generationcp.breeding.manager.customfields;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListTypeField extends HorizontalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 4506866031376540836L;
	private final static Logger LOG = LoggerFactory.getLogger(ListTypeField.class);

	private Label captionLabel;
	private final String caption;
	private ComboBox listTypeComboBox;
	private final boolean isMandatory;
	private Label mandatoryMark;
	private final String DEFAULT_LIST_TYPE = "LST";
	private boolean changed;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public ListTypeField(String caption, boolean isMandatory) {
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}

	@Override
	public void instantiateComponents() {
		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.listTypeComboBox = new ComboBox();
		this.listTypeComboBox.setDebugId("listTypeComboBox");
		this.listTypeComboBox.setWidth("180px");
		this.listTypeComboBox.setImmediate(true);

		if (this.isMandatory) {
			this.mandatoryMark = new MandatoryMarkLabel();
			this.mandatoryMark.setDebugId("mandatoryMark");

			this.listTypeComboBox.setNullSelectionAllowed(false);
			this.listTypeComboBox.setRequired(true);
			this.listTypeComboBox.setRequiredError("Please specify the type of the list.");
		}
	}

	@Override
	public void initializeValues() {
		try {
			// initialize List Type ComboBox
			this.populateListType(this.listTypeComboBox);
		} catch (MiddlewareQueryException e) {
			ListTypeField.LOG.error("Error in retrieving List Type", e);
		}
	}

	private void populateListType(ComboBox selectType) {
		List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();

		for (UserDefinedField listType : listTypes) {
			String typeCode = listType.getFcode();
			if (!AppConstants.DB.FOLDER.equals(typeCode)) {
				selectType.addItem(typeCode);
				selectType.setItemCaption(typeCode, listType.getFname());
				// set "Germplasm List" as the default value
				if (this.DEFAULT_LIST_TYPE.equals(typeCode)) {
					selectType.setValue(typeCode);
				}
			}
		}
	}

	@Override
	public void addListeners() {
		this.listTypeComboBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2323698194362809907L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListTypeField.this.changed = true;
			}

		});
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		this.addComponent(this.captionLabel);

		if (this.isMandatory) {
			this.addComponent(this.mandatoryMark);
		}

		this.addComponent(this.listTypeComboBox);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public ComboBox getListTypeComboBox() {
		return this.listTypeComboBox;
	}

	public void setListTypeComboBox(ComboBox listTypeComboBox) {
		this.listTypeComboBox = listTypeComboBox;
	}

	public void setValue(String value) {
		this.listTypeComboBox.select(value);
	}

	public String getValue() {
		return (String) this.listTypeComboBox.getValue();
	}

	public String getDEFAULT_LIST_TYPE() {
		return this.DEFAULT_LIST_TYPE;
	}

	public void validate() {
		this.listTypeComboBox.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}
