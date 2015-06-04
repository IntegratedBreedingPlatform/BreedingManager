
package org.generationcp.breeding.manager.customfields;

import com.vaadin.ui.ComboBox;

public class BreedingManagerMonthField extends ComboBox {

	private static final long serialVersionUID = 1L;

	String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
			"December"};

	public BreedingManagerMonthField() {
		super();
		this.initializeValues();

		this.setNullSelectionAllowed(false);
		this.setTextInputAllowed(false);

		this.setValue("Month");
	}

	public int getMonthNo() {
		String value = this.getValue().toString();

		if (value.equals("January")) {
			return 1;
		} else if (value.equals("February")) {
			return 2;
		} else if (value.equals("March")) {
			return 3;
		} else if (value.equals("April")) {
			return 4;
		} else if (value.equals("May")) {
			return 5;
		} else if (value.equals("June")) {
			return 6;
		} else if (value.equals("July")) {
			return 7;
		} else if (value.equals("August")) {
			return 8;
		} else if (value.equals("September")) {
			return 9;
		} else if (value.equals("October")) {
			return 10;
		} else if (value.equals("November")) {
			return 11;
		} else if (value.equals("December")) {
			return 12;
		} else {
			return 0;
		}
	}

	private void initializeValues() {
		this.addItem("Month");

		for (int i = 0; i < 12; i++) {
			this.addItem(this.months[i]);
		}
	}

}
