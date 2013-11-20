package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTabChangeListener;
import org.generationcp.breeding.manager.util.SelectedTabCloseHandler;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;

@Configurable
public class ListManagerDetailsLayout extends VerticalLayout implements
		InternationalizableComponent, InitializingBean {
	
	public static final String CLOSE_ALL_TABS_ID = "ListManagerTreeComponent Close All Tabs ID";

	private static final long serialVersionUID = 8092751288890434894L;

	@Autowired
	private GermplasmListManager germplasmListManager;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	private ListManagerTreeComponent treeComponent;
	
	private TabSheet detailsTabSheet;
    private AbsoluteLayout parentLayout;
	private Label heading;
	private Button btnCloseAllTabs;
	
	private boolean forGermplasmListWindow;

	
    public ListManagerDetailsLayout(ListManagerTreeComponent treeComponent, AbsoluteLayout parentLayout, boolean forGermplasmListWindow){
    	this.treeComponent = treeComponent;
    	this.parentLayout = parentLayout;
    	this.forGermplasmListWindow = forGermplasmListWindow;
    }
    
    public ListManagerDetailsLayout(AbsoluteLayout parentLayout, boolean forGermplasmListWindow){
    	this.parentLayout = parentLayout;
    	this.forGermplasmListWindow = forGermplasmListWindow;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		detailsTabSheet = new TabSheet();

	}

	@Override
	public void updateLabels() {

	}

	
    public void createGermplasmListInfoTab(int germplasmListId) throws MiddlewareQueryException {
        GermplasmList germplasmList=getGermplasmList(germplasmListId);
        String tabName = germplasmList.getName();
        
		createTab(germplasmListId, germplasmList, tabName);
    }
    
    
    public void createGermplasmInfoTab(int germplasmId) throws MiddlewareQueryException {
        String tabName = "Germplasm - " + germplasmId;
		createTab(germplasmId, null, tabName);
    }

    
	private void createTab(int id, GermplasmList germplasmList, String tabName) {
		
		if (!Util.isTabExist(detailsTabSheet, tabName)) {
			
			VerticalLayout layout = new VerticalLayout();
        	Component component = createTabContent(id, germplasmList, tabName);
        	layout.addComponent(component);
            
            Tab tab = detailsTabSheet.addTab(layout, tabName, null);
            tab.setClosable(true);
            
            if(detailsTabSheet.getComponentCount() <= 1){
            	initializeLayout();
            }
            addListeners(layout, component);
            
            
        } else {
            Tab tab = Util.getTabAlreadyExist(detailsTabSheet, tabName);
            detailsTabSheet.setSelectedTab(tab.getComponent());
        }
	}

	private Component createTabContent(int id, GermplasmList germplasmList, String tabName) {
		
		if (germplasmList != null){
			return new ListManagerTreeMenu(this, id,
					tabName,germplasmList.getStatus(), germplasmList.getUserId(), 
					false, forGermplasmListWindow);
		} else {
			return new BrowseGermplasmTreeMenu(id);
		}
		
	}

	private void addListeners(VerticalLayout layout, Component component) {
		detailsTabSheet.setSelectedTab(layout);
		detailsTabSheet.setCloseHandler(new SelectedTabCloseHandler());
		detailsTabSheet.addListener(new GermplasmListTabChangeListener(component));
		detailsTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {

			private static final long serialVersionUID = -7822326039221887888L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				if(detailsTabSheet.getComponentCount() <= 1){
					btnCloseAllTabs.setVisible(false);
				}
				else{
					btnCloseAllTabs.setVisible(true);
				}
			}
		});
	}

	private void initializeLayout() {
		//reset
		parentLayout.removeComponent(detailsTabSheet);
		
		btnCloseAllTabs = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
		btnCloseAllTabs.setData(CLOSE_ALL_TABS_ID);
		btnCloseAllTabs.setImmediate(true);
		btnCloseAllTabs.setStyleName(Reindeer.BUTTON_LINK);
		btnCloseAllTabs.addListener(new GermplasmListButtonClickListener(this));
		btnCloseAllTabs.setVisible(false);
		
		
		heading = new Label();
		heading.setWidth("300px");
		
		if (this.treeComponent != null){
			heading.setValue(messageSource.getMessage(Message.REVIEW_LIST_DETAILS)); //Browse Lists heading
		} else {
			heading.setValue(messageSource.getMessage(Message.DETAILS)); // Search Lists heading	
		}
		heading.addStyleName("gcp-content-title");
		
		//Browse Lists exact layout
		if (this.treeComponent != null){		
			parentLayout.addComponent(heading,"top:30px; left:340px;");
			parentLayout.addComponent(btnCloseAllTabs,"top:48px; left:340px;");
			parentLayout.addComponent(detailsTabSheet, "top:67px;left:340px");
			
		//Search Lists exact layout
		} else {
			parentLayout.addComponent(heading,"top:90px; left:390px;");
			parentLayout.addComponent(btnCloseAllTabs,"top:108px; left:390px;");
			parentLayout.addComponent(detailsTabSheet, "top:130px;left:390px");

		}
		
		parentLayout.setWidth("98%");
		parentLayout.setStyleName(Runo.TABSHEET_SMALL);
	}
    
    public void closeAllListDetailTabButtonClickAction() {
    	Util.closeAllTab(detailsTabSheet);
        parentLayout.removeComponent(heading);
        parentLayout.removeComponent(btnCloseAllTabs);
        parentLayout.removeComponent(detailsTabSheet);
    }
    
    private GermplasmList getGermplasmList(int germplasmListId) throws MiddlewareQueryException {
        return this.germplasmListManager.getGermplasmListById(germplasmListId);
    }
    
    public TabSheet getTabSheet(){
    	return this.detailsTabSheet;
    }
    
    public ListManagerTreeComponent getTreeComponent(){
    	return this.treeComponent;
    }

}
