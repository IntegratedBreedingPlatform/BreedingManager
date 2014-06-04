package org.generationcp.breeding.manager.listmanager.sidebyside;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
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

@Configurable
public class GermplasmSelectionComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

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
        this.setMargin(new MarginInfo(true,false,true,true));

        final HorizontalLayout selectionHeaderContainer = new HorizontalLayout();
        selectionHeaderContainer.setWidth("100%");

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS, headingLabel);
		//headingLayout.addStyleName("lm-title");
        //headingLayout.setHeight("30px");
        
		headerLayout.addComponent(headingLayout);

		instructionLayout.addComponent(searchDescription);
		instructionLayout.addStyleName("lm-subtitle");
		
		final Panel listDataTablePanel = new Panel();
        listDataTablePanel.setStyleName(Reindeer.PANEL_LIGHT + " "+AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
        
        final VerticalLayout listDataTableLayout = new VerticalLayout();
        listDataTableLayout.setMargin(true);
        listDataTableLayout.setSizeFull();
        listDataTableLayout.addStyleName("listDataTableLayout");

        listDataTableLayout.addComponent(searchBarComponent);
        listDataTableLayout.addComponent(searchResultsComponent);
	
        listDataTablePanel.setContent(listDataTableLayout);

        selectionHeaderContainer.addComponent(headingLayout);
        selectionHeaderContainer.addComponent(source.listBuilderToggleBtn2);
        selectionHeaderContainer.setExpandRatio(headingLayout,1.0F);
        selectionHeaderContainer.setComponentAlignment(source.listBuilderToggleBtn2,Alignment.TOP_RIGHT);

        addComponent(selectionHeaderContainer);
        addComponent(instructionLayout);
		addComponent(listDataTablePanel);

        this.setExpandRatio(listDataTablePanel,1.0F);
	}
	
    public GermplasmSearchResultsComponent getSearchResultsComponent(){
    	return searchResultsComponent;
    }

    public GermplasmSearchBarComponent getSearchBarComponent(){
    	return searchBarComponent;
    }    
    
	@Override
	public void updateLabels() {
	}
}

