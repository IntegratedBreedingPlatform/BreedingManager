
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Configurable
public class ListSearchBarComponent extends Panel implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(ListSearchBarComponent.class);
	private static final long serialVersionUID = 1L;

	public static final String SEARCH_BUTTON = "List Manager Search Button";
	private static final String GUIDE = "You may search for germplasm lists using partial or full germplasm names or list names, or GIDs."
			+ " <br/><br/><b>The search results will show lists in which: </b>" + " <ul>"
			+ "  <li>The list name contains the search term </li>"
			+ "  <li>The list contains germplasm with names that contain the search term </li>"
			+ "  <li>The list contains germplasm with GIDs that contain the search term </li>" + " </ul>"
			+ " The <b>Exact matches only</b> checkbox shows results that match the search "
			+ " term exactly when checked. If you uncheck this option, the search  "
			+ " will show results that contain the search term you enter.";

	private HorizontalLayout searchBarLayout;
	private TextField searchField;
	private final ListSearchResultsComponent searchResultsComponent;
	private Button searchButton;
	private CheckBox exactMatchesOnlyCheckBox;
	private PopupView popup;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private BreedingManagerService breedingManagerService;

	public ListSearchBarComponent(final ListSearchResultsComponent searchResultsComponent) {
		super();
		this.searchResultsComponent = searchResultsComponent;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {

		this.setWidth("100%");

		this.searchField = new TextField();
		this.searchField.setDebugId("searchField");
		this.searchField.setImmediate(true);

		this.searchButton = new Button(this.messageSource.getMessage(Message.SEARCH));
		this.searchButton.setDebugId("searchButton");
		this.searchButton.setHeight("24px");
		this.searchButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		this.searchButton.setData(ListSearchBarComponent.SEARCH_BUTTON);
		this.searchButton.setClickShortcut(KeyCode.ENTER);

		final Label descLbl = new Label(ListSearchBarComponent.GUIDE, Label.CONTENT_XHTML);
		descLbl.setDebugId("descLbl");
		descLbl.setWidth("300px");
		this.popup = new PopupView(" ? ", descLbl);
		this.popup.setDebugId("popup");
		this.popup.setStyleName("gcp-popup-view");

		this.exactMatchesOnlyCheckBox = new CheckBox();
		this.exactMatchesOnlyCheckBox.setDebugId("exactMatchesOnlyCheckBox");
		this.exactMatchesOnlyCheckBox.setValue(false);
		this.exactMatchesOnlyCheckBox.setCaption(this.messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
	}

	@Override
	public void initializeValues() {
		// Auto-generated method stub

	}

	@Override
	public void addListeners() {
		this.searchButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1926462184420334992L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListSearchBarComponent.this.searchButtonClickAction();
			}
		});

		this.addAction(new ShortcutListener("Next field", KeyCode.ENTER, null) {

			private static final long serialVersionUID = 288627665348761948L;

			@Override
			public void handleAction(final Object sender, final Object target) {
				ListSearchBarComponent.this.searchButtonClickAction();
			}
		});

	}

	@Override
	public void layoutComponents() {
		final CssLayout panelLayout = new CssLayout();
		panelLayout.setDebugId("panelLayout");
		panelLayout.setMargin(true);
		panelLayout.addStyleName("lm-search-bar");

		this.searchBarLayout = new HorizontalLayout();
		this.searchBarLayout.setDebugId("searchBarLayout");
		this.searchBarLayout.setHeight("24px");
		this.searchBarLayout.setSpacing(true);

		this.searchBarLayout.addComponent(this.searchField);
		this.searchBarLayout.addComponent(this.searchButton);
		this.searchBarLayout.addComponent(this.popup);
		this.searchBarLayout.addComponent(this.exactMatchesOnlyCheckBox);

		this.searchBarLayout.setComponentAlignment(this.exactMatchesOnlyCheckBox, Alignment.MIDDLE_CENTER);
		this.searchBarLayout.setComponentAlignment(this.popup, Alignment.MIDDLE_CENTER);

		panelLayout.addComponent(this.searchBarLayout);
		this.setContent(panelLayout);
	}

	@Override
	public void updateLabels() {
		// Auto-generated method stub
	}

	public void searchButtonClickAction() {
		final String q = this.searchField.getValue().toString();
		this.doSearch(q);
	}

	public void doSearch(final String q) {
		final boolean exactMatchedOnly = (Boolean) this.exactMatchesOnlyCheckBox.getValue();

		try {
			this.searchResultsComponent.applyGermplasmListResults(this.breedingManagerService.doGermplasmListSearch(q,
					exactMatchedOnly ? Operation.EQUAL : Operation.LIKE));

		} catch (final BreedingManagerSearchException e) {
			if (Message.NO_SEARCH_RESULTS.equals(e.getErrorMessage())) {
				this.searchResultsComponent.applyGermplasmListResults(new ArrayList<GermplasmList>());
			} else if (Message.SEARCH_QUERY_CANNOT_BE_EMPTY.equals(e.getErrorMessage())) {
				// invalid search string
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.UNABLE_TO_SEARCH),
						this.messageSource.getMessage(e.getErrorMessage()));
			} else {
				// case for no results, database error
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.SEARCH_RESULTS),
						this.messageSource.getMessage(e.getErrorMessage()));
			}
			ListSearchBarComponent.LOG.info(e.getMessage(), e);
		}
	}

	public TextField getSearchField() {
		return this.searchField;
	}

	public void setExactMatchesOnlyCheckBox(final CheckBox exactMatchesOnlyCheckBox) {
		this.exactMatchesOnlyCheckBox = exactMatchesOnlyCheckBox;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
