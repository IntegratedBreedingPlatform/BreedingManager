package org.generationcp.browser.cross.study.h2h.main;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;


import org.generationcp.browser.application.Message;

import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
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
    
    private static Integer  IMPORTANT = 1;
    private static Integer  CRITICAL = 2;
    private static Integer  DESIRABLE = 3;
    private static Integer  IGNORED = 4;
    
    private Map<CheckBox, Item> environmentCheckBoxMap;
    private List<ComboBox> environmentForComparison; //will contain all the tagged row
    
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
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
        addEnvironment.setEnabled(true);
        
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
        
        Set<String> traitNames = new HashSet<String>();
        createEnvironmentsTable(traitNames);
        
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
    public void populateEnvironmentsTable(List<TraitForComparison> traitForComparisonsList, Map<String, Map<String, TrialEnvironment>>  traitEnvMap, Map<String, TrialEnvironment> trialEnvMap){    
    
    	Iterator<TraitForComparison> iter = traitForComparisonsList.iterator();
    	Map<String, Map<String, TrialEnvironment>>  newTraitEnvMap = new HashMap();
    	Set<String> trialEnvironmentIds = new HashSet();
    	Set<String> traitNames = new HashSet<String>();
    	environmentCheckBoxMap = new HashMap();
    	environmentForComparison = new ArrayList();
    	while(iter.hasNext()){
    		TraitForComparison comparison = iter.next();
    		//System.out.println(comparison.getTraitInfo().getName() + "  " + comparison.getDirection());
    		String id = Integer.toString(comparison.getTraitInfo().getId());
    		if(traitEnvMap.containsKey(id)){
    			Map<String, TrialEnvironment> tempMap = traitEnvMap.get(id);
    			newTraitEnvMap.put(id, tempMap);
    			trialEnvironmentIds.addAll(tempMap.keySet());
    		}
    		traitNames.add(comparison.getTraitInfo().getName());
    	}
    	
    	//get trait names for columns        
        
    	this.environmentsTable.removeAllItems();
    	createEnvironmentsTable(traitNames);
		
    	Map<String, Item> trialEnvIdTableMap = new HashMap();
    	//clean the traitEnvMap
    	Iterator<String> trialEnvIdsIter = trialEnvironmentIds.iterator();
    	while(trialEnvIdsIter.hasNext()){
    		 Integer trialEnvId = Integer.parseInt(trialEnvIdsIter.next());
    		 String trialEnvIdString = trialEnvId.toString();
    		 
    		 if(!trialEnvIdTableMap.containsKey(trialEnvIdString)){
	    		 TrialEnvironment trialEnv = trialEnvMap.get(trialEnvIdString);
	    		//we build the table
	    		 Item item = environmentsTable.addItem(trialEnvId);
	             item.getItemProperty(ENV_NUMBER_COLUMN_ID).setValue(trialEnv.getId());
	             item.getItemProperty(LOCATION_COLUMN_ID).setValue(trialEnv.getLocation().getLocationName());
	             item.getItemProperty(COUNTRY_COLUMN_ID).setValue(trialEnv.getLocation().getCountryName());
	             item.getItemProperty(STUDY_COLUMN_ID).setValue(trialEnv.getStudy().getName());
	             /*
	             for(String traitName : ){
	                 Integer numberOfComparable = environment.getTraitAndNumberOfPairsComparableMap().get(traitName);
	                 item.getItemProperty(traitName).setValue(numberOfComparable);
	             }
	             */
	             Iterator<String> traitNameIter = traitNames.iterator();
	             while(traitNameIter.hasNext()){
	            	 String traitName = traitNameIter.next();
	            	 item.getItemProperty(traitName).setValue(0);
	             }
	             CheckBox box = new CheckBox();
	        	 ComboBox comboBox = getWeightComboBox();
	        	 box.setImmediate(true);
		        	
	             item.getItemProperty(TAG_COLUMN_ID).setValue(box);
	             item.getItemProperty(WEIGHT_COLUMN_ID).setValue(comboBox);
	             box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox));
		        	//traitMaps.put(comboBox, traitsTable.getItem(tableId));
	             environmentCheckBoxMap.put(box, item);
	             trialEnvIdTableMap.put(trialEnvIdString, item);
    		 }
             
    	}
