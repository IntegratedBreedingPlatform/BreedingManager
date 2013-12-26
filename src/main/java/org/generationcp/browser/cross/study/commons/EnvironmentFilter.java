package org.generationcp.browser.cross.study.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.main.QueryForAdaptedGermplasmMain;
import org.generationcp.browser.cross.study.adapted.main.SetUpTraitFilter;
import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.h2h.main.HeadToHeadCrossStudyMain;
import org.generationcp.browser.cross.study.h2h.main.ResultsComponent;
import org.generationcp.browser.cross.study.h2h.main.dialogs.AddEnvironmentalConditionsDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.browser.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.LocationDto;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class EnvironmentFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
private static final long serialVersionUID = -3667517088395779496L;
    
    private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.commons.EnvironmentFilter.class);
    
    private static final String TAG_COLUMN_ID = "EnvironmentFilter Tag Column Id";
    private static final String ENV_NUMBER_COLUMN_ID = "EnvironmentFilter Env Number Column Id";
    private static final String LOCATION_COLUMN_ID = "EnvironmentFilter Location Column Id";
    private static final String COUNTRY_COLUMN_ID = "EnvironmentFilter Country Column Id";
    private static final String STUDY_COLUMN_ID = "EnvironmentFilter Study Column Id";
    private static final String WEIGHT_COLUMN_ID = "EnvironmentFilter Weight Column Id";
	
    public static final String NEXT_BUTTON_ID = "EnvironmentFilter Next Button ID";
    public static final String BACK_BUTTON_ID = "EnvironmentFilter Back Button ID";
    
    public static final String FILTER_LOCATION_BUTTON_ID = "EnvironmentFilter Filter Location Button ID";
    public static final String FILTER_STUDY_BUTTON_ID = "EnvironmentFilter Filter Study Button ID";
    public static final String ADD_ENVIRONMENT_BUTTON_ID = "EnvironmentFilter Add Env Button ID";
    public static final String QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME = "Query_For_Adapted_Germplasm";
    
    /*Head to Head Query Variables*/
    private HeadToHeadCrossStudyMain mainScreen1;
    private ResultsComponent nextScreen1;
    
    private Map<String, ObservationList> observationMap;
    private Map<String, String> germplasmIdNameMap;
    private List<GermplasmPair> finalGermplasmPairs;
    private Set<TraitInfo> traitInfosNames;
    private Set<String> trialEnvironmentIds;
    private Map<String, Map<String, TrialEnvironment>>  traitEnvMap;
    private Map<String, TrialEnvironment> trialEnvMap;
    private List<TraitForComparison> traitForComparisonsList;
    
    /*Adapted Germplasm Variables*/
    private QueryForAdaptedGermplasmMain mainScreen2;
	private SetUpTraitFilter nextScreen2;
	
	private Label headerLabel;
	private Label headerValLabel;
	private Label chooseEnvLabel;
	private Label noOfEnvLabel;
	private Label numberOfEnvironmentSelectedLabel;
	
	private Button filterByLocationBtn;
	private Button filterByStudyBtn;
	private Button addEnvConditionsBtn;
	private Button nextButton;
	private Button backButton;
	
	private Table environmentsTable;	
	private TrialEnvironments environments;
	
	private Map<String, FilterByLocation> filterLocationCountryMap;
	private Map<String, List<StudyReference>> studyEnvironmentMap;
	
    private FilterLocationDialog filterLocation;
    private FilterStudyDialog filterStudy;
    private AddEnvironmentalConditionsDialog addConditionsDialog;
    
    private Map<String, String> filterSetLevel1;
    private Map<String, String> filterSetLevel3;
    private Map<String, String> filterSetLevel4;
    
    private boolean isFilterLocationClicked = false;
    private boolean isFilterStudyClicked = false;
    
	private int tableColumnSize = 0;
	
	private Map<String, Object[]> tableEntriesMap;
	private Map<String, EnvironmentForComparison> environmentCheckBoxComparisonMap;
    private Set<String> environmentForComparison; //will contain all the tagged row
    private List<String> addedEnvironmentColumns;
    
	private Map<CheckBox, Item> environmentCheckBoxMap;
	
	private CheckBox tagAllCheckBox;
	
	Set<Integer> environmentIds;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    @Autowired
    private CrossStudyDataManager crossStudyDataManager;
    
    private CrossStudyToolType crossStudyToolType;
    
    /*Constructors*/
    public EnvironmentFilter(HeadToHeadCrossStudyMain mainScreen, ResultsComponent nextScreen){
        this.mainScreen1 = mainScreen;
        this.nextScreen1 = nextScreen;
        
        this.crossStudyToolType = CrossStudyToolType.HEAD_TO_HEAD_QUERY;
    }
    
	public EnvironmentFilter(QueryForAdaptedGermplasmMain mainScreen, SetUpTraitFilter nextScreen) {
		 this.mainScreen2 = mainScreen;
		 this.nextScreen2 = nextScreen;
		 
		 this.crossStudyToolType = CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM;  
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
	       filterByLocationBtn.setWidth("150px");
	       filterByLocationBtn.setData(FILTER_LOCATION_BUTTON_ID);
	       filterByLocationBtn.addListener(new Button.ClickListener(){
	    	   private static final long serialVersionUID = 6624555365983829849L;

	    	   @Override
				public void buttonClick(ClickEvent event) {
					if(event.getButton().getData().equals(FILTER_LOCATION_BUTTON_ID)) {
				        selectFilterByLocationClickAction();       
					}
				}   
	       });
	       addComponent(filterByLocationBtn, "top:50px;left:20px");
	       
	       filterByStudyBtn = new Button(messageSource.getMessage(Message.FILTER_BY_STUDY));
	       filterByStudyBtn.setWidth("150px");
	       filterByStudyBtn.setData(FILTER_STUDY_BUTTON_ID);
	       filterByStudyBtn.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = -8782138170364187141L;

				@Override
				public void buttonClick(ClickEvent event) {
					if(event.getButton().getData().equals(FILTER_STUDY_BUTTON_ID)) {
						selectFilterByStudyClickAction();
					}
				}
	       });
	       addComponent(filterByStudyBtn, "top:50px;left:180px");
	       
	       addEnvConditionsBtn = new Button(messageSource.getMessage(Message.ADD_ENV_CONDITION));
	       addEnvConditionsBtn.setWidth("400px");
	       addEnvConditionsBtn.setData(ADD_ENVIRONMENT_BUTTON_ID);
	       addEnvConditionsBtn.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 4763719750664067113L;

				@Override
				public void buttonClick(ClickEvent event) {
					if(event.getButton().getData().equals(ADD_ENVIRONMENT_BUTTON_ID)) {
						addEnvironmentalConditionsClickAction();
					}
				}	    	   
	       });
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
	       
	       if(this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY){
	    	   Set<TraitInfo> traitInfos = new HashSet<TraitInfo>();
	    	   createEnvironmentsTable(traitInfos);
	       }
	       else if(this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM){
	    	   createEnvironmentsTable();
		       populateEnvironmentsTable();
	       }
	       
	       addComponent(environmentsTable, "top:110px;left:20px");
	
	       tagAllCheckBox = new CheckBox();
	       tagAllCheckBox.setImmediate(true);
	       
	       addComponent(tagAllCheckBox, "top:115px; left:52px;");
	       
	       tagAllCheckBox.addListener(new ValueChangeListener(){
	    	   	private static final long serialVersionUID = 1L;
				@Override
				public void valueChange(ValueChangeEvent event) {
					if((Boolean) tagAllCheckBox.getValue()==true)
						tagAllEnvironments();
					else
						untagAllEnvironments();
				}
	       });
	       
	       
	       noOfEnvLabel = new Label(messageSource.getMessage(Message.NO_OF_SELECTED_ENVIRONMENT));
	       noOfEnvLabel.setImmediate(true);
	       addComponent(noOfEnvLabel, "top:500px;left:20px");
	       
	       numberOfEnvironmentSelectedLabel = new Label("0");
	       numberOfEnvironmentSelectedLabel.setImmediate(true);
	       addComponent(numberOfEnvironmentSelectedLabel, "top:500px;left:230px");
	       
	       nextButton = new Button(messageSource.getMessage(Message.NEXT));
	       nextButton.setData(NEXT_BUTTON_ID);
	       nextButton.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					nextButtonClickAction();
				}
	       });
	       nextButton.setWidth("80px");
	       nextButton.setEnabled(false);
	       nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	       addComponent(nextButton, "top:490px;left:900px");
	       
	       if(this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY){
		       backButton = new Button(messageSource.getMessage(Message.BACK));
		       backButton.setData(BACK_BUTTON_ID);
		       backButton.addListener(new Button.ClickListener(){
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						backButtonClickAction();
					}
		       });
		       backButton.setWidth("80px");
		       backButton.setEnabled(true);
		       addComponent(backButton, "top:490px;left:810px");
	       }
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

        removeAddedEnvironmentConditionsColumns(this.addedEnvironmentColumns);
        
        environmentsTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        environmentsTable.addContainerProperty(LOCATION_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(COUNTRY_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(STUDY_COLUMN_ID, String.class, null);
        
        environmentsTable.setColumnHeader(TAG_COLUMN_ID, "TAG");
        environmentsTable.setColumnHeader(LOCATION_COLUMN_ID, "LOCATION");
        environmentsTable.setColumnHeader(COUNTRY_COLUMN_ID, "COUNTRY");
        environmentsTable.setColumnHeader(STUDY_COLUMN_ID, "STUDY");
        tableColumnSize = 4;
        
        for(TraitInfo traitInfo : traitInfos){
            environmentsTable.addContainerProperty(traitInfo.getId(), Integer.class, null);
            environmentsTable.setColumnHeader(traitInfo.getId(), traitInfo.getName());
            tableColumnSize++;
        }
        
        environmentsTable.addContainerProperty(WEIGHT_COLUMN_ID, ComboBox.class, null);
        environmentsTable.setColumnHeader(WEIGHT_COLUMN_ID, "WEIGHT");
        tableColumnSize++;
	}
	
	private void createEnvironmentsTable() {
		List<Object> propertyIds = new ArrayList<Object>();
		for(Object propertyId : environmentsTable.getContainerPropertyIds()){
		    propertyIds.add(propertyId);
		}
		
		tableColumnSize = 0;
		for(Object propertyId : propertyIds){
		    environmentsTable.removeContainerProperty(propertyId);
		}
		
		removeAddedEnvironmentConditionsColumns(this.addedEnvironmentColumns);
		
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

	public void populateEnvironmentsTable(List<TraitForComparison> traitForComparisonsListTemp,
    		Map<String, Map<String, TrialEnvironment>>  traitEnvMapTemp, Map<String, 
    		TrialEnvironment> trialEnvMapTemp, Set<Integer> germplasmIds, 
    		List<GermplasmPair> germplasmPairsTemp, Map<String, String> germplasmIdNameMap){    
    	
    	
    	Map<String, Map<String, TrialEnvironment>>  newTraitEnvMap = new HashMap<String, Map<String, TrialEnvironment>>();
    	tableEntriesMap = new HashMap<String, Object[]>();
    	trialEnvironmentIds = new HashSet<String>();
    	traitInfosNames = new LinkedHashSet<TraitInfo>();
    	
    	nextButton.setEnabled(false);
    	environmentCheckBoxComparisonMap = new HashMap<String, EnvironmentForComparison>();
    	environmentCheckBoxMap = new HashMap<CheckBox, Item>();
    	environmentForComparison = new HashSet<String>();
    	numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
    	
    	this.germplasmIdNameMap = germplasmIdNameMap;
    	this.finalGermplasmPairs= germplasmPairsTemp; 
    	
    	List<Integer> traitIds = new ArrayList<Integer>();
    	Set<Integer> environmentIds = new HashSet<Integer>();
    	filterLocationCountryMap = new HashMap<String, FilterByLocation>();
    	studyEnvironmentMap = new HashMap<String, List<StudyReference>>();
    	traitEnvMap = traitEnvMapTemp; 
    	trialEnvMap = trialEnvMapTemp;
    	traitForComparisonsList = traitForComparisonsListTemp;
    	//germplasmPairs = germplasmPairsTemp;
    	Iterator<TraitForComparison> iter = traitForComparisonsList.iterator();
    	
    	
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
    		observationMap = new HashMap<String, ObservationList>();
    		List<Observation> observationList = crossStudyDataManager.getObservationsForTraitOnGermplasms(traitIds, germplasmIdsList, environmentIdsList);
    		for(Observation obs : observationList){
    			String newKey = obs.getId().getTraitId() + ":" + obs.getId().getEnvironmentId() + ":" + obs.getId().getGermplasmId();
    			
    			ObservationList obsList = observationMap.get(newKey);
    			if(obsList == null){
    				obsList = new ObservationList(newKey);
    			}
    			obsList.addObservation(obs);
    			observationMap.put(newKey, obsList);    			
    		}
    	}catch(MiddlewareQueryException ex){
    		 ex.printStackTrace();
             LOG.error("Database error!", ex);
             MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
             //return new ArrayList<EnvironmentForComparison>();
    	}
    	//get trait names for columns        
    	recreateTable(true, false);
    	
    	Window parentWindow = this.getWindow();
        filterLocation = new FilterLocationDialog(this, parentWindow, filterLocationCountryMap);
        filterStudy = new FilterStudyDialog(this, parentWindow, studyEnvironmentMap);
        addConditionsDialog = new AddEnvironmentalConditionsDialog(this, parentWindow, environmentIdsList);
        
        isFilterLocationClicked = false;
        isFilterStudyClicked = false;
        
        //System.out.println("parentWindow in EnvironmentFilter1:" + parentWindow);
        //System.out.println("parentWindow in EnvironmentFilter1:" + getParent());
    }
	
	public void populateEnvironmentsTable() {
		tableEntriesMap = new HashMap<String, Object[]>();
		
		environmentCheckBoxComparisonMap = new HashMap<String, EnvironmentForComparison>();
    	environmentCheckBoxMap = new HashMap<CheckBox, Item>();
    	environmentForComparison = new HashSet<String>();
    	//numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
		
    	filterLocationCountryMap = new HashMap<String, FilterByLocation>();
		studyEnvironmentMap = new HashMap<String, List<StudyReference>>();
		environmentIds = new HashSet<Integer>();
		
		recreateTable(true,false);
		
		List<Integer> environmentIdsList = new ArrayList<Integer>(environmentIds);
		
		Window parentWindow = this.getWindow();
		
	    if(this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY){
	    	filterStudy = new FilterStudyDialog(this, parentWindow, studyEnvironmentMap);
	    } else if(this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM){
	    	filterStudy = new FilterStudyDialog(this, parentWindow, studyEnvironmentMap, QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME);
	    } else {
	    	filterStudy = new FilterStudyDialog(this, parentWindow, studyEnvironmentMap);
	    }
	    
	    filterLocation = new FilterLocationDialog(this, parentWindow, filterLocationCountryMap);
        addConditionsDialog = new AddEnvironmentalConditionsDialog(this, parentWindow, environmentIdsList);
        
        filterStudy.addStyleName(Reindeer.WINDOW_LIGHT);
        filterLocation.addStyleName(Reindeer.WINDOW_LIGHT);
        addConditionsDialog.addStyleName(Reindeer.WINDOW_LIGHT);
        
        isFilterLocationClicked = false;
        isFilterStudyClicked = false;
        
        //System.out.println("parentWindow in EnvironmentFilter2:" + parentWindow);
        //System.out.println("parentWindow in EnvironmentFilter2:" + getParent());
	}

	private void recreateTable(boolean recreateFilterLocationMap, boolean isAppliedClick){
    	this.environmentsTable.removeAllItems();
    	
    	if(recreateFilterLocationMap){
	    	environmentCheckBoxComparisonMap = new HashMap<String, EnvironmentForComparison>();
	    	environmentCheckBoxMap = new HashMap<CheckBox, Item>();	   
    	}
    	environmentForComparison = new HashSet<String>();
    	
    	Map<String, Item> trialEnvIdTableMap = new HashMap<String, Item>();
    	
    	if(this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM){
    		try {
    			environments = crossStudyDataManager.getAllTrialEnvironments();
    			
    			Set<TrialEnvironment> trialEnvSet = environments.getTrialEnvironments();
    			Iterator<TrialEnvironment> trialEnvIter = trialEnvSet.iterator();
    			while(trialEnvIter.hasNext()){

    				TrialEnvironment trialEnv = trialEnvIter.next();
    				
    				String trialEnvIdString = String.valueOf(trialEnv.getId());
    				
    				if(!trialEnvIdTableMap.containsKey(trialEnvIdString)){
    					final String tableKey = trialEnv.getId() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getCountryName() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getProvinceName()  + FilterLocationDialog.DELIMITER  +trialEnv.getLocation().getLocationName() + FilterLocationDialog.DELIMITER + trialEnv.getStudy().getName();
    					environmentIds.add(trialEnv.getId());
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
    				             final ComboBox comboBox = getWeightComboBox();
    				             
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
    				             box.addListener(new ValueChangeListener(){
    								private static final long serialVersionUID = -4759863142479248292L;

									@Override
    								public void valueChange(ValueChangeEvent event) {
    									clickCheckBox(tableKey, comboBox, (Boolean)event.getProperty().getValue());
    								}
    				             });
    				            		 	
    					         
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
    	         MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
    		}
    	}
    	else if(this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY){
    		createEnvironmentsTable(traitInfosNames);
    		
        	//clean the traitEnvMap
        	Iterator<String> trialEnvIdsIter = trialEnvironmentIds.iterator();
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
    							 clickCheckBox(tableKey, (ComboBox)objItem[objItem.length-1], true);
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
    			             int counterTrait = 0;
    			             objItem[counterTrait++] = box;
    			             //objItem[1] = trialEnv.getId();
    			             objItem[counterTrait++] = trialEnv.getLocation().getLocationName();
    			             objItem[counterTrait++] = trialEnv.getLocation().getCountryName();
    			             objItem[counterTrait++] = trialEnv.getStudy().getName();
    			             
    			             
    			             if(recreateFilterLocationMap){
    			            	 setupLocationMappings(trialEnv);
    			            	 tableEntriesMap.put(tableKey, objItem);
    			             }
    			             
    			             EnvironmentForComparison compare = new EnvironmentForComparison(trialEnv.getId(), trialEnv.getLocation().getLocationName(), trialEnv.getLocation().getCountryName(), trialEnv.getStudy().getName(), comboBox);
    			             LinkedHashMap<TraitForComparison, List<ObservationList>> traitAndObservationMap = new LinkedHashMap<TraitForComparison, List<ObservationList>>();
    			             Iterator<TraitForComparison> traitForCompareIter = traitForComparisonsList.iterator();
    			             while(traitForCompareIter.hasNext()){
    			            	 TraitForComparison traitForCompare = traitForCompareIter.next();
    			            	 
    			            	 List<ObservationList> obsList = new ArrayList<ObservationList>(); 
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
    			             box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox, tableKey));
    				        	//traitMaps.put(comboBox, traitsTable.getItem(tableId));
    			             environmentCheckBoxMap.put(box, item);
    			             environmentCheckBoxComparisonMap.put(tableKey, compare);
    			             trialEnvIdTableMap.put(trialEnvIdString, item);
    			             			            
    	    			 }
    		            
    	    			 //by default, should be checked
    		             //box.setValue(true);
    		             //clickCheckBox(comboBox, true);
    	    		 }
        		 }
                 
        	}
        	//numberOfEnvironmentSelectedLabel.setValue(getNumberOfTagged());
        	numberOfEnvironmentSelectedLabel.setValue(Integer.toString(environmentForComparison.size()));
    	}
    	
    	
	}
	
	public void clickCheckBox(String key, Component combo, boolean boolVal){

    	if(combo != null){
    		ComboBox comboBox = (ComboBox) combo;
    		comboBox.setEnabled(boolVal);
    		
    		if(boolVal){
    			comboBox.setValue(EnvironmentWeight.IMPORTANT);
    		}
    		else{
    			comboBox.setValue(EnvironmentWeight.IGNORED);
    		}
    		
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
	
    @SuppressWarnings("rawtypes")
	private String getNumberOfTagged(){
    	Iterator iter = environmentsTable.getItemIds().iterator();
    	int checked = 0;
    	while(iter.hasNext()){
    		String id = (String)iter.next();
    		Item item = environmentsTable.getItem(id);
    		CheckBox box = (CheckBox)item.getItemProperty(TAG_COLUMN_ID).getValue();
    		if(((Boolean)box.getValue()).booleanValue() == true){
    			checked++;
    		}
    	}
    	return Integer.toString(checked);
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
    		studyReferenceList = new ArrayList<StudyReference>();
    	}
    	studyReferenceList.add(study);
    	studyEnvironmentMap.put(studyKey, studyReferenceList);
    	
    }
	
	 private Integer getTraitCount(TraitInfo traitInfo, int envId, 
	    		List<GermplasmPair> germplasmPairs, List<ObservationList> obsList){
	    	int counter = 0;
	    	
	    	for(GermplasmPair pair : germplasmPairs){
	    		String keyToChecked1 = traitInfo.getId() + ":" +envId + ":" + pair.getGid1();
	    		String keyToChecked2 = traitInfo.getId() + ":" +envId + ":" + pair.getGid2();
	    		ObservationList obs1 = observationMap.get(keyToChecked1);
	    		ObservationList obs2 = observationMap.get(keyToChecked2);
	    		
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
	    		//System.out.println((obs1 != null && obs2 != null));
	    		if(obs1 != null && obs2 != null){    			
			    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
			    			counter++;
			    			obsList.add(obs1);
			    			obsList.add(obs2);	
			    		}    			
		    		
	    		}
	    		
	    		/*
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
	    		
	    		*/
	    	}
	    	return Integer.valueOf(counter);
	 }
	 
	private boolean isValidDoubleValue(String val){
		if(val != null && !val.equalsIgnoreCase("")){
			try{
				Double.parseDouble(val);
				return true;
			}catch(NumberFormatException ee){
				return false;
			}
		}
		return false;
	}
	
	public void nextButtonClickAction(){
        //this.nextScreen.populateResultsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
    	List<EnvironmentForComparison> toBeCompared = new ArrayList<EnvironmentForComparison>();

    	int total = 0;
    	//get the total of weights
    	for(String sKey : environmentForComparison){
    		EnvironmentForComparison envt = environmentCheckBoxComparisonMap.get(sKey);
    		EnvironmentWeight envtWeight = (EnvironmentWeight) envt.getWeightComboBox().getValue();
    		total += envtWeight.getWeight();
    	}
    	LOG.debug("TOTAL = " + total);
    	
    	for (String sKey : environmentForComparison){
    		EnvironmentForComparison envt = environmentCheckBoxComparisonMap.get(sKey);
    		EnvironmentWeight envtWeight = (EnvironmentWeight) envt.getWeightComboBox().getValue();
    		envt.computeWeight(total);
    		
    		//System.out.println("ENVT: " + envt.getLocationName() + ", weight = " + envt.getWeight());
    		toBeCompared.add(envt);
    	}
    	
    	nextTabAction(toBeCompared);
    }
	
	public void nextTabAction(List<EnvironmentForComparison> toBeCompared){
		if(crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY){
			this.nextScreen1.populateResultsTable(toBeCompared, germplasmIdNameMap, finalGermplasmPairs, observationMap);
	        this.mainScreen1.selectFourthTab();
		}
		else if(crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM){
			if (this.nextScreen2 != null){
	    		this.nextScreen2.populateTraitsTables(toBeCompared);
	    	}
	        this.mainScreen2.selectSecondTab();
		}
	}
	
    public void backButtonClickAction(){
    	if(crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY){
    		this.mainScreen1.selectSecondTab();
    	}
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
    
    public void addEnvironmentalConditionsClickAction(){
    	
    	Window parentWindow = this.getWindow();
        parentWindow.addWindow(addConditionsDialog);
    }
    
    public void clickFilterByLocationApply(List<FilterLocationDto> filterLocationDtoListLevel1, List<FilterLocationDto> filterLocationDtoListLevel3){
    	//MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
    	
    	isFilterLocationClicked = true;
    	filterSetLevel1 = new HashMap<String, String>();
    	filterSetLevel3 = new HashMap<String, String>();
    	
    	    
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
    	filterSetLevel4 = new HashMap<String, String>();
    	for(FilterLocationDto dto : filterLocationDtoListLevel4){
    		String studyName = dto.getStudyName();
        	
    		filterSetLevel4.put(studyName, studyName);
    	}
    	recreateTable(false, true);
    	
    	headerValLabel.setValue("");
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
    
    public void reopenAddEnvironmentConditionsWindow(){
    	//this is to simulate and refresh checkboxes
    	Window parentWindow = this.getWindow();
    	parentWindow.removeWindow(addConditionsDialog);    	    	
        
    	filterStudy.initializeButtons();
        parentWindow.addWindow(addConditionsDialog);
    }
	
	
	/*
     * Callback method for AddEnvironmentalConditionsDialog button
     */
    
    public void addEnviromentalConditionColumns(List<String> names, Set<TrialEnvironmentProperty> conditions){
    	// remove previously added envt conditions columns, if any
    	removeAddedEnvironmentConditionsColumns(names);

    	// add the selected envt condition column(s)
    	for (final TrialEnvironmentProperty condition: conditions){
    		
			this.environmentsTable.addGeneratedColumn(condition.getName(), new ColumnGenerator() {
				private static final long serialVersionUID = 1L;

				@Override
    			public Object generateCell(Table source, Object itemId, Object columnId) {
    				StringTokenizer st = new StringTokenizer((String)itemId, FilterLocationDialog.DELIMITER);
    				
    				String envtIdStr = st.nextToken();
    				if (envtIdStr != null && !envtIdStr.isEmpty()){
    					Integer envtId = Integer.parseInt(envtIdStr);
    					addedEnvironmentColumns.add(condition.getName());
    					
    					return condition.getEnvironmentValuesMap().get(envtId);
    				}
    				
    				return "";
    			}
    			
    		});
    	
    		
    	}	
    }
	
	private void removeAddedEnvironmentConditionsColumns(List<String> columns) {
		if (this.environmentsTable != null && columns != null){
			for (String columnHeader : columns){
				String existingColumn = this.environmentsTable.getColumnHeader(columnHeader);
				if (existingColumn != null && !existingColumn.isEmpty()){
					this.environmentsTable.removeGeneratedColumn(columnHeader);
				}
			}
		}
        this.addedEnvironmentColumns = new ArrayList<String>();
	}
	
	private enum CrossStudyToolType {
		HEAD_TO_HEAD_QUERY(org.generationcp.browser.cross.study.h2h.main.HeadToHeadCrossStudyMain.class, "Head to Head Query"),
		QUERY_FOR_ADAPTED_GERMPLASM(org.generationcp.browser.cross.study.adapted.main.QueryForAdaptedGermplasmMain.class,"Query for Adapted Germplasm");
		
		private Class<?> mainClass;
		private String className;
		
		private CrossStudyToolType(Class<?> mainClass, String className){
			this.mainClass = mainClass;
			this.className = className;
		}

		public Class<?> getMainClass() {
			return mainClass;
		}

		public String getClassName() {
			return className;
		}
		
	}
	
	private void tagAllEnvironments(){
		Object tableItemIds[] = environmentsTable.getItemIds().toArray();
		for(int i=0;i<tableItemIds.length;i++){
			if(environmentsTable.getItem(tableItemIds[i].toString()).getItemProperty(TAG_COLUMN_ID).getValue() instanceof CheckBox)
				((CheckBox) environmentsTable.getItem(tableItemIds[i]).getItemProperty(TAG_COLUMN_ID).getValue()).setValue(true);
		}
	}
	
	private void untagAllEnvironments(){
		Object tableItemIds[] = environmentsTable.getItemIds().toArray();
		for(int i=0;i<tableItemIds.length;i++){
			if(environmentsTable.getItem(tableItemIds[i].toString()).getItemProperty(TAG_COLUMN_ID).getValue() instanceof CheckBox)
				((CheckBox) environmentsTable.getItem(tableItemIds[i]).getItemProperty(TAG_COLUMN_ID).getValue()).setValue(false);
		}		
	}
}
