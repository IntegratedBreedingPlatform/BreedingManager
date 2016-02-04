
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
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
			"You may search for germplasm and germplasm lists using GIDs, Stock IDs, or partial or full germplasm names."
					+ "<br/><br/><b>The search results will show:</b>" + "<ul>" + "<li>Germplasm with matching GIDs</li>"
					+ "<li>Germplasm with matching Stock IDs</li>" + "<li>Germplasm with name(s) containing the search term</li>"
					+ "<li>Parents of the matching germplasm (if selected)</li>" + "</ul>";

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

	@Autowired
	private PlatformTransactionManager transactionManager;

	public GermplasmSearchBarComponent(final GermplasmSearchResultsComponent searchResultsComponent) {
		super();
		this.searchResultsComponent = searchResultsComponent;
	}

	public TextField getSearchField() {
		return this.searchField;
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
		this.searchField.setImmediate(true);

		this.searchButton = new Button(this.messageSource.getMessage(Message.SEARCH));
		this.searchButton.setHeight("24px");
		this.searchButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		this.searchButton.setData(GermplasmSearchBarComponent.SEARCH_BUTTON);
		this.searchButton.setClickShortcut(KeyCode.ENTER);

		final Label descLbl = new Label(GermplasmSearchBarComponent.GUIDE, Label.CONTENT_XHTML);
		descLbl.setWidth("300px");
		this.popup = new PopupView(" ? ", descLbl);
		this.popup.setStyleName("gcp-popup-view");

		this.withInventoryOnlyCheckBox = new CheckBox();
		this.withInventoryOnlyCheckBox.setValue(false);
		this.withInventoryOnlyCheckBox.setCaption(this.messageSource.getMessage(Message.WITH_INVENTORY_ONLY));

		this.includeParentsCheckBox = new CheckBox();
		this.includeParentsCheckBox.setValue(false);
		this.includeParentsCheckBox.setCaption(this.messageSource.getMessage(Message.INCLUDE_PARENTS));

		this.matchesStartingWith = this.messageSource.getMessage(Message.MATCHES_STARTING_WITH);
		this.exactMatches = this.messageSource.getMessage(Message.EXACT_MATCHES);
		this.matchesContaining = this.messageSource.getMessage(Message.MATCHES_CONTAINING);

		final List<String> searchTypes = Arrays.asList(new String[] {this.matchesStartingWith, this.exactMatches, this.matchesContaining});
		this.searchTypeOptions = new OptionGroup(null, searchTypes);
		this.searchTypeOptions.setValue(this.matchesStartingWith);
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
				GermplasmSearchBarComponent.this.searchButtonClickAction();
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setMargin(true);
		this.addStyleName("lm-search-bar");
		this.setWidth("100%");
		final VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(this.getFirstRow());
		verticalLayout.addComponent(this.getSecondRow());
		this.addComponent(verticalLayout);
		this.focusOnSearchField();
	}

	private Component getFirstRow() {
		this.searchBarLayoutLeft = new HorizontalLayout();
		this.searchBarLayoutLeft.setSpacing(true);
		this.searchBarLayoutRight = new CssLayout();

		// To allow for all of the elements to fit in the default width of the search bar. There may be a better way..
		this.searchField.setWidth("120px");

		this.searchBarLayoutLeft.addComponent(this.searchField);
		this.searchBarLayoutLeft.addComponent(this.searchButton);
		this.searchBarLayoutLeft.addComponent(this.popup);

		this.withInventoryOnlyCheckBox.addStyleName(GermplasmSearchBarComponent.LM_COMPONENT_WRAP);
		this.includeParentsCheckBox.addStyleName(GermplasmSearchBarComponent.LM_COMPONENT_WRAP);

		this.searchBarLayoutRight.addComponent(this.withInventoryOnlyCheckBox);
		this.searchBarLayoutRight.addComponent(this.includeParentsCheckBox);
		this.searchBarLayoutLeft.setComponentAlignment(this.popup, Alignment.MIDDLE_CENTER);

		final CssLayout firstRow = new CssLayout();
		firstRow.addComponent(this.searchBarLayoutLeft);
		firstRow.addComponent(this.searchBarLayoutRight);
		firstRow.setHeight("34px");
		return firstRow;
	}

	private Component getSecondRow() {
		return this.searchTypeOptions;
	}

	public void focusOnSearchField() {
		this.searchField.focus();
		this.searchField.selectAll();
	}

	@Override
	public void updateLabels() {
		// Auto-generated method stub

	}

	public void searchButtonClickAction() {

		final String q = GermplasmSearchBarComponent.this.searchField.getValue().toString();
		final String searchType = (String) GermplasmSearchBarComponent.this.searchTypeOptions.getValue();
		if (GermplasmSearchBarComponent.this.matchesContaining.equals(searchType)) {
			ConfirmDialog.show(GermplasmSearchBarComponent.this.getWindow(),
					GermplasmSearchBarComponent.this.messageSource.getMessage(Message.WARNING),
					GermplasmSearchBarComponent.this.messageSource.getMessage(Message.SEARCH_TAKE_TOO_LONG_WARNING),
					GermplasmSearchBarComponent.this.messageSource.getMessage(Message.OK),
					GermplasmSearchBarComponent.this.messageSource.getMessage(Message.CANCEL), new ConfirmDialog.Listener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(final ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								GermplasmSearchBarComponent.this.doSearch(q);
							}
						}
					});
		} else {
			GermplasmSearchBarComponent.this.doSearch(q);
		}
	}

	public void doSearch(final String q) {

		final TransactionTemplate inTx = new TransactionTemplate(this.transactionManager);
		inTx.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				final Monitor monitor = MonitorFactory.start("GermplasmSearchBarComponent.doSearch()");
				final String searchType = (String) GermplasmSearchBarComponent.this.searchTypeOptions.getValue();
				final String searchKeyword = GermplasmSearchBarComponent.this.getSearchKeyword(q, searchType);
				final Operation operation =
						GermplasmSearchBarComponent.this.exactMatches.equals(searchType) ? Operation.EQUAL : Operation.LIKE;

				try {
					final boolean includeParents = (Boolean) GermplasmSearchBarComponent.this.includeParentsCheckBox.getValue();
					final boolean withInventoryOnly = (Boolean) GermplasmSearchBarComponent.this.withInventoryOnlyCheckBox.getValue();
					GermplasmSearchBarComponent.this.searchResultsComponent
							.applyGermplasmResults(GermplasmSearchBarComponent.this.breedingManagerService.doGermplasmSearch(searchKeyword,
									operation, includeParents, withInventoryOnly));
				} catch (final BreedingManagerSearchException e) {
					if (Message.SEARCH_QUERY_CANNOT_BE_EMPTY.equals(e.getErrorMessage())) {
						// invalid search string
						MessageNotifier.showWarning(GermplasmSearchBarComponent.this.getWindow(),
								GermplasmSearchBarComponent.this.messageSource.getMessage(Message.UNABLE_TO_SEARCH),
								GermplasmSearchBarComponent.this.messageSource.getMessage(e.getErrorMessage()));
					} else {
						// case for no results, database error
						MessageNotifier.showWarning(GermplasmSearchBarComponent.this.getWindow(),
								GermplasmSearchBarComponent.this.messageSource.getMessage(Message.SEARCH_RESULTS),
								GermplasmSearchBarComponent.this.messageSource.getMessage(e.getErrorMessage()));
						if (Message.ERROR_DATABASE.equals(e.getErrorMessage())) {
							GermplasmSearchBarComponent.LOG.error("Database error occured while searching. Search string was: " + q, e);
						}
					}
				} finally {
					GermplasmSearchBarComponent.LOG.debug("" + monitor.stop());
				}
			}
		});
	}

	private String getSearchKeyword(final String query, final String searchType) {
		String searchKeyword = query;
		if (this.matchesStartingWith.equals(searchType)) {
			searchKeyword = searchKeyword + GermplasmSearchBarComponent.PERCENT;
		} else if (this.matchesContaining.equals(searchType)) {
			searchKeyword = GermplasmSearchBarComponent.PERCENT + searchKeyword + GermplasmSearchBarComponent.PERCENT;
		}
		return searchKeyword;
	}

	public CheckBox getIncludeParentsCheckBox() {
		return this.includeParentsCheckBox;
	}

	public void setIncludeParentsCheckBox(final CheckBox includeParentsCheckBox) {
		this.includeParentsCheckBox = includeParentsCheckBox;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public BreedingManagerService getBreedingManagerService() {
		return this.breedingManagerService;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public OptionGroup getSearchTypeOptions() {
		return this.searchTypeOptions;
	}

	public CheckBox getWithInventoryOnlyCheckBox() {
		return this.withInventoryOnlyCheckBox;
	}

	public void setSearchTypeOptions(final OptionGroup searchTypeOptions) {
		this.searchTypeOptions = searchTypeOptions;
	}

	protected void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
}
