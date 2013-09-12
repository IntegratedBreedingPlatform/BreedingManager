package org.generationcp.browser.cross.study.h2h;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.browser.cross.study.h2h.listeners.H2HComparisonQueryButtonClickListener;
import org.generationcp.browser.cross.study.h2h.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
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

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;

@Configurable
public class EnvironmentsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3667517088395779496L;
    
    private final static Logger LOG = LoggerFactory.getLogger(EnvironmentsAvailableComponent.class);
    
    private static final String ENV_NUMBER_COLUMN_ID = "EnvironmentsAvailableComponent Env Number Column Id";
    private static final String LOCATION_COLUMN_ID = "EnvironmentsAvailableComponent Location Column Id";
    private static final String COUNTRY_COLUMN_ID = "EnvironmentsAvailableComponent Country Column Id";
    private static final String STUDY_COLUMN_ID = "EnvironmentsAvailableComponent Study Column Id";
    
    public static final String NEXT_BUTTON_ID = "EnvironmentsAvailableComponent Next Button ID";
    public static final String BACK_BUTTON_ID = "EnvironmentsAvailableComponent Back Button ID";
    
    private static final String GET_STUDY_NAME_BY_ENVS_QUERY = "select distinct p.name, e.nd_geolocation_id "
    	+ "from project p join nd_experiment_project ep on p.project_id = ep.project_id "
    	+ "join nd_experiment e on e.nd_experiment_id = ep.nd_experiment_id "
    	+ "join projectprop pp on pp.project_id = p.project_id and pp.type_id = 1011 "
    	+ "where e.nd_geolocation_id in (:envIds)";
    
    private static final String GET_LOCATION_NAME_AND_COUNTRY_NAME_BY_ENVS = "select gp.nd_geolocation_id, l.lname, c.isofull "
    	+ "from nd_geolocationprop gp "
    	+ "join location l on gp.value = l.locid and gp.type_id = 8190 "
    	+ "left join cntry c on c.cntryid = l.cntryid "
    	+ "where gp.nd_geolocation_id in (:envIds)";
    
    private Table environmentsTable;

    private Button nextButton;
    private Button backButton;
    
    private HeadToHeadComparisonMain mainScreen;
    private ResultsComponent nextScreen;
    
    private Integer currentTestEntryGID;
    private Integer currentStandardEntryGID;
    
    private List<TraitForComparison> traitsForComparisonList;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    public EnvironmentsAvailableComponent(HeadToHeadComparisonMain mainScreen, ResultsComponent nextScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
        this.currentStandardEntryGID = null;
        this.currentTestEntryGID = null;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("500px");
        setWidth("1000px");
        
        environmentsTable = new Table();
        environmentsTable.setWidth("950px");
        environmentsTable.setHeight("400px");
        environmentsTable.setImmediate(true);
        environmentsTable.setColumnCollapsingAllowed(true);
        environmentsTable.setColumnReorderingAllowed(true);
        
        Set<String> traitNames = new HashSet<String>();
        createEnvironmentsTable(traitNames);
        
        addComponent(environmentsTable, "top:20px;left:30px");
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        nextButton.setEnabled(false);
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(backButton, "top:450px;left:820px");
    }
    
    public void populateEnvironmentsTable(Integer testEntryGID, Integer standardEntryGID, List<TraitForComparison> traitsForComparisonList){
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
        
        environmentsTable.addContainerProperty(ENV_NUMBER_COLUMN_ID, Integer.class, null);
        environmentsTable.addContainerProperty(LOCATION_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(COUNTRY_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(STUDY_COLUMN_ID, String.class, null);
        
        environmentsTable.setColumnHeader(ENV_NUMBER_COLUMN_ID, "ENV #");
        environmentsTable.setColumnHeader(LOCATION_COLUMN_ID, "LOCATION");
        environmentsTable.setColumnHeader(COUNTRY_COLUMN_ID, "COUNTRY");
        environmentsTable.setColumnHeader(STUDY_COLUMN_ID, "STUDY");
        
        for(String traitName : traitNames){
            environmentsTable.addContainerProperty(traitName, Integer.class, null);
            environmentsTable.setColumnHeader(traitName, traitName);
        }
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
            
            List<Integer> envIds = new ArrayList<Integer>();
            for(Integer key : environmentsMap.keySet()){
                toreturn.add(environmentsMap.get(key));
                envIds.add(key);
            }
            
            //get the study name for each environment
            Query queryForStudyName = dataManagerImpl.getCurrentSessionForCentral().createSQLQuery(GET_STUDY_NAME_BY_ENVS_QUERY);
            queryForStudyName.setParameterList("envIds", envIds);
            List queryForStudyNameResults = queryForStudyName.list();
            for(Object result : queryForStudyNameResults){
            	Object resultArray[] = (Object[]) result;
            	String studyName = (String) resultArray[0];
            	Integer envId = (Integer) resultArray[1];
            	
            	if(envId != null){
            		EnvironmentForComparison env = environmentsMap.get(envId);
            		env.setStudyName(studyName);
            	}
            }
            
            //get the location name and country name for each environment
            Query queryForLocationAndCountry = dataManagerImpl.getCurrentSessionForCentral().createSQLQuery(GET_LOCATION_NAME_AND_COUNTRY_NAME_BY_ENVS);
            queryForLocationAndCountry.setParameterList("envIds", envIds);
            List queryForLocationAndCountryResults = queryForLocationAndCountry.list();
            for(Object result : queryForLocationAndCountryResults){
            	Object resultArray[] = (Object[]) result;
            	Integer envId = (Integer) resultArray[0];
            	String locationName = (String) resultArray[1];
            	String countryName = (String) resultArray[2];
            	
            	EnvironmentForComparison env = environmentsMap.get(envId);
            	env.setCountryName(countryName);
            	env.setLocationName(locationName);
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
        this.nextScreen.populateResultsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
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
