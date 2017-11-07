package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

public class FillWithAttributeButtonClickListener implements ClickListener {

	private static final long serialVersionUID = -3199718433711032406L;

	private final AddColumnSource addColumnSource;
	private final ComboBox attributeBox;
	private String targetPropertyId;
	private GermplasmColumnValuesGenerator valuesGenerator;
	private Boolean isFromGermplasmSearchWindow;

	public FillWithAttributeButtonClickListener(final AddColumnSource addColumnSource, final ComboBox attributeBox,
			final String targetPropertyId, final Boolean isFromGermplasmSearchWindow) {
		super();
		this.addColumnSource = addColumnSource;
		this.attributeBox = attributeBox;
		this.targetPropertyId = targetPropertyId;
		this.valuesGenerator = new GermplasmColumnValuesGenerator(this.addColumnSource);
		this.isFromGermplasmSearchWindow = isFromGermplasmSearchWindow;
	}

	@Override
	public void buttonClick(final ClickEvent event) {
		final Integer attributeTypeId = (Integer) this.attributeBox.getValue();
		if (attributeTypeId != null) {
			final String attributeType = this.attributeBox.getItemCaption(attributeTypeId).toUpperCase();
			String finalProperty = this.targetPropertyId;
			// Add selected attribute type as column if no existing property was
			// specified
			if (finalProperty == null) {
				this.addColumnSource.addColumn(attributeType);
				finalProperty = attributeType;
			}

			// The generation of the values of the target column in germplasm
			// search window is handled in another class.
			if (!this.isFromGermplasmSearchWindow) {
				// Generate values for target column
				this.valuesGenerator.fillWithAttribute(attributeTypeId, finalProperty);
			}
		}

		// Close pop-up
		final Window attributeWindow = ((Button) event.getSource()).getWindow();
		attributeWindow.getParent().removeWindow(attributeWindow);
	}

	public void setValuesGenerator(final GermplasmColumnValuesGenerator valuesGenerator) {
		this.valuesGenerator = valuesGenerator;
	}

	public void setTargetPropertyId(final String targetPropertyId) {
		this.targetPropertyId = targetPropertyId;
	}

	public void setIsFromGermplasmSearchWindow(final Boolean isFromGermplasmSearchWindow) {
		this.isFromGermplasmSearchWindow = isFromGermplasmSearchWindow;
	}

}
