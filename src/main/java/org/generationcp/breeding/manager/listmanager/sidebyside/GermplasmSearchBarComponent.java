package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;

@Configurable
public class GermplasmSearchBarComponent extends CssLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	public static final String SEARCH_BUTTON = "List Manager Germplasm Search Button";
	private static final String GUIDE = 
	        "You may search for germplasm and germplasm lists using GIDs, or partial or full germplasm names." +
	        "<br/><br/><b>The search results will show:</b>" +
	        "<ul>"	+	
	        "<li>Germplasm with matching GIDs</li>" +
	        "<li>Germplasm with name(s) containing the search term</li>" +
	        "<li>Parents of the matching germplasm (if selected)</li>" +
	        "</ul>" +
	        "The <b>Exact matches only</b> checkbox shows results that match the search term exactly when checked. " +
	        " If you uncheck this option, the search will show results that contain the search term you enter." + 
	        " <br/><br/>The <b>Search public data</b> checkbox allows you to search public (central) data, in addition to the local germplasm data.";
	
	private HorizontalLayout searchBarLayoutLeft;
	private CssLayout searchBarLayoutRight;
	private TextField searchField;
	private final GermplasmSearchResultsComponent searchResultsComponent;
	private Button searchButton;
    private CheckBox exactMatchesOnlyCheckBox;
    private CheckBox includeParentsCheckBox;
    private CheckBox searchPublicDataCheckBox;
    private PopupView popup;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
		
	public GermplasmSearchBarComponent(final GermplasmSearchResultsComponent searchResultsComponent) {
		super();
		this.searchResultsComponent = searchResultsComponent;
	}
	
    public TextField getSearchField() {
        return searchField;
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
		
		setWidth("100%");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
        searchButton = new Button(messageSource.getMessage(Message.SEARCH));
        searchButton.setHeight("24px");
        searchButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        searchButton.setData(SEARCH_BUTTON);
        searchButton.setClickShortcut(KeyCode.ENTER);

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        exactMatchesOnlyCheckBox = new CheckBox();
        exactMatchesOnlyCheckBox.setValue(true);
        exactMatchesOnlyCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
        includeParentsCheckBox = new CheckBox();
        includeParentsCheckBox.setCaption(messageSource.getMessage(Message.INCLUDE_PARENTS));
        
        searchPublicDataCheckBox = new CheckBox();
        searchPublicDataCheckBox.setCaption(messageSource.getMessage(Message.SEARCH_PUBLIC_DATA));
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
	}

	@Override
	public void layoutComponents() {
		setMargin(true);
		addStyleName("lm-search-bar");
		setWidth("100%");
		
		searchBarLayoutLeft = new HorizontalLayout();
		//searchBarLayoutLeft.setHeight("24px");
		searchBarLayoutLeft.setSpacing(true);
		
		searchBarLayoutRight = new CssLayout();
		//searchBarLayoutRight.setHeight("24px");
		
		// To allow for all of the elements to fit in the default width of the search bar. There may be a better way..
		searchField.setWidth("120px");		
		
		searchBarLayoutLeft.addComponent(searchField);
		searchBarLayoutLeft.addComponent(searchButton);
		searchBarLayoutLeft.addComponent(popup);
		
		exactMatchesOnlyCheckBox.addStyleName("lm-component-wrap");
		includeParentsCheckBox.addStyleName("lm-component-wrap");
		searchPublicDataCheckBox.addStyleName("lm-component-wrap");
		
		searchBarLayoutRight.addComponent(exactMatchesOnlyCheckBox);
		searchBarLayoutRight.addComponent(includeParentsCheckBox);
		searchBarLayoutRight.addComponent(searchPublicDataCheckBox);
		
		searchBarLayoutLeft.setComponentAlignment(popup, Alignment.MIDDLE_CENTER);
		
		//searchBarLayoutRight.setComponentAlignment(exactMatchesOnlyCheckBox, Alignment.MIDDLE_CENTER);
        //searchBarLayoutRight.setComponentAlignment(includeParentsCheckBox, Alignment.MIDDLE_CENTER);
        //searchBarLayoutRight.setComponentAlignment(searchPublicDataCheckBox, Alignment.MIDDLE_CENTER);
        
        addComponent(searchBarLayoutLeft);
        addComponent(searchBarLayoutRight);
        
       
        
        focusOnSearchField();
	}

	public void focusOnSearchField(){
		searchField.focus();
		searchField.selectAll();
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
		
		if(q.replaceAll(" ", "").trim().equals("")){
			MessageNotifier.showWarning(getWindow(),
					messageSource.getMessage(Message.UNABLE_TO_SEARCH),
					messageSource.getMessage(Message.SEARCH_QUERY_CANNOT_BE_EMPTY));
			return;
		}
		
		try {
			boolean includeParents = (Boolean) includeParentsCheckBox.getValue();
			boolean searchPublicData = (Boolean) searchPublicDataCheckBox.getValue(); 
			boolean exactMatchesOnly = (Boolean) exactMatchesOnlyCheckBox.getValue();
			
			List<Germplasm> germplasms = doGermplasmSearch(q, exactMatchesOnly ? Operation.EQUAL : Operation.LIKE, includeParents, searchPublicData);
			
			if (germplasms == null || germplasms.isEmpty()) {
				MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.SEARCH_RESULTS), 
						messageSource.getMessage(Message.NO_SEARCH_RESULTS));
			} 
			searchResultsComponent.applyGermplasmResults(germplasms);
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}
	
	private List<Germplasm> doGermplasmSearch(String q, Operation o, boolean includeParents, boolean searchPublicData) throws MiddlewareQueryException{
		return germplasmDataManager.searchForGermplasm(q, o, includeParents, searchPublicData);
	}
}
