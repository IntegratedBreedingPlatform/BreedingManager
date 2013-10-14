package org.generationcp.browser.cross.study.commons.trait.filter;

import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.CategoricalTraitInfo;
import org.generationcp.middleware.domain.h2h.CategoricalValue;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CategoricalVariatesSection extends VerticalLayout implements InitializingBean, InternationalizableComponent{
	private static final long serialVersionUID = 9099796930978032454L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CategoricalVariatesSection.class);
	
	private static final String TRAIT_COLUMN_ID = "Trait Column";
	private static final String NUM_LOCATIONS_COLUMN_ID = "Number of Locations";
	private static final String NUM_LINES_COLUMN_ID = "Number of Lines";
	private static final String NUM_OBSERVATIONS_COLUMN_ID = "Number of Observations";
	private static final String DISTINCT_OBSERVED_VALUES_COLUMN_ID = "Distinct Observed Values";
	private static final String CLASS_COLUMN_ID = "Class";
	private static final String CONDITION_COLUMN_ID = "Condition";
	private static final String LIMITS_COLUMN_ID = "Limits";
	private static final String PRIORITY_COLUMN_ID = "Priority";
	public static final String TRAIT_BUTTON_ID = "CharacterTraitsSection Trait Button ID";
	
	private List<Integer> environmentIds = null;
	
	private Window parentWindow;
	private Label lblSectionTitle;
	private Table traitsTable;

	private List<CategoricalTraitInfo> categoricalValueObjects;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;
	
	public CategoricalVariatesSection(List<Integer> environmentIds, Window parentWindow){
		super();
		this.environmentIds = environmentIds;
		this.parentWindow = parentWindow;
	}

	private void initializeComponents(){
		

		if(this.environmentIds != null && !this.environmentIds.isEmpty()){
			try{
				categoricalValueObjects = crossStudyDataManager.getTraitsForCategoricalVariates(environmentIds);
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with getting categorical variate info given environment ids: " + this.environmentIds.toString(), ex);
				MessageNotifier.showError(parentWindow, "Database Error!", "Error with getting categorical variate info given environment ids."
						+ " Please report to IBP.", Notification.POSITION_CENTERED);
				return;
			}
		}
		
		lblSectionTitle = new Label(messageSource.getMessage(Message.CHARACTER_TRAITS_SECTION_TITLE));
		
		traitsTable = new Table();
		traitsTable.setImmediate(true);
		traitsTable.setColumnCollapsingAllowed(true);
		traitsTable.setColumnReorderingAllowed(true);
	
		traitsTable.addContainerProperty(TRAIT_COLUMN_ID, Button.class, null);
		traitsTable.addContainerProperty(NUM_LOCATIONS_COLUMN_ID, Integer.class, null);
		traitsTable.addContainerProperty(NUM_LINES_COLUMN_ID, Integer.class, null);
		traitsTable.addContainerProperty(NUM_OBSERVATIONS_COLUMN_ID, Integer.class, null);
		if(categoricalValueObjects!=null){
			for(int i=0;i<getMaxCategoryValueCount(categoricalValueObjects);i++){
				traitsTable.addContainerProperty(CLASS_COLUMN_ID+i, String.class, null);
			}
		}
		traitsTable.addContainerProperty(CONDITION_COLUMN_ID, ComboBox.class, null);
		traitsTable.addContainerProperty(LIMITS_COLUMN_ID, TextField.class, null);
		traitsTable.addContainerProperty(PRIORITY_COLUMN_ID, ComboBox.class, null);
		
		
		
		traitsTable.setColumnHeader(TRAIT_COLUMN_ID, messageSource.getMessage(Message.HEAD_TO_HEAD_TRAIT)); // Trait
		traitsTable.setColumnHeader(NUM_LOCATIONS_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_LOCATIONS)); // # of Locations
		traitsTable.setColumnHeader(NUM_LINES_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_LINES)); // # of Lines
		traitsTable.setColumnHeader(NUM_OBSERVATIONS_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_OBSERVATIONS)); // # of Observations
		if(categoricalValueObjects!=null){
			for(int i=0;i<getMaxCategoryValueCount(categoricalValueObjects);i++){
				traitsTable.setColumnHeader(CLASS_COLUMN_ID+i, messageSource.getMessage(Message.CLASS)+" "+(i+1));
			}
		}		
		traitsTable.setColumnHeader(CONDITION_COLUMN_ID, messageSource.getMessage(Message.CONDITION_HEADER)); // Condition
		traitsTable.setColumnHeader(LIMITS_COLUMN_ID, messageSource.getMessage(Message.LIMITS)); // Limits
		traitsTable.setColumnHeader(PRIORITY_COLUMN_ID, messageSource.getMessage(Message.PRIORITY)); // Priority
		
		
		
		traitsTable.setColumnWidth(TRAIT_COLUMN_ID, 120);
		traitsTable.setColumnWidth(NUM_LOCATIONS_COLUMN_ID, 120);
		traitsTable.setColumnWidth(NUM_LINES_COLUMN_ID, 120);
		traitsTable.setColumnWidth(NUM_OBSERVATIONS_COLUMN_ID, 120);
		if(categoricalValueObjects!=null){
			for(int i=0;i<getMaxCategoryValueCount(categoricalValueObjects);i++){
				traitsTable.setColumnWidth(CLASS_COLUMN_ID+i, 100);
			}
		}
		traitsTable.setColumnWidth(CONDITION_COLUMN_ID, 200);
		traitsTable.setColumnWidth(LIMITS_COLUMN_ID, 200);
		traitsTable.setColumnWidth(PRIORITY_COLUMN_ID, 200);
		
		setWidth((1200+(getMaxCategoryValueCount(categoricalValueObjects)*120))+"px");
	}
	

	
	private void initializeLayout(){
		setMargin(true);
		setSpacing(true);
		addComponent(lblSectionTitle);
		addComponent(traitsTable);
	}
	
	
	private int getMaxCategoryValueCount(List<CategoricalTraitInfo> categoricalValueObjects){
		int max=0;
		for(int i=0;i<categoricalValueObjects.size();i++){
			int count = categoricalValueObjects.get(i).getValues().size();
			if(count > max)
				max = count;
		}
		return max;
	}

	
	private void populateTraitsTable(){
		if(this.environmentIds != null && !this.environmentIds.isEmpty()){
			
			if(categoricalValueObjects != null){
				if(categoricalValueObjects.isEmpty()){
					MessageNotifier.showMessage(parentWindow, "Information", "There were no categorical variates observed in the environments you have selected."
							, 3000, Notification.POSITION_CENTERED);
					return;
				}
				
				for(CategoricalTraitInfo traitInfo : categoricalValueObjects){
					
					Button traitNameLink = new Button(traitInfo.getName());
					traitNameLink.setImmediate(true);
					traitNameLink.setStyleName(Reindeer.BUTTON_LINK);
					traitNameLink.setData(TRAIT_BUTTON_ID);
					traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this,traitInfo.getId(), traitInfo.getName(), "Categorical Variate", this.environmentIds));
					
					ComboBox conditionComboBox = CrossStudyUtil.getNumericTraitCombobox();
					conditionComboBox.setEnabled(true);
					
					ComboBox priorityComboBox = CrossStudyUtil.getTraitWeightsComboBox();
					TextField txtLimits = new TextField();
					
					Object[] itemObj = new Object[traitsTable.getColumnHeaders().length];

					itemObj[0] = traitNameLink;
					itemObj[1] = traitInfo.getLocationCount();
					itemObj[2] = traitInfo.getGermplasmCount();
					itemObj[3] = traitInfo.getObservationCount();

					int currentColumn = 4;
					for(int currentValueIndex=0;currentValueIndex<traitInfo.getValues().size();currentColumn++,currentValueIndex++){
						itemObj[currentColumn] = traitInfo.getValues().get(currentValueIndex).getName() + " (" + traitInfo.getValues().get(currentValueIndex).getCount() + ")"; 
					}
					
					//for cases wherein not all column count is equal
					for(;currentColumn<traitsTable.getColumnHeaders().length-3;currentColumn++){
						itemObj[currentColumn] = "";
					}
					
					itemObj[traitsTable.getColumnHeaders().length - 3] = conditionComboBox;
					itemObj[traitsTable.getColumnHeaders().length - 2] = txtLimits;
					itemObj[traitsTable.getColumnHeaders().length - 1] = priorityComboBox;
					
					traitsTable.addItem(itemObj, traitInfo);
				}
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setSpacing(true);
    	setMargin(true);
    	setHeight("500px");
    	
		initializeComponents();
		initializeLayout();
		populateTraitsTable();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(lblSectionTitle, Message.CHARACTER_TRAITS_SECTION_TITLE);
	}

	public void showTraitObservationClickAction(Integer traitId, String variateType, String traitName,
			List<Integer> envIds) {
		Window parentWindow = this.getWindow();
		parentWindow.addWindow(new ViewTraitObservationsDialog(this, parentWindow, variateType , traitId, traitName, envIds));
	}
}
