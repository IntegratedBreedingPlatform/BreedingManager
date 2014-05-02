package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.constants.ListManagerDetailsTabSource;
import org.generationcp.breeding.manager.customcomponent.ToggleButton;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerSearchListComponent extends VerticalLayout implements
			InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	private HorizontalSplitPanel hSplitPanel;
	private AbsoluteLayout leftLayout;
	private ToggleButton toggleLeftPaneButton;
	
	private SearchResultsComponent searchResultsComponent;
	private ListManagerMain source;
	private ListManagerDetailsLayout listManagerDetailsLayout;
	
	private static Float EXPANDED_SPLIT_POSITION_LEFT = Float.valueOf("390");
	private static Float COLLAPSED_SPLIT_POSITION_LEFT = Float.valueOf("60");
	
	public ListManagerSearchListComponent(ListManagerMain source) {
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
		//setSizeFull();
		setWidth("100%");
		setHeight("800px");
		
		hSplitPanel = new HorizontalSplitPanel();
		hSplitPanel.setMaxSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		hSplitPanel.setMinSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		hSplitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		
		listManagerDetailsLayout = new ListManagerDetailsLayout(source, ListManagerDetailsTabSource.SEARCH);
		searchResultsComponent = new SearchResultsComponent(source, listManagerDetailsLayout);
		toggleLeftPaneButton = new ToggleButton("Toggle Search Results Table");
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		toggleLeftPaneButton.addListener(new ClickListener(){
			private static final long serialVersionUID = -1284067126888699234L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(hSplitPanel.getSplitPosition() == hSplitPanel.getMaxSplitPosition()){
					collapseLeft();
				} else {
					expandLeft();
				}
			}
		});
	}

	@Override
	public void layoutComponents() {
		
		//left pane
		leftLayout = new AbsoluteLayout();
		leftLayout.setWidth("390px");
		leftLayout.addComponent(searchResultsComponent, "top:7px; left:18px");
		leftLayout.addComponent(toggleLeftPaneButton,"top:5px; left:18px");
	
		hSplitPanel.setFirstComponent(leftLayout);
		hSplitPanel.setSecondComponent(listManagerDetailsLayout);
		
		addComponent(hSplitPanel);
	}
	
    private void expandLeft(){
    	leftLayout.setWidth("390px");
    	hSplitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    }

    private void collapseLeft(){
    	leftLayout.setWidth("100%");
    	hSplitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    }
    
    public SearchResultsComponent getSearchResultsComponent(){
    	return searchResultsComponent;
    }

	@Override
	public void updateLabels() {
		
	}

	public ListManagerDetailsLayout getListManagerDetailsLayout() {
		return listManagerDetailsLayout;
	}
	
}

