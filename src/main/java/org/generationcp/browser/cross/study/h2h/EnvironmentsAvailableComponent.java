package org.generationcp.browser.cross.study.h2h;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.browser.cross.study.h2h.pojos.EnvironmentForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

@Configurable
public class EnvironmentsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3667517088395779496L;
    
    private static final String ENV_NUMBER_COLUMN_ID = "EnvironmentsAvailableComponent Env Number Column Id";
    private static final String LOCATION_COLUMN_ID = "EnvironmentsAvailableComponent Location Column Id";
    private static final String COUNTRY_COLUMN_ID = "EnvironmentsAvailableComponent Country Column Id";
    private static final String STUDY_COLUMN_ID = "EnvironmentsAvailableComponent Study Column Id";
    
    private Table environmentsTable;

    private Button nextButton;
    private Button backButton;
    
    @Override
    public void afterPropertiesSet() throws Exception {
    	setHeight("500px");
        setWidth("1000px");
        
        environmentsTable = new Table();
        environmentsTable.setWidth("800px");
        environmentsTable.setHeight("400px");
        environmentsTable.setImmediate(true);
        environmentsTable.setColumnCollapsingAllowed(true);
        environmentsTable.setColumnReorderingAllowed(true);
        
        Set<String> traitNames = new HashSet<String>();
        createEnvironmentsTable(traitNames);
        
        addComponent(environmentsTable, "top:20px;left:30px");
        
        nextButton = new Button("Next");
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        addComponent(backButton, "top:450px;left:820px");
    }
    
    public void populateEnvironmentsTable(Integer testEntryGID, Integer standardEntryGID){
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
    
    private List<EnvironmentForComparison> getEnvironmentsForComparison(Integer testEntryGID, Integer standardEntryGID){
        List<EnvironmentForComparison> toreturn = new ArrayList<EnvironmentForComparison>();
        
        Map<String, Integer> mapOne = new HashMap<String, Integer>();
        mapOne.put("GRAIN_YIELD", 1);
        mapOne.put("WEIGHT", 2);
        mapOne.put("BLB", 3);
        
        Map<String, Integer> mapTwo = new HashMap<String, Integer>();
        mapTwo.put("GRAIN_YIELD", 4);
        mapTwo.put("WEIGHT", 5);
        mapTwo.put("PLANT_HEIGHT", 6);
        
        Map<String, Integer> mapThree = new HashMap<String, Integer>();
        mapThree.put("LEAF_WEIGHT", 7);
        mapThree.put("LEAF_LENGTH", 8);
        mapThree.put("RESISTANCE", 9);
        
        if(testEntryGID == 50533 && standardEntryGID == 50532){
            toreturn.add(new EnvironmentForComparison(1, "IRRI", "Philippines", "Study1", mapOne));
            toreturn.add(new EnvironmentForComparison(2, "Los Banos", "Philippines", "Study1", mapOne));
            toreturn.add(new EnvironmentForComparison(3, "Manila", "Philippines", "Study2", mapTwo));
            toreturn.add(new EnvironmentForComparison(4, "Laguna", "Philippines", "Study2", mapOne));
        }
        
        if(testEntryGID == 1 && standardEntryGID == 2){
            toreturn.add(new EnvironmentForComparison(1, "ICRISAT", "India", "Study3", mapThree));
            toreturn.add(new EnvironmentForComparison(2, "CIP", "Peru", "Study3", mapThree));
            toreturn.add(new EnvironmentForComparison(3, "Saskatoon", "Canada", "Study4", mapThree));
            toreturn.add(new EnvironmentForComparison(4, "CIMMYT", "Mexico", "Study4", mapThree));
        }
        
        return toreturn;
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
