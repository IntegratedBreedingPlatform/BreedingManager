package org.generationcp.browser.cross.study.commons.trait.filter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmValueChangeListener;
import org.generationcp.browser.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.validators.NumericTraitLimitsValidator;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.constants.TraitWeight;
import org.generationcp.browser.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.NumericTraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class NumericTraitsSection extends VerticalLayout implements
		InitializingBean, InternationalizableComponent {
	
	public static final String TRAIT_BUTTON_ID = "NumericTraitsSection Trait Button ID";

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(NumericTraitsSection.class);
	
	private Window parentWindow;
	private Label lblSectionTitle;
	private Table traitsTable;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	private List<Integer> environmentIds = null;
	private List<Field> fieldsToValidate = new ArrayList<Field>();

	public NumericTraitsSection(List<Integer> environmentIds, Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
		this.environmentIds = environmentIds;
	}

	@Override
	public void updateLabels() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
    	setSpacing(true);
    	setMargin(true);
    	setHeight("500px");
    	setWidth("1000px");
    	
    	initializeComponents();
    	populateTable();
	}
	
	private void populateTable() {
		String limitsRequiredMessage = MessageFormat.format(messageSource.getMessage(Message.FIELD_IS_REQUIRED), 
                messageSource.getMessage(Message.LIMITS));
		
		List<NumericTraitInfo> numericTraits = null;
		
		try {
			numericTraits = crossStudyDataManager.getTraitsForNumericVariates(this.environmentIds);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			LOG.error("Database error!", e);
			MessageNotifier.showError(parentWindow, "Database Error!", "Error with getting numeric trait info given environment ids."
					+ " Please report to IBP.", Notification.POSITION_CENTERED);
		}
		
		if (numericTraits != null) {
			if(numericTraits.isEmpty()){
				MessageNotifier.showMessage(parentWindow, "Information", "There were no numeric traits observed in the environments you have selected."
						, 3000, Notification.POSITION_CENTERED);
				return;
			}
			for (NumericTraitInfo trait : numericTraits){
				double minValue = trait.getMinValue();
				double maxValue = trait.getMaxValue();

				Button traitNameLink = new Button(trait.getName());
				traitNameLink.setImmediate(true);
				traitNameLink.setStyleName(Reindeer.BUTTON_LINK);
				traitNameLink.setData(TRAIT_BUTTON_ID);

				traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this,trait.getId(),trait.getName(), "Numeric Variate", this.environmentIds));
				
				TextField limitsField = new TextField();
				limitsField.setWidth("80px");
				limitsField.setEnabled(false);
				limitsField.setImmediate(true);		
				limitsField.setRequired(true);
				limitsField.setRequiredError(limitsRequiredMessage);
				
				ComboBox conditionBox = CrossStudyUtil.getNumericTraitCombobox();
				conditionBox.setWidth("100px");
				ComboBox weightBox = CrossStudyUtil.getTraitWeightsComboBox();
				weightBox.setWidth("100px");

				conditionBox.addListener(new AdaptedGermplasmValueChangeListener(this, limitsField, weightBox));
				limitsField.addValidator(new NumericTraitLimitsValidator(conditionBox, minValue, maxValue));
				this.fieldsToValidate.add(limitsField);
				
				Object[] itemObj = new Object[]{traitNameLink, trait.getLocationCount(), trait.getGermplasmCount(), 
						trait.getObservationCount(), minValue, trait.getMedianValue(), maxValue,
						conditionBox, limitsField, weightBox};
	    		
				traitsTable.addItem(itemObj, trait);
			}	
		}
	}
	
	private void initializeComponents() {
		lblSectionTitle = new Label(messageSource.getMessage(Message.GET_NUMERIC_VARIATES));
		
		traitsTable = new Table();
		traitsTable.setImmediate(true);
		traitsTable.setColumnCollapsingAllowed(true);
		traitsTable.setColumnReorderingAllowed(true);
		
		for (TableColumn column : TableColumn.values()){
			traitsTable.addContainerProperty(column, column.getColumnClass(), null);
			traitsTable.setColumnHeader(column, messageSource.getMessage(column.getMessage()));
			traitsTable.setColumnAlignment(column, Table.ALIGN_CENTER);
			traitsTable.setColumnWidth(column, column.getWidth());				
		}
		addComponent(lblSectionTitle);
		addComponent(traitsTable);
	
	}
	
	public void showNumericVariateClickAction(Integer traitId, String traitName,
			List<Integer> envIds) {
		Window parentWindow = this.getWindow();
		parentWindow.addWindow(new ViewTraitObservationsDialog(this, parentWindow,"Numeric Variate", traitId, traitName, envIds));
	}
	
	
	
	
	// perform validation on limits textfields
	public boolean allFieldsValid(){
		try {
			for (Field field : this.fieldsToValidate){
				if (field.isEnabled())
					field.validate();
			}
			
			return true;
			
		} catch (InvalidValueException e) {
			MessageNotifier.showWarning(getWindow(), 
					this.messageSource.getMessage(Message.INCORRECT_LIMITS_VALUE), 
					e.getMessage(), Notification.POSITION_CENTERED);
			return false;
		}
	
	}
	
	@SuppressWarnings("unchecked")
	public List<NumericTraitFilter> getFilters(){
		List<NumericTraitFilter> toreturn = new ArrayList<NumericTraitFilter>();
		
		Collection<NumericTraitInfo> traitInfoObjects = (Collection<NumericTraitInfo>) this.traitsTable.getContainerDataSource().getItemIds();
		for(NumericTraitInfo traitInfo : traitInfoObjects){
			Item tableRow = this.traitsTable.getItem(traitInfo);
			
			ComboBox conditionComboBox = (ComboBox) tableRow.getItemProperty(TableColumn.NUM_CONDITION_COL_ID).getValue();
			NumericTraitCriteria condition = (NumericTraitCriteria) conditionComboBox.getValue();
			
			ComboBox priorityComboBox = (ComboBox) tableRow.getItemProperty(TableColumn.NUM_PRIORITY_COL_ID).getValue();
			TraitWeight priority = (TraitWeight) priorityComboBox.getValue();
			
			TextField limitsField = (TextField) tableRow.getItemProperty(TableColumn.NUM_LIMITS_COL_ID).getValue();
			String limitsString = limitsField.getValue().toString();
			
			if(condition != NumericTraitCriteria.DROP_TRAIT && priority != TraitWeight.IGNORED){
				if(condition == NumericTraitCriteria.KEEP_ALL){
					NumericTraitFilter filter = new NumericTraitFilter(traitInfo, condition, new ArrayList<String>(), priority);
					toreturn.add(filter);
				} else{
					if(limitsString != null && limitsString.length() > 0){
						StringTokenizer tokenizer = new StringTokenizer(limitsString, ",");
						List<String> givenLimits = new ArrayList<String>();
						
						while(tokenizer.hasMoreTokens()){
							String limit = tokenizer.nextToken().trim();
							givenLimits.add(limit);
						}
						
						NumericTraitFilter filter = new NumericTraitFilter(traitInfo, condition, givenLimits, priority);
						toreturn.add(filter);
					}
				}
				
			}
		}
		
		return toreturn;
	}
	

	private enum TableColumn {
		NUM_TRAIT_COL_ID (Message.HEAD_TO_HEAD_TRAIT, Button.class, 130)
		, NUM_NUMBER_OF_ENVTS_COL_ID (Message.NUMBER_OF_LOCATIONS, Integer.class, 85)
		, NUM_NUMBER_OF_LINES_COL_ID (Message.NUMBER_OF_LINES, Integer.class, 65)
		, NUM_NUMBER_OF_OBS_COL_ID (Message.NUMBER_OF_OBSERVATIONS, Integer.class, 105)
		, NUM_MIN_COL_ID (Message.MIN, Double.class, 40)
		, NUM_MEDIAN_COL_ID (Message.MEDIAN, Double.class, 50)
		, NUM_MAX_COL_ID(Message.MAX, Double.class, 40)
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
		
	}

}
