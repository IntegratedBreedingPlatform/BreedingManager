package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

@Configurable
public class GermplasmSelectionComponent extends CssLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

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
		setHeight("800px");
		
		addStyleName("pink");
		
		headerLayout = new HorizontalLayout();
		instructionLayout = new HorizontalLayout();
		
		headingLabel = new Label();
    	headingLabel.setImmediate(true);
    	headingLabel.setWidth("300px");
    	headingLabel.setStyleName(Bootstrap.Typography.H4.styleName());
    	headingLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		searchDescription = new Label();
		
		searchResultsComponent = new GermplasmSearchResultsComponent(source);
		searchBarComponent = new GermplasmSearchBarComponent(searchResultsComponent);
	}

	@Override
	public void initializeValues() {
		headingLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR_GERMPLASM));
		searchDescription.setValue(messageSource.getMessage(Message.SELECT_A_GERMPLASM_TO_VIEW_THE_DETAILS));
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		
		setMargin(true);
		
		headerLayout.setWidth("100%");
		instructionLayout.setWidth("100%");

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS, headingLabel);
		headingLayout.addStyleName("lm-title");
        headingLayout.setHeight("30px");
        
		headerLayout.addComponent(headingLayout);

		instructionLayout.addComponent(searchDescription);
		instructionLayout.addStyleName("lm-subtitle");
		
		final Panel listDataTablePanel = new Panel();
        listDataTablePanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
        
        final CssLayout listDataTableLayout = new CssLayout();
        listDataTableLayout.setMargin(true);
        listDataTableLayout.setWidth("605px");
        listDataTableLayout.setHeight("488px");
        
        listDataTableLayout.addComponent(searchBarComponent);
        listDataTableLayout.addComponent(searchResultsComponent);
	
        listDataTablePanel.setContent(listDataTableLayout);
        
		addComponent(headerLayout);
		addComponent(instructionLayout);
		addComponent(listDataTablePanel);
	}
	
    public GermplasmSearchResultsComponent getSearchResultsComponent(){
    	return searchResultsComponent;
    }

	@Override
	public void updateLabels() {
	}
}

