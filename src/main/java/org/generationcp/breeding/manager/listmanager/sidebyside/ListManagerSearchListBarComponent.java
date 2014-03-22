package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class ListManagerSearchListBarComponent extends HorizontalLayout implements
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
	
	private AbsoluteLayout searchBarLayout;
	private Label searchLabel;
	private TextField searchField;
	private SearchResultsComponent searchResultsComponent;
	private Button searchButton;
    private CheckBox likeOrEqualCheckBox;
    private CheckBox includeParentsCheckBox;
    private PopupView popup;

    private Panel searchPanel;
    
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
		
	public ListManagerSearchListBarComponent(SearchResultsComponent searchResultsComponent) {
		super();
		this.searchResultsComponent = searchResultsComponent;
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
		searchPanel.setWidth("100%");
		searchPanel.setHeight("45px");
        
        //searchPanel.setScrollable(false);
        
        searchLabel = new Label();
        searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+": ");
        searchLabel.setWidth("200px");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
        searchButton = new Button();
        searchButton.setWidth("30px");
        searchButton.setHeight("30px");
        searchButton.setStyleName(BaseTheme.BUTTON_LINK);
        searchButton.addStyleName("search-button");
        searchButton.setData(SEARCH_BUTTON);
        searchButton.addListener(new GermplasmListManagerButtonClickListener(this));

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        likeOrEqualCheckBox = new CheckBox();
        likeOrEqualCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
        includeParentsCheckBox = new CheckBox();
        includeParentsCheckBox.setCaption(messageSource.getMessage(Message.INCLUDE_PARENTS));
    }

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1926462184420334992L;

			@Override
			public void buttonClick(ClickEvent event) {
				searchButtonClickAction();	
			}
		});
		
		searchPanel.addAction(new ShortcutListener("Next field", KeyCode.ENTER, null) {
            private static final long serialVersionUID = 288627665348761948L;

            @Override
            public void handleAction(Object sender, Object target) {
                searchButtonClickAction();
            }
        });

	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		searchBarLayout = new AbsoluteLayout();
		searchBarLayout.setHeight("40px");
		searchBarLayout.addStyleName("searchBarLayout");
		searchBarLayout.addComponent(searchLabel, "top:13px; left:20px;");
        searchBarLayout.addComponent(searchField, "top:10px; left:100px;");
        searchBarLayout.addComponent(searchButton, "top:8px; left:265px;");
        searchBarLayout.addComponent(popup, "top:12px; left:315px;");
        searchBarLayout.addComponent(likeOrEqualCheckBox, "top:13px; left: 335px;");
        searchBarLayout.addComponent(includeParentsCheckBox, "top:13px; left: 485px;");
        
        searchPanel.setLayout(searchBarLayout);
        
        setWidth("99%");
        setHeight("75px");
        setMargin(true);
		addComponent(searchPanel);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	public void searchButtonClickAction(){
		String q = searchField.getValue().toString();
		doSearch(q);
	}
	
	public void doSearch(String q){
		try {
			
			List<GermplasmList> germplasmLists;
			List<Germplasm> germplasms;
			boolean includeParents = (Boolean) includeParentsCheckBox.getValue();
			if((Boolean) likeOrEqualCheckBox.getValue() == true){
				germplasmLists = doGermplasmListSearch(q, Operation.EQUAL);
				germplasms = doGermplasmSearch(q, Operation.EQUAL, includeParents);
			} else {
				germplasmLists = doGermplasmListSearch(q, Operation.LIKE);
				germplasms = doGermplasmSearch(q, Operation.LIKE, includeParents);
			}
			
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
	
	private List<GermplasmList> doGermplasmListSearch(String q, Operation o) throws MiddlewareQueryException{
		return germplasmListManager.searchForGermplasmList(q, o);
	}
	
	private List<Germplasm> doGermplasmSearch(String q, Operation o, boolean includeParents) throws MiddlewareQueryException{
		return germplasmDataManager.searchForGermplasm(q, o, includeParents);
	}
}
