package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class ListManagerSearchListsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 5314653969843976836L;
	public static final String SEARCH_BUTTON = "List Manager Search Button";

	private AbsoluteLayout searchBar;
	private Label searchLabel;
	private TextField searchField;
	private SearchResultsComponent searchResultsComponent;
	private Button searchButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		searchBar = new AbsoluteLayout();
		searchBar.setWidth("98%");
		searchBar.setHeight("45px");
		searchBar.addStyleName("list-manager-search-bar");
		
		searchLabel = new Label();
		searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+":");
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
		searchButton.setClickShortcut(KeyCode.ENTER);
		
		searchBar.addComponent(searchLabel, "top:13px; left:20px;");
		searchBar.addComponent(searchField, "top:10px; left:100px;");
		searchBar.addComponent(searchButton, "top:8px; left:255px;");
		
		searchResultsComponent = new SearchResultsComponent();
		
		addComponent(searchBar, "top:20px; left:20px;");
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
	
}
