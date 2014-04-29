package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ListManagerDetailsTabSource;
import org.generationcp.breeding.manager.constants.ToggleDirection;
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
import com.vaadin.ui.AbsoluteLayout;
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
	
	private ListManagerDetailsLayout listManagerDetailsLayout;
	
	private static Float EXPANDED_SPLIT_POSITION_LEFT = Float.valueOf("235");
	private static Float COLLAPSED_SPLIT_POSITION_LEFT = Float.valueOf("50");
	
	private ListManagerMain source;
	
	private Integer selectedListId;
		
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
		
		listTreeComponent = new ListManagerTreeComponent(source, selectedListId);
		listManagerDetailsLayout = new ListManagerDetailsLayout(source, ListManagerDetailsTabSource.BROWSE, selectedListId);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {

	}

	@Override
	public void layoutComponents() {
		
		//left pane
		leftLayout = new AbsoluteLayout();
		leftLayout.setWidth("235px");
		leftLayout.addComponent(listTreeComponent,"top:0px;left:15px");
		
		
		hSplitPanel.setFirstComponent(leftLayout);
		hSplitPanel.setSecondComponent(listManagerDetailsLayout);
		addComponent(hSplitPanel);
	}
	
    private void expandLeft(){
    	leftLayout.setWidth("235px");
    	hSplitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    }

    private void collapseLeft(){
    	leftLayout.setWidth("100%");
    	hSplitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
    }

    public void openListDetails(GermplasmList list) {
        try{
            listManagerDetailsLayout.createListDetailsTab(list.getId());
        } catch (MiddlewareQueryException e){
            LOG.error("Error in displaying germplasm list details.", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
	}
    
	protected void toggleListTreeComponent(){
		if(hSplitPanel.getSplitPosition() == hSplitPanel.getMaxSplitPosition()){
			collapseLeft();
		} else {
			expandLeft();
		}
	}

	public ListManagerDetailsLayout getListDetailsLayout() {
		return listManagerDetailsLayout;
	}
    
	public ListManagerTreeComponent getListTreeComponent(){
		return listTreeComponent;
	}
	
}
