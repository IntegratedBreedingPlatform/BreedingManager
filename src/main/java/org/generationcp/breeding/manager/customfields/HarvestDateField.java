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
	
	public void setValue(String harvestDate){
				
		if(harvestDate ==  null || harvestDate.length() == 0){
			reset();
		}
		else{
			if(harvestDate.length() > 8){//is in the date format
				setValueUsingDateString(harvestDate);
			}
			else{
				//set Month
				int month = Integer.valueOf(harvestDate.substring(4, 6));
				if(month >= 1 && month <= 12){
					String monthString = new DateFormatSymbols().getMonths()[month-1];
					harvestMonth.setValue(monthString);
				}
				else{
					harvestMonth.setValue("Month"); //default
				}
				
				//set Year
				int year = Integer.valueOf(harvestDate.substring(0, 4));
				harvestYear.setValue(year);
			}
		}
	}
	
	public void setValueUsingDateString(String harvestDate){

		if(harvestDate ==  null){
			reset();
		}
		else{
			//Date String to parse: 2016-02-01T07:52:14.109+08:00
			
			//set Month
			int month = Integer.valueOf(harvestDate.substring(5, 7));
			String monthString = new DateFormatSymbols().getMonths()[month - 1];
			harvestMonth.setValue(monthString);
			
			//set Year
			int year = Integer.valueOf(harvestDate.substring(0, 4));
			harvestYear.setValue(year);
		}
	}
	
	public String getValue(){
		int year = Integer.valueOf(harvestYear.getValue().toString());
		
		String month = String.valueOf(harvestMonth.getMonthNo());
		if(month.length() == 1){
			month = "0" + month;
		}
		
		String dateValue = year+month+"00";
				
		return dateValue;
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
