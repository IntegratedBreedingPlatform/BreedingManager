package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.ToogleButton;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalSplitPanel;

@Configurable
public class ListManagerMain extends AbsoluteLayout implements
		InternationalizableComponent, InitializingBean, BreedingManagerLayout, ListTreeActionsListener {

    private static final long serialVersionUID = 5976245899964745758L;
    private static final String VERSION = "1.0.0";
    
    private AbsoluteLayout titleLayout;
    private Label mainTitle;
    private Button buildNewListButton;
    public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";
    
    //For Main Tab 
    private HorizontalLayout tabHeaderLayout;
    private Button showBrowseListsButton;
    private Button showSearchListsButton;
    
    private HorizontalSplitPanel hSplitPanel;
    private VerticalSplitPanel vSplitPanel;
    
    //Layouts on every pane
    private AbsoluteLayout browserSearchLayout;
    private HorizontalLayout buildListLayout;
    
    //Components on every pane
    private ListManagerSearchListBarComponent searchListsBarComponent;
    private ListManagerBrowseListComponent browseListsComponent;
    private ListManagerSearchListComponent searchListsComponent;
    private BuildNewListComponent buildNewListComponent;
    
    private Button toggleBuildNewListButton;
	private static Float EXPANDED_SPLIT_POSITION_RIGHT = Float.valueOf(65); //actual width in pixel 650 
	private static Float COLLAPSED_SPLIT_POSITION_RIGHT = Float.valueOf(96); //actual width in pixel 50
	
	private static Float EXPANDED_SPLIT_POSITION_TOP = Float.valueOf(65); //actual width in pixel
	private static Float COLLAPSED_SPLIT_POSITION_TOP = Float.valueOf(0); //actual width in pixel
	
	private Integer selectedListId;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    	
    public ListManagerMain(){
    	super();
    	this.selectedListId = null;
    }
    
    public ListManagerMain(Integer selectedListId){
    	super();
    	this.selectedListId = selectedListId;
    }
	
    @Override
    public void afterPropertiesSet() throws Exception {
    	
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		
		collapseTop();
		collapseRight();
    }

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

	@Override
	public void instantiateComponents() {
		setSizeFull(); 
		
		//title
		titleLayout = new AbsoluteLayout();
		
        setTitleContent("");
        setTabHeader();
        setContent();		
	}

	private void setTitleContent(String string) {
		titleLayout.removeAllComponents();
        titleLayout.setWidth("100%");
        titleLayout.setHeight("40px");
        
        //TODO put software version in title
        String title =  messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL)+ "  <h2>" + VERSION + "</h2>";
        mainTitle = new Label();
        mainTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(title);
        
        buildNewListButton = new Button();
        buildNewListButton.setCaption(messageSource.getMessage(Message.START_A_NEW_LIST));
        buildNewListButton.setData(BUILD_NEW_LIST_BUTTON_DATA);
        buildNewListButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
        buildNewListButton.setIcon(AppConstants.Icons.ICON_PLUS);
        
        titleLayout.addComponent(mainTitle,"top:0px;left:0px");
        titleLayout.addComponent(buildNewListButton,"top:10px;right:0px");
	}
	
	private void setTabHeader(){
        showBrowseListsButton = new Button(messageSource.getMessage(Message.BROWSE_LISTS));
        showSearchListsButton = new Button(messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        showBrowseListsButton.addStyleName("tabHeaderSelectedStyle");
        showBrowseListsButton.addStyleName("tabStyleButton");
        showSearchListsButton.addStyleName("tabStyleButton");
        showBrowseListsButton.setImmediate(true);
        showSearchListsButton.setImmediate(true);
        
        tabHeaderLayout = new HorizontalLayout();
        tabHeaderLayout.addStyleName("tabHeaderStyle");
        tabHeaderLayout.setSpacing(true);
        tabHeaderLayout.addComponent(showBrowseListsButton);
        tabHeaderLayout.addComponent(showSearchListsButton);
	}
	
	private void setContent(){
		
		vSplitPanel = new VerticalSplitPanel();
		vSplitPanel.setMinSplitPosition(COLLAPSED_SPLIT_POSITION_TOP, Sizeable.UNITS_PIXELS);
		vSplitPanel.setMaxSplitPosition(EXPANDED_SPLIT_POSITION_TOP, Sizeable.UNITS_PIXELS);
		vSplitPanel.setImmediate(true);
		vSplitPanel.addStyleName("tabContainerStyle");
		
		hSplitPanel = new HorizontalSplitPanel();
		hSplitPanel.setMargin(false);
		hSplitPanel.setMaxSplitPosition(COLLAPSED_SPLIT_POSITION_RIGHT, Sizeable.UNITS_PERCENTAGE);
		hSplitPanel.setMinSplitPosition(EXPANDED_SPLIT_POSITION_RIGHT, Sizeable.UNITS_PERCENTAGE);
		hSplitPanel.setImmediate(true);
		hSplitPanel.setWidth("99%");
		
		searchListsComponent = new ListManagerSearchListComponent(this);
		searchListsBarComponent = new ListManagerSearchListBarComponent(searchListsComponent.getSearchResultsComponent());
		
		browseListsComponent = new ListManagerBrowseListComponent(this, selectedListId);
        browserSearchLayout = new AbsoluteLayout();
        browserSearchLayout.addComponent(browseListsComponent,"top:0px;left:0px");
        browserSearchLayout.addComponent(searchListsComponent,"top:0px;left:0px");
        
        toggleBuildNewListButton = new ToogleButton("Toggle Build New List Pane");
        
        buildNewListComponent = new BuildNewListComponent(this);
        
		buildListLayout = new HorizontalLayout();
		buildListLayout.setMargin(false,false,true,false);
		buildListLayout.addComponent(toggleBuildNewListButton);
		buildListLayout.addComponent(buildNewListComponent);
		
		hSplitPanel.setFirstComponent(browserSearchLayout);
		hSplitPanel.setSecondComponent(buildListLayout);
		
		vSplitPanel.setFirstComponent(searchListsBarComponent);
		vSplitPanel.setSecondComponent(hSplitPanel);
		
	}

	@Override
	public void initializeValues() {
		browserSearchLayout.setWidth("100%");
        browserSearchLayout.setHeight("530px");
        searchListsComponent.setVisible(false);
	}

	@Override
	public void addListeners() {
		
		showBrowseListsButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				showBrowseListPane();
				collapseTop();
				showBrowseListsButton.removeStyleName("tabHeaderStyle");
				showBrowseListsButton.addStyleName("tabHeaderSelectedStyle");
				
				showSearchListsButton.removeStyleName("tabHeaderSelectedStyle");
		        showSearchListsButton.addStyleName("tabHeaderStyle");
			}

		});
		
		showSearchListsButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				expandTop();
				showSearchListPane();
				showBrowseListsButton.removeStyleName("tabHeaderSelectedStyle");
				showBrowseListsButton.addStyleName("tabHeaderStyle");
				
				showSearchListsButton.removeStyleName("tabHeaderStyle");
		        showSearchListsButton.addStyleName("tabHeaderSelectedStyle");
			}
		});
		
		toggleBuildNewListButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if(hSplitPanel.getSplitPosition() == hSplitPanel.getMinSplitPosition()){
					collapseRight();
				} else {
					expandRight();
				}
			}
		});
		
		buildNewListButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				expandRight();
			}
		});
	}

	protected void showSearchListPane() {
		browseListsComponent.setVisible(false);
		searchListsComponent.setVisible(true);
		
		browserSearchLayout.setHeight("630px");
	}

	protected void showBrowseListPane() {
		browseListsComponent.setVisible(true);
		searchListsComponent.setVisible(false);
		
		browserSearchLayout.setHeight("530px");
	}

	@Override
	public void layoutComponents() {
		addComponent(titleLayout,"top:10px; left:10px");
		addComponent(tabHeaderLayout,"top:50px;left:10px;");
		addComponent(vSplitPanel,"top:75px;left:10px;");
	}
	
    private void expandRight(){
    	hSplitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_RIGHT, Sizeable.UNITS_PERCENTAGE);
    }

    private void collapseRight(){
    	hSplitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_RIGHT, Sizeable.UNITS_PERCENTAGE);
    }
    
    private void collapseTop(){
    	vSplitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_TOP, Sizeable.UNITS_PIXELS);
    }
    
    private void expandTop(){
    	vSplitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_TOP, Sizeable.UNITS_PIXELS);
    }
    
    public void showBuildNewListComponent(){
    	expandRight();
    }
    
    public void showBuildNewListComponent(GermplasmList list){
    	updateUIForDeletedList(list); // remove the list to be edited from the review list details tabsheet
		buildNewListComponent.editList(list);
    	expandRight();
    }
    
    public void addGermplasmToBuildNewListTable(Integer gid){
    	//TODO
    }

	@Override
	public void updateUIForDeletedList(GermplasmList list) {
		browseListsComponent.getListDetailsLayout().removeTab(list.getId());
		searchListsComponent.getListManagerDetailsLayout().removeTab(list.getId());
	}

	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		browseListsComponent.getListDetailsLayout().renameTab(list.getId(), newName);
		searchListsComponent.getListManagerDetailsLayout().renameTab(list.getId(), newName);
	}
	
	public void removeListTab(GermplasmList list) {
		browseListsComponent.getListDetailsLayout().removeTab(list.getId());
		searchListsComponent.getListManagerDetailsLayout().removeTab(list.getId());
	}

	@Override
	public void openListDetails(GermplasmList list) {
		browseListsComponent.openListDetails(list);
	}
	
	public void addFromListDataTable(Table sourceTable){
		buildNewListComponent.addFromListDataTable(sourceTable);
	}	
	
	public void showNodeOnTree(Integer listId){
		browseListsComponent.getListTreeComponent().setListId(listId);
		browseListsComponent.getListTreeComponent().createTree();
	}
	
	/* SETTERS AND GETTERS */
	public BuildNewListComponent getBuildNewListComponent() {
		return buildNewListComponent;
	}
	
	public ListManagerBrowseListComponent getBrowseListsComponent(){
		return browseListsComponent;
	}

	public void addGemplasmToBuildList(Integer gid) {
		buildNewListComponent.addGermplasm(gid);
		expandRight();
	}
	
}
