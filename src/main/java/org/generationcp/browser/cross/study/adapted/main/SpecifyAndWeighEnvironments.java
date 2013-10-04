package org.generationcp.browser.cross.study.adapted.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.LocationDto;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@Configurable
public class SpecifyAndWeighEnvironments extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
    private static final long serialVersionUID = -3667517088395779496L;
    
    private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.adapted.main.SpecifyAndWeighEnvironments.class);
    
    private static final String TAG_COLUMN_ID = "SpecifyAndWeighEnvironments Tag Column Id";
    private static final String ENV_NUMBER_COLUMN_ID = "SpecifyAndWeighEnvironments Env Number Column Id";
    private static final String LOCATION_COLUMN_ID = "SpecifyAndWeighEnvironments Location Column Id";
    private static final String COUNTRY_COLUMN_ID = "SpecifyAndWeighEnvironments Country Column Id";
    private static final String STUDY_COLUMN_ID = "SpecifyAndWeighEnvironments Study Column Id";
    private static final String WEIGHT_COLUMN_ID = "SpecifyAndWeighEnvironments Weight Column Id";
	
    public static final String NEXT_BUTTON_ID = "SpecifyAndWeighEnvironments Next Button ID";
    
    public static final String FILTER_LOCATION_BUTTON_ID = "SpecifyAndWeighEnvironments Filter Location Button ID";
    public static final String FILTER_STUDY_BUTTON_ID = "SpecifyAndWeighEnvironments Filter Study Button ID";
    public static final String ADD_ENVIRONMENT_BUTTON_ID = "SpecifyAndWeighEnvironments Add Env Button ID";
    
    private QueryForAdaptedGermplasmMain mainScreen;
	private SetUpTraitFilter nextScreen;
	private ResultsComponent resultsScreen;
	
	private Label headerLabel;
	private Label headerValLabel;
	private Label chooseEnvLabel;
	private Label noOfEnvLabel;
	private Label numberOfEnvironmentSelectedLabel;
	
	private Button filterByLocationBtn;
	private Button filterByStudyBtn;
	private Button addEnvConditionsBtn;
	private Button nextButton;
	
	private Table environmentsTable;	
	private TrialEnvironments environments;
	
	private Map<String, FilterByLocation> filterLocationCountryMap;
	private Map<String, List<StudyReference>> studyEnvironmentMap;
	
    private FilterLocationDialog filterLocation;
    private FilterStudyDialog filterStudy;
    
    private Map filterSetLevel1;
    private Map filterSetLevel3;
    private Map filterSetLevel4;
    
    private static Integer NON_NUMERIC_VAL = -1;
    private boolean isFilterLocationClicked = false;
    private boolean isFilterStudyClicked = false;
    
	private int tableColumnSize = 0;
	
	private Map<String, Object[]> tableEntriesMap;
	private Map<String, EnvironmentForComparison> environmentCheckBoxComparisonMap;
    private Set<String> environmentForComparison; //will contain all the tagged row
	
	private Map<CheckBox, Item> environmentCheckBoxMap;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    @Autowired
    private CrossStudyDataManager crossStudyDataManager;
	
	public SpecifyAndWeighEnvironments(QueryForAdaptedGermplasmMain mainScreen, SetUpTraitFilter nextScreen
			, ResultsComponent resultScreen) {
		 this.mainScreen = mainScreen;
		 this.nextScreen = nextScreen;
		 this.resultsScreen = resultScreen;
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	   setHeight("550px");
       setWidth("1000px");
       
       headerLabel = new Label(messageSource.getMessage(Message.ENVIRONMENT_FILTER));
       headerLabel.setImmediate(true);
       addComponent(headerLabel, "top:20px;left:20px");
       
       headerValLabel = new Label(messageSource.getMessage(Message.ENVIRONMENT_FILTER_VAL));
       headerValLabel.setStyleName("gcp-bold-italic");
       headerValLabel.setContentMode(Label.CONTENT_XHTML);
       headerValLabel.setImmediate(true);
       addComponent(headerValLabel, "top:20px;left:150px");
       
       filterByLocationBtn = new Button(messageSource.getMessage(Message.FILTER_BY_LOCATION));
       filterByLocationBtn.setWidth("200px");
       filterByLocationBtn.setData(FILTER_LOCATION_BUTTON_ID);
       filterByLocationBtn.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
       addComponent(filterByLocationBtn, "top:50px;left:20px");
       
       filterByStudyBtn = new Button(messageSource.getMessage(Message.FILTER_BY_STUDY));
       filterByStudyBtn.setWidth("200px");
       filterByStudyBtn.setData(FILTER_STUDY_BUTTON_ID);
       filterByStudyBtn.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
       addComponent(filterByStudyBtn, "top:50px;left:240px");
       
       addEnvConditionsBtn = new Button(messageSource.getMessage(Message.ADD_ENV_CONDITION));
       addEnvConditionsBtn.setWidth("400px");
       addComponent(addEnvConditionsBtn, "top:50px;left:580px");
       
       chooseEnvLabel = new Label(messageSource.getMessage(Message.CHOOSE_ENVIRONMENTS));
       chooseEnvLabel.setImmediate(true);
       addComponent(chooseEnvLabel, "top:90px;left:20px");
       
       environmentsTable = new Table();
       environmentsTable.setWidth("960px");
       environmentsTable.setHeight("370px");
       environmentsTable.setImmediate(true);
       environmentsTable.setPageLength(-1);
       environmentsTable.setColumnCollapsingAllowed(true);
       environmentsTable.setColumnReorderingAllowed(true);
       
       createEnvironmentsTable();
       populateEnvironmentsTable();
       addComponent(environmentsTable, "top:110px;left:20px");
       
       noOfEnvLabel = new Label(messageSource.getMessage(Message.NO_OF_SELECTED_ENVIRONMENT));
       noOfEnvLabel.setImmediate(true);
       addComponent(noOfEnvLabel, "top:500px;left:20px");
       
       numberOfEnvironmentSelectedLabel = new Label("0");
       numberOfEnvironmentSelectedLabel.setImmediate(true);
       addComponent(numberOfEnvironmentSelectedLabel, "top:500px;left:230px");
       
       nextButton = new Button(messageSource.getMessage(Message.NEXT));
       nextButton.setData(NEXT_BUTTON_ID);
       nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
       nextButton.setWidth("100px");
       nextButton.setEnabled(true);
       addComponent(nextButton, "top:490px;left:880px");
	}

	private void createEnvironmentsTable(){
        List<Object> propertyIds = new ArrayList<Object>();
        for(Object propertyId : environmentsTable.getContainerPropertyIds()){
            propertyIds.add(propertyId);
        }
        
        tableColumnSize = 0;
        for(Object propertyId : propertyIds){
            environmentsTable.removeContainerProperty(propertyId);
        }
        environmentsTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        environmentsTable.addContainerProperty(ENV_NUMBER_COLUMN_ID, Integer.class, null);
        environmentsTable.addContainerProperty(LOCATION_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(COUNTRY_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(STUDY_COLUMN_ID, String.class, null);
        
        environmentsTable.setColumnHeader(TAG_COLUMN_ID, "TAG");
        environmentsTable.setColumnHeader(ENV_NUMBER_COLUMN_ID, "ENV No");
        environmentsTable.setColumnHeader(LOCATION_COLUMN_ID, "LOCATION");
        environmentsTable.setColumnHeader(COUNTRY_COLUMN_ID, "COUNTRY");
        environmentsTable.setColumnHeader(STUDY_COLUMN_ID, "STUDY");
        tableColumnSize = 5;
        
        environmentsTable.addContainerProperty(WEIGHT_COLUMN_ID, ComboBox.class, null);
        environmentsTable.setColumnHeader(WEIGHT_COLUMN_ID, "WEIGHT");
        tableColumnSize++;
    }
	
	private void populateEnvironmentsTable() {
		tableEntriesMap = new HashMap();
		
		environmentCheckBoxComparisonMap = new HashMap();
    	environmentCheckBoxMap = new HashMap();
    	environmentForComparison = new HashSet();
    	//numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
		
    	filterLocationCountryMap = new HashMap();
		studyEnvironmentMap = new HashMap();
    	
		recreateTable(true,false);
		
		Window parentWindow = this.getWindow();
        filterLocation = new FilterLocationDialog(this, parentWindow, filterLocationCountryMap);
        filterStudy = new FilterStudyDialog(this, parentWindow, studyEnvironmentMap);
        isFilterLocationClicked = false;
        isFilterStudyClicked = false;
	}
	
	private void recreateTable(boolean recreateFilterLocationMap, boolean isAppliedClick){
    	this.environmentsTable.removeAllItems();
    	
    	if(recreateFilterLocationMap){
	    	environmentCheckBoxComparisonMap = new HashMap();
	    	environmentCheckBoxMap = new HashMap();	   
    	}
    	environmentForComparison = new HashSet();
    	
    	Map<String, Item> trialEnvIdTableMap = new HashMap();
    	
    	try {
			environments = crossStudyDataManager.getAllTrialEnvironments();
			
			Set<TrialEnvironment> trialEnvSet = environments.getTrialEnvironments();
			Iterator<TrialEnvironment> trialEnvIter = trialEnvSet.iterator();
			while(trialEnvIter.hasNext()){

				TrialEnvironment trialEnv = trialEnvIter.next();
				
				String trialEnvIdString = String.valueOf(trialEnv.getId());
				
				if(!trialEnvIdTableMap.containsKey(trialEnvIdString)){
					String tableKey = trialEnv.getId() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getCountryName() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getProvinceName()  + FilterLocationDialog.DELIMITER  +trialEnv.getLocation().getLocationName() + FilterLocationDialog.DELIMITER + trialEnv.getStudy().getName();
					
					boolean isValidEntryAdd = true;
					if(isAppliedClick){
						isValidEntryAdd = isValidEntry(trialEnv); 
					}
		    		 
		    		 if(isValidEntryAdd){
		    			 Object[] objItem = new Object[tableColumnSize];
		    			 
		    			 if(tableEntriesMap.containsKey(tableKey)){
		    				 objItem = tableEntriesMap.get(tableKey);
		    				 environmentsTable.addItem(objItem, tableKey);
		    				 
		    				 if(isAppliedClick){	 
		    					 //we simulate the checkbox
		    					 ((CheckBox)objItem[0]).setValue(true);
								 clickCheckBox(tableKey, (ComboBox)objItem[objItem.length-1], true);
		    				 }
		    			 }else{
		    				 CheckBox box = new CheckBox();
		    				 
		    				 box.setImmediate(true);
				             ComboBox comboBox = getWeightComboBox();
				             
				             int counterTrait = 0;
				             objItem[counterTrait++] = box;
				             objItem[counterTrait++] = trialEnv.getId();
				             objItem[counterTrait++] = trialEnv.getLocation().getLocationName();
				             objItem[counterTrait++] = trialEnv.getLocation().getCountryName();
				             objItem[counterTrait++] = trialEnv.getStudy().getName();
				             
				             
				             if(recreateFilterLocationMap){
				            	 setupLocationMappings(trialEnv);
				            	 tableEntriesMap.put(tableKey, objItem);
				             }
				             
				             //insert environment condition here
				             EnvironmentForComparison compare = new EnvironmentForComparison(trialEnv.getId(), trialEnv.getLocation().getLocationName(), trialEnv.getLocation().getCountryName(), trialEnv.getStudy().getName(), comboBox);
				             
				             objItem[counterTrait++] = comboBox;
				            
				             environmentsTable.addItem(objItem, tableKey);
				             Item item = environmentsTable.getItem(tableKey);
				             box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox, tableKey));
					         
				             environmentCheckBoxMap.put(box, item);
				             environmentCheckBoxComparisonMap.put(tableKey, compare);
				             trialEnvIdTableMap.put(trialEnvIdString, item);
		    			 }
		    			 
		    		 }
				}//end of if
				
				
			}
			
			//numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));

			
		} catch(MiddlewareQueryException ex){
	   		 ex.printStackTrace();
	         LOG.error("Database error!", ex);
	         MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
		}
   }
    	
	 public void clickCheckBox(String key, Component combo, boolean boolVal){

    	if(combo != null){
    		ComboBox comboBox = (ComboBox) combo;
    		comboBox.setEnabled(boolVal);
    	}
    			
		if(boolVal){
			environmentForComparison.add(key);
		}else{
			environmentForComparison.remove(key);
		}    				    				

		if(environmentForComparison.isEmpty()){
			nextButton.setEnabled(false);
		}else{								
			nextButton.setEnabled(true);
		}
		
		numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
	}

	private boolean isValidEntry(TrialEnvironment trialEnv) {
		String countryName = trialEnv.getLocation().getCountryName();
    	String locationName = trialEnv.getLocation().getLocationName();
    	String studyName = trialEnv.getStudy().getName();
    	
    	boolean isValid = false;
    	
    	String level1Key = countryName;
    	String level3Key = countryName + FilterLocationDialog.DELIMITER + locationName;
    	String level4Key = studyName;
    	
    	//check against the map
    	if(isFilterLocationClicked){
	    	if(filterSetLevel1.containsKey(level1Key)){
	    		isValid = true;
	    	}else if(filterSetLevel3.containsKey(level3Key)){
	    		isValid = true;
	    	}
    	}
    	
    	if(isFilterStudyClicked){
    		
    		if(isFilterLocationClicked){
    			//meaning there is a filter in location already
    			if(isValid){
    				//we only filter again if its valid
	    			if(filterSetLevel4.containsKey(level4Key)){
	    	    		isValid = true;
	    	    	}else{
	    	    		isValid = false;
	    	    	}
    			}
    		}else{
    			if(filterSetLevel4.containsKey(level4Key)){
    	    		isValid = true;
    	    	}else{
    	    		isValid = false;
    	    	}
    		}
    		
    	}
    	
    	return isValid;
	}
	
    private ComboBox getWeightComboBox(){
    	return CrossStudyUtil.getWeightComboBox();
    }
    
    private void setupLocationMappings(TrialEnvironment trialEnv){
    	LocationDto location = trialEnv.getLocation();
    	StudyReference study = trialEnv.getStudy();
    	String trialEnvId = Integer.toString(trialEnv.getId());
    	String countryName = location.getCountryName();
    	String provinceName = location.getProvinceName();
    	String locationName = location.getLocationName();
    	String studyName = study.getName();
    	    	    	    	
    	FilterByLocation countryFilter = filterLocationCountryMap.get(countryName);
    	
    	if(countryFilter == null){
    		countryFilter = new FilterByLocation(countryName, trialEnvId);
    	}
    	    	
    	countryFilter.addProvinceAndLocationAndStudy(provinceName,locationName, studyName);
    	filterLocationCountryMap.put(countryName, countryFilter);
    	
    	//for the mapping in the study level
    	String studyKey = study.getName() + FilterLocationDialog.DELIMITER + study.getDescription();
    	List<StudyReference> studyReferenceList = studyEnvironmentMap.get(studyKey);
    	if(studyReferenceList == null){
    		studyReferenceList = new ArrayList();
    	}
    	studyReferenceList.add(study);
    	studyEnvironmentMap.put(studyKey, studyReferenceList);
    	
    }
    
    public void nextButtonClickAction(){
        //this.nextScreen.populateResultsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
    	List<EnvironmentForComparison> toBeCompared = new ArrayList();
    	    	
    	int total = 0;
    	//get the total of weights
    	for(String sKey : environmentForComparison){
    		EnvironmentForComparison envt = environmentCheckBoxComparisonMap.get(sKey);
    		EnvironmentWeight envtWeight = (EnvironmentWeight) envt.getWeightComboBox().getValue();
    		total += envtWeight.getWeight();
    	}
    	LOG.debug("TOTAL = " + total);
    	
    	// compute the weight percentages
    	for (String sKey : environmentForComparison){
    		EnvironmentForComparison envt = environmentCheckBoxComparisonMap.get(sKey);
    		EnvironmentWeight envtWeight = (EnvironmentWeight) envt.getWeightComboBox().getValue();
    		envt.computeWeight(total);
    		
    		System.out.println("ENVT: " + envt.getLocationName() + ", weight = " + envt.getWeight());
    		toBeCompared.add(envt);
    	}
    	
        this.mainScreen.selectSecondTab();
    }
    
    public void selectFilterByLocationClickAction(){

    	Window parentWindow = this.getWindow();
    	filterLocation.initializeButtons();
        parentWindow.addWindow(filterLocation);
    }
    
    public void selectFilterByStudyClickAction(){
    	
    	Window parentWindow = this.getWindow();
    	filterStudy.initializeButtons();
        parentWindow.addWindow(filterStudy);
    	System.out.println("window: " + parentWindow.toString());
        System.out.println("Open Study Details");
    }
    
    public void clickFilterByLocationApply(List<FilterLocationDto> filterLocationDtoListLevel1, List<FilterLocationDto> filterLocationDtoListLevel3){
    	//MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
    	
    	isFilterLocationClicked = true;
    	filterSetLevel1 = new HashMap();
    	filterSetLevel3 = new HashMap();
    	
    	    
    	for(FilterLocationDto dto : filterLocationDtoListLevel1){
    		String countryName = dto.getCountryName();
        	
    		filterSetLevel1.put(countryName, countryName);
    	}

    	for(FilterLocationDto dto : filterLocationDtoListLevel3){
    		String countryName = dto.getCountryName();
    		String locationName = dto.getLocationName();
    		//String studyName = dto.getStudyName();
    		String key = countryName + FilterLocationDialog.DELIMITER + locationName;// + FilterLocationDialog.DELIMITER + studyName;
    	
        	
    		filterSetLevel3.put(key, key);
    		//we need to remove in the 1st level since this mean we want specific level 2 filter
    		filterSetLevel1.remove(countryName);
    	}
    	
    	recreateTable(false, true);
    	
    	headerValLabel.setValue("");
    }
    
    public void clickFilterByStudyApply(List<FilterLocationDto> filterLocationDtoListLevel4){
    	isFilterStudyClicked = true;
    	filterSetLevel4 = new HashMap();
    	for(FilterLocationDto dto : filterLocationDtoListLevel4){
    		String studyName = dto.getStudyName();
        	
    		filterSetLevel4.put(studyName, studyName);
    	}
    	recreateTable(false, true);
    }
    
    public void reopenFilterWindow(){
    	//this is to simulate and refresh checkboxes
    	Window parentWindow = this.getWindow();
    	parentWindow.removeWindow(filterLocation);    	    	
        
    	filterLocation.initializeButtons();
        parentWindow.addWindow(filterLocation);
    }

    public void reopenFilterStudyWindow(){
    	//this is to simulate and refresh checkboxes
    	Window parentWindow = this.getWindow();
    	parentWindow.removeWindow(filterStudy);    	    	
        
    	filterStudy.initializeButtons();
        parentWindow.addWindow(filterStudy);
    }



}

