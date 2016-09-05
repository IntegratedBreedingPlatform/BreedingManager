
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmSelectionComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Label headingLabel;
	private Label searchDescription;

	private HorizontalLayout headerLayout;
	private HorizontalLayout instructionLayout;

	private GermplasmSearchBarComponent searchBarComponent;
	private GermplasmSearchResultsComponent searchResultsComponent;

	private final ListManagerMain source;

	public GermplasmSelectionComponent(final ListManagerMain source) {
		super();
		this.source = source;
		this.setDebugId("GermplasmSelectionComponent");
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
		this.setHeight("800px");

		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setDebugId("headerLayout");
		this.headerLayout.setDebugId("headerLayout");

		this.instructionLayout = new HorizontalLayout();
		this.instructionLayout.setDebugId("instructionLayout");
		this.headerLayout.setDebugId("instructionLayout");

		this.instructionLayout.setWidth("100%");

		this.headingLabel = new Label();
		this.headingLabel.setDebugId("headingLabel");
		this.headingLabel.setDebugId("headingLabel");
		this.headingLabel.setImmediate(true);
		this.headingLabel.setWidth("200px");
		this.headingLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.headingLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.searchDescription = new Label();
		this.searchDescription.setDebugId("searchDescription");
		this.searchDescription.setDebugId("searchDescription");
		this.searchDescription.addStyleName("lm-word-wrap");
		this.searchDescription.setHeight("55px");

		this.searchResultsComponent = new GermplasmSearchResultsComponent(this.source);
		this.searchResultsComponent.setDebugId("searchResultsComponent");
		this.searchResultsComponent.setDebugId("searchResultsComponent");
		this.searchBarComponent = new GermplasmSearchBarComponent(this.searchResultsComponent);
		this.searchBarComponent.setDebugId("searchBarComponent");
		this.searchBarComponent.setDebugId("searchBarComponent");

	}

	@Override
	public void initializeValues() {
		this.headingLabel.setValue(this.messageSource.getMessage(Message.SEARCH_FOR_GERMPLASM));
		this.searchDescription.setValue(this.messageSource.getMessage(Message.SELECT_A_GERMPLASM_TO_VIEW_THE_DETAILS));
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.setMargin(new MarginInfo(true, false, true, true));

		final HorizontalLayout selectionHeaderContainer = new HorizontalLayout();
		selectionHeaderContainer.setDebugId("selectionHeaderContainer");
		selectionHeaderContainer.setWidth("100%");

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS, this.headingLabel);
		headingLayout.setDebugId("headingLayout");
		headingLayout.setDebugId("headingLayout");
		this.headerLayout.addComponent(headingLayout);
		this.instructionLayout.addComponent(this.searchDescription);

		final Panel listDataTablePanel = new Panel();
		listDataTablePanel.setDebugId("listDataTablePanel");
		listDataTablePanel.setStyleName(Reindeer.PANEL_LIGHT + " " + AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);

		final VerticalLayout listDataTableLayout = new VerticalLayout();
		listDataTableLayout.setDebugId("listDataTableLayout");
		listDataTableLayout.setMargin(true);
		listDataTableLayout.addStyleName("listDataTableLayout");

		listDataTableLayout.addComponent(this.searchBarComponent);
		listDataTableLayout.addComponent(this.searchResultsComponent);

		listDataTablePanel.setContent(listDataTableLayout);

		selectionHeaderContainer.addComponent(headingLayout);
		selectionHeaderContainer.addComponent(this.source.listBuilderToggleBtn2);
		selectionHeaderContainer.setExpandRatio(headingLayout, 1.0F);
		selectionHeaderContainer.setComponentAlignment(this.source.listBuilderToggleBtn2, Alignment.TOP_RIGHT);

		this.addComponent(selectionHeaderContainer);
		this.addComponent(this.instructionLayout);
		this.addComponent(listDataTablePanel);

		this.setExpandRatio(listDataTablePanel, 1.0F);
	}

	public GermplasmSearchResultsComponent getSearchResultsComponent() {
		return this.searchResultsComponent;
	}

	public GermplasmSearchBarComponent getSearchBarComponent() {
		return this.searchBarComponent;
	}

	@Override
	public void updateLabels() {
		// not implemented
	}
}
