
package org.generationcp.breeding.manager.customfields;

import com.vaadin.ui.ComboBox;

public class BreedingManagerYearField extends ComboBox {

	private static final long serialVersionUID = 1L;

	private static final Integer RANGE_INTERVAL_FROM_BASE_YEAR = 30;

	public BreedingManagerYearField(Integer year) {
		super();

		this.initializeValues(year);
		this.setValue(year);

		this.setNullSelectionAllowed(false);
		this.setTextInputAllowed(false);
	}

	private void initializeValues(Integer year) {
		this.addItem("Year");

		int minYear = year - BreedingManagerYearField.RANGE_INTERVAL_FROM_BASE_YEAR;
		int maxYear = year + BreedingManagerYearField.RANGE_INTERVAL_FROM_BASE_YEAR;

		for (int i = minYear; i <= maxYear; i++) {
			this.addItem(i);
		}
	}
}
