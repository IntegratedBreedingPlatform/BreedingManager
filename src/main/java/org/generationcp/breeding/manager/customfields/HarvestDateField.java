package org.generationcp.breeding.manager.customfields;

import java.text.DateFormatSymbols;
import java.util.Date;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.HorizontalLayout;

public class HarvestDateField extends HorizontalLayout {
	
	BreedingManagerYearField harvestYear;
	BreedingManagerMonthField harvestMonth;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	public HarvestDateField(Integer year, String caption){
		super();
		setCaption(caption);
		
		initializeValues(year);
		layoutComponent();
	}
	
	private void initializeValues(Integer year) {
		harvestYear = new BreedingManagerYearField(year);
		harvestYear.setWidth("90px");
		
		harvestMonth = new BreedingManagerMonthField();
		harvestMonth.setWidth("120px");
	}
	
	private void layoutComponent(){
		setSpacing(true);
		addStyleName("mandatory-field");
		addComponent(harvestYear);
		addComponent(harvestMonth);
	}
	
	public void setValue(Long harvestDate){
		
		String harvestDateStr = String.valueOf(harvestDate);
		
		if(harvestDate ==  null){
			reset();
		}
		else{
			//set Month
			int month = Integer.valueOf(harvestDateStr.substring(4, 6));
			String monthString = new DateFormatSymbols().getMonths()[month - 1];
			harvestMonth.setValue(monthString);
			
			//set Year
			int year = Integer.valueOf(harvestDateStr.substring(0, 4));
			harvestYear.setValue(year);
		}
	}
	
	public Long getValue(){
		int year = Integer.valueOf(harvestYear.getValue().toString());
		
		String month = String.valueOf(harvestMonth.getMonthNo());
		if(month.length() == 1){
			month = "0" + month;
		}
		
		String dateValue = year+month+"00";
				
		return Long.valueOf(dateValue);
	}
	
	public void reset(){
		harvestYear.setValue(2014);
		harvestMonth.setValue("");
	}
	
	public void validate() {
		if(harvestYear.getValue().toString().equals("Year")){
			throw new InvalidValueException("Year is required.");
		}
	}
}
