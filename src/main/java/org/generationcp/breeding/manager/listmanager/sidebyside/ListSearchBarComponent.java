package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

@Configurable
public class ListSearchBarComponent extends Panel implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	public static final String SEARCH_BUTTON = "List Manager Search Button";
	private static final String GUIDE = "You may search for germplasm lists using partial or full germplasm names or list names, or GIDs."
			+ " <br/><br/><b>The search results will show lists in which: </b>"
			+ " <ul>"
			+ "  <li>The list name contains the search term </li>"
			+ "  <li>The list description contains the search term </li>"
			+ "  <li>The list contains germplasm with names that contain the search term </li>"
			+ "  <li>The list contains germplasm with GIDs that contain the search term </li>"
			+ " </ul>"
			+ " The <b>Exact matches only</b> checkbox shows results that match the search "
			+ " term exactly when checked. If you uncheck this option, the search  "
			+ " will show results that contain the search term you enter.";
	        //+ " <br/><br/>The <b>Search public data</b> checkbox allows you to search public (central) data, in addition to the local germplasm list data.";

	private HorizontalLayout searchBarLayout;
	private TextField searchField;
	private final ListSearchResultsComponent searchResultsComponent;
	private Button searchButton;
	private CheckBox exactMatchesOnlyCheckBox;
	private CheckBox searchPublicDataCheckBox;
	private PopupView popup;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public ListSearchBarComponent(final ListSearchResultsComponent searchResultsComponent) {
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
		
		setWidth("100%");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
        searchButton = new Button(messageSource.getMessage(Message.SEARCH));
        searchButton.setHeight("24px");
        searchButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        searchButton.setData(SEARCH_BUTTON);
        searchButton.addListener(new GermplasmListManagerButtonClickListener(this));
        searchButton.setClickShortcut(KeyCode.ENTER);

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        exactMatchesOnlyCheckBox = new CheckBox();
        exactMatchesOnlyCheckBox.setValue(false);
        exactMatchesOnlyCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
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

		addAction(new ShortcutListener("Next field", KeyCode.ENTER,
				null) {
			private static final long serialVersionUID = 288627665348761948L;

			@Override
			public void handleAction(Object sender, Object target) {
				searchButtonClickAction();
			}
		});

	}

	@Override
	public void layoutComponents() {
		final CssLayout panelLayout = new CssLayout();
		panelLayout.setMargin(true);
		panelLayout.addStyleName("lm-search-bar");
		
		searchBarLayout = new HorizontalLayout();
		searchBarLayout.setHeight("24px");
		searchBarLayout.setSpacing(true);
		
        searchBarLayout.addComponent(searchField);
        searchBarLayout.addComponent(searchButton);
        searchBarLayout.addComponent(popup);
        searchBarLayout.addComponent(exactMatchesOnlyCheckBox);
        searchBarLayout.addComponent(searchPublicDataCheckBox);

        searchBarLayout.setComponentAlignment(exactMatchesOnlyCheckBox, Alignment.MIDDLE_CENTER);
        searchBarLayout.setComponentAlignment(popup, Alignment.MIDDLE_CENTER);
        searchBarLayout.setComponentAlignment(searchPublicDataCheckBox, Alignment.MIDDLE_CENTER);

        panelLayout.addComponent(searchBarLayout);
        setContent(panelLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

	public void searchButtonClickAction() {
		String q = searchField.getValue().toString();
		doSearch(q);
	}

	public void doSearch(String q) {
		
		if(q.replaceAll(" ", "").trim().equals("")){
			MessageNotifier.showWarning(getWindow(),
					messageSource.getMessage(Message.UNABLE_TO_SEARCH),
					messageSource.getMessage(Message.SEARCH_QUERY_CANNOT_BE_EMPTY),
					Notification.POSITION_CENTERED);
			return;
		}
		
		try {
			boolean searchPublicData = (Boolean) searchPublicDataCheckBox.getValue();
			boolean exactMatchedOnly = (Boolean) exactMatchesOnlyCheckBox.getValue();
				
			List<GermplasmList> germplasmLists = doGermplasmListSearch(q, exactMatchedOnly ? Operation.EQUAL : Operation.LIKE, searchPublicData);

			if (germplasmLists == null || germplasmLists.isEmpty()) {
				MessageNotifier.showWarning(getWindow(),
						messageSource.getMessage(Message.SEARCH_RESULTS),
						messageSource.getMessage(Message.NO_SEARCH_RESULTS),
						Notification.POSITION_CENTERED);
			}
			searchResultsComponent.applyGermplasmListResults(germplasmLists);

		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	private List<GermplasmList> doGermplasmListSearch(String q, Operation o, boolean searchPublicData)
			throws MiddlewareQueryException {
		return germplasmListManager.searchForGermplasmList(q, o, searchPublicData);
	}

	public TextField getSearchField(){
		return searchField;
	}
	
}
