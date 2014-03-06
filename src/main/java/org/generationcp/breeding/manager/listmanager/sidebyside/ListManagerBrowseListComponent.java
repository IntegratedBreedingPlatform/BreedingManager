package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListComponent;
import org.generationcp.breeding.manager.listmanager.DropHandlerComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerBrowseListComponent extends VerticalLayout implements
	InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private ListManagerTreeComponent listManagerTreeComponent;
	private SelectGermplasmListComponent selectListComponent;
	
	private HorizontalSplitPanel hSplitPanel;
	private AbsoluteLayout leftLayout;
	private VerticalLayout rightLayout;
	
	private Label projectLists;
	private TabSheet tabSheetList;
	private ListManagerTreeMenu list1;
	private ListManagerTreeMenu list2;
	private DropHandlerComponent dropHandler;
	
	private Button toggleLeftPaneButton;
	
	private static Float EXPANDED_SPLIT_POSITION_LEFT = Float.valueOf("250");
	private static Float COLLAPSED_SPLIT_POSITION_LEFT = Float.valueOf("50");
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		expandLeft();
	}
	
	private void createViewListDetailsTabSheet(){
		tabSheetList = new TabSheet();
		tabSheetList.setWidth("95%");
		tabSheetList.setHeight("500px");
		
		VerticalLayout layout = new VerticalLayout();
		list1 = new ListManagerTreeMenu(1426,"IIRON-1986",1,1,false,null);
		layout.addComponent(list1);
		Tab tab1 = tabSheetList.addTab(layout, "IIRON-1986");
		tab1.setClosable(true);
    	
		VerticalLayout layout2 = new VerticalLayout();
		list2 = new ListManagerTreeMenu(1427,"IIRON-1987",1,1,false,null);
		layout2.addComponent(list2);
		Tab tab2 = tabSheetList.addTab(layout2, "IIRON-1987");
		tab2.setClosable(true);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void instantiateComponents() {
		setSizeFull();
		
		hSplitPanel = new HorizontalSplitPanel();
		hSplitPanel.setMaxSplitPosition(EXPANDED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		hSplitPanel.setMinSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT, Sizeable.UNITS_PIXELS);
		
		//left pane
		leftLayout = new AbsoluteLayout();
		leftLayout.setWidth("240px");
		
		toggleLeftPaneButton = new Button();
		toggleLeftPaneButton.setCaption("<<");
		toggleLeftPaneButton.setDescription("Toggle List Manager Tree");
		
		projectLists = new Label();
		projectLists.setValue(messageSource.getMessage(Message.PROJECT_LISTS));
		projectLists.setStyleName(Bootstrap.Typography.H4.styleName());
		projectLists.setWidth("90px");
		
		listManagerTreeComponent = new ListManagerTreeComponent(selectListComponent);
		
		dropHandler = new DropHandlerComponent(null, 220);
		dropHandler.enableDropHandler();
		
		
		leftLayout.addComponent(projectLists,"top:30px;left:20px");
		leftLayout.addComponent(listManagerTreeComponent,"top:60px;left:20px");
		leftLayout.addComponent(dropHandler,"top:345px;left:20px");
		
		leftLayout.addComponent(toggleLeftPaneButton,"top:0px; right:0px");
		
		//right pane
		createViewListDetailsTabSheet();
		
		rightLayout = new VerticalLayout();
		rightLayout.setMargin(true);
		rightLayout.addComponent(tabSheetList);
		
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
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
}
