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
	private static final String GUIDE = "You may search for germplasm lists using GID's, germplasm names (partial/full), or list names (partial/full)"
			+ " <br/><br/><b>Matching lists would contain</b> <br/>"
			+ "  - Lists with names containing the search query <br/>"
			+ "  - Lists containing germplasms given a GID <br/>"
			+ "  - Lists containing germplasms with names <br/>"
			+ " containing the search query"
			+ " <br/><br/>The <b>Exact matches only</b> checkbox allows you search using partial names (when unchecked)"
			+ " or to only return results which match the query exactly (when checked).";

	private HorizontalLayout searchBarLayout;
	private TextField searchField;
	private final ListSearchResultsComponent searchResultsComponent;
	private Button searchButton;
	private CheckBox likeOrEqualCheckBox;
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

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        likeOrEqualCheckBox = new CheckBox();
        likeOrEqualCheckBox.setValue(true);
        likeOrEqualCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
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
		searchBarLayout.setWidth("55%");
		searchBarLayout.setHeight("24px");
		
		searchBarLayout.setSpacing(true);
		
        searchBarLayout.addComponent(searchField);
        searchBarLayout.addComponent(searchButton);
        searchBarLayout.addComponent(likeOrEqualCheckBox);
        searchBarLayout.addComponent(popup);
        
        searchBarLayout.setComponentAlignment(likeOrEqualCheckBox, Alignment.MIDDLE_CENTER);
        searchBarLayout.setComponentAlignment(popup, Alignment.MIDDLE_CENTER);

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
		try {

			List<GermplasmList> germplasmLists;
			if ((Boolean) likeOrEqualCheckBox.getValue() == true) {
				germplasmLists = doGermplasmListSearch(q, Operation.EQUAL);
			} else {
				germplasmLists = doGermplasmListSearch(q, Operation.LIKE);
			}

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

	private List<GermplasmList> doGermplasmListSearch(String q, Operation o)
			throws MiddlewareQueryException {
		return germplasmListManager.searchForGermplasmList(q, o);
	}

}
