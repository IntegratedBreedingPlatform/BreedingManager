package org.generationcp.browser.cross.study.h2h;

import java.util.List;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Table;

@Configurable
public class EnvironmentsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3667517088395779496L;
    
    private static final String ENV_NUMBER_COLUMN_ID = "EnvironmentsAvailableComponent Env Number Column Id";
    private static final String LOCATION_COLUMN_ID = "EnvironmentsAvailableComponent Location Column Id";
    private static final String COUNTRY_COLUMN_ID = "EnvironmentsAvailableComponent Country Column Id";
    private static final String STUDY_COLUMN_ID = "EnvironmentsAvailableComponent Study Column Id";
    
    private Table environmentsTable;

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        
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
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
