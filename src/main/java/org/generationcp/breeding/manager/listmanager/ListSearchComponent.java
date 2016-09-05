
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListSearchComponent extends VerticalLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 2325345518077870690L;

	private final ListManagerMain source;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final ListSelectionLayout listSelectionLayout;

	private Label searchDescription;

	private ListSearchBarComponent searchBar;
	private ListSearchResultsComponent searchResultsComponent;

	public ListSearchComponent(ListManagerMain source, final ListSelectionLayout listSelectionLayout) {
		super();
		this.source = source;
		this.listSelectionLayout = listSelectionLayout;
	}

	@Override
	public void instantiateComponents() {
		this.searchDescription = new Label();
		this.searchDescription.setDebugId("searchDescription");
		this.searchDescription.setValue(this.messageSource.getMessage(Message.SELECT_A_MATCHING_LIST_TO_VIEW_THE_DETAILS));
		this.searchDescription.setWidth("375px");
		this.searchResultsComponent = new ListSearchResultsComponent(this.source, this.listSelectionLayout);
		this.searchResultsComponent.setDebugId("searchResultsComponent");
		this.searchBar = new ListSearchBarComponent(this.searchResultsComponent);
		this.searchBar.setDebugId("searchBar");
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

		this.setSizeFull();

		final HorizontalLayout instructionLayout = new HorizontalLayout();
		instructionLayout.setDebugId("instructionLayout");

		instructionLayout.setWidth("100%");

		instructionLayout.addComponent(this.searchDescription);
		instructionLayout.addStyleName("lm-subtitle");

		final Panel listDataTablePanel = new Panel();
		listDataTablePanel.setDebugId("listDataTablePanel");
		listDataTablePanel.setSizeFull();
		listDataTablePanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);

		final VerticalLayout listDataTableLayout = new VerticalLayout();
		listDataTableLayout.setDebugId("listDataTableLayout");
		listDataTableLayout.setMargin(true);
		listDataTableLayout.setSizeFull();

		listDataTableLayout.addComponent(this.searchBar);
		listDataTableLayout.addComponent(this.searchResultsComponent);

		listDataTablePanel.setContent(listDataTableLayout);

		this.addComponent(instructionLayout);
		this.addComponent(listDataTablePanel);

		this.setExpandRatio(listDataTablePanel, 1.0F);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();

	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	public ListSearchResultsComponent getSearchResultsComponent() {
		return this.searchResultsComponent;
	}

	public void focusOnSearchField() {
		this.searchBar.getSearchField().focus();
		this.searchBar.getSearchField().selectAll();
	}

}
