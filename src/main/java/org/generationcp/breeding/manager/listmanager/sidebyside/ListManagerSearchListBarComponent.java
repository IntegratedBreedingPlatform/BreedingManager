package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Runo;

@Configurable
public class ListManagerSearchListBarComponent extends AbsoluteLayout implements
	InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	public static final String SEARCH_BUTTON = "List Manager Search Button";
	private static final String GUIDE = 
	        "You may search for germplasms and germplasm lists using GID's, germplasm names (partial/full), or list names (partial/full)" +
	        " <br/><br/><b>Matching lists would contain</b> <br/>" +
	        "  - Lists with names containing the search query <br/>" +
	        "  - Lists containing germplasms given a GID <br/>" +
	        "  - Lists containing germplasms with names <br/>" +
	        " containing the search query" +
	        " <br/><br/><b>Matching germplasms would contain</b> <br/>" +
	        "  - Germplasms with matching GID's <br/>" +
	        "  - Germplasms with name containing search query <br/>" +
	        "  - Parents of the result germplasms (if selected)" +
	        " <br/><br/>The <b>Exact matches only</b> checkbox allows you search using partial names (when unchecked)" +
	        " or to only return results which match the query exactly (when checked).";
	
	private AbsoluteLayout searchBar;
	private Label searchLabel;
	private TextField searchField;
	private SearchResultsComponent searchResultsComponent;
	private Button searchButton;
    private CheckBox likeOrEqualCheckBox;
    private CheckBox includeParentsCheckBox;

    private Table matchingListsTable;
    private Table matchingGermplasmsTable;
    
    Panel searchPanel;
    
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	

	public ListManagerSearchListBarComponent() {
		super();
	}
	
	public ListManagerSearchListBarComponent(Table matchingListsTable, Table matchingGermplasmsTable) {
		super();
		this.matchingGermplasmsTable = matchingGermplasmsTable;
		this.matchingListsTable = matchingListsTable;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		addStyleName("searchPaneLayout");
		
		searchPanel = new Panel();
        searchPanel.addStyleName("search-panel");
        searchPanel.addStyleName(Runo.PANEL_LIGHT);
        searchPanel.addStyleName("list-manager-search-bar");
        searchPanel.setScrollable(false);
        
        searchBar = new AbsoluteLayout();
        searchBar.setWidth("80%");
        searchBar.setHeight("40px");
        
        searchLabel = new Label();
        searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+": ");
        searchLabel.setWidth("200px");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
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

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        PopupView popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        likeOrEqualCheckBox = new CheckBox();
        likeOrEqualCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
        includeParentsCheckBox = new CheckBox();
        includeParentsCheckBox.setCaption(messageSource.getMessage(Message.INCLUDE_PARENTS));
        
        searchBar.addComponent(searchLabel, "top:13px; left:20px;");
        searchBar.addComponent(searchField, "top:10px; left:100px;");
        searchBar.addComponent(searchButton, "top:8px; left:280px;");
        searchBar.addComponent(popup, "top:12px; left:330px;");
        searchBar.addComponent(likeOrEqualCheckBox, "top:13px; left: 350px;");
        searchBar.addComponent(includeParentsCheckBox, "top:13px; left: 500px;");
        
        searchPanel.setLayout(searchBar);
       
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutComponents() {
		 addComponent(searchPanel, "top:10px; left:20px;");
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

}
