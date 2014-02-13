package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerSidebysideMain extends VerticalLayout implements
		InternationalizableComponent, InitializingBean {

    private static final long serialVersionUID = 5976245899964745758L;
    
    private Button toggleButton;
	private HorizontalSplitPanel splitPanel;

	private static Float EXPANDED_SPLIT_POSITION = Float.valueOf("50");
	private static Float COLLAPSED_SPLIT_POSITION = Float.valueOf("96");

    private TabSheet tabSheet; 
    private ListManagerBrowseListComponent browseListsComponent;
    private ListManagerSearchListComponent searchListsComponent;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    @Override
    public void afterPropertiesSet() throws Exception {

    	setSizeFull();
    	
    	setMargin(false);
        setSpacing(false);
    	
		Label content = new Label("Content");

		toggleButton = new Button();
		toggleButton.addListener(new ClickListener(){
			public void buttonClick(ClickEvent event) {
				if(splitPanel.getSplitPosition() == EXPANDED_SPLIT_POSITION){
					collapse();
				} else {
					expand();
				}
			}
		});
		
		splitPanel = new HorizontalSplitPanel();
		splitPanel.setSizeFull();
		splitPanel.setMargin(false);
		
		collapse();

		//Attach browse/search lists tabsheet here
		
        browseListsComponent = new ListManagerBrowseListComponent();
        searchListsComponent = new ListManagerSearchListComponent();

        tabSheet = new TabSheet();
        tabSheet.addTab(browseListsComponent, messageSource.getMessage(Message.BROWSE_LISTS));
        tabSheet.addTab(searchListsComponent, messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        tabSheet.setHeight("600px");
        tabSheet.setWidth("100%");

		splitPanel.setFirstComponent(tabSheet);
		
		//Attach build new list here
		splitPanel.setSecondComponent(toggleButton);
        
		addComponent(splitPanel);

    }
    
    private void expand(){
    	splitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION);
    	toggleButton.setCaption(">>");
    }

    private void collapse(){
    	splitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION);
    	toggleButton.setCaption("<<");
    }

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
