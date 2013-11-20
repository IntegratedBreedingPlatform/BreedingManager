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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
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
	private ListManagerTab listManagerTab;
	
	private TabSheet detailsTabSheet;
    private AbsoluteLayout parentLayout;
	private Label heading;
	private Button btnCloseAllTabs;
	private HorizontalLayout headingBar;
	
	private boolean forGermplasmListWindow;
	
    
    private enum ListManagerTab {
    	BROWSE_LISTS, SEARCH_LISTS_GERMPLASMS
    }
    
    public ListManagerDetailsLayout(ListManagerTreeComponent treeComponent, AbsoluteLayout parentLayout, boolean forGermplasmListWindow){
    	this.treeComponent = treeComponent;
    	this.parentLayout = parentLayout;
    	this.forGermplasmListWindow = forGermplasmListWindow;
    	this.listManagerTab = ListManagerTab.BROWSE_LISTS;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		detailsTabSheet = new TabSheet();

	}

	@Override
	public void updateLabels() {

	}
	
    public void createGermplasmListInfoTab(int germplasmListId) throws MiddlewareQueryException {
    	VerticalLayout layout = new VerticalLayout();

        GermplasmList germplasmList=getGermplasmList(germplasmListId);
        
        if (!Util.isTabExist(detailsTabSheet, germplasmList.getName())) {
        	ListManagerTreeMenu component = new ListManagerTreeMenu(this, germplasmListId,
        			germplasmList.getName(),germplasmList.getStatus(), germplasmList.getUserId(), 
        			false, forGermplasmListWindow);
        	layout.addComponent(component);
            
            Tab tab = detailsTabSheet.addTab(layout, germplasmList.getName(), null);
            tab.setClosable(true);
            
            if(detailsTabSheet.getComponentCount() <= 1){
            	
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
            		heading.setValue(messageSource.getMessage(Message.REVIEW_LIST_DETAILS)); 
            		// TODO USE Details for search tab
            	}
        		heading.addStyleName("gcp-content-title");
        		
        		headingBar = new HorizontalLayout();
        		headingBar.setWidth("100%");
        		headingBar.setHeight("30px");
        		headingBar.addComponent(heading);
        		headingBar.addComponent(btnCloseAllTabs);
        		headingBar.setComponentAlignment(heading, Alignment.BOTTOM_LEFT);
        		headingBar.setComponentAlignment(btnCloseAllTabs, Alignment.BOTTOM_RIGHT);
        		
        		//parentLayout.addComponent(heading,"top:30px; left:340px;");
        		//parentLayout.addComponent(btnCloseAllTabs,"top:48px; left:340px;");
        		
        		parentLayout.addComponent(headingBar,"top:20px; left:340px;");
            	parentLayout.addComponent(detailsTabSheet, "top:55px;left:340px");
                parentLayout.setWidth("98%");
                parentLayout.setStyleName(Runo.TABSHEET_SMALL);
            }
            
            detailsTabSheet.setSelectedTab(layout);
            detailsTabSheet.setCloseHandler(new SelectedTabCloseHandler());
            detailsTabSheet.addListener(new GermplasmListTabChangeListener(component));
            detailsTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {
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
        } else {
            Tab tab = Util.getTabAlreadyExist(detailsTabSheet, germplasmList.getName());
            detailsTabSheet.setSelectedTab(tab.getComponent());
        }
    }
    
    public void closeAllListDetailTabButtonClickAction() {
    	Util.closeAllTab(detailsTabSheet);
        parentLayout.removeComponent(headingBar);
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