/*
        if(areCurrentGIDsDifferentFromGiven(testEntryGID, standardEntryGID)){
            this.traitsForComparisonList = traitsForComparisonList;
            this.environmentsTable.removeAllItems();
            
            List<EnvironmentForComparison> environments = getEnvironmentsForComparison(testEntryGID, standardEntryGID);
            
            //get trait names for columns
            Set<String> traitNames = new HashSet<String>();
            for(EnvironmentForComparison environment : environments){
                for(String traitName : environment.getTraitAndNumberOfPairsComparableMap().keySet()){
                    traitNames.add(traitName);
                }
            }
            
            createEnvironmentsTable(traitNames);
            
            for(EnvironmentForComparison environment : environments){
                Item item = environmentsTable.addItem(environment.getEnvironmentNumber());
                item.getItemProperty(ENV_NUMBER_COLUMN_ID).setValue(environment.getEnvironmentNumber());
                item.getItemProperty(LOCATION_COLUMN_ID).setValue(environment.getLocationName());
                item.getItemProperty(COUNTRY_COLUMN_ID).setValue(environment.getCountryName());
                item.getItemProperty(STUDY_COLUMN_ID).setValue(environment.getStudyName());
                
                for(String traitName : environment.getTraitAndNumberOfPairsComparableMap().keySet()){
                    Integer numberOfComparable = environment.getTraitAndNumberOfPairsComparableMap().get(traitName);
                    item.getItemProperty(traitName).setValue(numberOfComparable);
                }
            }
            
            this.environmentsTable.requestRepaint();
            
            if(this.environmentsTable.getItemIds().isEmpty()){
                this.nextButton.setEnabled(false);
            } else{
                this.currentStandardEntryGID = standardEntryGID;
                this.currentTestEntryGID = testEntryGID;
                this.nextButton.setEnabled(true);
            }
        } 
        */
    }
    
    private boolean areCurrentGIDsDifferentFromGiven(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.currentTestEntryGID != null && this.currentStandardEntryGID != null){
            if(this.currentTestEntryGID == currentTestEntryGID && this.currentStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
    }
    
    private void createEnvironmentsTable(Set<String> traitNames){
        List<Object> propertyIds = new ArrayList<Object>();
        for(Object propertyId : environmentsTable.getContainerPropertyIds()){
            propertyIds.add(propertyId);
        }
        
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
        
        for(String traitName : traitNames){
            environmentsTable.addContainerProperty(traitName, Integer.class, null);
            environmentsTable.setColumnHeader(traitName, traitName);
        }
        
        environmentsTable.addContainerProperty(WEIGHT_COLUMN_ID, ComboBox.class, null);
        environmentsTable.setColumnHeader(WEIGHT_COLUMN_ID, "Weight");
    }
    
    @SuppressWarnings("rawtypes")
    private List<EnvironmentForComparison> getEnvironmentsForComparison(Integer testEntryGID, Integer standardEntryGID){
        List<EnvironmentForComparison> toreturn = new ArrayList<EnvironmentForComparison>();
        
        try{
            Germplasm testEntry = this.germplasmDataManager.getGermplasmWithPrefName(testEntryGID);
            Germplasm standardEntry = this.germplasmDataManager.getGermplasmWithPrefName(standardEntryGID);
            
            String testEntryPrefName = null;
            if(testEntry.getPreferredName() != null){
                testEntryPrefName = testEntry.getPreferredName().getNval().trim();
            } else{
                MessageNotifier.showWarning(getWindow(), "Warning!", "The germplasm you selected as test entry doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
                return new ArrayList<EnvironmentForComparison>();
            }
            
            String standardEntryPrefName = null;
            if(standardEntry.getPreferredName() != null){
                standardEntryPrefName = standardEntry.getPreferredName().getNval().trim();
            } else{
            MessageNotifier.showWarning(getWindow(), "Warning!", "The standard entry germplasm you selected as standard entry doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
                return new ArrayList<EnvironmentForComparison>();
            }
            
            
            Map<Integer, EnvironmentForComparison> environmentsMap = new HashMap<Integer, EnvironmentForComparison>();
            
            GermplasmDataManagerImpl dataManagerImpl = (GermplasmDataManagerImpl) this.germplasmDataManager;
            String queryString = "call h2h_traitXenv('"+ testEntryPrefName + "','" + standardEntryPrefName + "')";
            Query query = dataManagerImpl.getCurrentSessionForCentral().createSQLQuery(queryString);
            List results = query.list();
            for(Object result : results){
                Object resultArray[] = (Object[]) result;
                Integer locationId = (Integer) resultArray[0];
                String traitName = (String) resultArray[1];
                if(traitName != null){
                    traitName = traitName.trim().toUpperCase();
                }
                
                EnvironmentForComparison environment = environmentsMap.get(locationId);
                if(environment == null){
                    EnvironmentForComparison newEnvironment = new EnvironmentForComparison(locationId, null, null, null, null);
                    environmentsMap.put(locationId, newEnvironment);
                    environment = newEnvironment;
                }
                
                environment.getTraitAndNumberOfPairsComparableMap().put(traitName, Integer.valueOf(1));
            }
            
            for(Integer key : environmentsMap.keySet()){
                toreturn.add(environmentsMap.get(key));
            }
        } catch(MiddlewareQueryException ex){
            ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
            return new ArrayList<EnvironmentForComparison>();
        } catch(Exception ex){
            ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
            return new ArrayList<EnvironmentForComparison>();
        }
        
        return toreturn;
    }
    
    public void nextButtonClickAction(){
        //this.nextScreen.populateResultsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
        this.mainScreen.selectFourthTab();
    }
    
    public void backButtonClickAction(){
        this.mainScreen.selectSecondTab();
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
