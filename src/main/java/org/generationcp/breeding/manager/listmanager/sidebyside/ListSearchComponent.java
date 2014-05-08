package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.listmanager.ListSearchResultsComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

@Configurable
public class ListSearchComponent extends CssLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 2325345518077870690L;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	private final ListSelectionLayout listSelectionLayout;

	private Label searchDescription;

	private ListSearchBarComponent searchBar;
	private ListSearchResultsComponent searchResultsComponent;

	public ListSearchComponent(final ListSelectionLayout listSelectionLayout) {
		super();
		this.listSelectionLayout = listSelectionLayout;
	}

	@Override
	public void instantiateComponents() {
		searchDescription = new Label();
		searchDescription.setValue(messageSource.getMessage(Message.SELECT_A_MATCHING_LIST_TO_VIEW_THE_DETAILS));
		searchDescription.setWidth("375px");
		searchResultsComponent = new ListSearchResultsComponent(listSelectionLayout);
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
		
		setHeight("480px");
		setWidth("880px");
		
		final HorizontalLayout instructionLayout = new HorizontalLayout();
		
		instructionLayout.setWidth("100%");

		instructionLayout.addComponent(searchDescription);
		instructionLayout.addStyleName("lm-subtitle");
		
		final Panel listDataTablePanel = new Panel();
        listDataTablePanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
        
        final CssLayout listDataTableLayout = new CssLayout();
        listDataTableLayout.setMargin(true);
        listDataTableLayout.setWidth("100%");
        listDataTableLayout.setHeight("400px");
        
        listDataTableLayout.addComponent(searchBar);
        listDataTableLayout.addComponent(searchResultsComponent);
	
        listDataTablePanel.setContent(listDataTableLayout);
        
		addComponent(instructionLayout);
		addComponent(listDataTablePanel);
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

}
