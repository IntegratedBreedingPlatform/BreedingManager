package org.generationcp.browser.cross.study.commons.trait.filter;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmValueChangeListener;
import org.generationcp.browser.cross.study.adapted.main.pojos.CharacterTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.browser.cross.study.commons.trait.filter.listeners.CharacterTraitLimitsValueChangeListener;
import org.generationcp.browser.cross.study.constants.CharacterTraitCondition;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.constants.TraitWeight;
import org.generationcp.browser.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.CharacterTraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

/**
 * The component class for the Character Traits Section of the Trait Filter
 * @author Kevin Manansala (kevin@efficio.us.com)
 *
 */
@Configurable
public class CharacterTraitsSection extends VerticalLayout implements InitializingBean, InternationalizableComponent{
	private static final long serialVersionUID = 9099796930978032454L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CharacterTraitsSection.class);
	
	private static final String TRAIT_COLUMN_ID = "Trait Column";
	private static final String NUM_LOCATIONS_COLUMN_ID = "Number of Locations";
	private static final String NUM_LINES_COLUMN_ID = "Number of Lines";
	private static final String NUM_OBSERVATIONS_COLUMN_ID = "Number of Observations";
	private static final String DISTINCT_OBSERVED_VALUES_COLUMN_ID = "Distinct Observed Values";
	private static final String CONDITION_COLUMN_ID = "Condition";
	private static final String LIMITS_COLUMN_ID = "Limits";
	private static final String PRIORITY_COLUMN_ID = "Priority";
	public static final String TRAIT_BUTTON_ID = "CharacterTraitsSection Trait Button ID";
	
	private List<Integer> environmentIds = null;
	
	private Window parentWindow;
	private Label lblSectionTitle;
	private Table traitsTable;
	
	private int characterTraitCount;
	private boolean emptyMessageShown = false;
	private List<CharacterTraitFilter> filters;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;
	
	public CharacterTraitsSection(List<Integer> environmentIds, Window parentWindow){
		super();
		this.environmentIds = environmentIds;
		this.parentWindow = parentWindow;
	}

	private void initializeComponents(){
		lblSectionTitle = new Label(messageSource.getMessage(Message.CHARACTER_TRAITS_SECTION_TITLE));
		
		traitsTable = new Table();
		traitsTable.setImmediate(true);
		traitsTable.setColumnCollapsingAllowed(true);
		traitsTable.setColumnReorderingAllowed(true);
		
		traitsTable.addContainerProperty(TRAIT_COLUMN_ID, Button.class, null);
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
		
		traitsTable.setColumnWidth(DISTINCT_OBSERVED_VALUES_COLUMN_ID, 250);
		
		traitsTable.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                             
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if(propertyId != null && propertyId == DISTINCT_OBSERVED_VALUES_COLUMN_ID) {
			    	Table theTraitsTable = (Table) source;
			    	Item item = theTraitsTable.getItem(itemId);
			    	String distinctValues = (String) item.getItemProperty(DISTINCT_OBSERVED_VALUES_COLUMN_ID).getValue();
			    	return "<b>Distinct values:</b>  " + distinctValues;
			    }                                                                       
			    return null;
			}
		});
	}
	
	private void initializeLayout(){
		setMargin(true);
		setSpacing(true);
		setWidth("995px");
		addComponent(lblSectionTitle);
		
		traitsTable.setHeight("360px");
		traitsTable.setWidth("960px");
		addComponent(traitsTable);
	}
	
	private void populateTraitsTable(){
		if(this.environmentIds != null && !this.environmentIds.isEmpty()){
			List<CharacterTraitInfo> traitInfoObjects = null;
			try{
				traitInfoObjects = crossStudyDataManager.getTraitsForCharacterVariates(environmentIds);
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with getting character trait info given environment ids: " + this.environmentIds.toString(), ex);
				MessageNotifier.showError(parentWindow, "Database Error!", "Error with getting character trait info given environment ids. "
						+ messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
				return;
			}
			
			if(traitInfoObjects != null){
				characterTraitCount = traitInfoObjects.size();
				if(traitInfoObjects.isEmpty()){
					return;
				}
				
				for(CharacterTraitInfo traitInfo : traitInfoObjects){
					StringBuffer distinctValuesObserved = new StringBuffer();
					for(String value: traitInfo.getValues()){
						distinctValuesObserved.append(value);
						distinctValuesObserved.append(", ");
					}
					
					Button traitNameLink = new Button(traitInfo.getName());
					traitNameLink.setImmediate(true);
					traitNameLink.setStyleName(Reindeer.BUTTON_LINK);
					traitNameLink.setData(TRAIT_BUTTON_ID);
					traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this,traitInfo.getId(), traitInfo.getName(), "Character Variate", this.environmentIds));
					
					ComboBox conditionComboBox = CrossStudyUtil.getCharacterTraitConditionsComboBox();
					ComboBox priorityComboBox = CrossStudyUtil.getTraitWeightsComboBox();
					TextField txtLimits = new TextField();
					txtLimits.setImmediate(true);
					txtLimits.setEnabled(false);
					
					conditionComboBox.addListener(new AdaptedGermplasmValueChangeListener(this, txtLimits, priorityComboBox));
					priorityComboBox.addListener(new AdaptedGermplasmValueChangeListener(this, conditionComboBox, null, txtLimits));
					txtLimits.addListener(new CharacterTraitLimitsValueChangeListener(parentWindow, traitInfo.getValues()));
					
					Object[] itemObj = new Object[]{traitNameLink, traitInfo.getLocationCount(), traitInfo.getGermplasmCount(), traitInfo.getObservationCount()
							, distinctValuesObserved.toString(), conditionComboBox, txtLimits, priorityComboBox};
					traitsTable.addItem(itemObj, traitInfo);
				}
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeComponents();
		initializeLayout();
		populateTraitsTable();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(lblSectionTitle, Message.CHARACTER_TRAITS_SECTION_TITLE);
	}
	
	public void showEmptyTraitsMessage() {
		if(!emptyMessageShown && characterTraitCount == 0){
			MessageNotifier.showMessage(parentWindow, "Information", "There were no character traits observed in the environments you have selected."
					, 3000, Notification.POSITION_CENTERED);
			emptyMessageShown = true;
		}
	}

	public void showTraitObservationClickAction(Integer traitId, String variateType, String traitName,
			List<Integer> envIds) {
		Window parentWindow = this.getWindow();
		ViewTraitObservationsDialog viewTraitDialog = new ViewTraitObservationsDialog(this, parentWindow, variateType, traitId, traitName, envIds);
		viewTraitDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(viewTraitDialog);
	}
	
	@SuppressWarnings("unchecked")
	public List<CharacterTraitFilter> getFilters(){
		this.filters = new ArrayList<CharacterTraitFilter>();
		
		Collection<CharacterTraitInfo> traitInfoObjects = (Collection<CharacterTraitInfo>) this.traitsTable.getItemIds();
		for(CharacterTraitInfo traitInfo : traitInfoObjects){
			Item tableRow = this.traitsTable.getItem(traitInfo);
			
			ComboBox conditionComboBox = (ComboBox) tableRow.getItemProperty(CONDITION_COLUMN_ID).getValue();
			CharacterTraitCondition condition = (CharacterTraitCondition) conditionComboBox.getValue();
			
			ComboBox priorityComboBox = (ComboBox) tableRow.getItemProperty(PRIORITY_COLUMN_ID).getValue();
			TraitWeight priority = (TraitWeight) priorityComboBox.getValue();
			
			TextField limitsField = (TextField) tableRow.getItemProperty(LIMITS_COLUMN_ID).getValue();
			String limitsString = limitsField.getValue().toString();
			
			if(condition != CharacterTraitCondition.DROP_TRAIT && priority != TraitWeight.IGNORED){
				if(condition == CharacterTraitCondition.KEEP_ALL){ 
					CharacterTraitFilter filter = new CharacterTraitFilter(traitInfo, condition, new ArrayList<String>(), priority);
					this.filters.add(filter);
				} else {
					if(limitsString != null && limitsString.length() > 0){
						StringTokenizer tokenizer = new StringTokenizer(limitsString, ",");
						List<String> givenLimits = new ArrayList<String>();
						
						while(tokenizer.hasMoreTokens()){
							String limit = tokenizer.nextToken().trim();
							givenLimits.add(limit);
						}
						
						CharacterTraitFilter filter = new CharacterTraitFilter(traitInfo, condition, givenLimits, priority);
						this.filters.add(filter);
					}
				}
			}
		}
		
		return this.filters;
	}
	
	/*
	 * If at least one trait is NOT dropped, allow to proceed
	 */
	public boolean allTraitsDropped(){
		if (this.filters == null){
			this.filters = getFilters();
		}
		if (!this.filters.isEmpty()){
			for (CharacterTraitFilter filter: this.filters){
				if (!CharacterTraitCondition.DROP_TRAIT.equals(filter.getCondition())){
					return false;
				}
			}
		}
		return true;
	}

}
