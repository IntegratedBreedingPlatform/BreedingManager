package org.generationcp.browser.cross.study.adapted.main;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmValueChangeListener;
import org.generationcp.browser.cross.study.adapted.main.validators.NumericTraitLimitsValidator;
import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.NumericTraitInfo;
import org.generationcp.middleware.domain.h2h.TraitType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SetUpTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	public static final String PROCEED1_BUTTON_ID = "SetUpTraitFilter Numeric Apply Button ID";
	public static final String TRAIT_BUTTON_ID = "SetUpTraitFilter Trait Button ID";
    
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(SetUpTraitFilter.class);

	private static final int NUM_OF_SECTIONS = 3;
	private static final Message[] tabLabels = {Message.FIRST_SECTION, Message.SECOND_SECTION, Message.THIRD_SECTION};
	private static final Message[] tableLabels = {Message.GET_NUMERIC_VARIATES, Message.SECOND_SECTION, Message.THIRD_SECTION};
	private static final TraitType[] traitTypes = {TraitType.NUMERIC, TraitType.CHARACTER, TraitType.CATEGORICAL};
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;
	
	private VerticalLayout[] tabLayouts = new VerticalLayout[NUM_OF_SECTIONS];
	private Table[] traitTables = new Table[NUM_OF_SECTIONS];
	private Button[] proceedButtons = new Button[NUM_OF_SECTIONS];

	private TabSheet mainTabSheet;
	private Button firstProceedButton;
	private Button secondProceedButton;
	private Button thirdProceedButton;
	
	private List<EnvironmentForComparison> environmentsForComparisonList;
	private List<Integer> environmentIds;
	private List<Field> fieldsToValidate = new ArrayList<Field>();
	
	
	public SetUpTraitFilter(
			QueryForAdaptedGermplasmMain queryForAdaptedGermplasmMain,
			ResultsComponent screenThree) {
	}

	@Override
	public void updateLabels() {
		messageSource.setCaption(firstProceedButton, Message.APPLY_AND_PROCEED_TO_NEXT_SECTION);
		messageSource.setCaption(secondProceedButton, Message.APPLY_AND_PROCEED_TO_NEXT_SECTION);
		messageSource.setCaption(thirdProceedButton, Message.APPLY_AND_PROCEED_TO_NEXT_SECTION);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setHeight("550px");
        setWidth("1000px");	
        
        this.firstProceedButton = new Button();
        this.firstProceedButton.setData(PROCEED1_BUTTON_ID);
        this.firstProceedButton.addListener(new AdaptedGermplasmButtonClickListener(this));
        
        this.secondProceedButton = new Button();
        this.thirdProceedButton = new Button();

        proceedButtons[0] = this.firstProceedButton;
        proceedButtons[1] = this.secondProceedButton;
        proceedButtons[2] = this.thirdProceedButton;
        
	}
	
	public void createTraitsTabs() {		
		mainTabSheet = new TabSheet();
        for (int i = 0; i < NUM_OF_SECTIONS; i++){
        	VerticalLayout layout = new VerticalLayout();
        	layout.setSpacing(true);
        	layout.setMargin(true);
        	layout.setHeight("500px");
        	layout.setWidth("1000px");
        	
        	Table table = generateTraitTable(traitTypes[i]);
        	populateTable(table, traitTypes[i]);
        	
        	layout.addComponent(new Label(messageSource.getMessage(tableLabels[i])));
        	layout.addComponent(table);
        	HorizontalLayout buttonLayout = new HorizontalLayout();
        	buttonLayout.addComponent(proceedButtons[i]);
        	buttonLayout.setComponentAlignment(proceedButtons[i], Alignment.MIDDLE_RIGHT);
        	
        	layout.addComponent(buttonLayout);
        	layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
        	
        	mainTabSheet.addTab(layout, messageSource.getMessage(tabLabels[i]));
        	tabLayouts[i] = layout;
        	traitTables[i] = table;
        }
        
        addComponent(mainTabSheet, "top:20px");
	}

	public void populateTraitsTables(List<EnvironmentForComparison> environments) {
		this.environmentsForComparisonList = environments;
		this.environmentIds = new ArrayList<Integer>();
		for (EnvironmentForComparison envt : environments){
			this.environmentIds.add(envt.getEnvironmentNumber());
		}
		
		this.fieldsToValidate = new ArrayList<Field>();
		
		createTraitsTabs();	
	}

	
	private Table generateTraitTable(TraitType type) {
		Table table = new Table();
		table.setWidth("950px");
		table.setHeight("400px");
		table.setImmediate(true);
		table.setColumnCollapsingAllowed(true);
		table.setColumnReorderingAllowed(true);
		
		if (TraitType.NUMERIC.equals(type)){
			for (TableColumn column : TableColumn.getTableColumnsByType(type)){
				table.addContainerProperty(column, column.getColumnClass(), null);
				table.setColumnHeader(column, messageSource.getMessage(column.getMessage()));
				table.setColumnAlignment(column, Table.ALIGN_CENTER);
				table.setColumnWidth(column, column.getWidth());				
			}
		}
	
	    return table;
	}
	
	private void populateTable(Table table, TraitType type) {
		try {
			
			if (TraitType.NUMERIC.equals(type)){
				String limitsRequiredMessage = MessageFormat.format(
	                    messageSource.getMessage(Message.FIELD_IS_REQUIRED), 
	                    messageSource.getMessage(Message.LIMITS));
				List<NumericTraitInfo> numericTraits = crossStudyDataManager.getTraitsForNumericVariates(this.environmentIds);
				for (NumericTraitInfo trait : numericTraits){
					double minValue = trait.getMinValue();
					double maxValue = trait.getMaxValue();

					CheckBox box = new CheckBox();
					box.setImmediate(true);
					
					Button traitNameLink = new Button(trait.getName());
					traitNameLink.setImmediate(true);
					traitNameLink.setStyleName(Reindeer.BUTTON_LINK);
					traitNameLink.setData(TRAIT_BUTTON_ID);
					traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this,trait.getId(),trait.getName(),this.environmentIds));
					
					TextField limitsField = new TextField();
					limitsField.setWidth("80px");
					limitsField.setEnabled(false);
					limitsField.setImmediate(true);		
					limitsField.setRequired(true);
					limitsField.setRequiredError(limitsRequiredMessage);
					
					ComboBox conditionBox = CrossStudyUtil.getNumericTraitCombobox();
					conditionBox.setWidth("100px");
					ComboBox weightBox = CrossStudyUtil.getWeightComboBox();
					weightBox.setWidth("100px");

					box.addListener(new AdaptedGermplasmValueChangeListener(this, conditionBox, weightBox, limitsField));
					conditionBox.addListener(new AdaptedGermplasmValueChangeListener(this, limitsField, weightBox));
					limitsField.addValidator(new NumericTraitLimitsValidator(conditionBox, minValue, maxValue));
					this.fieldsToValidate.add(limitsField);
					
					Object[] itemObj = new Object[]{ box, traitNameLink, trait.getLocationCount(), trait.getGermplasmCount(), 
							trait.getObservationCount(), minValue, trait.getMedianValue(), maxValue,
							conditionBox, limitsField, weightBox};
		    		
					table.addItem(itemObj, trait);
				}	
				
			}
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			LOG.error("Database error!", e);
			MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
		}
		
	}
	
	
	public void toggleTrait(boolean selected, Component conditionCombobox, Component weightCombobox, Component textField){
		if (conditionCombobox != null && weightCombobox != null && textField != null){
			conditionCombobox.setEnabled(selected);
						
			if (!selected){
				textField.setEnabled(false);
				weightCombobox.setEnabled(false);
			} else {
				Object value = ((ComboBox)conditionCombobox).getValue();
				toggleDependentFields( value, textField, weightCombobox);
			}
		}
	}
	
	
	
	public void toggleDependentFields(Object value, Component textfield, Component weightCombobox){
		if (value != null && value instanceof NumericTraitCriteria && textfield != null){
			NumericTraitCriteria criteria = (NumericTraitCriteria) value;
			
			boolean doDisable = (NumericTraitCriteria.KEEP_ALL.equals (criteria) || NumericTraitCriteria.DROP_TRAIT.equals(criteria));
			textfield.setEnabled(!doDisable);
			
			toggleWeightCombobox(!NumericTraitCriteria.DROP_TRAIT.equals(criteria), weightCombobox);
		}
	}
	
	public void toggleWeightCombobox(boolean enabled, Component weightCombobox){
		weightCombobox.setEnabled(enabled);
		((ComboBox) weightCombobox).setValue(
				enabled? EnvironmentWeight.IMPORTANT : EnvironmentWeight.IGNORED);
	}
	
	// perform validations and if no error, proceed to next tab
	public void proceedButtonClickAction(int index){
		if (index <= NUM_OF_SECTIONS){
			Component nextTab = tabLayouts[index+1];
			
			if (nextTab != null){
				try {
					
					for (Field field : this.fieldsToValidate){
						if (field.isEnabled())
							field.validate();
					}
					mainTabSheet.setSelectedTab(nextTab);
					
				} catch (InvalidValueException e) {
					MessageNotifier.showWarning(getWindow(), 
							this.messageSource.getMessage(Message.INCORRECT_LIMITS_VALUE), 
							e.getMessage(), Notification.POSITION_CENTERED);
				}
			}
		}
	}
	

	private enum TableColumn {
		NUM_TAG_COL_ID (Message.HEAD_TO_HEAD_TAG, CheckBox.class, 25)
		, NUM_TRAIT_COL_ID (Message.HEAD_TO_HEAD_TRAIT, Button.class, 120)
		, NUM_NUMBER_OF_ENVTS_COL_ID (Message.NUMBER_OF_LOCATIONS, Integer.class, 60)
		, NUM_NUMBER_OF_LINES_COL_ID (Message.NUMBER_OF_LINES, Integer.class, 65)
		, NUM_NUMBER_OF_OBS_COL_ID (Message.NUMBER_OF_OBSERVATIONS, Integer.class, 60)
		, NUM_MIN_COL_ID (Message.MIN, Double.class, 40)
		, NUM_MEDIAN_COL_ID (Message.MEDIAN, Double.class, 50)
		, NUM_MAX_COL_ID(Message.MAX, Double.class, 50)
		, NUM_CONDITION_COL_ID (Message.CONDITION_HEADER, ComboBox.class, 105)
		, NUM_LIMITS_COL_ID (Message.LIMITS, TextField.class, 90)
		, NUM_PRIORITY_COL_ID (Message.PRIORITY, ComboBox.class, 105);
		
		private Message message;
		private Class<?> columnClass;
		private int width;
		private TableColumn(Message message, Class<?> columnClass, int width){
			this.message = message;
			this.columnClass = columnClass;
			this.width = width;
		}
		
		public Message getMessage() {
			return message;
		}
		
		public Class<?> getColumnClass() {
			return columnClass;
		}
		public int getWidth() {
			return width;
		}
		
		public static List<TableColumn> getTableColumnsByType(TraitType type){
			if (TraitType.NUMERIC.equals(type)){
				return Arrays.asList(NUM_TAG_COL_ID, NUM_TRAIT_COL_ID, NUM_NUMBER_OF_ENVTS_COL_ID, NUM_NUMBER_OF_ENVTS_COL_ID, 
						NUM_NUMBER_OF_LINES_COL_ID, NUM_NUMBER_OF_OBS_COL_ID, NUM_MIN_COL_ID, NUM_MEDIAN_COL_ID, 
						NUM_MAX_COL_ID, NUM_CONDITION_COL_ID, NUM_LIMITS_COL_ID, NUM_PRIORITY_COL_ID
						);
			}
			
			return null;
		}
	}
	
	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}

	public void showNumericVariateClickAction(Integer traitId, String traitName,
			List<Integer> envIds) {
		Window parentWindow = this.getWindow();
		parentWindow.addWindow(new ViewTraitObservationsDialog(this, parentWindow,"Numeric Variate", traitId, traitName, envIds));
	}
	

}
