package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmSearchBarComponent extends CssLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmSearchBarComponent.class);
    private static final long serialVersionUID = 1L;
	
	public static final String SEARCH_BUTTON = "List Manager Germplasm Search Button";
	private static final String GUIDE = 
	        "You may search for germplasm and germplasm lists using GIDs, Stock IDs, or partial or full germplasm names." +
	        "<br/><br/><b>The search results will show:</b>" +
	        "<ul>"	+	
	        "<li>Germplasm with matching GIDs</li>" +
	        "<li>Germplasm with matching Stock IDs</li>" +
	        "<li>Germplasm with name(s) containing the search term</li>" +
	        "<li>Parents of the matching germplasm (if selected)</li>" +
	        "</ul>";
	
    public static final String LM_COMPONENT_WRAP = "lm-component-wrap";
	private static final String PERCENT = "%";

    private HorizontalLayout searchBarLayoutLeft;
	private CssLayout searchBarLayoutRight;
	private TextField searchField;
	private final GermplasmSearchResultsComponent searchResultsComponent;
	private Button searchButton;
    private CheckBox withInventoryOnlyCheckBox;
    private CheckBox includeParentsCheckBox;
    private PopupView popup;
    private String matchesStartingWith;
    private String exactMatches;
	private String matchesContaining;
    private OptionGroup searchTypeOptions;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	

    @Autowired
    private BreedingManagerService breedingManagerService;
	
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
        
        withInventoryOnlyCheckBox = new CheckBox();
        withInventoryOnlyCheckBox.setValue(false);
        withInventoryOnlyCheckBox.setCaption(messageSource.getMessage(Message.WITH_INVENTORY_ONLY));
        
        includeParentsCheckBox = new CheckBox();
        includeParentsCheckBox.setValue(false);
        includeParentsCheckBox.setCaption(messageSource.getMessage(Message.INCLUDE_PARENTS));
        
        matchesStartingWith = messageSource.getMessage(Message.MATCHES_STARTING_WITH);
        exactMatches = messageSource.getMessage(Message.EXACT_MATCHES);
        matchesContaining = messageSource.getMessage(Message.MATCHES_CONTAINING);
        
        List<String> searchTypes = Arrays.asList(new String[] {
        		matchesStartingWith,exactMatches,matchesContaining});
        searchTypeOptions = new OptionGroup(null, searchTypes);
        searchTypeOptions.setValue(matchesStartingWith);
        searchTypeOptions.setStyleName("v-select-optiongroup-horizontal");
    }

	@Override
	public void initializeValues() {
		//Auto-generated method stub
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
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(getFirstRow());
		verticalLayout.addComponent(getSecondRow());
		addComponent(verticalLayout);
        focusOnSearchField();
	}

	private Component getFirstRow() {
		searchBarLayoutLeft = new HorizontalLayout();
		searchBarLayoutLeft.setSpacing(true);
		searchBarLayoutRight = new CssLayout();
		
		// To allow for all of the elements to fit in the default width of the search bar. There may be a better way..
		searchField.setWidth("120px");		
		
		searchBarLayoutLeft.addComponent(searchField);
		searchBarLayoutLeft.addComponent(searchButton);
		searchBarLayoutLeft.addComponent(popup);
		
		withInventoryOnlyCheckBox.addStyleName(LM_COMPONENT_WRAP);
		includeParentsCheckBox.addStyleName(LM_COMPONENT_WRAP);
		
		searchBarLayoutRight.addComponent(withInventoryOnlyCheckBox);
		searchBarLayoutRight.addComponent(includeParentsCheckBox);
		searchBarLayoutLeft.setComponentAlignment(popup, Alignment.MIDDLE_CENTER);
		
		CssLayout firstRow = new CssLayout();
		firstRow.addComponent(searchBarLayoutLeft);
		firstRow.addComponent(searchBarLayoutRight);
		firstRow.setHeight("34px");
		return firstRow;
	}
	
	private Component getSecondRow() {
		return searchTypeOptions;
	}

	public void focusOnSearchField(){
		searchField.focus();
		searchField.selectAll();
	}
	
	@Override
	public void updateLabels() {
		//Auto-generated method stub
		
	}

	public void searchButtonClickAction(){
		String q = searchField.getValue().toString();
		doSearch(q);

    }
	
	public void doSearch(String q) {
		Monitor monitor = MonitorFactory.start("GermplasmSearchBarComponent.doSearch()");
		String searchType = (String)searchTypeOptions.getValue();
		String searchKeyword = getSearchKeyword(q,searchType);
        Operation operation = exactMatches.equals(searchType) ? Operation.EQUAL : Operation.LIKE;

        try {
        	boolean includeParents = (Boolean) includeParentsCheckBox.getValue();
    		boolean withInventoryOnly = (Boolean) withInventoryOnlyCheckBox.getValue();
    		searchResultsComponent.applyGermplasmResults(breedingManagerService.doGermplasmSearch(
            		searchKeyword,operation,includeParents,withInventoryOnly));
        } catch (BreedingManagerSearchException e) {
            if (Message.SEARCH_QUERY_CANNOT_BE_EMPTY.equals(e.getErrorMessage())) {
                // invalid search string
                MessageNotifier.showWarning(this.getWindow(),messageSource.getMessage(Message.UNABLE_TO_SEARCH),messageSource.getMessage(e.getErrorMessage()));
            } else {
                // case for no results, database error
                MessageNotifier.showWarning(this.getWindow(),messageSource.getMessage(Message.SEARCH_RESULTS),messageSource.getMessage(e.getErrorMessage()));
            }
            LOG.info(e.getMessage(), e);
        } finally {
        	LOG.debug("" + monitor.stop());
        }
	}

	private String getSearchKeyword(String query, String searchType) {
		String searchKeyword = query;
		if(matchesStartingWith.equals(searchType)) {
        	searchKeyword = searchKeyword+PERCENT;
        } else if(matchesContaining.equals(searchType)) {
        	searchKeyword = PERCENT+searchKeyword+PERCENT;
        }
        return searchKeyword;
	}

	public CheckBox getIncludeParentsCheckBox() {
		return includeParentsCheckBox;
	}

	public void setIncludeParentsCheckBox(CheckBox includeParentsCheckBox) {
		this.includeParentsCheckBox = includeParentsCheckBox;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public BreedingManagerService getBreedingManagerService() {
		return breedingManagerService;
	}

	public void setBreedingManagerService(
			BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public OptionGroup getSearchTypeOptions() {
		return searchTypeOptions;
	}

	public CheckBox getWithInventoryOnlyCheckBox() {
		return withInventoryOnlyCheckBox;
	}

	public void setSearchTypeOptions(OptionGroup searchTypeOptions) {
		this.searchTypeOptions = searchTypeOptions;
	}
	
	
	
	
}
