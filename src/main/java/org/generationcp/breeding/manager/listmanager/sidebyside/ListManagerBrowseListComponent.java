package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ListManagerDetailsTabSource;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerBrowseListComponent extends VerticalLayout implements
	InternationalizableComponent, InitializingBean, BreedingManagerLayout{
    
    private static final Logger LOG = LoggerFactory.getLogger(ListManagerBrowseListComponent.class);

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
	
	private Integer selectedListId;
	
	//Theme Resource
	private static final ThemeResource ICON_TOOGLE = new ThemeResource("images/toogle_icon.PNG");
	
	public ListManagerBrowseListComponent(ListManagerMain source) {
		super();
		this.source = source;
		this.selectedListId = null;
	}
	
	public ListManagerBrowseListComponent(ListManagerMain source, Integer listId) {
		super();
		this.source = source;
		this.selectedListId = listId;
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
		toggleLeftPaneButton.setIcon(ICON_TOOGLE);
		toggleLeftPaneButton.setDescription("Toggle List Manager Tree");
		toggleLeftPaneButton.setStyleName(Reindeer.BUTTON_LINK);
		toggleLeftPaneButton.setWidth("30px");
		listTreeComponent = new ListManagerTreeComponent(source, selectedListId);
		
		//right pane
		listDetailsLayout = new ListManagerDetailsLayout(source, ListManagerDetailsTabSource.BROWSE, selectedListId);
		
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
    }

    private void collapseLeft(){
    	leftLayout.setWidth("100%");
    	hSplitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    }

    public void openListDetails(GermplasmList list) {
        try{
            listDetailsLayout.createListDetailsTab(list.getId());
        } catch (MiddlewareQueryException e){
            LOG.error("Error in displaying germplasm list details.", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
	}

	public ListManagerDetailsLayout getListDetailsLayout() {
		return listDetailsLayout;
	}
    
}
