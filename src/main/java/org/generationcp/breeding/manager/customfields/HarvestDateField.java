
package org.generationcp.breeding.manager.customfields;

import java.text.DateFormatSymbols;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.HorizontalLayout;

public class HarvestDateField extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	BreedingManagerYearField harvestYear;
	BreedingManagerMonthField harvestMonth;

	public HarvestDateField(Integer year, String caption) {
		super();
		this.setCaption(caption);

		this.initializeValues(year);
		this.layoutComponent();
	}

	private void initializeValues(Integer year) {
		this.harvestYear = new BreedingManagerYearField(year);
		this.harvestYear.setDebugId("harvestYear");
		this.harvestYear.setWidth("90px");

		this.harvestMonth = new BreedingManagerMonthField();
		this.harvestMonth.setDebugId("harvestMonth");
		this.harvestMonth.setWidth("120px");
	}

	private void layoutComponent() {
		this.setSpacing(true);
		this.addComponent(this.harvestYear);
		this.addComponent(this.harvestMonth);
	}

	public void setValue(String harvestDate) {

		if (harvestDate == null || harvestDate.length() == 0) {
			this.reset();
		} else {
			// is in the date format
			if (harvestDate.length() > 8) {
				this.setValueUsingDateString(harvestDate);
			} else {
				// set Month
				int month = Integer.valueOf(harvestDate.substring(4, 6));
				if (month >= 1 && month <= 12) {
					String monthString = new DateFormatSymbols().getMonths()[month - 1];
					this.harvestMonth.setValue(monthString);
				} else {
					this.harvestMonth.setValue("Month"); // default
				}

				// set Year
				int year = Integer.valueOf(harvestDate.substring(0, 4));
				this.harvestYear.setValue(year);
			}
		}
	}

	public void setValueUsingDateString(String harvestDate) {

		if (harvestDate == null) {
			this.reset();
		} else {
			// Date String to parse: 2016-02-01T07:52:14.109+08:00

			// set Month
			int month = Integer.valueOf(harvestDate.substring(5, 7));
			String monthString = new DateFormatSymbols().getMonths()[month - 1];
			this.harvestMonth.setValue(monthString);

			// set Year
			int year = Integer.valueOf(harvestDate.substring(0, 4));
			this.harvestYear.setValue(year);
		}
	}

	public String getValue() {
		int year = Integer.valueOf(this.harvestYear.getValue().toString());

		String month = String.valueOf(this.harvestMonth.getMonthNo());
		if (month.length() == 1) {
			month = "0" + month;
		}

		String dateValue = year + month + "00";

		return dateValue;
	}

	public void reset() {
		this.harvestYear.setValue(2014);
		this.harvestMonth.setValue("");
	}

	public void validate() {
		if (this.harvestYear.getValue().toString().equals("Year")) {
			throw new InvalidValueException("Year is required.");
		}
	}
}
