package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class PlantSelectionComponent extends VerticalLayout implements
			InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	private CssLayout layout;
	
	private GermplasmSearchBarComponent searchBarComponent;
	private GermplasmSearchResultsComponent searchResultsComponent;

	private final ListManagerMain source;
	
	public PlantSelectionComponent(final ListManagerMain source) {
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
		
		searchResultsComponent = new GermplasmSearchResultsComponent(source);
		searchBarComponent = new GermplasmSearchBarComponent(searchResultsComponent);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		
		layout = new CssLayout();
		layout.setWidth("100%");
		layout.addComponent(searchBarComponent);
		layout.addComponent(searchResultsComponent);
	
		addComponent(layout);
	}
	
    public GermplasmSearchResultsComponent getSearchResultsComponent(){
    	return searchResultsComponent;
    }

	@Override
	public void updateLabels() {
	}
}

