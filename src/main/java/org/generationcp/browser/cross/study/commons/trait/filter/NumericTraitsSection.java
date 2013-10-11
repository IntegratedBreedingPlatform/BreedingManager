package org.generationcp.browser.cross.study.commons.trait.filter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmValueChangeListener;
import org.generationcp.browser.cross.study.adapted.main.validators.NumericTraitLimitsValidator;
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

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
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
		// TODO Auto-generated method stub

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
		try {
			
			String limitsRequiredMessage = MessageFormat.format(messageSource.getMessage(Message.FIELD_IS_REQUIRED), 
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
				traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this,trait.getId(),trait.getName(),"Numeric Variate",this.environmentIds));
				
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
				conditionBox.addListener(new AdaptedGermplasmValueChangeListener(this, box, limitsField, weightBox));
				limitsField.addValidator(new NumericTraitLimitsValidator(conditionBox, minValue, maxValue));
				this.fieldsToValidate.add(limitsField);
				
				Object[] itemObj = new Object[]{ box, traitNameLink, trait.getLocationCount(), trait.getGermplasmCount(), 
						trait.getObservationCount(), minValue, trait.getMedianValue(), maxValue,
						conditionBox, limitsField, weightBox};
	    		
				traitsTable.addItem(itemObj, trait);
			}	
				
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			LOG.error("Database error!", e);
			MessageNotifier.showError(parentWindow, "Database Error!", "Error with getting numeric trait info given environment ids."
					+ " Please report to IBP.", Notification.POSITION_CENTERED);
		}
		
	}
	
	private void initializeComponents() {
		lblSectionTitle = new Label(messageSource.getMessage(Message.GET_NUMERIC_VARIATES));
		
		traitsTable = new Table();
		traitsTable.setWidth("950px");
		traitsTable.setHeight("400px");
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
	
	
	
	
	// perform validations and if no error, proceed to next tab
//	public void proceedButtonClickAction(int index){
//		if (index <= NUM_OF_SECTIONS){
//			Component nextTab = tabLayouts[index+1];
//			
//			if (nextTab != null){
//				try {
//					
//					for (Field field : this.fieldsToValidate){
//						if (field.isEnabled())
//							field.validate();
//					}
//					mainTabSheet.setSelectedTab(nextTab);
//					
//				} catch (InvalidValueException e) {
//					MessageNotifier.showWarning(getWindow(), 
//							this.messageSource.getMessage(Message.INCORRECT_LIMITS_VALUE), 
//							e.getMessage(), Notification.POSITION_CENTERED);
//				}
//			}
//		}
//	}
	

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
		
	}

}
