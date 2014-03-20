package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerBrowseListComponent extends VerticalLayout implements
	InternationalizableComponent, InitializingBean, BreedingManagerLayout{

	private static final long serialVersionUID = -383145225475654748L;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private ListManagerTreeComponent listTreeComponent;
	
	private HorizontalSplitPanel hSplitPanel;
	private AbsoluteLayout leftLayout;
	private VerticalLayout rightLayout;
	
	private ListManagerDetailsLayout listDetailsLayout;
	
	private Button toggleLeftPaneButton;
	
	private static Float EXPANDED_SPLIT_POSITION_LEFT = Float.valueOf("250");
	private static Float COLLAPSED_SPLIT_POSITION_LEFT = Float.valueOf("50");
	
	private ListManagerMain source;
	
	public ListManagerBrowseListComponent(ListManagerMain source) {
		super();
		this.source = source;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		expandLeft();
	}
	
	@Override
	public void updateLabels() {
		
	}

	@Override
	public void instantiateComponents() {
		setSizeFull();
		
		hSplitPanel = new HorizontalSplitPanel();
		hSplitPanel.setMaxSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		hSplitPanel.setMinSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		
		//left pane
		leftLayout = new AbsoluteLayout();
		
		toggleLeftPaneButton = new Button();
		toggleLeftPaneButton.setCaption("<<");
		toggleLeftPaneButton.setDescription("Toggle List Manager Tree");
		
		listTreeComponent = new ListManagerTreeComponent(source);
		
		//right pane
		listDetailsLayout = new ListManagerDetailsLayout();
		
		rightLayout = new VerticalLayout();
		rightLayout.setMargin(true);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		toggleLeftPaneButton.addListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

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
		leftLayout.setWidth("240px");
		leftLayout.addComponent(listTreeComponent,"top:30px;left:20px");
		leftLayout.addComponent(toggleLeftPaneButton,"top:0px; left:0px");
		
		rightLayout.addComponent(listDetailsLayout);
		
		hSplitPanel.setFirstComponent(leftLayout);
		hSplitPanel.setSecondComponent(rightLayout);
		addComponent(hSplitPanel);
	}
	
    private void expandLeft(){
    	leftLayout.setWidth("240px");
    	hSplitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    	toggleLeftPaneButton.setCaption("<<");
    }

    private void collapseLeft(){
    	leftLayout.setWidth("100%");
    	hSplitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    	toggleLeftPaneButton.setCaption(">>");
    }

    public void openListDetails(GermplasmList list) {
		listDetailsLayout.createListDetailsTab(list.getId());
	}
}
