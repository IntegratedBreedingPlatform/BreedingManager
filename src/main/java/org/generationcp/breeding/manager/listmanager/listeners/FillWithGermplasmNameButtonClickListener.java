package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

public class FillWithGermplasmNameButtonClickListener implements ClickListener {

	private static final long serialVersionUID = -3199718433711032406L;

	private final AddColumnSource addColumnSource;
	private final ComboBox nameTypesComboBox;
	private String targetPropertyId;
	private GermplasmColumnValuesGenerator valuesGenerator;
	private Boolean isFromGermplasmSearchWindow;

	public FillWithGermplasmNameButtonClickListener(final AddColumnSource addColumnSource, final ComboBox nameTypesComboBox,
			final String targetPropertyId, final Boolean isFromGermplasmSearchWindow) {
		super();
		this.addColumnSource = addColumnSource;
		this.nameTypesComboBox = nameTypesComboBox;
		this.targetPropertyId = targetPropertyId;
		this.valuesGenerator = new GermplasmColumnValuesGenerator(this.addColumnSource);
		this.isFromGermplasmSearchWindow = isFromGermplasmSearchWindow;
	}

	@Override
	public void buttonClick(final ClickEvent event) {
		final Integer nameTypeId = (Integer) this.nameTypesComboBox.getValue();
		if (nameTypeId != null) {
			final String nameType = this.nameTypesComboBox.getItemCaption(nameTypeId).toUpperCase();
			String finalProperty = this.targetPropertyId;
			// Add selected name type as column if no existing property was specified
			if (finalProperty == null) {
				this.addColumnSource.addColumn(nameType);
				finalProperty = nameType;
			}

			// The generation of the values of the target column in germplasm
			// search window is handled in another class.
			if (!this.isFromGermplasmSearchWindow) {
				// Generate values for target column
				this.valuesGenerator.fillWithGermplasmName(nameTypeId, finalProperty);
			}
		}

		// Close pop-up
		final Window fillWithNameTypesWindow = ((Button) event.getSource()).getWindow();
		fillWithNameTypesWindow.getParent().removeWindow(fillWithNameTypesWindow);
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
