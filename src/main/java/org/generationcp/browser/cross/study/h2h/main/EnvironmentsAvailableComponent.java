package org.generationcp.browser.cross.study.h2h.main;

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


import org.generationcp.browser.application.Message;

import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.LocationDto;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

@Configurable
public class EnvironmentsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3667517088395779496L;
    
    private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.h2h.main.EnvironmentsAvailableComponent.class);
    
    private static final String TAG_COLUMN_ID = "EnvironmentsAvailableComponent Tag Column Id";
    private static final String ENV_NUMBER_COLUMN_ID = "EnvironmentsAvailableComponent Env Number Column Id";
    private static final String LOCATION_COLUMN_ID = "EnvironmentsAvailableComponent Location Column Id";
    private static final String COUNTRY_COLUMN_ID = "EnvironmentsAvailableComponent Country Column Id";
    private static final String STUDY_COLUMN_ID = "EnvironmentsAvailableComponent Study Column Id";
    private static final String WEIGHT_COLUMN_ID = "EnvironmentsAvailableComponent Weight Column Id";
    
    public static final String NEXT_BUTTON_ID = "EnvironmentsAvailableComponent Next Button ID";
    public static final String BACK_BUTTON_ID = "EnvironmentsAvailableComponent Back Button ID";
    
    public static final String FILTER_LOCATION_BUTTON_ID = "EnvironmentsAvailableComponent Filter Location Button ID";
    public static final String FILTER_STUDY_BUTTON_ID = "EnvironmentsAvailableComponent Filter Study Button ID";
    public static final String ADD_ENVIRONMENT_BUTTON_ID = "EnvironmentsAvailableComponent Add Env Button ID";
    
    
    private Table environmentsTable;

    private Button nextButton;
    private Button backButton;
    
    private HeadToHeadCrossStudyMain mainScreen;
    private ResultsComponent nextScreen;
    
    private Integer currentTestEntryGID;
    private Integer currentStandardEntryGID;
    
    private List<TraitForComparison> traitsForComparisonList;
    
    private Label environmentLabel;
    private Label chooseEnvironmentLabel;
    
    private Label numberOfEnvironmentLabel;
    private Label numberOfEnvironmentSelectedLabel;
    
    private Button filterByLocation;
    private Button filterByStudy;
    private Button addEnvironment;
    
    public static final Integer  IMPORTANT = 1;
    public static final Integer  CRITICAL = 2;
    public static final Integer  DESIRABLE = 3;
    public static final Integer  IGNORED = 4;
    
    private Map<CheckBox, Item> environmentCheckBoxMap;
    private Map<ComboBox, EnvironmentForComparison> environmentCheckBoxComparisonMap;
    private Set<ComboBox> environmentForComparison; //will contain all the tagged row
    
    @Autowired
    private CrossStudyDataManager crossStudyDataManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    private Map<String, Observation> observationMap;
    private Map<String, String> germplasmIdNameMap;
    private List<GermplasmPair> finalGermplasmPairs;
    private Map<String, FilterByLocation> filterLocationCountryMap;
    private FilterLocationDialog filterLocation;
    private FilterStudyDialog filterStudy;
    private Set<TraitInfo> traitInfosNames;
    private Set<String> trialEnvironmentIds;
    private Map<String, Map<String, TrialEnvironment>>  traitEnvMap;
    private Map<String, TrialEnvironment> trialEnvMap;
    private List<TraitForComparison> traitForComparisonsList;
    //private List<GermplasmPair> germplasmPairs;
    private int tableColumnSize = 0;
    
    private Map<String, Object[]> tableEntriesMap;
    
    private Map<String, List<StudyReference>> studyEnvironmentMap;
    
    private Map filterSetLevel1;
    private Map filterSetLevel2;
    private Map filterSetLevel3;
    private Map filterSetLevel4;
    
    private static Integer NON_NUMERIC_VAL = -1;
    private boolean isFilterLocationClicked = false;
    private boolean isFilterStudyClicked = false;
    
    public EnvironmentsAvailableComponent(HeadToHeadCrossStudyMain mainScreen, ResultsComponent nextScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
        this.currentStandardEntryGID = null;
        this.currentTestEntryGID = null;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("500px");
        setWidth("1000px");
        
        environmentLabel = new Label("Environment Filter");
        environmentLabel.setImmediate(true);
        
        addComponent(environmentLabel, "top:20px;left:30px");
        
        
        
        
        filterByLocation = new Button("Filter by Location");
        filterByLocation.setData(FILTER_LOCATION_BUTTON_ID);
        filterByLocation.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        filterByLocation.setEnabled(true);
        
        addComponent(filterByLocation, "top:40px;left:30px");
        
        filterByStudy = new Button("Filter by Study");
        filterByStudy.setData(FILTER_STUDY_BUTTON_ID);
        filterByStudy.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        filterByStudy.setEnabled(true);
        
        addComponent(filterByStudy, "top:40px;left:200px");
        
        addEnvironment = new Button("Add Environment Conditions columns to the Environment Filter");
        addEnvironment.setData(ADD_ENVIRONMENT_BUTTON_ID);
        addEnvironment.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        addEnvironment.setEnabled(false);
        
        addComponent(addEnvironment, "top:40px;left:500px");
        

        
        chooseEnvironmentLabel = new Label("Choose Environment");
        chooseEnvironmentLabel.setImmediate(true);
        
        addComponent(chooseEnvironmentLabel, "top:70px;left:30px");
        
        
        environmentsTable = new Table();
        environmentsTable.setWidth("950px");
        environmentsTable.setHeight("320px");
        environmentsTable.setImmediate(true);
        environmentsTable.setColumnCollapsingAllowed(true);
        environmentsTable.setColumnReorderingAllowed(true);
        
        Set<TraitInfo> traitInfos = new HashSet<TraitInfo>();
        createEnvironmentsTable(traitInfos);
        
        addComponent(environmentsTable, "top:90px;left:30px");
        
        numberOfEnvironmentLabel = new Label("Number of Environments selected: ");
        numberOfEnvironmentLabel.setImmediate(true);
        
        addComponent(numberOfEnvironmentLabel, "top:430px;left:30px");
        
        numberOfEnvironmentSelectedLabel = new Label("");
        numberOfEnvironmentSelectedLabel.setValue(0);
        numberOfEnvironmentSelectedLabel.setImmediate(true);
        
        addComponent(numberOfEnvironmentSelectedLabel, "top:430px;left:230px");
        
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        nextButton.setEnabled(true);
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        addComponent(backButton, "top:450px;left:820px");
    }
    

    private ComboBox getWeightComboBox(){
    	ComboBox combo = new ComboBox();
    	combo.setNullSelectionAllowed(false);
    	combo.setTextInputAllowed(false);
    	combo.setImmediate(true);
    	
		combo.addItem(IMPORTANT);
		combo.setItemCaption(IMPORTANT,"Important");
		
		combo.addItem(CRITICAL);
		combo.setItemCaption(CRITICAL,"Critical");
		
		combo.addItem(DESIRABLE);
		combo.setItemCaption(DESIRABLE,"Desirable");
		
		combo.addItem(IGNORED);
		combo.setItemCaption(IGNORED,"Ignored");
			
		combo.setValue(IMPORTANT);
		
		combo.setEnabled(false);
		return combo;
		
    }
    
    public void clickCheckBox(Component combo, boolean boolVal){
    	
    	
    	if(combo != null){
    		ComboBox comboBox = (ComboBox) combo;
    		comboBox.setEnabled(boolVal);
    		
    		//TraitInfo info = traitMaps.get(comboBox);
    		
    			
    			//if( info != null){    				
    				if(boolVal){
    					environmentForComparison.add(comboBox);	
    					
    					//environmentCheckBoxComparisonMap.get(comboBox)
    				}else{
    					environmentForComparison.remove(comboBox);
					}    				    				
    			//}
    			
			if(environmentForComparison.isEmpty()){
				nextButton.setEnabled(false);
			}else{								
				nextButton.setEnabled(true);
			}
			numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
    	}
    	
    }
    public void populateEnvironmentsTable(List<TraitForComparison> traitForComparisonsListTemp,
    		Map<String, Map<String, TrialEnvironment>>  traitEnvMapTemp, Map<String, 
    		TrialEnvironment> trialEnvMapTemp, Set<Integer> germplasmIds, 
    		List<GermplasmPair> germplasmPairsTemp, Map<String, String> germplasmIdNameMap){    
    	
    	Iterator<TraitForComparison> iter = traitForComparisonsListTemp.iterator();
    	Map<String, Map<String, TrialEnvironment>>  newTraitEnvMap = new HashMap();
    	tableEntriesMap = new HashMap();
    	trialEnvironmentIds = new HashSet();
    	traitInfosNames = new LinkedHashSet<TraitInfo>();
    	
    	nextButton.setEnabled(false);
    	environmentCheckBoxComparisonMap = new HashMap();
    	environmentCheckBoxMap = new HashMap();
    	environmentForComparison = new HashSet();
    	numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
    	
    	this.germplasmIdNameMap = germplasmIdNameMap;
    	this.finalGermplasmPairs= germplasmPairsTemp; 
    	
    	List<Integer> traitIds = new ArrayList();
    	Set<Integer> environmentIds = new HashSet();
    	filterLocationCountryMap = new HashMap();
    	studyEnvironmentMap = new HashMap();
    	traitEnvMap = traitEnvMapTemp; 
    	trialEnvMap = trialEnvMapTemp;
    	traitForComparisonsList = traitForComparisonsListTemp;
    	//germplasmPairs = germplasmPairsTemp;
    	
    	
    	
    	while(iter.hasNext()){
    		TraitForComparison comparison = iter.next();
    		//System.out.println(comparison.getTraitInfo().getName() + "  " + comparison.getDirection());
    		String id = Integer.toString(comparison.getTraitInfo().getId());
    		if(traitEnvMap.containsKey(id)){
    			Map<String, TrialEnvironment> tempMap = traitEnvMap.get(id);
    			newTraitEnvMap.put(id, tempMap);
    			trialEnvironmentIds.addAll(tempMap.keySet());
    			Iterator<String> envIdsIter = tempMap.keySet().iterator();
    			while(envIdsIter.hasNext()){
    				environmentIds.add(Integer.valueOf(envIdsIter.next()));
    			}
    			traitIds.add(Integer.parseInt(id));
    		}
    		
    		traitInfosNames.add(comparison.getTraitInfo());
    	}
    	List<Integer> germplasmIdsList = new ArrayList<Integer>(germplasmIds);
    	List<Integer> environmentIdsList = new ArrayList<Integer>(environmentIds);
    	try{
    		observationMap = new HashMap();
    		List<Observation> observationList = crossStudyDataManager.getObservationsForTraitOnGermplasms(traitIds, germplasmIdsList, environmentIdsList);
    		for(Observation obs : observationList){
    			String newKey = obs.getId().getTraitId() + ":" + obs.getId().getEnvironmentId() + ":" + obs.getId().getGermplasmId();
    			observationMap.put(newKey, obs);    			
    		}
    	}catch(MiddlewareQueryException ex){
    		 ex.printStackTrace();
             LOG.error("Database error!", ex);
             MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
             //return new ArrayList<EnvironmentForComparison>();
    	}
    	//get trait names for columns        
    	recreateTable(true, false);
    	
    	Window parentWindow = this.getWindow();
        filterLocation = new FilterLocationDialog(this, parentWindow, filterLocationCountryMap);
        filterStudy = new FilterStudyDialog(this, parentWindow, studyEnvironmentMap);
        isFilterLocationClicked = false;
        isFilterStudyClicked = false;
    }
    
    private void recreateTable(boolean recreateFilterLocationMap, boolean isAppliedClick){
    	this.environmentsTable.removeAllItems();
    	createEnvironmentsTable(traitInfosNames);
    	if(recreateFilterLocationMap){
	    	environmentCheckBoxComparisonMap = new HashMap();
	    	environmentCheckBoxMap = new HashMap();	   
    	}
    	environmentForComparison = new HashSet();
    	
		
    	Map<String, Item> trialEnvIdTableMap = new HashMap();
    	//clean the traitEnvMap
    	Iterator<String> trialEnvIdsIter = trialEnvironmentIds.iterator();
    	Map<String, String> checkerMap = new HashMap();
    	while(trialEnvIdsIter.hasNext()){
    		 Integer trialEnvId = Integer.parseInt(trialEnvIdsIter.next());
    		 String trialEnvIdString = trialEnvId.toString();
    		 
    		 if(!trialEnvIdTableMap.containsKey(trialEnvIdString)){
	    		 TrialEnvironment trialEnv = trialEnvMap.get(trialEnvIdString);
	    		//we build the table
	    		 String tableKey = trialEnvIdString + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getCountryName() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getProvinceName()  + FilterLocationDialog.DELIMITER  +trialEnv.getLocation().getLocationName() + FilterLocationDialog.DELIMITER + trialEnv.getStudy().getName();
	    		 /*
	    		 if(checkerMap.get(tableKey) != null)
	    			 System.out.println("Hello " + tableKey);
	    		 
	    		 if(checkerMap.get(tableKey) == null)
	    			 checkerMap.put(tableKey, tableKey);
	    		 */
	    		 boolean isValidEntryAdd = true;
	    		 if(isAppliedClick){
	    			 isValidEntryAdd = isValidEntry(trialEnv);
	    			 
	    		 }
	    		 
	    		 if(isValidEntryAdd){
	    			 
	    			 Object[] objItem = new Object[tableColumnSize];
	    			 
	    			 if(tableEntriesMap.containsKey(tableKey)){
	    				 //to be use when filtering only
	    				 //for recycling same object
	    				 objItem = tableEntriesMap.get(tableKey);
	    				 environmentsTable.addItem(objItem, tableKey);
	    				 
	    				 if(isAppliedClick){
	    					 
	    					 //we simulate the checkbox
	    					 ((CheckBox)objItem[0]).setValue(true);
							 clickCheckBox((ComboBox)objItem[objItem.length-1], true);
	    				 }
	    			 }else{
		    			 
		    			 CheckBox box = new CheckBox();
		    			 //box.setValue(true);
			             box.setImmediate(true);
			             ComboBox comboBox = getWeightComboBox();
			             /*
			    		 Item item = environmentsTable.addItem(tableKey);	    		 	    		 
			             item.getItemProperty(ENV_NUMBER_COLUMN_ID).setValue(trialEnv.getId());
			             item.getItemProperty(LOCATION_COLUMN_ID).setValue(trialEnv.getLocation().getLocationName());
			             item.getItemProperty(COUNTRY_COLUMN_ID).setValue(trialEnv.getLocation().getCountryName());
			             item.getItemProperty(STUDY_COLUMN_ID).setValue(trialEnv.getStudy().getName());
			             */
			             objItem[0] = box;
			             objItem[1] = trialEnv.getId();
			             objItem[2] = trialEnv.getLocation().getLocationName();
			             objItem[3] = trialEnv.getLocation().getCountryName();
			             objItem[4] = trialEnv.getStudy().getName();
			             int counterTrait = 5;
			             
			             if(recreateFilterLocationMap){
			            	 setupLocationMappings(trialEnv);
			            	 tableEntriesMap.put(tableKey, objItem);
			             }
			             
			             
			             
			        	 
			        	 
			             EnvironmentForComparison compare = new EnvironmentForComparison(trialEnv.getId(), trialEnv.getLocation().getLocationName(), trialEnv.getLocation().getCountryName(), trialEnv.getStudy().getName(), comboBox);
			             LinkedHashMap<TraitForComparison, List<Observation>> traitAndObservationMap = new LinkedHashMap();
			             Iterator<TraitForComparison> traitForCompareIter = traitForComparisonsList.iterator();
			             while(traitForCompareIter.hasNext()){
			            	 TraitForComparison traitForCompare = traitForCompareIter.next();
			            	 
			            	 List<Observation> obsList = new ArrayList(); 
			                 Integer count = getTraitCount(traitForCompare.getTraitInfo(), trialEnv.getId(), finalGermplasmPairs, obsList);
			                 //item.getItemProperty(traitForCompare.getTraitInfo().getName()).setValue(count);
			                 traitAndObservationMap.put(traitForCompare, obsList);
			                 traitForCompare.setDisplay(true);
			                 /*
			                 if(count.intValue() == NON_NUMERIC_VAL.intValue()){
			                	 //we should hide the column and mark the trait so it wont be displayed anymore
			                	 traitForCompare.setDisplay(false);			                	 
			                 }
			                 */
			                 objItem[counterTrait++] = count;
			             }
			             compare.setTraitAndObservationMap(traitAndObservationMap);
			             	
			             //item.getItemProperty(TAG_COLUMN_ID).setValue(box);
			             //item.getItemProperty(WEIGHT_COLUMN_ID).setValue(comboBox);
			             objItem[counterTrait++] = comboBox;
			             
			             environmentsTable.addItem(objItem, tableKey);
			             Item item = environmentsTable.getItem(tableKey);
			             box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox));
				        	//traitMaps.put(comboBox, traitsTable.getItem(tableId));
			             environmentCheckBoxMap.put(box, item);
			             environmentCheckBoxComparisonMap.put(comboBox, compare);
			             trialEnvIdTableMap.put(trialEnvIdString, item);
	    			 }
		             
		             
		             
		            
		             
		             //by default, should be checked
		             //box.setValue(true);
		             //clickCheckBox(comboBox, true);
	    		 }
    		 }
             
    	}
    	numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
    	//set up table column visibility
    }
    
    private boolean isValidEntry(TrialEnvironment trialEnv){
    	
    	String countryName = trialEnv.getLocation().getCountryName();
    	String provinceName = trialEnv.getLocation().getProvinceName();
    	String locationName = trialEnv.getLocation().getLocationName();
    	String studyName = trialEnv.getStudy().getName();
    	
    	boolean isValid = false;
    	
    	String level1Key = countryName;
    	String level2Key = countryName + FilterLocationDialog.DELIMITER + provinceName;
    	String level3Key = countryName + FilterLocationDialog.DELIMITER + provinceName + FilterLocationDialog.DELIMITER + locationName + FilterLocationDialog.DELIMITER + studyName;
    	String level4Key = studyName;
    	
    	//check against the map
    	if(isFilterLocationClicked){
	    	if(filterSetLevel1.containsKey(level1Key)){
	    		isValid = true;
	    	}else if(filterSetLevel2.containsKey(level2Key)){
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
    
    public void clickFilterByStudyApply(List<FilterLocationDto> filterLocationDtoListLevel4){
    	isFilterStudyClicked = true;
    	filterSetLevel4 = new HashMap();
    	for(FilterLocationDto dto : filterLocationDtoListLevel4){
    		String studyName = dto.getStudyName();
        	
    		filterSetLevel4.put(studyName, studyName);
    	}
    	recreateTable(false, true);
    }
    
    public void clickFilterByLocationApply(List<FilterLocationDto> filterLocationDtoListLevel1, List<FilterLocationDto> filterLocationDtoListLevel2, List<FilterLocationDto> filterLocationDtoListLevel3){
    	//MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
    	
    	
    	isFilterLocationClicked = true;
    	filterSetLevel1 = new HashMap();
    	filterSetLevel2 = new HashMap();
    	filterSetLevel3 = new HashMap();
    	
    	    
    	for(FilterLocationDto dto : filterLocationDtoListLevel1){
    		String countryName = dto.getCountryName();
        	
    		filterSetLevel1.put(countryName, countryName);
    	}
    	
    	for(FilterLocationDto dto : filterLocationDtoListLevel2){
    		String countryName = dto.getCountryName();
    		String provinceName = dto.getProvinceName();
    		String key = countryName + FilterLocationDialog.DELIMITER + provinceName;
        	
    		filterSetLevel2.put(key, key);
    		//we need to remove in the 1st level since this mean we want specific level 2 filter
    		filterSetLevel1.remove(countryName);
    	}
    	
    	for(FilterLocationDto dto : filterLocationDtoListLevel3){
    		String countryName = dto.getCountryName();
    		String provinceName = dto.getProvinceName();
    		String locationName = dto.getLocationName();
    		String studyName = dto.getStudyName();
    		String key = countryName + FilterLocationDialog.DELIMITER + provinceName + FilterLocationDialog.DELIMITER + locationName + FilterLocationDialog.DELIMITER + studyName; 
        	
    		filterSetLevel3.put(key, key);
    		//we need to remove in the 1st level since this mean we want specific level 2 filter
    		filterSetLevel2.remove(countryName + FilterLocationDialog.DELIMITER + provinceName);
    		filterSetLevel1.remove(countryName);
    	}
    	
    	
    	recreateTable(false, true);
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
    
    private Integer getTraitCount(TraitInfo traitInfo, int envId, List<GermplasmPair> germplasmPairs, List<Observation> obsList){
    	int counter = 0;
    	
    	for(GermplasmPair pair : germplasmPairs){
    		String keyToChecked1 = traitInfo.getId() + ":" +envId + ":" + pair.getGid1();
    		String keyToChecked2 = traitInfo.getId() + ":" +envId + ":" + pair.getGid2();
    		Observation obs1 = observationMap.get(keyToChecked1);
    		Observation obs2 = observationMap.get(keyToChecked2);
    		
    		//for test data
    		/*
    		if(true){
    			return NON_NUMERIC_VAL;
    			
    			counter++;
    			obs1.setValue("aa2");
    			obs2.setValue("aa3");
    			obsList.add(obs1);
    			obsList.add(obs2);
    			
    			continue;
    			
    		}
    	*/
    		
    		if(obs1 != null && obs2 != null && obs1.getValue() != null 
    				&& obs2.getValue() != null && !obs1.getValue().equalsIgnoreCase("") &&
    				!obs2.getValue().equalsIgnoreCase("")){
    			if(isValidDoubleValue(obs1.getValue()) && isValidDoubleValue(obs2.getValue())){
	    			counter++;
	    			obsList.add(obs1);
	    			obsList.add(obs2);
    			}else{
    				;//return NON_NUMERIC_VAL;
    			}
    			
    			//if(obs1.getValue())
    		}
    		
    		
    	}
    	return Integer.valueOf(counter);
    }
    
    private boolean isValidDoubleValue(String val){
    	if(val != null && !val.equalsIgnoreCase("")){
    		try{
    			double d = Double.parseDouble(val);
    			return true;
    		}catch(NumberFormatException ee){
    			return false;
    		}
    	}
    	return false;
    }
    
    private boolean areCurrentGIDsDifferentFromGiven(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.currentTestEntryGID != null && this.currentStandardEntryGID != null){
            if(this.currentTestEntryGID == currentTestEntryGID && this.currentStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
    }
    
    private void createEnvironmentsTable(Set<TraitInfo> traitInfos){
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
        environmentsTable.setColumnHeader(ENV_NUMBER_COLUMN_ID, "ENV #");
        environmentsTable.setColumnHeader(LOCATION_COLUMN_ID, "LOCATION");
        environmentsTable.setColumnHeader(COUNTRY_COLUMN_ID, "COUNTRY");
        environmentsTable.setColumnHeader(STUDY_COLUMN_ID, "STUDY");
        tableColumnSize = 5;
        
        for(TraitInfo traitInfo : traitInfos){
            environmentsTable.addContainerProperty(traitInfo.getName(), Integer.class, null);
            environmentsTable.setColumnHeader(traitInfo.getName(), traitInfo.getName());
            tableColumnSize++;
        }
        
        environmentsTable.addContainerProperty(WEIGHT_COLUMN_ID, ComboBox.class, null);
        environmentsTable.setColumnHeader(WEIGHT_COLUMN_ID, "Weight");
        tableColumnSize++;
    }
   
    
    public void nextButtonClickAction(){
        //this.nextScreen.populateResultsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
    	List<EnvironmentForComparison> toBeCompared = new ArrayList();
    	for(ComboBox box : environmentForComparison){
    		toBeCompared.add(environmentCheckBoxComparisonMap.get(box));
    	}
    	this.nextScreen.populateResultsTable(toBeCompared, germplasmIdNameMap, finalGermplasmPairs, observationMap);
        this.mainScreen.selectFourthTab();
    }
    
    public void backButtonClickAction(){
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
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
