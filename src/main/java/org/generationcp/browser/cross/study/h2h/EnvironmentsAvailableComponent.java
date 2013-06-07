package org.generationcp.browser.cross.study.h2h;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

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
        
        List<String> traitNames = new ArrayList<String>();
        traitNames.add("GRAIN_YIELD");
        traitNames.add("PLANT_HEIGHT");
        traitNames.add("BLB");
        traitNames.add("LEAF_COLOR");
        traitNames.add("NITROGEN");
        traitNames.add("GRAIN_WEIGHT");
        createEnvironmentsTable(traitNames);
        
        addComponent(environmentsTable, "top:20px;left:30px");
        
        nextButton = new Button("Next");
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        addComponent(backButton, "top:450px;left:820px");
    }
    
    private void createEnvironmentsTable(List<String> traitNames){
        for(Object propertyId : environmentsTable.getContainerPropertyIds()){
            environmentsTable.removeContainerProperty(propertyId);
        }
        
        environmentsTable.addContainerProperty(ENV_NUMBER_COLUMN_ID, Integer.class, null);
        environmentsTable.addContainerProperty(LOCATION_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(STUDY_COLUMN_ID, String.class, null);
        environmentsTable.addContainerProperty(STUDY_COLUMN_ID, String.class, null);
        
        environmentsTable.setColumnHeader(ENV_NUMBER_COLUMN_ID, "ENV #");
        environmentsTable.setColumnHeader(LOCATION_COLUMN_ID, "LOCATION");
        environmentsTable.setColumnHeader(COUNTRY_COLUMN_ID, "COUNTRY");
        environmentsTable.setColumnHeader(STUDY_COLUMN_ID, "STUDY");
        
        int idNumber = 1;
        for(String traitName : traitNames){
        	String traitId = traitName + idNumber;
        	
        	environmentsTable.addContainerProperty(traitId, Integer.class, null);
        	environmentsTable.setColumnHeader(traitId, traitName);
        	idNumber++;
        }
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
