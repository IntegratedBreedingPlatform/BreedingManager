package org.generationcp.browser.cross.study.h2h;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

@Configurable
public class TraitsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 991899235025710803L;
    
    private static final String TRAIT_COLUMN_ID = "TraitsAvailableComponent Trait Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "TraitsAvailableComponent Number of Environments Column Id";
    
    private Table traitsTable;
    
    private Button nextButton;
    private Button backButton;

    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("500px");
        setWidth("1000px");
        
        traitsTable = new Table();
        traitsTable.setWidth("500px");
        traitsTable.setHeight("400px");
        
        traitsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
        traitsTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
        
        traitsTable.setColumnHeader(TRAIT_COLUMN_ID, "TRAIT");
        traitsTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# OF ENV");
        
        addComponent(traitsTable, "top:20px;left:30px");
        
        nextButton = new Button("Next");
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        addComponent(backButton, "top:450px;left:820px");
    }

    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
}
