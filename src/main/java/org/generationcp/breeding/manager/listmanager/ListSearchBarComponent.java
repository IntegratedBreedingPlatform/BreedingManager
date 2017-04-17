
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.util.SearchType;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
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
			+ "  <li>The list contains germplasm with GIDs that contain the search term </li>" + " </ul>";

	private HorizontalLayout searchBarLayoutLeft;
	private CssLayout searchBarLayoutRight;
	private TextField searchField;
	private final ListSearchResultsComponent searchResultsComponent;
	private Button searchButton;
	private PopupView popup;
	private OptionGroup searchTypeOptions;

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

		this.searchTypeOptions = new OptionGroup();
		this.searchTypeOptions.setDebugId("searchTypeOptions");
		for (final SearchType searchType : SearchType.values()) {
			this.searchTypeOptions.addItem(searchType);
			this.searchTypeOptions.setItemCaption(searchType, this.messageSource.getMessage(searchType.getLabel()));
		}

		this.searchTypeOptions.setValue(SearchType.STARTS_WITH_KEYWORD);
		this.searchTypeOptions.setStyleName("v-select-optiongroup-horizontal");
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

		this.searchBarLayoutLeft = new HorizontalLayout();
		this.searchBarLayoutLeft.setDebugId("searchBarLayoutLeft");
		this.searchBarLayoutLeft.setSpacing(true);
		this.searchBarLayoutLeft.addComponent(this.searchField);
		this.searchBarLayoutLeft.addComponent(this.searchButton);
		this.searchBarLayoutLeft.addComponent(this.popup);

		this.searchBarLayoutRight = new CssLayout();
		this.searchBarLayoutRight.setDebugId("searchBarLayoutRight");
		this.searchBarLayoutRight.addComponent(this.searchTypeOptions);

		panelLayout.addComponent(this.searchBarLayoutLeft);
		panelLayout.addComponent(this.searchBarLayoutRight);

		this.setContent(panelLayout);
	}

	@Override
	public void updateLabels() {
		// Auto-generated method stub
	}

	void searchButtonClickAction() {
		final String queryString = ListSearchBarComponent.this.searchField.getValue().toString();
		final SearchType searchType = this.getSelectedSearchType();
		// Show a warning message that search could be slow if search type = "Contains keyword"
		if (SearchType.CONTAINS_KEYWORD.equals(searchType)) {
			ConfirmDialog.show(this.getWindow().getParent(), ListSearchBarComponent.this.messageSource.getMessage(Message.WARNING),
					ListSearchBarComponent.this.messageSource.getMessage(Message.SEARCH_TAKE_TOO_LONG_WARNING),
					ListSearchBarComponent.this.messageSource.getMessage(Message.OK),
					ListSearchBarComponent.this.messageSource.getMessage(Message.CANCEL), new ConfirmDialog.Listener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(final ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								ListSearchBarComponent.this.doSearch(queryString);
							}
						}
					});
		} else {
			ListSearchBarComponent.this.doSearch(queryString);
		}
	}

	public void doSearch(final String query) {

		try {

			final SearchType searchType = this.getSelectedSearchType();
			final Operation operation = searchType.getOperation();
			final String searchKeyword = SearchType.getSearchKeyword(query, searchType);

			final List<GermplasmList> matchingLists = this.breedingManagerService.doGermplasmListSearch(searchKeyword, operation);
			this.searchResultsComponent.applyGermplasmListResults(matchingLists);

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

	private SearchType getSelectedSearchType() {
		return (SearchType) ListSearchBarComponent.this.searchTypeOptions.getValue();
	}

	public TextField getSearchField() {
		return this.searchField;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setSearchType(final SearchType searchType) {
		this.searchTypeOptions.setValue(searchType);
	}

}
