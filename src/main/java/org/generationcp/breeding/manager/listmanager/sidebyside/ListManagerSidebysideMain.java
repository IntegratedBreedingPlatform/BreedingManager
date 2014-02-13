package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerSidebysideMain extends VerticalLayout implements
		InternationalizableComponent, InitializingBean {

    private static final long serialVersionUID = 5976245899964745758L;

    private HorizontalSplitPanel horiSplitPanel;    	
    private TabSheet tabSheet; 
    private ListManagerBrowseListComponent browseListsComponent;
    private ListManagerSearchListComponent searchListsComponent;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    	
    @Override
    public void afterPropertiesSet() throws Exception {
        horiSplitPanel = new HorizontalSplitPanel();
        horiSplitPanel.setSplitPosition(50); // percent
        horiSplitPanel.setSizeFull();
        
        browseListsComponent = new ListManagerBrowseListComponent();
        searchListsComponent = new ListManagerSearchListComponent();
        
        tabSheet = new TabSheet();
        tabSheet.addTab(browseListsComponent, messageSource.getMessage(Message.BROWSE_LISTS));
        tabSheet.addTab(searchListsComponent, messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        tabSheet.setHeight("600px");
        tabSheet.setWidth("100%");
        
        // left component:
        horiSplitPanel.addComponent(tabSheet);
        
        // right component:
        horiSplitPanel.addComponent(new Label("Right Side"));
        
        addComponent(horiSplitPanel);
    }

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
