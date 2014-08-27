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
	
	private ListManagerMain source;
	
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
		searchDescription = new Label();
		searchDescription.setValue(messageSource.getMessage(Message.SELECT_A_MATCHING_LIST_TO_VIEW_THE_DETAILS));
		searchDescription.setWidth("375px");
		searchResultsComponent = new ListSearchResultsComponent(source, listSelectionLayout);
		searchBar = new ListSearchBarComponent(searchResultsComponent);
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
		
		instructionLayout.setWidth("100%");

		instructionLayout.addComponent(searchDescription);
		instructionLayout.addStyleName("lm-subtitle");
		
		final Panel listDataTablePanel = new Panel();
        listDataTablePanel.setSizeFull();
        listDataTablePanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
        
        final VerticalLayout listDataTableLayout = new VerticalLayout();
        listDataTableLayout.setMargin(true);
        listDataTableLayout.setSizeFull();

        listDataTableLayout.addComponent(searchBar);
        listDataTableLayout.addComponent(searchResultsComponent);
	
        listDataTablePanel.setContent(listDataTableLayout);

		addComponent(instructionLayout);
		addComponent(listDataTablePanel);

        this.setExpandRatio(listDataTablePanel,1.0F);
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	public ListSearchResultsComponent getSearchResultsComponent(){
		return searchResultsComponent;
	}
	
	public void focusOnSearchField(){
		searchBar.getSearchField().focus();
		searchBar.getSearchField().selectAll();
	}
	
}
