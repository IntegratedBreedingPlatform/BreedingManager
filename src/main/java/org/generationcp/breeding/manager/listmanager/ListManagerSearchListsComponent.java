package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.EnterShortcutListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Runo;

@Configurable
public class ListManagerSearchListsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 5314653969843976836L;
	public static final String SEARCH_BUTTON = "List Manager Search Button";

	private AbsoluteLayout searchBar;
	private Label searchLabel;
	private TextField searchField;
	private SearchResultsComponent searchResultsComponent;
	private ListManagerMain listManagerMain;
	private Button searchButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	private AbsoluteLayout searchPanel;
	
	
	public ListManagerSearchListsComponent(ListManagerMain listManagerMain){
		this.listManagerMain = listManagerMain;
	}
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
    @Override
	public void afterPropertiesSet() throws Exception {
	    
        
        Panel p = new Panel();
        //p.setWidth("100%");
        //p.setHeight("47px");
        p.addStyleName("search-panel");
        p.addStyleName(Runo.PANEL_LIGHT);
        p.addStyleName("list-manager-search-bar");
        p.setScrollable(false);
        
        searchBar = new AbsoluteLayout();
        searchBar.setWidth("80%");
        searchBar.setHeight("40px");
        //searchBar.addStyleName("list-manager-search-bar");
        
        searchLabel = new Label();
        searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+": ");
        searchLabel.setWidth("200px");
        
        searchField = new TextField();
        
        /**
         * TODO: replace with image button
         */
        
        
        searchButton = new Button();
        searchButton.setWidth("30px");
        searchButton.setHeight("30px");
        searchButton.setStyleName(BaseTheme.BUTTON_LINK);
        searchButton.addStyleName("search-button");
        searchButton.setData(SEARCH_BUTTON);
        searchButton.addListener(new GermplasmListManagerButtonClickListener(this));
        //searchButton.setClickShortcut(KeyCode.ENTER);
        /*
        p.addComponent(searchLabel);
        p.addComponent(searchField);
        p.addComponent(searchButton);
        */
        p.addAction(new ShortcutListener("Next field", KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                // The panel is the sender, loop trough content
                searchButtonClickAction();
            }
        });
        
            
            //p.addComponent(searchButton);
        //searchPanel.addComponent(p, "top:0px; left:3px;");
       
        
        searchBar.addComponent(searchLabel, "top:13px; left:20px;");
        searchBar.addComponent(searchField, "top:10px; left:100px;");
        searchBar.addComponent(searchButton, "top:8px; left:255px;");
        
        
        searchResultsComponent = new SearchResultsComponent(this.listManagerMain, this);
        
        //addComponent(searchBar, "top:20px; left:20px;");
        //p.addComponent(searchBar);
        p.setLayout(searchBar);
        addComponent(p, "top:20px; left:20px;");
        addComponent(searchResultsComponent, "top:90px; left:20px;");
        
        
	    

	}

	public void searchButtonClickAction(){
		String q = searchField.getValue().toString();
		System.out.println("Search button clicked! Searching for '"+q+"'");
		doSearch(q);
	}
	
	public void doSearch(String q){
		try {
			List<GermplasmList> germplasmLists = doGermplasmListSearch(q);
			List<Germplasm> germplasms = doGermplasmSearch(q);
			if ((germplasmLists == null || germplasmLists.isEmpty()) &&
					(germplasms == null || germplasms.isEmpty())){
				MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.SEARCH_RESULTS), 
						messageSource.getMessage(Message.NO_SEARCH_RESULTS), Notification.POSITION_CENTERED);
			} 
			searchResultsComponent.applyGermplasmListResults(germplasmLists);
			searchResultsComponent.applyGermplasmResults(germplasms);
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private List<GermplasmList> doGermplasmListSearch(String q) throws MiddlewareQueryException{
		return germplasmListManager.searchForGermplasmList(q);
	}
	
	private List<Germplasm> doGermplasmSearch(String q) throws MiddlewareQueryException{
		return germplasmDataManager.searchForGermplasm(q);
	}	
	
	public SearchResultsComponent getSearchResultsComponent(){
		return searchResultsComponent;
	}
	
}
