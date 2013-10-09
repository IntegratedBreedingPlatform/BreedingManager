package org.generationcp.browser.cross.study.commons.trait.filter;

import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CharacterTraitsSection extends VerticalLayout implements InitializingBean, InternationalizableComponent{
	private static final long serialVersionUID = 9099796930978032454L;
	
	private static final String TRAIT_COLUMN_ID = "Trait Column";
	private static final String NUM_LOCATIONS_COLUMN_ID = "Number of Locations";
	private static final String NUM_LINES_COLUMN_ID = "Number of Lines";
	private static final String NUM_OBSERVATIONS_COLUMN_ID = "Number of Observations";
	private static final String DISTINCT_OBSERVED_VALUES_COLUMN_ID = "Distinct Observed Values";
	private static final String CONDITION_COLUMN_ID = "Condition";
	private static final String LIMITS_COLUMN_ID = "Limits";
	private static final String PRIORITY_COLUMN_ID = "Priority";
	
	private List<Integer> environmentIds;
	
	private Label lblSectionTitle;
	private Table traitsTable;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public CharacterTraitsSection(List<Integer> environmentIds){
		super();
		this.environmentIds = environmentIds;
	}

	private void initializeComponents(){
		lblSectionTitle = new Label(messageSource.getMessage(Message.CHARACTER_TRAITS_SECTION_TITLE));
		
		traitsTable = new Table();
		traitsTable.setImmediate(true);
		traitsTable.setColumnCollapsingAllowed(true);
		traitsTable.setColumnReorderingAllowed(true);
		
		traitsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
		traitsTable.addContainerProperty(NUM_LOCATIONS_COLUMN_ID, Integer.class, null);
		traitsTable.addContainerProperty(NUM_LINES_COLUMN_ID, Integer.class, null);
		traitsTable.addContainerProperty(NUM_OBSERVATIONS_COLUMN_ID, Integer.class, null);
		traitsTable.addContainerProperty(DISTINCT_OBSERVED_VALUES_COLUMN_ID, String.class, null);
		traitsTable.addContainerProperty(CONDITION_COLUMN_ID, ComboBox.class, null);
		traitsTable.addContainerProperty(LIMITS_COLUMN_ID, TextField.class, null);
		traitsTable.addContainerProperty(PRIORITY_COLUMN_ID, ComboBox.class, null);
		
		traitsTable.setColumnHeader(TRAIT_COLUMN_ID, messageSource.getMessage(Message.HEAD_TO_HEAD_TRAIT)); // Trait
		traitsTable.setColumnHeader(NUM_LOCATIONS_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_LOCATIONS)); // # of Locations
		traitsTable.setColumnHeader(NUM_LINES_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_LINES)); // # of Lines
		traitsTable.setColumnHeader(NUM_OBSERVATIONS_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_OBSERVATIONS)); // # of Observations
		traitsTable.setColumnHeader(DISTINCT_OBSERVED_VALUES_COLUMN_ID, messageSource.getMessage(Message.DISTINCT_OBSERVED_VALUES)); // Distinct Observed Values
		traitsTable.setColumnHeader(CONDITION_COLUMN_ID, messageSource.getMessage(Message.CONDITION_HEADER)); // Condition
		traitsTable.setColumnHeader(LIMITS_COLUMN_ID, messageSource.getMessage(Message.LIMITS)); // Limits
		traitsTable.setColumnHeader(PRIORITY_COLUMN_ID, messageSource.getMessage(Message.PRIORITY)); // Priority
	}
	
	private void initializeLayout(){
		this.addComponent(lblSectionTitle);
		this.addComponent(traitsTable);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeComponents();
		initializeLayout();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(lblSectionTitle, Message.CHARACTER_TRAITS_SECTION_TITLE);
	}
}
